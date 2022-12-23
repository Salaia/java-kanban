package managers;

import tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Long, Node<Task>> history; // История
    private Node<Task> head; // Он же first
    private Node<Task> tail; // Он же last

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

    public void linkLast(Task task) { //  будет добавлять задачу в конец этого списка
        final Node<Task> oldTail = tail;
        final Node<Task> newTail = new Node<>(oldTail, task, null);
        tail = newTail;

        if (head == null && oldTail == null) {
            head = newTail;
        } else {
            oldTail.next = newTail;
        }
    } // linkLast

    private List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        Node<Task> currentNode = head;
        result.add(head.data);

        while (currentNode.next != null) {
            result.add(currentNode.next.data);
            currentNode = currentNode.next;
        }
        return result;
    } // getTasks

    public void removeNode(Node<Task> node) {
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

}