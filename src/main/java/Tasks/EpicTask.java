package Tasks;

import java.util.ArrayList;

public class EpicTask extends Task {
    public ArrayList<Long> subTasksIDs; // не могу сообразить, как будут выглядеть методы редактирования списка, так что пока паблик

    // Конструктор только под создание - статус расчитывается по сабтаскам
    public EpicTask(String name, String description) {
        super(name, description);
        subTasksIDs = new ArrayList<>();
    }
}
