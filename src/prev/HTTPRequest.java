import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.function.BiConsumer;

class HTTPRequest {
    int port = 80;
    /* initialize default values */
    String method = "GET";
    String path = "/";
    String version = "HTTP/1.0";

    HashMap<String, String> headers = new HashMap<>();

    Socket socket;
    DataOutputStream outputStream;
    BufferedReader reader;

    HTTPRequest (String method, String path, String version) throws Exception {
        try {
            URL url = new URL(path);
            this.path = url.getPath();
            headers.put("Host", url.getHost());
        } catch (Exception e) {
            System.out.println("HTTPRequest ERROR\n"+e.getMessage());
        }
        // TODO: Read more lines using reader.readLine() and parse request headers

//        headers.put("Accept-Encoding", "identity");
//        headers.put("Connection", "close");
    }

    public Response getResponse() throws Exception {
        // 1. Get host address using DNS and connect

        InetAddress address = InetAddress.getByName((headers.get("Host")));

        System.out.println("REQ: Connecting to http://" + address + ":"+port);

        socket = new Socket(address, port);
        outputStream = new DataOutputStream(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // 2. Send request

        write(method + " " + path + " " + version);

        for(String key : headers.keySet()) {
            write(key + ": " + headers.get(key));
        }

        write("");

        // 3. Read response

        Response response = new Response(reader);

        return response;
    }

    private void write (String line) throws Exception {
        System.out.println("REQ > " + line);
        outputStream.writeBytes(line + "\r\n");
        outputStream.flush();
    }

    @Override
    public String toString() {
        return "[REQUEST] " + "Method: " + method + " " + "Path: " + path + " " + "Version: " + version;
    }
}