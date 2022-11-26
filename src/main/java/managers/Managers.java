package managers;
/*
На нём будет лежать вся ответственность за создание менеджера задач.
То есть Managers должен сам подбирать нужную реализацию TaskManager и возвращать объект правильного типа.
*/
public class Managers {

    private InMemoryTaskManager inMemoryTaskManager = null;
    private static InMemoryHistoryManager inMemoryHistoryManager = null;

    // При этом вызывающему неизвестен конкретный класс, только то, что
    // объект, который возвращает getDefault(), реализует интерфейс TaskManager.
    public TaskManager getDefault() {
        if (inMemoryTaskManager != null) {
            return inMemoryTaskManager;
        } else {
            return new InMemoryTaskManager();
        }
    }

    public static HistoryManager getDefaultHistory() {
        if (inMemoryHistoryManager != null) {
            return inMemoryHistoryManager;
        } else {
            return new InMemoryHistoryManager();
        }
    }

}