import org.testng.annotations.Test;
import path.DijkstraWikipediaPathFinder001;
import path.IWikipediaPathFinder;
import path.meta.DownloadingPathFinder;
import wikipedia.WikipediaCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by joris on 1/30/18.
 */
public class LinkCountPerformanceTest {


    private static Random RANDOM = new java.util.Random(System.currentTimeMillis());
    private int N = 1000;

    @Test
    public void testPerformance()
    {
        // create IWikipediaPathFinder
        long initialLoadTime = System.currentTimeMillis();
        IWikipediaPathFinder pathFinder = new DownloadingPathFinder(new DijkstraWikipediaPathFinder001());
        initialLoadTime = System.currentTimeMillis() - initialLoadTime;

        // perform N tests
        List<String> articles = new ArrayList<>(WikipediaCache.get().articles());
        List<Integer> measurements = new ArrayList<>();
        for(int i=0;i<N;i++)
        {
            String start = articles.get(RANDOM.nextInt(articles.size()));
            String goal = articles.get(RANDOM.nextInt(articles.size()));
            while(goal.equals(start))
                goal = articles.get(RANDOM.nextInt(articles.size()));
            measurements.add(measure(start, goal, pathFinder));
        }

        // display statistics
        double succes = 0;
        double maxLen = 0.0;
        double minLen = Double.MAX_VALUE;
        double avgLen = 0.0;
        for(int i=0;i<N;i++)
        {
            if(measurements.get(i) != -1)
            {
                succes++;
                maxLen = java.lang.Math.max(maxLen, measurements.get(i));
                minLen = java.lang.Math.min(minLen, measurements.get(i));
                avgLen += measurements.get(i);
            }
        }
        avgLen /=  N;
        succes /= N;
        System.out.println("load time    : " + initialLoadTime + " ms");
        System.out.println("success rate : " + succes);
        System.out.println("max          : " + (int) maxLen + " clicks");
        System.out.println("min          : " + (int) minLen + " clicks");
        System.out.println("avg          : " + ((int) avgLen * 100) / 100.0 + " clicks");
    }

    private int measure(String start, String goal, IWikipediaPathFinder pathFinder)
    {
        String[] path = pathFinder.find(start, goal);
        if(path != null && path.length > 0 && path[0].equals(start) && path[path.length -1].equals(goal))
        {
            return path.length;
        }
        else
        {
            return -1;
        }
    }

}
