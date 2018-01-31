import miner.DepthFirstWikipediaMiner;
import org.testng.annotations.Test;
import wikipedia.WikipediaCache;

/**
 * Created by joris on 1/30/18.
 */
public class BuildCacheTest {

    @Test
    public void test()
    {
        String[] coreArticles = {"United_States","World_War_II", "Biology","Language", "Puppy", "IText"};

        for(String article : coreArticles)
        {
            System.out.println("====================================================");
            System.out.println(article);
            System.out.println("====================================================");
            new DepthFirstWikipediaMiner().start(article, 2);
        }

        // explicit store
        WikipediaCache.get().store();
    }
}
