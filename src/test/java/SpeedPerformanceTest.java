import org.testng.annotations.Test;
import path.AdvancedDijkstraWikipediaPathFinder;
import path.IWikipediaPathFinder;
import wikipedia.WikipediaCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by joris on 1/29/18.
 */
public class SpeedPerformanceTest {

    private static Random RANDOM = new java.util.Random(System.currentTimeMillis());
    private int N = 32;

    @Test
    public void testPerformance()
    {
        // create IWikipediaPathFinder
        long initialLoadTime = System.currentTimeMillis();
        IWikipediaPathFinder pathFinder = new AdvancedDijkstraWikipediaPathFinder();
        initialLoadTime = System.currentTimeMillis() - initialLoadTime;

        // perform N tests
        List<String> articles = new ArrayList<>(WikipediaCache.get().articles());
        List<Long> measurements = new ArrayList<>();
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
        double maxTime = 0.0;
        double minTIme = Double.MAX_VALUE;
        double avgTime = 0.0;
        for(int i=0;i<N;i++)
        {
            if(measurements.get(i) != -1)
            {
                succes++;
                maxTime = java.lang.Math.max(maxTime, measurements.get(i));
                minTIme = java.lang.Math.min(minTIme, measurements.get(i));
                avgTime += measurements.get(i);
            }
        }
        avgTime /=  N;
        succes /= N;
        System.out.println("load time    : " + initialLoadTime + " ms");
        System.out.println("success rate : " + succes);
        System.out.println("max          : " + (int) maxTime + " ms");
        System.out.println("min          : " + (int) minTIme + " ms");
        System.out.println("avg          : " + (int) avgTime + " ms");
    }

    private long measure(String start, String goal, IWikipediaPathFinder pathFinder)
    {
        long before = System.currentTimeMillis();
        String[] path = pathFinder.find(start, goal);
        if(path != null && path.length > 0 && path[0].equals(start) && path[path.length -1].equals(goal))
        {
            before = System.currentTimeMillis() - before;
        }
        else
        {
            before = -1;
        }
        return before;
    }

}
