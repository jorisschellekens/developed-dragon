import org.testng.annotations.Test;
import path.DijkstraWikipediaPathFinder001;
import path.IWikipediaPathFinder;
import path.meta.DownloadingPathFinder;
import wikipedia.WikipediaCache;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by joris on 2/19/18.
 */
public class PlayFixedGameTest {

    private static Random RANDOM = new java.util.Random(System.currentTimeMillis());
    private File GAME_FILE = new File(System.getProperty("user.home"), getClass().getSimpleName() + ".txt");

    @Test
    public void buildFile() throws IOException {

        int N = 128;
        List<String[]> tuples = new ArrayList<>();
        List<String> articles = new ArrayList<>(WikipediaCache.get().articles());
        for(int i=0;i<N;i++)
        {
            String start = articles.get(RANDOM.nextInt(articles.size()));
            String goal = articles.get(RANDOM.nextInt(articles.size()));
            while(goal.equals(start))
                goal = articles.get(RANDOM.nextInt(articles.size()));
            tuples.add(new String[]{start, goal});
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(GAME_FILE));
        for(int i=0;i<N;i++)
        {
            writer.write(tuples.get(i)[0] + "\t" + tuples.get(i)[1] + "\n");
        }
        writer.close();

    }

    @Test
    public void testFile() throws FileNotFoundException {

        // create IWikipediaPathFinder
        long initialLoadTime = System.currentTimeMillis();
        IWikipediaPathFinder pathFinder = new DownloadingPathFinder(new DijkstraWikipediaPathFinder001());
        initialLoadTime = System.currentTimeMillis() - initialLoadTime;

        List<String[]> tuples = new ArrayList<>();
        Scanner sc = new Scanner(GAME_FILE);
        while(sc.hasNextLine())
        {
            tuples.add(sc.nextLine().split("\t"));
        }
        sc.close();

        // measure performance
        double success = 0;
        for(int i=0;i<tuples.size();i++)
        {
            String start = tuples.get(i)[0];
            String goal = tuples.get(i)[1];
            int[] m = measure(start, goal, pathFinder);
            if(m[0] != -1 && m[1] != -1)
                success++;
            System.out.println(i + "\t" + m[0] + "\t" + m[1]);
            // System.out.println(i + "\t" + start + "\t" + goal + "\t" + m[0] + "\t" + m[1]);
        }
        success /= tuples.size();
        System.out.println(success);
    }

    private int[] measure(String start, String goal, IWikipediaPathFinder pathFinder)
    {
        long time = System.currentTimeMillis();
        String[] path = pathFinder.find(start, goal);
        time = System.currentTimeMillis() - time;

        if(path != null && path.length > 0 && path[0].equals(start) && path[path.length -1].equals(goal))
        {
            return new int[]{path.length, (int) time};
        }
        else
        {
            return new int[]{-1, -1};
        }
    }
}
