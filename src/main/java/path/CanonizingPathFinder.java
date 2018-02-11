package path;

import miner.DepthFirstWikipediaMiner;
import wikipedia.WikipediaCache;

/**
 * This class adds canonization to the IWikipediaPathFinder
 * Rather than having to add this logic to every implementation,
 * client code can wrap expect to be called only with valid Wikipedia articles.
 * Even though the wrapper enables end-users to call the IWikipediaPathFinder with
 * invalid article titles such as "Michael Jackson" rather than "Michael_Jackson".
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
