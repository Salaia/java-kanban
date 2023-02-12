package managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.KVServer;

import java.io.IOException;

/*
На нём будет лежать вся ответственность за создание менеджера задач.
То есть Managers должен сам подбирать нужную реализацию TaskManager и возвращать объект правильного типа.
*/
public class Managers {

    public static HttpTaskManager getDefaultHttp() {
        return new HttpTaskManager(KVServer.PORT);
    }

    // Это чтобы KVServer протестировать
    public static KVServer getDefaultKVServer() throws IOException {
        final KVServer kvServer = new KVServer();
        kvServer.start();
        return kvServer;
    }

    // Оставила пока, чтобы не рушить старые тесты
    public static InMemoryTaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getGson() { // Тут были адаптеры, но потом оказалось, что и без них все работает
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.create();
    }

}