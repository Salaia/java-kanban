package managers;
/*
На нём будет лежать вся ответственность за создание менеджера задач.
То есть Managers должен сам подбирать нужную реализацию TaskManager и возвращать объект правильного типа.
*/
public class Managers {

    // При этом вызывающему неизвестен конкретный класс, только то, что
    // объект, который возвращает getDefault(), реализует интерфейс TaskManager.
    public TaskManager getDefault() {
        // TODO затычка!
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
