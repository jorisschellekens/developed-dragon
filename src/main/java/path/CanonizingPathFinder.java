package path;

import miner.DepthFirstWikipediaMiner;
import wikipedia.WikipediaCache;

/**
 * Created by joris on 2/7/18.
 */
public class CanonizingPathFinder implements IWikipediaPathFinder {

    private IWikipediaPathFinder innerPathFinder;

    public CanonizingPathFinder(IWikipediaPathFinder innerPathFinder)
    {
        this.innerPathFinder = innerPathFinder;
    }

    private String canonize(String article)
    {
        String s2 = article.replaceAll("(-|_)+"," ").toUpperCase();
        for(String a : WikipediaCache.get().articles())
        {
            String a2 = a.toUpperCase().replaceAll("(-|_)+"," ");
            if(a2.equals(s2))
                return a;
        }

        return article;
    }

    @Override
    public String[] find(String start, String goal) {


        // canonize start
        start = canonize(start);

        // canonize goal
        goal = canonize(goal);

        // find goal if needed
        if(!WikipediaCache.get().has(goal)) {
            new DepthFirstWikipediaMiner().start(goal, 2);
            WikipediaCache.get().store();
            goal = canonize(goal);
        }

        // find start if needed
        if(!WikipediaCache.get().has(start))
        {
            new DepthFirstWikipediaMiner().start(start, 2);
            WikipediaCache.get().store();
            start = canonize(start);
        }

        String[] path = innerPathFinder.find(start, goal);
        if(path == null || path.length < 2)
            return null;
        return path;
    }
}
