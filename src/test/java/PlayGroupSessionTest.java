import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import path.meta.CanonizingPathFinder;
import path.DijkstraWikipediaPathFinder001;
import path.IWikipediaPathFinder;
import path.meta.DownloadingPathFinder;
import wikipedia.WikipediaCache;

import java.io.File;

/**
 * This testFile live-plays a game (or multiple games) of TheWikiGame
 * Created by joris on 2/11/18.
 */
public class PlayGroupSessionTest {

    private static File DRIVER_FILE = new File(System.getProperty("user.home"), "Downloads/chromedriver");
    private static WebDriver DRIVER = initDriver();
    private static IWikipediaPathFinder PATHFINDER = new CanonizingPathFinder(new DownloadingPathFinder(new DijkstraWikipediaPathFinder001()));

    private static long WAIT_TIME = 5000;
    private static boolean UPDATE_CACHE_WHILE_PLAYING = true;
    private int MAX_NOF_RETRY = 5;

    private static final int PUBLIC_GROUP_CODE = 123456;
    private static final String MY_NAME = "JorisSchellekens";

    private static WebDriver initDriver() {
        System.setProperty("webdriver.chrome.driver", DRIVER_FILE.getAbsolutePath());
        WebDriver driver = new ChromeDriver();
        return driver;
    }

    @Test
    public void login() {
        DRIVER.get("https://groups.thewikigame.com/login?code=" + PUBLIC_GROUP_CODE);
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DRIVER.findElement(By.tagName("input")).sendKeys(MY_NAME);
        DRIVER.findElement(By.tagName("button")).click();
    }

    private boolean initScreenVisible()
    {
        try{
            if(!DRIVER.getCurrentUrl().equals("https://groups.thewikigame.com/group"))
                return false;
            if(DRIVER.findElements(By.cssSelector("div.wgg-article-link")).size() != 2)
                return false;
            if(!DRIVER.findElement(By.tagName("button")).getText().equalsIgnoreCase("Play Now"))
                return false;
            return true;
        }catch (Exception ex){}
        return false;
    }

    private void waitForInitScreen()
    {
        while(!initScreenVisible())
        {
            try{
                Thread.sleep(WAIT_TIME);
            }catch (Exception ex){}
        }
    }

    private String[] scrapeStartAndGoal()
    {
        if(!initScreenVisible())
            return null;
        return new String[]{
                DRIVER.findElements(By.cssSelector("div.wgg-article-link")).get(0).getText(),
                DRIVER.findElements(By.cssSelector("div.wgg-article-link")).get(1).getText()
        };
    }

    private void waitForNewGame(String[] startAndGoal)
    {
        String[] tmp = scrapeStartAndGoal();
        while(tmp == null || (tmp[0].equals(startAndGoal[0]) && tmp[1].equals(startAndGoal[1]))) {
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tmp = scrapeStartAndGoal();
        }
    }

    private boolean clickLink(String current, String goal)
    {
        try {
            for (WebElement element : DRIVER.findElements(By.tagName("a"))) {
                String href = element.getAttribute("href");
                if (href == null)
                    continue;
                if (href.endsWith("/wiki/" + goal)) {
                    element.click();
                    Thread.sleep(WAIT_TIME);
                    return true;
                }
            }
        }catch (Exception ex)
        {
            return false;
        }
        return false;
    }

    private boolean clickPath(String[] path, int retryNr)
    {
        boolean out = true;
        int i = 0;
        for(i=0;i<path.length;i++)
        {
            // exception for entering game
            if(i == 0)
            {
                try {
                    DRIVER.findElement(By.cssSelector("button.h1")).click();
                    Thread.sleep(WAIT_TIME);
                }catch (Exception ex){}
            }
            else {
                out &= clickLink(path[i-1], path[i]);
            }
            if(!out)
                break;
        }
        if(!out && UPDATE_CACHE_WHILE_PLAYING && retryNr < MAX_NOF_RETRY)
        {
            System.out.println("Calculating new path");
            WikipediaCache.get().removeLink(path[i-1], path[i]);
            path = PATHFINDER.find(path[i-1], path[path.length-1]);
            for(int j=0;j<path.length;j++)
                System.out.println(j + "\t" + path[j]);
            return clickPath(path, retryNr+1);
        }
        return out;
    }

    private void closeBox()
    {
        DRIVER.switchTo().parentFrame();
        try {
            WebElement element = DRIVER.findElement(By.tagName("button"));
            if (element != null)
            {
                element.click();
                Thread.sleep(WAIT_TIME);
            }
        }catch (Exception ex){}
    }

    @Test
    public void singleSession()
    {
        // log in
        login();

        // wait for initial screen
        waitForInitScreen();

        // scrap start and goal
        String[] startAndGoal = scrapeStartAndGoal();
        System.out.println("start : " + startAndGoal[0]);
        System.out.println("goal  : " + startAndGoal[1]);

        // find path
        String[] path = PATHFINDER.find(startAndGoal[0], startAndGoal[1]);

        // if no path exists, wait for new game
        if(path == null || path.length == 0){
            System.out.println("No valid path found. Abandon game.");
            waitForNewGame(startAndGoal);
            return;
        }

        // print path
        for(int i=0;i<path.length;i++)
            System.out.println(i + "\t" + path[i]);

        // click the path
        boolean won = clickPath(path, 0);

        // wait for next game
        if(!won) {
            System.out.println("Failure to click path during game. Abandon game.");
            waitForNewGame(startAndGoal);
        }
        else {
            closeBox();
            waitForInitScreen();
        }
    }

    @Test
    public void multipleSessions()
    {
        multipleSessions(1000);
    }

    public void multipleSessions(int k)
    {
        // log in
        login();

        for(int r=0;r<k;r++) {
            // wait for initial screen
            waitForInitScreen();

            // debug output
            System.out.println("=========================================");

            // scrap start and goal
            String[] startAndGoal = scrapeStartAndGoal();
            System.out.println("start : " + startAndGoal[0]);
            System.out.println("goal  : " + startAndGoal[1]);

            // find path
            String[] path = PATHFINDER.find(startAndGoal[0], startAndGoal[1]);

            // if no path exists, wait for new game
            if (path == null || path.length == 0) {
                System.out.println("No valid path found. Abandon game.");
                waitForNewGame(startAndGoal);
                continue;
            }

            // print path
            for (int i = 0; i < path.length; i++)
                System.out.println(i + "\t" + path[i]);

            // click the path
            boolean won = clickPath(path, 0);

            // wait for next game
            if (!won) {
                System.out.println("Failure to click path during game. Abandon game.");
                waitForNewGame(startAndGoal);
            } else {
                closeBox();
                waitForInitScreen();
            }
        }
    }
}
