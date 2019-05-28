import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Main {
    private static ArrayList<Socket> requestLists = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerSocket proxyServer = new ServerSocket(8080);
        System.out.println("Proxy server is listening on port 8080");
        Proxy.resetCache();

        Runtime.getRuntime().addShutdownHook(new Thread(()->handleShutDown()));

        while (true) {
            try{
                Socket clientSocket = proxyServer.accept();
//                requestLists.add(clientSocket);
                Proxy request = new Proxy(clientSocket);
                request.start();
            }catch(Exception e){
                if(e instanceof IOException)
                    System.out.println("Error during accepting client request");
                else
                    System.out.println("Unknown Error");
            }
        }
    }

    private static void handleShutDown(){
        System.out.println("Exit Proxy Server! Close requested connections "+requestLists.size());
        Proxy.removeCache();
//        for (Socket s : requestLists) {
//            try {
//                s.close();
//            } catch (IOException e) {  }
//        }
    }
}
