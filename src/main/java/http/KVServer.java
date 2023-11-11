package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/*
	Этот класс - хранилище. Хранит в мапе, data, данные хранятся по принципу <ключ-значение>.
	Живёт и слушает запросы постоянно
	К нему обращается KVTaskClient, когда в HttpTaskManager вызывается (переопределенный!) метод save
 */
public class KVServer {
    public static final int PORT = 8078;
    private final String apiToken;
    private final HttpServer server;
    private final Map<String, String> data = new HashMap<>();

    public KVServer() throws IOException {
        apiToken = generateApiToken();
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/register", this::register);
        server.createContext("/save", this::save);
        server.createContext("/load", this::load);
    }

    // GET /load/<ключ>?API_TOKEN= — возвращать сохранённые значение по ключу.
    private void load(HttpExchange h) throws IOException {
        try {
            if (!hasAuth(h)) {
                h.sendResponseHeaders(403, 0);
                return;
            }
            if ("GET".equals(h.getRequestMethod())) {
                String key = h.getRequestURI().getPath().substring("/load/".length());
                if (key.isEmpty()) {
                    h.sendResponseHeaders(400, 0);
                    return;
                }
                if (!data.containsKey(key)) {
                    h.sendResponseHeaders(404, 0);
                }
                sendText(h, data.get(key)); // метод этого класса
                h.sendResponseHeaders(200, 0);
            } else {
                h.sendResponseHeaders(405, 0);
            }
        } finally {
            h.close();
        }
    }

    // POST /save/<ключ>?API_TOKEN= — сохранять содержимое тела запроса, привязанное к ключу.
    private void save(HttpExchange h) throws IOException {
        try {
            if (!hasAuth(h)) {
                h.sendResponseHeaders(403, 0);
                return;
            }
            if ("POST".equals(h.getRequestMethod())) {
                String key = h.getRequestURI().getPath().substring("/save/".length());
                if (key.isEmpty()) {
                    h.sendResponseHeaders(400, 0);
                    return;
                }
                String value = readText(h);
                if (value.isEmpty()) {
                    h.sendResponseHeaders(400, 0);
                    return;
                }
                data.put(key, value); // всё ради вот этого
                h.sendResponseHeaders(200, 0);
            } else {
                h.sendResponseHeaders(405, 0);
            }
        } finally {
            h.close();
        }
    }

    // GET /register — регистрировать клиента и выдавать уникальный токен доступа (аутентификации).
    // Это нужно, чтобы хранилище могло работать сразу с несколькими клиентами.
    private void register(HttpExchange h) throws IOException {
        try {
            if ("GET".equals(h.getRequestMethod())) {
                sendText(h, apiToken);
            } else {
                h.sendResponseHeaders(405, 0);
            }
        } finally {
            h.close();
        }
    }

    public void start() {
        System.out.println("Starting KV-server on port: " + PORT);
        server.start();
    }

    public void stop() {
        System.out.println("KVServer stopped");
        server.stop(1);
    }

    private String generateApiToken() {
        return "" + System.currentTimeMillis();
    }

    protected boolean hasAuth(HttpExchange exchange) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        return rawQuery != null && (rawQuery.contains("API_TOKEN=" + apiToken) || rawQuery.contains("API_TOKEN=DEBUG"));
    }

    protected String readText(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), UTF_8);
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
    }
}
