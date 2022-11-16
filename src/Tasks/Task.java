package Tasks;

import java.util.Objects;

public class Task {
    // Тут напрашивается ограничение доступа, хотя пока не могу сообразить, какие будут проверки
    private String name;
    private String description;
    private long ID; // Счётчик countID в TaskManager. Интересно, мы его оставим как счётчик или потом заменим на hash
    private String status; // NEW, IN_PROGRESS, DONE

    public String getStatus() {
        return status;
    }

    public Task(String name, String description, long ID, String status) {
        this.name = name;
        this.description = description;
        this.ID = ID;
        this.status = status;
    }

    public void setStatus(String status) {
        // Сюда напрашивается Enum, но сама я их плохо знаю, а в курсе ещё не было
        // Может, на следующих ТЗ будет исправлено на перечисление
        if (status != null && (status.equals("NEW") || status.equals("IN_PROGRESS") || status.equals("DONE"))) {
            this.status = status;
        }
    } // setStatus

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", ID=" + ID +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return ID == task.ID && Objects.equals(name, task.name) && Objects.equals(description, task.description) && Objects.equals(status, task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, ID, status);
    }

}
