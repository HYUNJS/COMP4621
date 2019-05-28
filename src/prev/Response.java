import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Response {
    ArrayList<String> lines = new ArrayList<>();

    // String resLine;
    // HashMap<String, String> headers = new HashMap<>();
    // String resBody;

    Response(BufferedReader reader) throws Exception {
        String line;

        while((line = reader.readLine()) != null) {
            lines.add(line);
        }

        // TODO: set resLine, headers and resBody using lines<>
    }

    void send (DataOutputStream stream) throws Exception {
        for (String line : lines) {
            System.out.println("RES > " + line);
            stream.writeBytes(line + "\r\n");
            stream.flush();
        }
        stream.writeBytes("\r\n");
        stream.flush();
    }
}