import miner.DepthFirstWikipediaMiner;
import path.AdvancedDijkstraWikipediaPathFinder;
import wikipedia.WikipediaCache;

import java.util.Scanner;

/**
 * Created by joris on 1/26/18.
 */
public class Main {

    private static String canonize(String s)
    {

        String s2 = s.replaceAll("(-|_)+"," ").toUpperCase();
        for(String a : WikipediaCache.get().articles())
        {
            String a2 = a.toUpperCase().replaceAll("(-|_)+"," ");
            if(a2.equals(s2))
                return a;
        }

        return s;
    }

    public static void main(String[] args)
    {
        String start = "";
        String goal = "";
        
        while(!start.equals("EXIT"))
        {
            System.out.print("start > ");

            Scanner sc = new Scanner(System.in);
            start = sc.nextLine();

            System.out.print("goal > ");
            goal = sc.nextLine();

            start = canonize(start);
            goal = canonize(goal);
            System.out.println("FROM '" + start + "' TO '" + goal + "'");
            
            // find goal if needed
            if(!WikipediaCache.get().has(goal)) {
                new DepthFirstWikipediaMiner().start(goal, 2);
                WikipediaCache.get().store();
                start = canonize(start);
            }
        
            // find start if needed
            if(!WikipediaCache.get().has(start))
            {
                new DepthFirstWikipediaMiner().start(start, 2);
                WikipediaCache.get().store();
                goal = canonize(goal);
            }


            // display path
            String[] path = new AdvancedDijkstraWikipediaPathFinder().find(start, goal);
            for(int i=0;i<path.length;i++)
            {
                System.out.println(i + "\t" + path[i]);
            }

        }       
    }
}
