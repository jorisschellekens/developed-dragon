package wikipedia;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by joris on 1/26/18.
 */
public class WikipediaCache {

    private static final WikipediaCache self = new WikipediaCache();

    private Map<Integer, String> articleIds = new HashMap<>();
    private Map<String, Integer> invArticleIds = new HashMap<>();
    private Map<Integer, Set<Integer>> cache = new HashMap<>();

    private int nofChanges = 0;
    private int nofChangesBeforeSave = 512;

    private WikipediaCache() {
        load();
    }

    public static WikipediaCache get() {
        return self;
    }

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

    private void addLink(String from, String to) {
        int fromId = createOrLookup(from);
        int toId = createOrLookup(to);
        if (!cache.containsKey(fromId))
            cache.put(fromId, new HashSet<Integer>());
        cache.get(fromId).add(toId);
        nofChanges++;
        if (nofChanges % nofChangesBeforeSave == 0) {
            store();
        }
    }

    public int lookup(String article)
    {
        return invArticleIds.get(article);
    }

    public String lookup(int articleId)
    {
        return articleIds.get(articleId);
    }

    public boolean has(String article) {
        return invArticleIds.containsKey(article);
    }

    public boolean has(int articleId) {
        return articleIds.containsKey(articleId);
    }

    public Set<String> articles()
    {
        return invArticleIds.keySet();
    }

    public Set<Integer> outgoing(int articleId)
    {
        return cache.get(articleId);
    }

    public Set<Integer> outgoing(String article)
    {
        if(!has(article) || !cache.containsKey(invArticleIds.get(article)))
            onlineLookup(article);
        return outgoing(invArticleIds.get(article));
    }

    private void onlineLookup(String article)
    {
        String url = "https://en.wikipedia.org/wiki/" + article;
        Set<String> tmp = new HashSet<>();
        try {
            Document htmlDoc = Jsoup.parse(new URL(url), 5000);
            for(Element e : htmlDoc.select("a"))
            {
                String href = e.attr("href");
                if(href.startsWith("/wiki/") && !e.text().isEmpty())
                {
                    String link = href.substring(6);
                    if(link.contains("Category:") || link.contains("Special:") ||
                            link.contains("Template:") || link.contains("Portal:") ||
                            link.contains("Talk:") || link.contains("Help:") ||
                            link.contains("Template_talk:") || link.contains("File:") ||
                            link.contains("Book:"))
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
        for(Map.Entry<Integer, Set<Integer>> en : cache.entrySet())
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
            cache.put(articleId, toIds);

        }
        sc.close();
    }
}
