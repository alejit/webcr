import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Crawler {
    private final Set<String> linksVisited = Collections.synchronizedSet(new HashSet<>());
    private final String rootUrl;
    private final ForkJoinPool pool;

    public Crawler(String rootUrl, int n_threads) {
        this.rootUrl = rootUrl;
        pool = new ForkJoinPool(n_threads);
    }

    public static void main(String[] args) {
        Crawler startLink = new Crawler("https://tomblomfield.com/", 2);
        startLink.run();
    }

    private void run() {
        JSONObject result = new JSONObject();
        result.put(this.rootUrl, new JSONArray());
        pool.invoke(new Task(this.rootUrl, linksVisited, result));
        System.out.println(result.toString(4));
    }
}

 class Task extends RecursiveTask<List<String>> {

    private final String url;
    private final JSONObject jsonNode;
    private final Set<String> linksVisited;

    public Task(String url, Set<String> linksCrawled, JSONObject jsonNode) {
        this.url = url;
        this.linksVisited = linksCrawled;
        this.jsonNode = jsonNode;
    }

    boolean checkLink(String link) throws MalformedURLException {
        URL url = new URL(link);
        return !link.isEmpty() && url.getHost().equals("tomblomfield.com") && !linksVisited.contains(link);
    }

    @Override
    public List<String> compute() {
        Document doc = null;
        List<Task> tasks = new LinkedList<>();

        if (!linksVisited.contains(url)) {
            try {
                doc = Jsoup.connect(url).get();
                Elements links = doc.select("a[href]");
                linksVisited.add(url);
                for (Element link : links) {
                    String child = link.absUrl("href");
                    if (checkLink(child)) {
                        JSONObject childJson = new JSONObject();
                        childJson.put(child, new JSONArray());
                        JSONArray childArray = jsonNode.getJSONArray(url);
                        childArray.put(childJson);
                        tasks.add(new Task(child, linksVisited, childJson));
                    }
                }
                invokeAll(tasks);
                return new LinkedList<>();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new LinkedList<>();
    }

 }

