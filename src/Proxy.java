import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Proxy extends Thread {
    Socket clientSocket;
    private static List<String> blockedWebsites = Arrays.asList("youtube.com","baidu.com");
    public static Map<String, File> cache = new HashMap<>();
    private final static String CACHE_FOLDER = "Proxy_Cache";

    Proxy(Socket clientSocket) {
        super();
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try{
            RequestHandler requestHandler = new RequestHandler(clientSocket);
            requestHandler.run();
            clientSocket.close();
        } catch (Exception e) {
            System.out.println("ERROR during socket close");
            e.printStackTrace();
        }
    }

    public static boolean isAllowedSite(String url) {
        return !blockedWebsites.stream().anyMatch(str->url.contains(str));
    }

    public static void filenameMapping(String url) {
        String replaced = url.replaceAll("[:/?*<>\"|]","_");
        replaced = replaced.replace("\\","_");
        replaced += ".html";

        cache.put(url,new File(CACHE_FOLDER+"/"+replaced));
    }

    public static void removeCache() {
        try {
            Files.walk(new File(CACHE_FOLDER).toPath())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete())
                            System.out.println("There is error while deleting "+file.getName());
                    });
        } catch (IOException ex) { }
    }
    public static void resetCache() {
        System.out.println("Reset Cache");
        removeCache();

        /* Create cache folder */
        File cacheFolder = new File(CACHE_FOLDER);
        if (!cacheFolder.mkdir())
            System.out.println("There is error while creating cache folder");
    }
}