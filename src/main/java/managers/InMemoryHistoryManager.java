package managers;

import tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Long, Node<Task>> history; // История
    private Node<Task> head; // Он же first
    private Node<Task> tail; // Он же last
    private int size = 0;

    // Конструктор
    public InMemoryHistoryManager() {

        history = new HashMap<>();
    }

    @Override
    public ArrayList<Task> getHistory() {
        return getTasks();
    }

    // Добавляет просмотренные задачи в историю просмотров
    @Override
    public void add(Task task) {
        if (history.containsKey(task.getID())) {
            remove(task.getID());
        }
        linkLast(task);
        history.put(task.getID(), tail);
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
        size++;
    } // linkLast

    private ArrayList<Task> getTasks() {

        if (size == 0) {
            return new ArrayList<>();
        }

        ArrayList<Task> arrayList = new ArrayList<>();
        Node<Task> currentNode = head;
        for (int i = 0; i < this.size; i++) {
            if (i == 0) {
                arrayList.add(head.data);
            } else if (i < this.size - 1) {
                arrayList.add(currentNode.next.data);
                currentNode = currentNode.next;
            } else if (i == this.size - 1) {
                arrayList.add(tail.data);
            }
        }
        return arrayList;
    } // getTasks

    public void removeNode(Node<Task> node) {
        if (node == null) {
            return;
        }
        Task task = node.data;
        history.remove(task.getID());
        if (node.prev != null && node.next != null) {  // BODY
            node.prev.next = node.next;
            node.next.prev = node.prev;
            history.remove(task.getID());
        } else if (node.prev != null) {// TAIL
            tail = node.prev;
            node.prev.next = null;
            history.remove(task.getID());
        } else if (node.next != null) { // HEAD
            node.next.prev = node.prev;
                head = node.next;
            history.remove(task.getID());
        }
        size--;
    } // removeNode

}