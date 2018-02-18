package graph;


import java.util.*;

/**
 * Created by joris on 2/18/18.
 */
public abstract class AbstractDijkstraAlgorithm {

    /**
     * When this algorithm needs to choose between two vertices at equal distance, a tie breaker
     * method is called. This method is expected to make a decision between both IDs and return
     * the ID that ought to be explored first.
     * @param ID0
     * @param ID1
     * @return
     */
    public abstract int tieBreaker(int ID0, int ID1);

    /**
     * This method returns the cost of going from startID to goalID
     * assuming that startID and goalID are 1 hop apart
     * @param startID
     * @param goalID
     * @return
     */
    public abstract int cost(int startID, int goalID);

    /**
     * This method returns the next possible vertices from a given vertex
     * @param ID
     * @return
     */
    public abstract Collection<Integer> nextHop(int ID);

    /**
     * This method checks whether the graph contains a vertex with given ID
     * @param ID
     * @return
     */
    public abstract boolean has(int ID);

    /**
     * returns true iff a contains any element from b
     * @param a
     * @param b
     * @return
     */
    private boolean containsAny(Collection<Integer> a, Collection<Integer> b)
    {
        for(Integer bElem : b)
        {
            if(a.contains(bElem))
                return true;
        }
        return false;
    }

    private Integer pickRandomFromIntersect(Collection<Integer> a, Collection<Integer> b)
    {
        Set<Integer> tmp = new HashSet<>(a);
        tmp.retainAll(b);
        if(tmp.isEmpty())
            return null;
        return tmp.iterator().next();
    }

    public int[] path(Integer startID, Collection<Integer> goalIDs)
    {
        Map<Integer, Integer> distance = new HashMap<>();
        distance.put(startID, 0);
        Map<Integer, Integer> previous = new HashMap<>();
        Set<Integer> visited = new HashSet<>();

        Integer current = startID;
        while(!goalIDs.contains(current) && !containsAny(previous.keySet(), goalIDs))
        {
            // update distances
            visited.add(current);
            for(Integer out : nextHop(current))
            {
                if(!distance.containsKey(out)){
                    distance.put(out, distance.get(current) + cost(current, out));
                    previous.put(out, current);
                }
                else
                {
                    int d1 = distance.get(out);
                    int d2 = distance.get(current) + cost(current, out);
                    if(d2 < d1)
                    {
                        distance.put(out, d2);
                        previous.put(out, current);
                    }
                }
            }

            // find unvisited node with smallest distance
            Integer next = -1;
            Integer nextHopDistance = Integer.MAX_VALUE;
            for(Map.Entry<Integer, Integer> e : distance.entrySet())
            {
                int nextHop = e.getKey();
                if(visited.contains(nextHop))
                    continue;
                if(!has(nextHop))
                    continue;
                if(nextHop(nextHop) == null)
                    continue;
                if(next == -1                                                           // next hop is unknown
                        || e.getValue() < nextHopDistance                               // next hop with smaller distance is found
                        || (e.getValue().intValue() == nextHopDistance.intValue() 
                        && tieBreaker(nextHop, next) == nextHop)) {                     // next hop with same distance is found
                    next = e.getKey();
                    nextHopDistance = e.getValue();
                }
            }
            if(next == -1)
                break;
            current = next;
        }

        // build path
        if(containsAny(previous.keySet(), goalIDs))
        {
            List<Integer> path = new ArrayList<>();
            path.add(pickRandomFromIntersect(previous.keySet(), goalIDs));

            while(!path.get(0).equals(startID)) {
                path.add(0, previous.get(path.get(0)));
            }

            // convert to primitive
            int[] out = new int[path.size()];
            for(int i=0;i<out.length;i++)
                out[i] = path.get(i);

            // return
            return out;
        }

        // default
        return new int[]{};
    }

    public int[] path(int startID, int goalID)
    {
        return path(startID, java.util.Collections.singleton(goalID));
    }

}
