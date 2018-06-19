package live;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import path.DijkstraWikipediaPathFinder001;
import path.IWikipediaPathFinder;
import path.meta.CanonizingPathFinder;
import path.meta.DownloadingPathFinder;
import wikipedia.WikipediaCache;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReplayGameTest {

    enum GameState{
        LOGIN,
        WAIT_FOR_NEXT_ROUND,
        NEXT_ROUND_ANNOUNCED,
        PLAYING,
        REPLAYING,
        ERROR
    }

    // selenium logic
    private static File DRIVER_FILE = new File(System.getProperty("user.home"), "Downloads/chromedriver");
    private static WebDriver DRIVER = initDriver();
    private static long WAIT_TIME = 5000;

    private static WebDriver initDriver() {
        System.setProperty("webdriver.chrome.driver", DRIVER_FILE.getAbsolutePath());
        WebDriver driver = new ChromeDriver();
        return driver;
    }

    // game logic
    private static String MY_NAME = "JorisSchellekens";
    private GameState currentState = GameState.LOGIN;

    // pathfinding
    private static IWikipediaPathFinder PATHFINDER = new CanonizingPathFinder(new DownloadingPathFinder(new DijkstraWikipediaPathFinder001()));
    private String start = "";
    private String goal = "";

    private List<String[]> previousPaths = new ArrayList<>();
    private List<String[]> tempRemovedLinks = new ArrayList<>();

    @Test
    public void playGame(){
        while(currentState != GameState.ERROR){
            System.out.println(currentState.toString());
            nextState();
        }
    }

    public void nextState(){
        if(currentState == GameState.LOGIN){
            onLogin();
        }
        if(currentState == GameState.WAIT_FOR_NEXT_ROUND){
            onWaitForNextRound();
        }
        if(currentState == GameState.NEXT_ROUND_ANNOUNCED){
            onNextRoundAnnounced();
        }
        if(currentState == GameState.REPLAYING){
            onReplay();
        }
        if(currentState == GameState.ERROR){
            onError();
        }
    }

    private void onLogin(){
        DRIVER.get("https://thewikigame.com/");
        try {Thread.sleep(WAIT_TIME); } catch (InterruptedException e) {}

        DRIVER.findElement(By.tagName("input")).sendKeys(MY_NAME);
        DRIVER.findElement(By.tagName("button")).click();
        try {Thread.sleep(WAIT_TIME); } catch (InterruptedException e) {}

        // decide next state
        if(!DRIVER.getCurrentUrl().equals("https://thewikigame.com/group")){
            currentState = GameState.ERROR;
            return;
        }
        if(DRIVER.findElements(By.cssSelector("div.wgg-article-link")).size() != 2){
            currentState = GameState.ERROR;
            return;
        }
        if(DRIVER.findElements(By.cssSelector("button#playNowButton")).isEmpty()){
            currentState = GameState.WAIT_FOR_NEXT_ROUND;
            return;
        }
        if(DRIVER.findElement(By.cssSelector("button#playNowButton")).getText().startsWith("PLAY NOW")){
           currentState = GameState.NEXT_ROUND_ANNOUNCED;
           return;
        }
    }

    private void onWaitForNextRound(){

        boolean isNextRoundAnnounced = false;
        while(!isNextRoundAnnounced){
            // decide next state
            if(!DRIVER.getCurrentUrl().equals("https://thewikigame.com/group")){
                currentState = GameState.ERROR;
                return;
            }
            if(DRIVER.findElements(By.cssSelector("div.wgg-article-link")).size() != 2){
                currentState = GameState.ERROR;
                return;
            }
            String tmpA = DRIVER.findElements(By.cssSelector("div.wgg-article-link")).get(0).getText();
            String tmpB = DRIVER.findElements(By.cssSelector("div.wgg-article-link")).get(1).getText();
            if(tmpA.equals(start) && tmpB.equals(goal))
                isNextRoundAnnounced = false;
            else
                isNextRoundAnnounced = DRIVER.findElements(By.cssSelector("button#playNowButton")).size() > 0;

            if(!isNextRoundAnnounced){
                try {Thread.sleep(WAIT_TIME); } catch (InterruptedException e) {}
            }
        }

        currentState = GameState.NEXT_ROUND_ANNOUNCED;
    }

    private void onNextRoundAnnounced(){
        start = DRIVER.findElements(By.cssSelector("div.wgg-article-link")).get(0).getText();
        goal = DRIVER.findElements(By.cssSelector("div.wgg-article-link")).get(1).getText();
        previousPaths.clear();
        for(String[] tuple : tempRemovedLinks)
            WikipediaCache.get().addLink(tuple[0], tuple[1]);
        tempRemovedLinks.clear();

        String[] path = PATHFINDER.find(start, goal);
        System.out.println(Arrays.toString(path));
        previousPaths.add(path);

        // if no path exists, wait for new game
        if(path == null || path.length == 0){
            System.out.println("No valid path found. Abandoning current round.");
            currentState = GameState.WAIT_FOR_NEXT_ROUND;
            return;
        }

        // click the path
        currentState = GameState.PLAYING;
        for(int i=0;i<path.length;i++)
        {
            // exception for entering game
            if(i == 0) {
                DRIVER.findElement(By.cssSelector("button#playNowButton")).click();
            }
            // click next link
            else {
                if(!DRIVER.getCurrentUrl().contains("/wiki/")) {
                    System.out.println("Lost game due to time-out");
                    currentState = GameState.WAIT_FOR_NEXT_ROUND;
                    return;
                }
                if(!clickLink(path[i-1], path[i])) {
                    System.out.println("Theoretical path does not match Wikipedia.");
                    WikipediaCache.get().removeLink(path[i-1], path[i]);
                    path = PATHFINDER.find(path[i-1], path[path.length-1]);

                    List<String> tmp = new ArrayList<>(Arrays.asList(previousPaths.get(previousPaths.size()-1)).subList(0, i-1));
                    tmp.addAll(Arrays.asList(path));
                    previousPaths.set(previousPaths.size() - 1, tmp.toArray(new String[]{}));

                    System.out.println(Arrays.toString(path));
                    System.out.println("");
                    i = 0;
                }
            }
            // sleep
            try {Thread.sleep(WAIT_TIME); }catch (Exception ex){}
        }

        // close win box
        try{
            DRIVER.switchTo().parentFrame();
            if(DRIVER.findElement(By.tagName("button")) != null) {
                DRIVER.findElement(By.tagName("button")).click();
                try {Thread.sleep(WAIT_TIME); }catch (Exception ex){}
            }
        }catch (Exception ex){
            currentState = GameState.ERROR;
            return;
        }

        // next
        currentState = GameState.REPLAYING;
    }

    private boolean clickLink(String prev, String next){
        try {
            for (WebElement element : DRIVER.findElements(By.tagName("a"))) {
                String href = element.getAttribute("href");
                if (href == null)
                    continue;
                if (href.endsWith("/wiki/" + next)) {
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

    private void onReplay(){
        if(previousPaths.isEmpty()){
            currentState = GameState.ERROR;
        }
        String[] prevPath = previousPaths.get(previousPaths.size() - 1);

        // get new path
        tempRemovedLinks.add(new String[]{prevPath[0], prevPath[1]});
        WikipediaCache.get().removeLink(prevPath[0], prevPath[1]);
        String[] path = PATHFINDER.find(start, goal);
        previousPaths.add(path);
        System.out.println(Arrays.toString(path));

        // if no path exists, wait for new game
        if(path == null || path.length == 0){
            System.out.println("No valid path found. Abandoning current round.");
            currentState = GameState.WAIT_FOR_NEXT_ROUND;
            return;
        }

        // click the path
        currentState = GameState.PLAYING;
        for(int i=1;i<path.length;i++)
        {
                if(!DRIVER.getCurrentUrl().contains("/wiki/")) {
                    System.out.println("Lost game due to time-out");
                    currentState = GameState.WAIT_FOR_NEXT_ROUND;
                    return;
                }
                if(!clickLink(path[i-1], path[i])) {
                    System.out.println("Theoretical path does not match Wikipedia.");
                    WikipediaCache.get().removeLink(path[i-1], path[i]);
                    path = PATHFINDER.find(path[i-1], path[path.length-1]);
                    System.out.println(Arrays.toString(path));
                    System.out.println("");
                    i = 0;
                }

            // sleep
            try {Thread.sleep(WAIT_TIME); }catch (Exception ex){}
        }

        // close win box
        try {
            DRIVER.switchTo().parentFrame();
            if (DRIVER.findElement(By.tagName("button")) != null) {
                DRIVER.findElement(By.tagName("button")).click();
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (Exception ex) {
                }
            }
        }catch (Exception ex){
            currentState = GameState.ERROR;
            return;
        }

        // update status
        currentState = GameState.REPLAYING;
    }

    private void onError(){
        DRIVER.get("https://thewikigame.com/group");
        try {Thread.sleep(WAIT_TIME); }catch (Exception ex){}
        currentState = GameState.WAIT_FOR_NEXT_ROUND;
    }
}
