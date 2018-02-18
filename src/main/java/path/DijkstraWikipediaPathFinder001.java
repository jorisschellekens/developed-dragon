package path;

import graph.AbstractDijkstraAlgorithm;
import wikipedia.WikipediaCache;

import java.io.*;
import java.util.*;

/**
 * This class implements IWikipediaPathFinder.
 * Conceptually, it splits the Wikipedia connectivity graph in two subgraphs.
 * The 'core' are all the nodes that are interconnected,
 * the 'frontier' are those nodes that contain only links to the core.
 * By splitting the vertices like this, finding a route can be reduced to
 * a two-step process.
 * First finding a way to go from the frontier to the core (linear in the
 * number of nodes in the code), and second, finding a path within the core
 * (at most quadratic in the number of vertices in the core).
 * Typically, the core is about 100 times smaller than the frontier. So this
 * algorithm really saves on useless calculations.
 * Further optimization includes calculating the eigenvalues of all vertices in
 * the core. And using those to break ties in AbstractDijkstraAlgorithm's algorithm.
 * By doing so, we ensure that paths with highly connected vertices are
 * considered first. Thus guaranteeing minimum lookup time.
 * Created by joris on 1/27/18.
 */
public class DijkstraWikipediaPathFinder001 implements IWikipediaPathFinder {

    protected static Map<Integer, Set<Integer>> core = buildCore();
    private static Map<Integer, Double> priorities = new HashMap<>();

    public DijkstraWikipediaPathFinder001()
    {
        if(priorities.isEmpty())
            load();
        double loadRatio = (double) priorities.size() / (double) core.size();
        if(loadRatio < 0.75) {
            priorities = calculateEigenvalues();
            store();
        }
    }

    @Override
    public String[] find(String start, String goal) {
        if(!WikipediaCache.get().has(start) || !WikipediaCache.get().has(goal))
            return new String[]{};

        final int startId = WikipediaCache.get().lookup(start);
        int endId = WikipediaCache.get().lookup(goal);

        if(WikipediaCache.get().outgoing(startId) != null && WikipediaCache.get().outgoing(startId).contains(endId))
            return new String[]{start, goal};

        if(WikipediaCache.get().outgoing(startId) == null || WikipediaCache.get().outgoing(startId).isEmpty())
            return new String[]{};

        // call AbstractDijkstraAlgorithm
        final Collection<Integer> goals = goToCore(endId);
        if(!goals.contains(endId))
            goals.add(endId);

        int[] pathA = new AbstractDijkstraAlgorithm() {
            @Override
            public int tieBreaker(int ID0, int ID1){return priority(ID0) > priority(ID1) ? ID0 : ID1;}
            @Override
            public int cost(int startID, int goalID) { return 1; }
            @Override
            public Collection<Integer> nextHop(int ID) { return ID == startId ? WikipediaCache.get().outgoing(ID) : core.get(ID);}
            @Override
            public boolean has(int ID) { return ID == startId || core.containsKey(ID) || goals.contains(ID);}
        }.path(startId, goToCore(endId));

        // exception
        if(pathA == null || pathA.length == 0)
            return new String[]{};

        int[] pathB = pathA;
        if(pathA[pathA.length-1] != endId)
        {
            pathB = new int[pathA.length + 1];
            for(int i=0;i<pathA.length;i++)
                pathB[i] = pathA[i];
            pathB[pathB.length-1] = endId;
        }

        String[] pathC = new String[pathB.length];
        for(int i=0;i<pathB.length;i++)
            pathC[i] = WikipediaCache.get().lookup(pathB[i]);
        return pathC;
    }

    /**
     * Get the eigenvalue of a given article, or a sensible default value
     * if the eigenvalue of the given article is not calculated yet.
     * @param id the ID of the article
     * @return
     */
    protected double priority(int id)
    {
        if(priorities.containsKey(id))
            return priorities.get(id);
        return 0.75;
    }

