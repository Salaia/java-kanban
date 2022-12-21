package managers;

import tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    // история просмотров.
    // По ТЗ "список", но я не могу использовать List, потому что
    // CustomLinkedList не наследует этот интерфейс (как и какой-либо)
    private final CustomLinkedList<Task> history;
    private final Map<Long, Node<Task>> fastHistory; // мапа для быстрого поиска узлов-задач в истории по ID задачи

    // Конструктор
    public InMemoryHistoryManager() {

        history = new CustomLinkedList<>();
        fastHistory = new HashMap<>();
    }

    @Override
    public ArrayList<Task> getHistory() { // Вот тут я не поняла, кто что делать будет.
        return history.getTasks(); // по ТЗ getHistory и getTasks делают, кажется, одно и то же, так что сделала getTasks приватным
    }

    // Добавляет просмотренные задачи в историю просмотров
    @Override
    public void add(Task task) {
        if(fastHistory.containsKey(task.getID())) {
            remove(task.getID());
        }
        history.linkLast(task);
        fastHistory.put(task.getID(), history.tail);
    }

    @Override // // для удаления задачи из просмотра
    public void remove(Long id) {
        if (fastHistory.containsKey(id)) {
            history.removeNode(fastHistory.get(id));
        }
    } // remove

    class CustomLinkedList<T> {
        private Node<T> head; // Он же first
        private Node<T> tail; // Он же last
        private int size = 0;

        public void linkFirst(T element) { // добавляет задачу в начало списка
            final Node<T> oldHead = head;
            final Node<T> newNode = new Node<>(null, element, oldHead);
            head = newNode;
            if (oldHead == null)
                tail = newNode;
            else
                oldHead.prev = newNode;
            size++;
        }

        public T getFirst() {
            final Node<T> curHead = head;
            if (curHead == null)
                throw new NoSuchElementException();
            return head.data;
        }

        public void linkLast(T element) { //  будет добавлять задачу в конец этого списка
            if (element == head) {
                head.prev = tail;
                tail.next = head;
            }
            final Node<T> oldTail = tail;
            final Node<T> newTail = new Node<>(oldTail, element, null);
            tail = newTail;
            if (head == null && oldTail == null) {
                head = newTail;
                //System.out.println("Tail: " + tail + ", head: " + head + ",Old tail: " + oldTail);
            } else {
                oldTail.next = newTail;
                //System.out.println("Tail: " + tail + ", head: " + head + ",Old tail: " + oldTail);

            }
            size++;
        }

        public T getLast() {
            final Node<T> curTail = tail;
            if (curTail == null) {
                throw new NoSuchElementException();
            }
            return tail.data;
        }

        public int size() {
            return this.size;
        }

        private ArrayList<T> getTasks() { // this CustomArrayList -> ArrayList

           //System.out.println("Custom -> ArrayList: ");
            //System.out.println("Fast history: " + fastHistory + ", history size: " + history.size + ", tail: " + tail.prev.data);
            if (history.size == 0) {return new ArrayList<>();}

            ArrayList<T> arrayList = new ArrayList<>();
            Node<T> currentNode = head;
            for(int i = 0; i < this.size; i++) {
                if (i == 0) {
                    arrayList.add(head.data);
                    //System.out.println("HEAD: " + head.data + ", node: " + head);
                } else if (i < this.size-1) {
                    //System.out.println("BODY: " + currentNode.next.data + ", node: " + currentNode.next);
                    arrayList.add(currentNode.next.data);
                    currentNode = currentNode.next;
                } else if (i == this.size-1){
                    arrayList.add(tail.data);
                    //System.out.println("LIST TAIL: " + tail.data);
                    //System.out.println("This size" + this.size + ",Array size: " + arrayList.size() + "\n");
                }
            }
            //System.out.println("Custom history size: " + history.size + "array size: " + arrayList.size());
            return arrayList;

        } // getTasks

        public void removeNode(Node<T> node) { // по ТЗ: "Добавьте метод removeNode в класс" - в какой не сказано.
            // Положила внутрь самодельного спика, потому что метод, кажется, чётко его, а не менеджера истории
            if (node == null) {return;}
            //System.out.println("\nREMOVE node: " + node.data);

            Node<T> nodeToRemove = node;
            Task task = (Task)node.data;
            fastHistory.remove(task.getID());
            //System.out.println("Check");
            if (node.prev != null && node.next != null){  // BODY
                //System.out.println("Prev: " + node.prev.data + ", next: " + node.next.data);
                //System.out.println("TAIL: " + tail.data + ", HEY!"+ getTasks());
                node.prev.next = node.next;
                //System.out.println("1");
                node.next.prev = node.prev;
                //System.out.println("2");
                //System.out.println(tail.data);
                fastHistory.remove(task.getID());
            }
            // if head or tail - it will reassign null
            else if (node.prev != null && node.next == null) {// TAIL
                //System.out.println("REMOVE TAIL");
                node.prev.next = null;
                fastHistory.remove(task.getID());
            }
            else if (node.next != null && node.prev == null) { // HEAD
                //System.out.println("REMOVE HEAD");
                node.next.prev = node.prev;
                if (nodeToRemove.equals(head)){
                    //System.out.println("IS HEAD");
                    head = node.next;
                }
                fastHistory.remove(task.getID());
            }
            // на узел node никто не указывает - жертва сборщика
            // и доступа нет, данные убивать нет смысла
            size--;
        }

    } // CustomArrayList

}