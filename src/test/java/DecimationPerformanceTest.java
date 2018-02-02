import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;
import path.DijkstraWikipediaPathFinder002;
import path.IWikipediaPathFinder;
import wikipedia.WikipediaCache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by joris on 2/2/18.
 */
public class DecimationPerformanceTest {

    private static Random RANDOM = new java.util.Random(System.currentTimeMillis());
    private DijkstraWikipediaPathFinder002 pathFinder = null;
    private int N = 200;

    @Test
    public void test()
    {
        double[] rates = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.75, 0.8, 0.85};
        for(double p : rates)
            System.out.println(p + "\t" + testSeries(N, p));
    }

    private double testSeries(int n, double decimationRate)
    {
        // copy cache
        copyCache();

        // test
        List<String> articles = new ArrayList<>(WikipediaCache.get().articles());
        pathFinder = new DijkstraWikipediaPathFinder002(decimationRate);
        double p = 0.0;
        for(int i=0;i<n;i++)
        {
            String start = articles.get(RANDOM.nextInt(articles.size()));
            String goal = articles.get(RANDOM.nextInt(articles.size()));
            while(goal.equals(start))
                goal = articles.get(RANDOM.nextInt(articles.size()));
            if(measure(start, goal, pathFinder))
                p++;
        }
        p /= n;

        // restore cache
        restoreCache();

        // return
        return p;
    }

    private void copyCache()
    {
        File cache = new File(System.getProperty("user.home"), WikipediaCache.class.getSimpleName() + ".bin");
        if(!cache.exists())
            return;

        File copy = new File(System.getProperty("user.home"), WikipediaCache.class.getSimpleName() + "_copy.bin");
        try {
            FileUtils.copyFile(cache, copy);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreCache()
    {
        File copy = new File(System.getProperty("user.home"), WikipediaCache.class.getSimpleName() + "_copy.bin");
        if(!copy.exists())
            return;

        File cache = new File(System.getProperty("user.home"), WikipediaCache.class.getSimpleName() + ".bin");
        if(cache.exists())
            cache.delete();
        try {
            FileUtils.copyFile(copy, cache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean measure(String start, String goal, IWikipediaPathFinder pathFinder)
    {
        String[] path = pathFinder.find(start, goal);
        if(path != null && path.length > 0 && path[0].equals(start) && path[path.length -1].equals(goal))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
