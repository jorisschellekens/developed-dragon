package wikipedia;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * This class represents a linkage between the program
 * and Wikipedia. This class avoids making too many calls to
 * Wikipedia. It stores the articles and links between them.
 * Created by joris on 1/26/18.
 */
public class WikipediaCache {

    // singleton pattern
    private static final WikipediaCache self = new WikipediaCache();

    // articles are internally represented as integers
    private Map<Integer, String> articleIds = new HashMap<>();
    private Map<String, Integer> invArticleIds = new HashMap<>();

    // map from article ID to IDs of articles linked
    private Map<Integer, Set<Integer>> linkage = new HashMap<>();

    // parameters for auto-saving the cache
    private int nofChanges = 0;
    private int nofChangesBeforeSave = 5000;

    private WikipediaCache() {
        load();
    }

    public static WikipediaCache get() {
        return self;
    }

    /**
     * Return the ID of an article,
     * or creates the ID if the given key is not yet
     * present in the ID table(s).
     * @param article
     * @return
     */
    private int createOrLookup(String article) {
        if (invArticleIds.containsKey(article))
            return invArticleIds.get(article);
        else {
            int N = articleIds.size();
            invArticleIds.put(article, N);
            articleIds.put(N, article);
            return N;
        }
    }

    /**
     * Register a link between a given start article
     * and target article
     * @param from start article
     * @param to target article
     */
    private void addLink(String from, String to) {
        int fromId = createOrLookup(from);
        int toId = createOrLookup(to);
        if (!linkage.containsKey(fromId))
            linkage.put(fromId, new HashSet<Integer>());
        linkage.get(fromId).add(toId);
        nofChanges++;
        if (nofChanges % nofChangesBeforeSave == 0) {
            store();
        }
    }

    /**
     * Return the ID corresponding to a given article title
     * @param article
     * @return
     */
    public int lookup(String article)
    {
        return invArticleIds.get(article);
    }

    /**
     * Return the article title corresponding to a given article ID
     * @param articleId
     * @return
     */
    public String lookup(int articleId)
    {
        return articleIds.get(articleId);
    }

    /**
     * Return true iff the cache currently contains the given article
     * @param article
     * @return
     */
    public boolean has(String article) {
        return invArticleIds.containsKey(article);
    }

    /**
     * Return true iff the cache currently contains the given article
     * @param articleId
     * @return
     */
    public boolean has(int articleId) {
        return articleIds.containsKey(articleId);
    }

    /**
     * Return all articles currently contained in the cache
     * @return
     */
    public Set<String> articles()
    {
        return invArticleIds.keySet();
    }

    /**
     * Get all outgoing links from a given article ID
     * @param articleId
     * @return
     */
    public Set<Integer> outgoing(int articleId)
    {
        return linkage.get(articleId);
    }

    /**
     * Get all outgoing links from a given article title
     * @param article
     * @return
     */
    public Set<Integer> outgoing(String article)
    {
        if(!has(article) || !linkage.containsKey(invArticleIds.get(article)))
            onlineLookup(article);
        return outgoing(invArticleIds.get(article));
    }

    private boolean hasParent(Element e, String nodeName, String nodeClass, String nodeID)
    {
        Element tmp = e;
        while(tmp != null)
        {
            tmp = tmp.parent();
            if(tmp == null)
                break;
            if(nodeName != null && tmp.nodeName().equals(nodeName))
                return true;
            if(nodeClass != null && Arrays.asList(tmp.className().split(" ")).contains(nodeClass))
                return true;
            if(nodeID != null && tmp.id().equals(nodeID))
                return true;
        }
        return false;
    }

    /**
     * Perform live lookup of outgoing links if the article
     * has not been cached yet.
     * This method also updates the 'nofChanges' count.
     * Which in turn might trigger an auto-save.
     * @param article
     */
    private void onlineLookup(String article)
    {
        String url = "https://en.wikipedia.org/wiki/" + article;
        try {
            Document htmlDoc = Jsoup.parse(new URL(url), 5000);
            Element pageElement = htmlDoc.select("div#bodyContent").first();
            for(Element e : pageElement.select("a"))
            {
                if(hasParent(e, "cite", null, null))
                    continue;
                if(hasParent(e, null, "references", null))
                    continue;
                if(hasParent(e, null, "refbegin", null))
                    continue;
                String href = e.attr("href");
                if(href.startsWith("/wiki/") && !e.text().isEmpty())
                {
                    String link = href.substring(6);
                    if(link.contains("Category:") || link.contains("Special:") ||
                            link.contains("Template:") || link.contains("Portal:") ||
                            link.contains("Talk:") || link.contains("Help:") ||
                            link.contains("Template_talk:") || link.contains("File:") ||
                            link.contains("Book:") || link.contains("Wikipedia:"))
                        continue;
                    addLink(article, link);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean store()
    {
        try {
            _store();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void _store() throws IOException
    {
        File out = new File(System.getProperty("user.home"), this.getClass().getSimpleName() + ".bin");
        FileWriter fileWriter = new FileWriter(out);
        fileWriter.write(articleIds.size() + "\n");
        for(Map.Entry<Integer, String> en : articleIds.entrySet())
        {
            fileWriter.write(en.getKey() + "\t" + en.getValue() + "\n");
        }
        for(Map.Entry<Integer, Set<Integer>> en : linkage.entrySet())
        {
            fileWriter.write(en.getKey() + "\t");
            for(Integer toId : en.getValue())
                fileWriter.write(toId + "\t");
            fileWriter.write("\n");
        }
        fileWriter.flush();
        fileWriter.close();
    }

    public boolean load()
    {
        try {
            _load();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void _load() throws IOException
    {
        File in = new File(System.getProperty("user.home"), this.getClass().getSimpleName() + ".bin");
        if(!in.exists())
            return;

        BufferedReader sc = new BufferedReader(new FileReader(in));
        int nofEntries = Integer.parseInt(sc.readLine());
        for(int i=0;i<nofEntries;i++)
        {
            String[] line = sc.readLine().split("\t");
            int articleId = Integer.parseInt(line[0]);
            String article = line[1];
            articleIds.put(articleId, article);
            invArticleIds.put(article, articleId);
        }
        while(sc.ready())
        {
            String[] line = sc.readLine().split("\t");
            int articleId = Integer.parseInt(line[0]);
            Set<Integer> toIds = new HashSet<>();
            for(int i=1;i<line.length;i++)
                toIds.add(Integer.parseInt(line[i]));
            linkage.put(articleId, toIds);

        }
        sc.close();
    }
}
