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
 * Furthermore, this implementation adds the concept of decimation.
 * It essentially throws away nodes in the core with probability inversely
 * proportional to their respective priorities.
 * Created by joris on 1/27/18.
 */
public class DijkstraWikipediaPathFinder002 extends DijkstraWikipediaPathFinder001 {

    // decimation
    private Set<Integer> decimatedVertices = new HashSet<>();
    private double decimationRatio = 0.25;

    public DijkstraWikipediaPathFinder002(double decimationRatio)
    {
        this.decimationRatio = decimationRatio;
        setDecimatedVertices();
    }

    @Override
    public String[] find(String start, String goal) {
        if(!WikipediaCache.get().has(start) || !WikipediaCache.get().has(goal))
            return new String[]{};

        final int startId = WikipediaCache.get().lookup(start);
        final int endId = WikipediaCache.get().lookup(goal);

        if(WikipediaCache.get().outgoing(startId) != null && WikipediaCache.get().outgoing(startId).contains(endId))
            return new String[]{start, goal};

        // call AbstractDijkstraAlgorithm
        int[] pathA = new AbstractDijkstraAlgorithm() {
            @Override
            public int tieBreaker(int ID0, int ID1){return priority(ID0) > priority(ID1) ? ID0 : ID1;}
            @Override
            public int cost(int startID, int goalID) { return 1; }
            @Override
            public Collection<Integer> nextHop(int ID) { return ID == startId ? WikipediaCache.get().outgoing(ID) : core.get(ID);}
            @Override
            public boolean has(int ID) { return (core.containsKey(ID) && !decimatedVertices.contains(ID)) || (ID == endId);}
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

    private void setDecimatedVertices()
    {
        int f = 5;
        int[] lookupTable = new int[core.size() * f];
        int i = 0;
        for(Integer articleID : core.keySet())
        {
            if(i >= lookupTable.length)
                break;
            double p = 1.0 - priority(articleID) / 3.0;
            for(int j=0;j<(p*f);j++)
                lookupTable[++i] = articleID;
        }
        Random rnd = new java.util.Random(System.currentTimeMillis());
        decimatedVertices.clear();
        for(i=0;i<(int)(decimationRatio * core.size());i++)
        {
            int articleID = lookupTable[rnd.nextInt(lookupTable.length)];
            decimatedVertices.add(articleID);
        }
    }

}
