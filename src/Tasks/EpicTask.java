package Tasks;

import java.util.ArrayList;
import java.util.Objects;

public class EpicTask extends Task {
    public ArrayList<Long> subTasksIDs; // не могу сообразить, как будут выглядеть методы редактирования списка, так что пока паблик

    public EpicTask(String name, String description, long ID, String status) {
        super(name, description, ID, status);
        subTasksIDs = new ArrayList<>();
        // Думаю, при создании эпика не будет возможности создать его с подзадачами.
        // А будет - добавлю в принимаемые параметры.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EpicTask epicTask = (EpicTask) o;
        return Objects.equals(subTasksIDs, epicTask.subTasksIDs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subTasksIDs);
    }

    @Override
    public String toString() {
        return "Epic" + super.toString() + "EpicTask{" +
                "subTasksIDs=" + subTasksIDs +
                '}';
    }
}
