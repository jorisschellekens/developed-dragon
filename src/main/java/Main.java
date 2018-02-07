import miner.DepthFirstWikipediaMiner;
import path.CanonizingPathFinder;
import path.DijkstraWikipediaPathFinder001;
import wikipedia.WikipediaCache;

import java.util.Scanner;

/**
 * Created by joris on 1/26/18.
 */
public class Main {

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

            System.out.println("FROM '" + start + "' TO '" + goal + "'");

            // display path
            String[] path = new CanonizingPathFinder(new DijkstraWikipediaPathFinder001()).find(start, goal);
            for(int i=0;i<path.length;i++)
            {
                System.out.println(i + "\t" + path[i]);
            }

        }       
    }
}