    /**
     * Find all possible articles from the core that link to a given
     * article in the frontier.
     * @param articleId the ID of the article in the frontier
     * @return
     */
    protected Set<Integer> goToCore(int articleId)
    {
        if(core.containsKey(articleId))
            return java.util.Collections.singleton(articleId);
        else
        {
            Set<Integer> prevInCore = new HashSet<>();
            for(int cId : core.keySet())
            {
                if(WikipediaCache.get().outgoing(cId).contains(articleId))
                    prevInCore.add(cId);
            }
            return prevInCore;
        }
    }

    /**
     * Calculate the eigenvalues of the articles in the core
     * @return
     */
    private static Map<Integer, Double> calculateEigenvalues()
    {
        Map<Integer, Double> tmp0 = new HashMap<>();
        Map<Integer, Double> tmp1 = new HashMap<>();
        for(int i : core.keySet())
        {
            tmp0.put(i, 1.0);
        }
        double alpha = 0.85;
        for(int iteration=0;iteration<16;iteration++) {
            for (int i : core.keySet()) {
                double v = (tmp0.get(i) / WikipediaCache.get().outgoing(i).size()) * alpha;
                for (int outId : WikipediaCache.get().outgoing(i)) {
                    if(!core.containsKey(outId))
                        continue;
                    if (!tmp1.containsKey(outId))
                        tmp1.put(outId, 0.0);
                    tmp1.put(outId, tmp1.get(outId) + v);
                }
            }
            for(int i : core.keySet())
            {
                if(!tmp1.containsKey(i))
                    tmp1.put(i, 0.0);
                tmp1.put(i, tmp1.get(i) + (1.0 - alpha));
            }
            tmp0.clear();
            tmp0.putAll(tmp1);
            tmp1.clear();
        }
        return tmp0;
    }

    /**
     * Calculate the frontier subgraph
     * @return
     */
    private static final Set<Integer> buildFrontier()
    {
        Set<Integer> frontier = new HashSet<>();
        for(String article : WikipediaCache.get().articles())
        {
            int articleId = WikipediaCache.get().lookup(article);
            if(WikipediaCache.get().outgoing(articleId) ==  null)
            {
                frontier.add(articleId);
            }
        }
        return frontier;
    }

    /**
     * Calculate the core subgraph
     * @return
     */
    private static final Map<Integer, Set<Integer>> buildCore()
    {
        Map<Integer, Set<Integer>> tmp = new HashMap<>();
        for(String article : WikipediaCache.get().articles())
        {
            int articleId = WikipediaCache.get().lookup(article);
            if(WikipediaCache.get().outgoing(articleId) !=  null)
            {
                tmp.put(articleId, new HashSet<Integer>());
            }
        }
        for(int articleID : tmp.keySet())
        {
            Set<Integer> nextHops = new HashSet<>();
            for(int nextHopID : WikipediaCache.get().outgoing(articleID))
            {
                if(tmp.containsKey(nextHopID))
                    nextHops.add(nextHopID);
            }
            tmp.put(articleID, nextHops);
        }
        return tmp;
    }

    /**
     * Load the externally saved eigenvalues for the core subgraph
     * @return
     */
    public boolean load()
    {
        try {
            _load();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Load the externally saved eigenvalues for the core subgraph
     * @throws IOException
     */
    private void _load() throws IOException
    {
        File in = new File(System.getProperty("user.home"), this.getClass().getSimpleName() + ".bin");
        if(!in.exists())
            return;

        BufferedReader sc = new BufferedReader(new FileReader(in));
        while(sc.ready())
        {
            String[] line = sc.readLine().split("\t");
            priorities.put(Integer.parseInt(line[0]), Double.parseDouble(line[1]));
        }
        sc.close();
    }

    /**
     * Store the eigenvalues for the core subgraph
     * @return
     */
    public boolean store()
    {
        try {
            _store();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Store the eigenvalues for the core subgraph
     * @throws IOException
     */
    private void _store() throws IOException
    {
        File out = new File(System.getProperty("user.home"), this.getClass().getSimpleName() + ".bin");
        FileWriter fileWriter = new FileWriter(out);
        for(Map.Entry<Integer,Double> e : priorities.entrySet())
            fileWriter.write(e.getKey() + "\t" + e.getValue() + "\n");
        fileWriter.flush();
        fileWriter.close();
    }
}
