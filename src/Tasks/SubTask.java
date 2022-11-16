package Tasks;

import java.util.Objects;

public class SubTask extends Task {
    long epicID; // Какому эпику принадлежит

    public SubTask(String name, String description, long ID, String status, long epicID) {
        super(name, description, ID, status);
        this.epicID = epicID;
    }

    public long getEpicID() {
        return epicID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SubTask subTask = (SubTask) o;
        return epicID == subTask.epicID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicID);
    }

    @Override
    public String toString() {
        return "Sub" + super.toString() + "SubTask{" +
                "epicID=" + epicID +
                '}';
    }
}
