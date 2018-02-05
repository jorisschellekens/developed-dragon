import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import path.DijkstraWikipediaPathFinder001;
import path.IWikipediaPathFinder;
import wikipedia.WikipediaCache;

import java.io.File;
import java.net.MalformedURLException;

/**
 * Created by joris on 2/5/18.
 */
public class PlayLiveTest {

    private static final File DRIVER_FILE = new File(System.getProperty("user.home"),"Downloads/chromedriver");
    private static IWikipediaPathFinder pathFinder = new DijkstraWikipediaPathFinder001();

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

    @Test
    public void test() throws MalformedURLException
    {
        System.setProperty("webdriver.chrome.driver", DRIVER_FILE.getAbsolutePath());
        ChromeDriver driver = new ChromeDriver();

        // open game
        driver.get("https://thewikigame.com/speed-race");

        // parse page source
        Document document = Jsoup.parse(driver.getPageSource());

        // get START and END
        String start = document.select("p#start").select("span").text();
        String end = document.select("p#end").select("span").text();

        // find a working path
        String startB = canonize(start);
        String endB = canonize(end);
        String[] path = pathFinder.find(startB, endB);
        if(path == null || path.length == 0) {
            System.out.println("No path found. Abandoning game.");
            return;
        }

        // click first link
       for(WebElement element : driver.findElements(By.cssSelector("span"))) {
           try {
               String txt = element.getText();
               if (start.equalsIgnoreCase(txt)) {
                   System.out.println("Entering game ..");
                   element.click();
               }
           } catch (Exception ex) {
           }
       }

    }
}
