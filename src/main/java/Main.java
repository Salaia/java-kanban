import com.google.gson.Gson;
import http.HttpTaskServer;
import http.KVServer;
import managers.Managers;
import managers.TaskManager;

import java.io.IOException;
import java.net.http.HttpClient;

public class Main {

    static TaskManager taskManager;
    static HttpClient client;
    static Gson gson;

    public static void main(String[] args) throws IOException {
        new KVServer().start();
        new HttpTaskServer().start();

        taskManager = Managers.getDefault();
        client = HttpClient.newHttpClient();
        gson = Managers.getGson();
    }

}