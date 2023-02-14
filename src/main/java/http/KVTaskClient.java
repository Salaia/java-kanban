package http;

import exceptions.ManagerSaveException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*
	Этот класс - посредник
	Умеет все, что умеет KVServer, но инкапсулирует его
 */

public class KVTaskClient {

    private final String url;
    private final String apiToken;

    /*
    Конструктор принимает URL к серверу хранилища и регистрируется.
    При регистрации выдаётся токен (API_TOKEN), который нужен при работе с сервером.
     */
    public KVTaskClient(String url) {
        this.url = url;
        apiToken = register(url);
    } // Конструктор

    // В ответ от сервера KVServer он получает токен
    private String register(String url) { // GET /register
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "register"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Can't do save request, status code:" + response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Can't do save request: " + e);
        }
    } // register

    // Должен сохранять состояние менеджера задач через
    // запрос POST /save/<ключ>?API_TOKEN=. (высылает серверу)
    public void put(String key, String value) { // value in JSON
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "save/" + key + "?API_TOKEN=" + apiToken))
                    .POST(HttpRequest.BodyPublishers.ofString(value))
                    .build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Can't do save request, status code:" + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Can't do save request: " + e);
        }
    } // put

    // Должен возвращать состояние менеджера задач через запрос (высылает)
    // GET /load/<ключ>?API_TOKEN=. для сервера KVServer (слушает)
    public String load(String key) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "load/" + key + "?API_TOKEN=" + apiToken))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Can't do save request, status code:" + response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Can't do save request: " + e);
        }
    }
}
