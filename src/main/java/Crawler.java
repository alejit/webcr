import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        List<String> result = pool.invoke(new Task(this.rootUrl, linksVisited));
        for(String l : result){
            System.out.println(l);
        }
    }
}

 class Task extends RecursiveTask<List<String>> {

    private final String url;
    private final Set<String> linksVisited;

    public Task(String url, Set<String> linksCrawled) {
        this.url = url;
        this.linksVisited = linksCrawled;
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
                for (Element link : links) {
                    String child = link.absUrl("href");
                    if (checkLink(child)) {
                        tasks.add(new Task(child, linksVisited));
                    }
                }
                linksVisited.add(url);

                invokeAll(tasks);
                return tasks.stream().map(Task::getUrl).collect(Collectors.toList());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new LinkedList<>();
    }

     public String getUrl() {
         return url;
     }
 }

