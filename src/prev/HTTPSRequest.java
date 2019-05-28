//import java.net.*;
//import java.io.*;
//
//public class HTTPSRequest {
//    private InetAddress address;
//    private int port;
//    private String version;
//
//    // Used for getResponse()
//    Socket socket;
//    DataOutputStream outputStream;
//    BufferedReader reader;
//
//    public HTTPSRequest(String url, int port, String version){
//        try {
//            address = InetAddress.getByName(url);
//            this.port = port;
//            this.version = version;
//        }catch(Exception e){ }
//    }
//
//    public Response getResponse() throws Exception {
//        System.out.println("REQ: Connecting to https://" + address + ":"+port);
//
//        socket = new Socket(address, port);
//        socket.setSoTimeout(5000);
//
//        outputStream = new DataOutputStream(socket.getOutputStream());
//        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//        String line = version+" 200 Connection established\r\n" +
//                "Proxy-Agent: ProxyServer/1.0\r\n" +
//                "\r\n";
//
//        write(line);
//
//        Response response = new Response(reader);
//
//        return response;
//    }
//
//    private void write (String line) throws Exception {
//        System.out.println("REQ > " + line);
//        outputStream.writeBytes(line + "\r\n");
//        outputStream.flush();
//    }
//
//
//    class ClientToServerHttpsTransmit implements Runnable{
//
//        InputStream proxyToClientIS;
//        OutputStream proxyToServerOS;
//
//        /**
//         * Creates Object to Listen to Client and Transmit that data to the server
//         * @param proxyToClientIS Stream that proxy uses to receive data from client
//         * @param proxyToServerOS Stream that proxy uses to transmit data to remote server
//         */
//        public ClientToServerHttpsTransmit(InputStream proxyToClientIS, OutputStream proxyToServerOS) {
//            this.proxyToClientIS = proxyToClientIS;
//            this.proxyToServerOS = proxyToServerOS;
//        }
//
//        @Override
//        public void run(){
//            try {
//                // Read byte by byte from client and send directly to server
//                byte[] buffer = new byte[4096];
//                int read;
//                do {
//                    read = proxyToClientIS.read(buffer);
//                    if (read > 0) {
//                        proxyToServerOS.write(buffer, 0, read);
//                        if (proxyToClientIS.available() < 1) {
//                            proxyToServerOS.flush();
//                        }
//                    }
//                } while (read >= 0);
//            }
//            catch (SocketTimeoutException ste) {
//                // TODO: handle exception
//            }
//            catch (IOException e) {
//                System.out.println("Proxy to client HTTPS read timed out");
//                e.printStackTrace();
//            }
//        }
//    }
//}
//
