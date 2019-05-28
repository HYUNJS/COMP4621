//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.Socket;
//import java.util.HashMap;
//
//public class RequestHandler{
//    private Socket clientSocket;
//    private static final String P2Cline = "HTTP/1.0 200 Connection established\r\nProxy-Agent: ProxyServer/1.0\r\n";
//    private HashMap<String, String> readResult = new HashMap<>();
//    private HashMap<String, String> headerResult = new HashMap<>();
//
//    RequestHandler(Socket clientSocket){
//        this.clientSocket = clientSocket;
//    }
//
////    private void readInput(BufferedReader br){
////    private void readInput(DataInputStream br, DataOutputStream bw){
//    private void readInput(DataInputStream br){
//        StringBuilder header = new StringBuilder();
//        StringBuilder body = new StringBuilder();
//        String line;
//        int contentLength = 0;
//
//        try{
//            /* Read Header */
//            while(!(line = br.readLine()).isEmpty()){
//                header.append(line+"\r\n");
//                int sepIndex = line.indexOf(":");
//
//                if(sepIndex!=-1){
//                    String elemTitle = line.substring(0,sepIndex);
//                    String elemContent = line.substring(sepIndex+2);
//
//                    headerResult.put(elemTitle,elemContent);
//
//                    if(elemTitle.toLowerCase().contains("content-length"))
//                        contentLength = Integer.valueOf(elemContent);
//                }
//            }
//            System.out.println(header.toString());
//            System.out.println();
//
//            /* Read Body  */
//            byte[] buffer = new byte[8192 * 8];
//
//            String transferType = headerResult.get("Transfer-Encoding") != null ? headerResult.get("Transfer-Encoding") : " ";
//            if(transferType.equals("chunked")){
//                System.out.println("CHUNK responsee!!!!!!!!");
//
//                int chunkSize;
//                while(true){
//                    line = br.readLine();
//                    chunkSize = hexToDec(line);
//                    body.append(line).append("\r\n");
//                    if(chunkSize <= 0)
//                        break;
//
//                    chunkSize += 2;
//                    while(0 < chunkSize) {
//                        int readSize = br.read(buffer,0,chunkSize);
//                        body.append(new String(buffer, 0, readSize));
//                        chunkSize -= readSize;
//                    }
//                }
//            }else{
//                int received = 0;
//                while(received < contentLength) {
//                    int readSize = br.read(buffer, 0,contentLength-received);
//
//                    if (readSize == -1)
//                        break;
//
//                    received += readSize;
//                    body.append(new String(buffer), 0, readSize);
//                }
//                System.out.println("RECEVIED !!!!!!!   "+received);
//            }
//            System.out.println(body.toString());
//            System.out.println();
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        readResult.put("header",header.toString());
//        readResult.put("body", body.toString());
//    }
//
//    private static int hexToDec(String hex) {
//        String digits = "0123456789ABCDEF";
//        hex = hex.toUpperCase();
//        int val = 0;
//        for (int i = 0; i < hex.length(); i++)
//        {
//            char c = hex.charAt(i);
//            int d = digits.indexOf(c);
//            val = 16*val + d;
//        }
//        return val;
//    }
//
//    public void run(){
//        try {
//            DataInputStream C2PBufferReader = new DataInputStream(clientSocket.getInputStream());
//            DataOutputStream C2PBufferWriter = new DataOutputStream(clientSocket.getOutputStream());
//
//            /* 1st request line */
//            String requestLine = C2PBufferReader.readLine();
//            if(requestLine == null)
//                return;
//
//            String[] split = requestLine.split(" ");
//            String method = split[0];
//            String path = split[1];
//            String version = split[2];
//
//            if(method.equals("CONNECT")){ /* HTTPS request handler */
////                String[] s = path.split(":");
////                InetAddress address = InetAddress.getByName(s[0]);
////                int port = Integer.valueOf(s[1]);
////                System.out.println("Connect to "+address + port);
////                Socket serverSocket = new Socket(address,port);
////                InputStream serverIS = serverSocket.getInputStream();
////                OutputStream serverOS = serverSocket.getOutputStream();
////
////                clientSocket.getInputStream().transferTo(serverOS);
////
//////                C2PBufferWriter.writeBytes(P2Cline);
////                serverIS.transferTo(clientSocket.getOutputStream());
////
//////                BufferedReader S2PBufferReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
//////                DataOutputStream S2PBufferWriter = new DataOutputStream(serverSocket.getOutputStream());
//////
//////                C2PBufferWriter.writeBytes(P2Cline);
//////                C2PBufferWriter.flush();
//////
//////                try {
//////                    byte[] buffer = new byte[4096];
//////                    int read;
//////                    do {
//////                        read = C2PBufferReader.read(buffer);
//////                        if (read > 0) {
//////                            proxyToServerOS.write(buffer, 0, read);
//////                            if (proxyToClientIS.available() < 1) {
//////                                proxyToServerOS.flush();
//////                            }
//////                        }
//////                    } while (read >= 0);
//////                } catch (IOException e) {
//////                    System.out.println("Proxy to client HTTPS read timed out");
//////                    e.printStackTrace();
//////                }
//            }else{ /* HTTP Request handler */
//                System.out.println("[REQ] : "+requestLine);
////                S2PBufferWriter.writeBytes(requestLine+"\r\n");
//                readInput(C2PBufferReader);
//
//                InetAddress address = InetAddress.getByName(headerResult.get("Host"));
//                Socket serverSocket = new Socket(address, 80);
//                DataInputStream S2PBufferReader = new DataInputStream(serverSocket.getInputStream());
//                DataOutputStream S2PBufferWriter = new DataOutputStream(serverSocket.getOutputStream());
//
//                /* Write request to server  */
////                S2PBufferWriter.writeBytes(requestLine+"\r\n");
//                S2PBufferWriter.writeBytes(readResult.get("header")+"\r\n");
//                S2PBufferWriter.writeBytes(readResult.get("body")+"\r\n");
//
//                System.out.println("get response!!!!!!!!!");
//                /* Get response from server */
//                readInput(S2PBufferReader);
//                C2PBufferWriter.writeBytes(readResult.get("header")+"\r\n");
//                C2PBufferWriter.writeBytes(readResult.get("body")+"\r\n");
//
//                System.out.println("Sent response!!!!!");
//                /* Close streams */
//                S2PBufferReader.close();
//                S2PBufferWriter.close();
//                serverSocket.close();
//            }
//
//
//            // Close Resource connection
//            C2PBufferReader.close();
//            C2PBufferWriter.close();
//        } catch (Exception e) {
//            System.err.println("* " + e);
//            e.printStackTrace();
//        }
//    }
//}
