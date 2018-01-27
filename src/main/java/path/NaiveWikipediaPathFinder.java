package path;

import wikipedia.WikipediaCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by joris on 1/26/18.
 */
public class NaiveWikipediaPathFinder implements IWikipediaPathFinder
{
    private String[] winningPath;
    private int maxPath = 6;

    public String[] find(String start, String end)
    {
        winningPath = null;
        find(start, end, new ArrayList<String>());
        return winningPath;
    }

    private void find(String start, String end, List<String> path)
    {
        if(path.isEmpty())
            path.add(start);

        if(path.size() > maxPath)
            return;

        if(winningPath != null)
            return;

        if(path.get(path.size()-1).equals(end))
        {
            winningPath = path.toArray(new String[path.size()]);
        }

        Set<Integer> out = WikipediaCache.get().outgoing(path.get(path.size()-1));
        out = bestOrder(start, end, out);
        for(Integer toId : out)
        {
            String toArticle = WikipediaCache.get().lookup(toId);
            if(path.contains(toArticle))
                continue;
            path.add(toArticle);
            find(start, end, path);
            path.remove(path.size()-1);
        }
    }

    private Set<Integer> bestOrder(String start, String end, Set<Integer> links)
    {
        return links;
    }
}
