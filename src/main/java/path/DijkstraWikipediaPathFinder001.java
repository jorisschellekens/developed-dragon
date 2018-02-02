package path;

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
 * the core. And using those to break ties in Dijkstra's algorithm.
 * By doing so, we ensure that paths with highly connected vertices are
 * considered first. Thus guaranteeing minimum lookup time.
 * Created by joris on 1/27/18.
 */
public class DijkstraWikipediaPathFinder001 implements IWikipediaPathFinder {

    private static Set<Integer> frontier = buildFrontier();
    private static Set<Integer> core = buildCore();
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

        int startId = WikipediaCache.get().lookup(start);
        int endId = WikipediaCache.get().lookup(goal);

        List<Integer> path = searchCore(startId, goToCore(endId));
        if(path.isEmpty())
            return new String[]{};
        if(path.get(path.size() -1) != endId)
            path.add(endId);

        String[] out = new String[path.size()];
        for(int i=0;i<path.size();i++)
            out[i] = WikipediaCache.get().lookup(path.get(i));
        return out;
    }

    private boolean hasAny(Set<Integer> s0, Set<Integer> s1)
    {
        for(Integer i1 : s1)
            if(s0.contains(i1))
                return true;
        return false;
    }

    /**
     * Search for a path within the core articles
     * @param startId the ID of the starting vertex
     * @param endIds the ID of the goal vertex
     * @return
     */
    private List<Integer> searchCore(int startId, Set<Integer> endIds)
    {
        Map<Integer, Integer> distance = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        Set<Integer> visited = new HashSet<>();

        distance.put(startId, 0);


        int current = startId;
        if(!core.contains(current)) {
            WikipediaCache.get().outgoing(WikipediaCache.get().lookup(current));
            frontier.remove(current);
            core.add(current);
        }
        while(!endIds.contains(current) && !hasAny(previous.keySet(), endIds))
        {
            if(!WikipediaCache.get().has(current))
                continue;
            if(WikipediaCache.get().outgoing(current) == null)
                continue;
            if(frontier.contains(current))
                continue;

            // update distances
            visited.add(current);
            for(int out : WikipediaCache.get().outgoing(current))
            {
                if(frontier.contains(out))
                    continue;
                if(!distance.containsKey(out)){
                    distance.put(out, distance.get(current) + 1);
                    previous.put(out, current);
                }
                else
                {
                    int d1 = distance.get(out);
                    int d2 = distance.get(current) + 1;
                    if(d2 < d1)
                    {
                        distance.put(out, d2);
                        previous.put(out, current);
                    }
                }
            }

            // find unvisited node with smallest distance
            int next = -1;
            for(Map.Entry<Integer, Integer> e : distance.entrySet())
            {
                if(visited.contains(e.getKey()))
                    continue;
                if(!WikipediaCache.get().has(e.getKey()))
                    continue;
                if(WikipediaCache.get().outgoing(e.getKey()) == null)
                    continue;
                if(next == -1 ||                                                                                    // best distance isn't known yet
                        e.getValue() < distance.get(next) ||                                                        // distance is better
                        (e.getValue() == distance.get(next) && priority(e.getKey()) > priority(next)))              // distance is equal, but eigenvalue is better
                    next = e.getKey();
            }
            if(next == -1)
                break;
            current = next;
        }

        // build path
        if(hasAny(previous.keySet(), endIds))
        {
            List<Integer> path = new ArrayList<>();

            Set<Integer> matchingEndIds = new HashSet<>(previous.keySet());
            matchingEndIds.retainAll(endIds);
            path.add(matchingEndIds.iterator().next());

            while(!path.get(0).equals(startId)) {
                path.add(0, previous.get(path.get(0)));
            }
            return path;
        }
        return new ArrayList<>();
    }

    /**
     * Get the eigenvalue of a given article, or a sensible default value
     * if the eigenvalue of the given article is not calculated yet.
     * @param id the ID of the article
     * @return
     */
    private double priority(int id)
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
    private Set<Integer> goToCore(int articleId)
    {
        if(core.contains(articleId))
            return java.util.Collections.singleton(articleId);
        else
        {
            Set<Integer> prevInCore = new HashSet<>();
            for(int cId : core)
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
        for(int i : core)
        {
            tmp0.put(i, 1.0);
        }
        double alpha = 0.85;
        for(int iteration=0;iteration<16;iteration++) {
            for (int i : core) {
                double v = (tmp0.get(i) / WikipediaCache.get().outgoing(i).size()) * alpha;
                for (int outId : WikipediaCache.get().outgoing(i)) {
                    if(!core.contains(outId))
                        continue;
                    if (!tmp1.containsKey(outId))
                        tmp1.put(outId, 0.0);
                    tmp1.put(outId, tmp1.get(outId) + v);
                }
            }
            for(int i : core)
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
    private static final Set<Integer> buildCore()
    {
        Set<Integer> core = new HashSet<>();
        for(String article : WikipediaCache.get().articles())
        {
            int articleId = WikipediaCache.get().lookup(article);
            if(WikipediaCache.get().outgoing(articleId) !=  null)
            {
                core.add(articleId);
            }
        }
        return core;
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
