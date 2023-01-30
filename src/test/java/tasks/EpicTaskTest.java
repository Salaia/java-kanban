package tasks;

import managers.Managers;
import managers.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
    Наверное, все эти сценарии уже были в моих предыдущих тестах,
    но здесь то, что требуется по ТЗ 7
 */

class EpicTaskTest {

    TaskManager manager = Managers.getDefault();
    static Long epicId;
    static Long sub1Id;
    Long sub2Id;

    @BeforeEach
    void beforeEach() {
        epicId = manager.recordEpicTask(new EpicTask("n", "d"));
        sub1Id = manager.recordSubTask(new SubTask("n", "d", epicId));
        sub2Id = manager.recordSubTask(new SubTask("n", "d", epicId));
    }

    //  Пустой список подзадач
    @Test
    public void emptySubsList() {
        Long epicIdEmpty = manager.recordEpicTask(new EpicTask("n", "d"));
        EpicTask epic = manager.getEpicTaskByIdOrNull(epicIdEmpty);
        assertEquals(0, epic.getSubTasksIds().size());
    }

    //  Все подзадачи со статусом NEW.
    @Test
    public void allSubsNew() {
        assertEquals(Status.NEW, manager.getEpicTaskByIdOrNull(epicId).getStatus());
        assertEquals(Status.NEW, manager.getSubTaskByIdOrNull(sub1Id).getStatus());
        assertEquals(Status.NEW, manager.getSubTaskByIdOrNull(sub2Id).getStatus());
    }

    //  Все подзадачи со статусом DONE.
    @Test
    public void allSubsDone() {
        SubTask sub1Done = manager.getSubTaskByIdOrNull(sub1Id);
        SubTask sub2Done = manager.getSubTaskByIdOrNull(sub2Id);
        sub1Done.setStatus(Status.DONE);
        sub2Done.setStatus(Status.DONE);
        manager.updateSubTask(sub1Done);
        manager.updateSubTask(sub2Done);
        assertEquals(Status.DONE, manager.getEpicTaskByIdOrNull(epicId).getStatus());
        assertEquals(Status.DONE, manager.getSubTaskByIdOrNull(sub1Id).getStatus());
        assertEquals(Status.DONE, manager.getSubTaskByIdOrNull(sub2Id).getStatus());
    }

    // Подзадачи со статусами NEW и DONE.
    @Test
    public void subsNewAndDone() {
        SubTask sub1Done = manager.getSubTaskByIdOrNull(sub1Id);
        sub1Done.setStatus(Status.DONE);
        manager.updateSubTask(sub1Done);
        assertEquals(Status.IN_PROGRESS, manager.getEpicTaskByIdOrNull(epicId).getStatus());
        assertEquals(Status.DONE, manager.getSubTaskByIdOrNull(sub1Id).getStatus());
        assertEquals(Status.NEW, manager.getSubTaskByIdOrNull(sub2Id).getStatus());
    }

    //   Подзадачи со статусом IN_PROGRESS.
    @Test
    public void subsInProgress() {
        SubTask sub1Pr = manager.getSubTaskByIdOrNull(sub1Id);
        SubTask sub2Pr = manager.getSubTaskByIdOrNull(sub2Id);
        sub1Pr.setStatus(Status.IN_PROGRESS);
        sub2Pr.setStatus(Status.IN_PROGRESS);
        manager.updateSubTask(sub1Pr);
        manager.updateSubTask(sub2Pr);
        assertEquals(Status.IN_PROGRESS, manager.getEpicTaskByIdOrNull(epicId).getStatus());
        assertEquals(Status.IN_PROGRESS, manager.getSubTaskByIdOrNull(sub1Id).getStatus());
        assertEquals(Status.IN_PROGRESS, manager.getSubTaskByIdOrNull(sub2Id).getStatus());
    }

}