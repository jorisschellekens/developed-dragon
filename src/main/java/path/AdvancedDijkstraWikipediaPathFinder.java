package path;

import wikipedia.WikipediaCache;

import java.util.*;

/**
 * Created by joris on 1/27/18.
 */
public class AdvancedDijkstraWikipediaPathFinder implements IWikipediaPathFinder {

    private static Set<Integer> frontier = buildFrontier();
    private static Set<Integer> core = buildCore();
    private static Map<Integer, Double> priorities = eigenvalues();

    @Override
    public String[] find(String start, String end) {
        if(!WikipediaCache.get().has(start) || !WikipediaCache.get().has(end))
            return new String[]{};

        int startId = WikipediaCache.get().lookup(start);
        int endId = WikipediaCache.get().lookup(end);

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

    private double priority(int id)
    {
        if(priorities.containsKey(id))
            return priorities.get(id);
        return 0.75;
    }

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

    private static Map<Integer, Double> eigenvalues()
    {
        Map<Integer, Double> tmp0 = new HashMap<>();
        Map<Integer, Double> tmp1 = new HashMap<>();
        for(int i : core)
        {
            tmp0.put(i, 1.0);
        }
        double alpha = 0.85;
        for(int iteration=0;iteration<20;iteration++) {
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

}
