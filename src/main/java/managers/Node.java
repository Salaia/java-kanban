package managers;

public class Node<E> {
    public E data; // данные в узле
    public Node<E> next;
    public Node<E> prev;

    // Конструктор
    public Node(Node<E> prev, E data, Node<E> next) {
        this.data = data;
        this.next = next;
        this.prev = prev;
    }
}