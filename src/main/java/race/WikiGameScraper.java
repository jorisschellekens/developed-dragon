package race;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by joris on 1/26/18.
 */
public class WikiGameScraper {

    private static final WikiGameScraper self = new WikiGameScraper();

    private WikiGameScraper(){}

    public static WikiGameScraper get() { return self; }


}
