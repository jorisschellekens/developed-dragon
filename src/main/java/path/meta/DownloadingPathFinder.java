package path.meta;

import miner.DepthFirstWikipediaMiner;
import path.IWikipediaPathFinder;
import wikipedia.WikipediaCache;

/**
 * Created by joris on 2/18/18.
 */
public class DownloadingPathFinder implements IWikipediaPathFinder {

    private IWikipediaPathFinder innerPathFinder;

    public DownloadingPathFinder(IWikipediaPathFinder pathFinder)
    {
        this.innerPathFinder = pathFinder;
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

        // download start article page if needed
        if(!WikipediaCache.get().has(start) || WikipediaCache.get().outgoing(start) == null)
        {
            new DepthFirstWikipediaMiner().start(start, 1);
            WikipediaCache.get().store();
            start = canonize(start);
        }

        // download goal article page if needed
        if(!WikipediaCache.get().has(goal) || WikipediaCache.get().outgoing(goal) == null) {
            new DepthFirstWikipediaMiner().start(goal, 1);
            WikipediaCache.get().store();
            goal = canonize(goal);
        }

        return innerPathFinder.find(start, goal);
    }

}
