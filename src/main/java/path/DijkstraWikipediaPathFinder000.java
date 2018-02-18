package path;

import graph.AbstractDijkstraAlgorithm;
import wikipedia.WikipediaCache;

import java.util.*;

/**
 * This class provides a basic implementation of IWikipediaPathFinder
 * using AbstractDijkstraAlgorithm's algorithm for minimal path construction.
 * Created by joris on 1/26/18.
 */

public class DijkstraWikipediaPathFinder000 implements IWikipediaPathFinder {

    @Override
    public String[] find(String start, String goal) {
        if(!WikipediaCache.get().has(start) || !WikipediaCache.get().has(goal))
            return new String[]{};

        int startId = WikipediaCache.get().lookup(start);
        int endId = WikipediaCache.get().lookup(goal);

        // call AbstractDijkstraAlgorithm
        int[] pathA = new AbstractDijkstraAlgorithm() {
            @Override
            public int tieBreaker(int ID0, int ID1){return ID1;}
            @Override
            public int cost(int startID, int goalID) { return 1; }
            @Override
            public Collection<Integer> nextHop(int ID) { return WikipediaCache.get().outgoing(ID);}
            @Override
            public boolean has(int ID) { return WikipediaCache.get().has(ID);}
        }.path(startId, endId);

        // exception
        if(pathA.length == 0)
            return new String[0];

        // convert IDs to links
        String[] pathB = new String[pathA.length];
        for(int i=0;i<pathB.length;i++)
            pathB[i] = WikipediaCache.get().lookup(pathA[i]);

        // return
        return pathB;
    }

}