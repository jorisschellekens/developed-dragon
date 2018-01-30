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
        String[] coreArticles = {"United_States","World_War_II", "Biology","Language"};

        for(String article : coreArticles)
        {
            new DepthFirstWikipediaMiner().start(article, 2);
        }
    }
}
