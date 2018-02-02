package path;

import wikipedia.WikipediaCache;

import java.util.*;

/**
 * This class provides a basic implementation of IWikipediaPathFinder
 * using Dijkstra's algorithm for minimal path construction.
 * Created by joris on 1/26/18.
 */

public class DijkstraWikipediaPathFinder000 implements IWikipediaPathFinder {

    @Override
    public String[] find(String start, String goal) {
        if(!WikipediaCache.get().has(start) || !WikipediaCache.get().has(goal))
            return new String[]{};

        int startId = WikipediaCache.get().lookup(start);
        int endId = WikipediaCache.get().lookup(goal);

        Map<Integer, Integer> distance = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        Set<Integer> visited = new HashSet<>();

        distance.put(startId, 0);


        int current = startId;
        if(!WikipediaCache.get().has(current)) {
            WikipediaCache.get().outgoing(current);
            WikipediaCache.get().store();
        }
        while(current != endId && !previous.containsKey(endId))
        {
            if(!WikipediaCache.get().has(current))
                continue;
            if(WikipediaCache.get().outgoing(current) == null)
                continue;

            // update distances
            visited.add(current);
            for(int out : WikipediaCache.get().outgoing(current))
            {
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
                if(next == -1 || e.getValue() < distance.get(next))
                    next = e.getKey();
            }
            if(next == -1)
                break;
            current = next;

        }

        if(previous.containsKey(endId))
        {
            List<Integer> path = new ArrayList<>();
            path.add(endId);

            while(!path.get(0).equals(startId)) {
                path.add(0, previous.get(path.get(0)));
            }

            String[] out = new String[path.size()];
            for(int i=0;i<path.size();i++)
                out[i] = WikipediaCache.get().lookup(path.get(i));
            return out;
        }

        return new String[0];
    }

}