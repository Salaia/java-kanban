package managers;

import tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Long, Node> history; // История
    private Node head; // Он же first
    private Node tail; // Он же last

    // Конструктор
    public InMemoryHistoryManager() {
        history = new HashMap<>();
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    // Добавляет просмотренные задачи в историю просмотров
    @Override
    public void add(Task task) {
        if (history.containsKey(task.getId())) {
            remove(task.getId());
        }
        linkLast(task);
        history.put(task.getId(), tail);
    }

    @Override // // для удаления задачи из просмотра
    public void remove(Long id) {
        if (history.containsKey(id)) {
            removeNode(history.get(id));
        }
    } // remove

    public void linkLast(Task element) { //  будет добавлять задачу в конец этого списка
        final Node oldTail = tail;
        final Node newTail = new Node(oldTail, element, null);
        tail = newTail;

        if (head == null && oldTail == null) {
            head = newTail;
        } else {
            oldTail.next = newTail;
        }
    } // linkLast

    private List<Task> getTasks() {
        if(history.isEmpty()) {
            return null; // нужно в FileBackedTaskManager для проверки сохранять ли историю в файл или NullPointer настигнет вас
        }
        List<Task> result = new ArrayList<>();
        Node currentNode = head;
        result.add(head.data);

        while (currentNode.next != null && history.size() > 1) {
            result.add(currentNode.next.data);
            currentNode = currentNode.next;
        }
        return result;
    } // getTasks

    public void removeNode(Node node) {
        if (node == null) {
            return;
        }
        Task task = node.data;
        history.remove(task.getId());
        if (node.prev != null && node.next != null) {  // BODY
            node.prev.next = node.next;
            node.next.prev = node.prev;
            history.remove(task.getId());
        } else if (node.prev != null) {// TAIL
            tail = node.prev;
            node.prev.next = null;
            history.remove(task.getId());
        } else if (node.next != null) { // HEAD
            node.next.prev = node.prev;
            head = node.next;
            history.remove(task.getId());
        }
    } // removeNode
    class Node {
        public Task data; // данные в узле
        public Node next;
        public Node prev;

        // Конструктор
        public Node(Node prev, Task data, Node next) {
            this.data = data;
            this.next = next;
            this.prev = prev;
        } // Конструктор
    } // class Node

}