import java.net.*;
import java.io.*;
import java.util.*;

public class RequestHandler {
    private Socket clientSocket;
    private final static String CONN_SUCCESS_MSG = "HTTP/1.1 200 Connection established\r\n"+"Proxy-Agent: ProxyServer/1.0\r\n" +"\r\n";
    private final static String HTTP_NOT_FOUND_MSG = "HTTP/1.1 404 NOT FOUND\r\n" +"Proxy-agent: ProxyServer/1.0\r\n" + "\r\n";
    private final static String TIMEOUT_MSG = "HTTP/1.1 504 Timeout Occured after 10s\r\n" + "Proxy-Agent: ProxyServer/1.0\r\n" + "\r\n";
    private HashMap<String, byte[]> readResult = new HashMap<>();
    private HashMap<String, String> headerResult = new HashMap<>();
    private final static int BUFFER_SIZE = 8192 * 16;

    RequestHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    private void readInput(DataInputStream br){
        StringBuilder header = new StringBuilder();
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        String line;
        int contentLength = 0;

        try{
            /* Read Header */
            while(!(line = br.readLine()).isEmpty()){
                header.append(line+"\r\n");
                int sepIndex = line.indexOf(":");

                if(sepIndex!=-1){
                    String elemTitle = line.substring(0,sepIndex);
                    String elemContent = line.substring(sepIndex+2);

                    headerResult.put(elemTitle,elemContent);

                    if(elemTitle.toLowerCase().contains("content-length"))
                        contentLength = Integer.valueOf(elemContent);
                }
            }
            System.out.println(header.toString());
            System.out.println();

            /* Read Body  */
            byte[] buffer = new byte[BUFFER_SIZE];

            String transferType = headerResult.get("Transfer-Encoding") != null ? headerResult.get("Transfer-Encoding") : " ";
            if(transferType.equals("chunked")){
                int chunkSize;
                while(true){
                    line = br.readLine();
                    chunkSize = hex2Dec(line);
                    body.write((line+"\r\n").getBytes());
                    if(chunkSize <= 0)
                        break;

                    chunkSize += 2;
                    while(0 < chunkSize) {
                        int readSize = br.read(buffer,0,chunkSize);
                        body.write(buffer, 0, readSize);
                        chunkSize -= readSize;
                    }
                }
            }else{
                int received = 0;
                while(received < contentLength) {
                    int readSize = br.read(buffer, 0,buffer.length);

                    if (readSize == -1)
                        break;

                    received += readSize;
                    body.write(buffer, 0, readSize);
                }
            }
            body.write("\r\n".getBytes());
//            System.out.println(body.toString());
//            System.out.println();

            readResult.put("header",(header.toString()+"\r\n").getBytes());
            readResult.put("body", body.toByteArray());
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int hex2Dec(String hex) {
        String digits = "0123456789ABCDEF";
        hex = hex.toUpperCase();
        int val = 0;
        for (int i = 0; i < hex.length(); i++)
        {
            char c = hex.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return val;
    }

    public void run(){
        try {
            DataInputStream C2PBufferReader = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream C2PBufferWriter = new DataOutputStream(clientSocket.getOutputStream());

            /* REQUEST LINE */
            String requestLine = C2PBufferReader.readLine();
            if(requestLine == null)
                return;
            System.out.println("[REQUEST] "+requestLine);
            String[] split = requestLine.split(" ");
            String method = split[0];
            String path = split[1];

            if(method.equals("CONNECT")){ /* HTTPS request handler */
                String[] s = path.split(":");

                if(!Proxy.isAllowedSite(s[0])){
                    System.out.println("This is Blocked Website" + s[0]);
                    C2PBufferWriter.writeBytes(HTTP_NOT_FOUND_MSG);
                    C2PBufferWriter.flush();
                    C2PBufferReader.close();
                    C2PBufferWriter.close();
                    return;
                }
                InetAddress address = InetAddress.getByName(s[0]);
                int port = Integer.valueOf(s[1]);

                System.out.println("HTTPS Connect to "+address +":" + port);
                Socket serverSocket = new Socket(address,port);
                serverSocket.setSoTimeout(5000);

                String a = C2PBufferReader.readLine();
                while(!a.isEmpty()){
                    a = C2PBufferReader.readLine();
                }

                C2PBufferWriter.writeBytes(CONN_SUCCESS_MSG);
                C2PBufferWriter.flush();

                new Thread(()->{
                    try {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int read;
                        do {
                            read = clientSocket.getInputStream().read(buffer);
                            if (read > 0) {
                                serverSocket.getOutputStream().write(buffer, 0, read);
                                if (clientSocket.getInputStream().available() < 1) {
                                    serverSocket.getOutputStream().flush();
                                }
                            }
                        } while (read >= 0);
                    }
                    catch (Exception e) {
                    }
                }).start();

                try {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int read;
                    do {
                        read = serverSocket.getInputStream().read(buffer);
                        if (read > 0) {
                            clientSocket.getOutputStream().write(buffer, 0, read);
                            if (serverSocket.getInputStream().available() < 1)
                                clientSocket.getOutputStream().flush();
                        }
                    } while (read >= 0);
                } catch (SocketTimeoutException e) {

                    try{
                        C2PBufferWriter.writeBytes(TIMEOUT_MSG);
                        C2PBufferWriter.flush();
                    } catch (IOException ioe) {
                    }
                } catch (IOException e) {
                }
            }else{ /* HTTP Request handler */
                if(!Proxy.isAllowedSite(path)){
                    System.out.println("This is Blocked Website" + path);
                    C2PBufferWriter.writeBytes(HTTP_NOT_FOUND_MSG);
                    C2PBufferWriter.flush();
                    C2PBufferReader.close();
                    C2PBufferWriter.close();
                    return;
                }

                if(method.equals("GET") && Proxy.cache.containsKey(path)){
                    /* Cache Exists */
                    System.out.println("[NOTE] Cache Exists "+path);
                    DataInputStream file2cacheRW = new DataInputStream(new FileInputStream(Proxy.cache.get(path)));
                    readInput(file2cacheRW);
                    C2PBufferWriter.write(readResult.get("header"));
                    C2PBufferWriter.write(readResult.get("body"));
                    file2cacheRW.close();
                }else{
                    /* No Cache Exists */
                    readInput(C2PBufferReader);

                    InetAddress address = InetAddress.getByName(headerResult.get("Host"));
                    Socket serverSocket = new Socket(address, 80);
                    DataInputStream S2PBufferReader = new DataInputStream(serverSocket.getInputStream());
                    DataOutputStream S2PBufferWriter = new DataOutputStream(serverSocket.getOutputStream());

                    /* Write request to server  */
                    S2PBufferWriter.writeBytes(requestLine+"\r\n");
                    S2PBufferWriter.write(readResult.get("header"));
                    S2PBufferWriter.write(readResult.get("body"));

                    /* Get response from server */
                    readInput(S2PBufferReader);
                    C2PBufferWriter.write(readResult.get("header"));
                    C2PBufferWriter.write(readResult.get("body"));

                    /* Store as cache */
                    System.out.println("[WRITE CACHE]");
                    Proxy.filenameMapping(path);
                    DataOutputStream fileToCacheBW = new DataOutputStream(new FileOutputStream(Proxy.cache.get(path)));
                    fileToCacheBW.write(readResult.get("header"));
                    fileToCacheBW.write(readResult.get("body"));

                    /* Close streams */
                    S2PBufferReader.close();
                    S2PBufferWriter.close();
                    fileToCacheBW.close();
                    serverSocket.close();
                }
            }

            // Close Client connection
            C2PBufferReader.close();
            C2PBufferWriter.close();
        } catch (Exception e) {
        }
    }
}
