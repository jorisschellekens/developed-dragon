package path;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joris on 2/2/18.
 */
public class CompositeWikipediaPathFinder implements IWikipediaPathFinder {

    private List<IWikipediaPathFinder> pathFinderList = new ArrayList<>();

    public CompositeWikipediaPathFinder add(IWikipediaPathFinder iWikipediaPathFinder)
    {
        pathFinderList.add(iWikipediaPathFinder);
        return this;
    }

    @Override
    public String[] find(String start, String goal) {
        for(int i=0;i<pathFinderList.size();i++)
        {
            String[] path = pathFinderList.get(i).find(start, goal);
            if(path != null && path.length >= 2 && path[0].equals(start) && path[path.length-1].equals(goal))
                return path;
        }
        return new String[0];
    }

}
