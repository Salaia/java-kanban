package Tasks;

public class Task {
    private String name;
    private String description;


    private long ID; // Счётчик countID в TaskManager. Интересно, мы его оставим как счётчик или потом заменим на hash
    private Status status; // теперь Enum

    public Status getStatus() {
        return status;
    }

    // Конструктор под создание: только имя и описание
    public Task(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void setID(long ID) {
        this.ID = ID;
    }


    public void setStatus(Status status) {
        this.status = status;
    } // setStatus


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getID() {
        return ID;
    }
}
