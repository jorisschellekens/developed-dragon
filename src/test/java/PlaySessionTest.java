import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import path.CanonizingPathFinder;
import path.DijkstraWikipediaPathFinder001;
import path.IWikipediaPathFinder;

import java.io.File;

/**
 * Created by joris on 2/7/18.
 */
public class PlaySessionTest {

    private static File DRIVER_FILE = new File(System.getProperty("user.home"),"Downloads/chromedriver");
    private static WebDriver DRIVER = initDriver();
    private static IWikipediaPathFinder PATHFINDER = new CanonizingPathFinder(new DijkstraWikipediaPathFinder001());

    private static WebDriver initDriver()
    {
        System.setProperty("webdriver.chrome.driver", DRIVER_FILE.getAbsolutePath());
        WebDriver driver = new ChromeDriver();
        return driver;
    }

    private boolean showMainPage()
    {
        // show page
        if(!DRIVER.getCurrentUrl().equalsIgnoreCase("https://thewikigame.com/speed-race")) {
            DRIVER.get("https://thewikigame.com/speed-race");
            try {Thread.sleep(2000);} catch (InterruptedException e) {}
        }
        return true;
    }

    private boolean login(String username, String password)
    {
        // show page
        showMainPage();
        try {
            DRIVER.findElement(By.cssSelector("a#accounts_login")).click();
            Thread.sleep(2000);

            // fill in username and password
            DRIVER.findElement(By.cssSelector("input#id_username")).sendKeys(username);
            DRIVER.findElement(By.cssSelector("input#id_password")).sendKeys(password);

            // return
            return true;
        }catch (Exception ex){return false;}
    }


    private boolean initScreenVisible()
    {
        showMainPage();
        boolean startFound = false;
        boolean endFound = false;
        try {
            startFound = DRIVER.findElement(By.cssSelector("p#start")) != null && DRIVER.findElement(By.cssSelector("p#start")).getText().length() > 0;
            endFound = DRIVER.findElement(By.cssSelector("p#end")) != null && DRIVER.findElement(By.cssSelector("p#end")).getText().length() > 0;
        }catch(Exception ex){}

        return startFound && endFound;
    }

    private void waitForInitScreen()
    {
        while(!initScreenVisible())
        {
            try{
                Thread.sleep(2000);
            }catch (Exception ex){}
        }
    }

    private String[] scrapeStartAndGoal()
    {
        if(!initScreenVisible())
            return null;
        return new String[]{
                DRIVER.findElement(By.cssSelector("p#start")).getText(),
                DRIVER.findElement(By.cssSelector("p#end")).getText()
        };
    }

    private void waitForNewGame(String[] currentStartAndGoal)
    {
        showMainPage();
        String[] startAndGoal = scrapeStartAndGoal();
        while(startAndGoal == null || (startAndGoal[0].equals(currentStartAndGoal[0]) && startAndGoal[1].equals(currentStartAndGoal[1]))) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startAndGoal = scrapeStartAndGoal();
        }
    }

    private boolean clickLink(String goal)
    {
        try {
            for (WebElement element : DRIVER.findElements(By.tagName("a"))) {
                String href = element.getAttribute("href");
                if (href == null)
                    continue;
                if (href.endsWith("/wiki/" + goal)) {
                    element.click();
                    Thread.sleep(2000);
                    return true;
                }
            }
        }catch (Exception ex)
        {
            return false;
        }
        return false;
    }

    private boolean clickPath(String[] path)
    {
        boolean out = true;
        for(int i=0;i<path.length;i++)
        {
            // exception for entering game
            if(i == 0)
            {
                try {
                    DRIVER.findElement(By.cssSelector("p#start")).click();
                    Thread.sleep(2000);
                    DRIVER.switchTo().frame("wiki");
                }catch (Exception ex){}
            }
            else {
                out &= clickLink(path[i]);
            }
            if(!out)
                break;
        }
        return out;
    }

    private void closeBox()
    {
        DRIVER.switchTo().parentFrame();
        try {
            WebElement element = DRIVER.findElement(By.cssSelector("div#cboxClose"));
            if (element != null)
            {
                element.click();
                Thread.sleep(2000);
            }
        }catch (Exception ex){}
    }

    @Test
    public void login()
    {
        login("JorisSchellekens","");
    }

    @Test
    public void singleGame()
    {
        // wait for initial screen
        waitForInitScreen();

        // scrap start and goal
        String[] startAndGoal = scrapeStartAndGoal();
        System.out.println("start : " + startAndGoal[0]);
        System.out.println("goal  : " + startAndGoal[1]);

        // find path
        String[] path = PATHFINDER.find(startAndGoal[0], startAndGoal[1]);
        if(path == null || path.length == 0){
            System.out.println("Abandon game");
            waitForNewGame(startAndGoal);
            return;
        }

        // click the path
        boolean won = clickPath(path);

        // wait for next game
        if(!won) {
            waitForNewGame(startAndGoal);
        }
        else {
            closeBox();
            waitForInitScreen();
        }
    }

    @Test
    public void multipleGames()
    {
        for(int i=0;i<10;i++)
            singleGame();
    }
}
