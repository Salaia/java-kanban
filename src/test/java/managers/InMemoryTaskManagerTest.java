package managers;

import org.junit.jupiter.api.BeforeEach;
import tasks.EpicTask;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {

    @BeforeEach
    public void setUp() {
        taskManager = Managers.getDefault();
        initTasks();
    }

    @Test
    void createSimpleTask() {
        Long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        assertEquals("SimpleTaskName", taskManager.getSimpleTaskByIdOrNull(taskID).getName());
        assertEquals("SimpleTaskDescription", taskManager.getSimpleTaskByIdOrNull(taskID).getDescription());
        assertEquals(Status.NEW, taskManager.getSimpleTaskByIdOrNull(taskID).getStatus());
    }

    @Test
    void createEpicAndSubtask() {
        Long epicID = taskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        assertEquals("EpicName", taskManager.getEpicTaskByIdOrNull(epicID).getName());
        assertEquals("EpicDescription", taskManager.getEpicTaskByIdOrNull(epicID).getDescription());
        Long subTaskID = taskManager.recordSubTask(new SubTask("SubTaskName", "SubTaskDescription", epicID));
        assertEquals("SubTaskName", taskManager.getSubTaskByIdOrNull(subTaskID).getName());
        assertEquals("SubTaskDescription", taskManager.getSubTaskByIdOrNull(subTaskID).getDescription());
    }

    @Test
    void updateSimpleTask() {
        Long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        Task simpleTaskForUpdate = taskManager.getSimpleTaskByIdOrNull(taskID);
        simpleTaskForUpdate.setStatus(Status.IN_PROGRESS);
        simpleTaskForUpdate.setName("Task1 name1");
        simpleTaskForUpdate.setDescription("Some description Task1");
        taskManager.updateSimpleTask(simpleTaskForUpdate);
        assertEquals("Task1 name1", taskManager.getSimpleTaskByIdOrNull(taskID).getName());
        assertEquals("Some description Task1", taskManager.getSimpleTaskByIdOrNull(taskID).getDescription());
        assertEquals(Status.IN_PROGRESS, taskManager.getSimpleTaskByIdOrNull(taskID).getStatus());
    }

    @Test
    void updateEpicTask() {
        Long epicID = taskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        EpicTask epicTaskForUpdate = taskManager.getEpicTaskByIdOrNull(epicID);
        epicTaskForUpdate.setName("NewName");
        epicTaskForUpdate.setDescription("NewDescription");
        taskManager.updateEpicTask(epicTaskForUpdate);
        assertEquals("NewName", taskManager.getEpicTaskByIdOrNull(epicID).getName());
        assertEquals("NewDescription", taskManager.getEpicTaskByIdOrNull(epicID).getDescription());
        assertEquals(Status.NEW, taskManager.getEpicTaskByIdOrNull(epicID).getStatus());
    }

    @Test
    void updateSubTaskProgress() {
        Long epicID = taskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        Long subTaskID = taskManager.recordSubTask(new SubTask("SubTaskName", "SubTaskDescription", epicID));

        SubTask subTaskForUpdate = taskManager.getSubTaskByIdOrNull(subTaskID);
        subTaskForUpdate.setName("NewName");
        subTaskForUpdate.setDescription("NewDescription");
        subTaskForUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTaskForUpdate);

        var subtask = taskManager.getSubTaskByIdOrNull(subTaskID);
        assertEquals("NewName", subtask.getName());
        assertEquals("NewDescription", subtask.getDescription());
        assertEquals(Status.IN_PROGRESS, subtask.getStatus());
        assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIdOrNull(epicID).getStatus());
    }

    @Test
    void updateSubTaskDone() {
        Long epicID = taskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        Long subTaskID = taskManager.recordSubTask(new SubTask("SubTaskName", "SubTaskDescription", epicID));
        SubTask subTaskForUpdate = taskManager.getSubTaskByIdOrNull(subTaskID);
        subTaskForUpdate.setName("NewName");
        subTaskForUpdate.setDescription("NewDescription");
        subTaskForUpdate.setStatus(Status.DONE);
        taskManager.updateSubTask(subTaskForUpdate);
        var subtask = taskManager.getSubTaskByIdOrNull(subTaskID);
        assertEquals("NewName", subtask.getName());
        assertEquals("NewDescription", subtask.getDescription());
        assertEquals(Status.DONE, subtask.getStatus());
        assertEquals(Status.DONE, taskManager.getEpicTaskByIdOrNull(epicID).getStatus());
    }

    @Test
    void deleteAllSimpleTasks() {
        assertEquals(2, taskManager.getSimpleTasks().size());
        taskManager.deleteAllSimpleTasks();
        assertEquals(0, taskManager.getSimpleTasks().size());
    }

    @Test
    void deleteAllEpicTasks() {
        assertEquals(2, taskManager.getEpicTasks().size());
        assertEquals(7, taskManager.getSubTasks().size());

        taskManager.deleteAllEpicTasks();
        assertEquals(0, taskManager.getEpicTasks().size());
        assertEquals(0, taskManager.getSubTasks().size());
    }

    @Test
    void deleteAllSubTasks() {
        assertEquals(2, taskManager.getEpicTasks().size());
        assertEquals(7, taskManager.getSubTasks().size());

        taskManager.deleteAllSubTasks();
        assertEquals(2, taskManager.getEpicTasks().size());
        assertEquals(0, taskManager.getSubTasks().size());
        for (EpicTask epicTask : taskManager.getEpicTasks()) {
            assertEquals(Status.NEW, epicTask.getStatus());
        }
    }

    @Test
    void deleteSimpleTaskByID() {
        assertEquals(2, taskManager.getSimpleTasks().size());
        taskManager.deleteSimpleTask(task1ID);
        assertEquals(1, taskManager.getSimpleTasks().size());
        assertNull(taskManager.getSimpleTaskByIdOrNull(task1ID));
        assertNotNull(taskManager.getSimpleTaskByIdOrNull(task2ID));
    }

    @Test
    void deleteEpicTaskByID() {
        taskManager.deleteEpicTask(epic2ID);
        assertEquals(1, taskManager.getEpicTasks().size());
        assertEquals(4, taskManager.getSubTasks().size());

        assertNull(taskManager.getSubTaskByIdOrNull(subTask1InEpic2)); // Subtask этого эпика действительно удалена
        assertNotNull(taskManager.getSubTaskByIdOrNull(subTask1InEpic1)); // чужая сабтаска была не тронута
        assertNull(taskManager.getEpicTaskByIdOrNull(epic2ID)); // epic2 deleted
        assertNotNull(taskManager.getEpicTaskByIdOrNull(epic1ID)); // epic1 still here
    }

    // В эпике 2 задачи, NEW и IN_PROGRESS, удаляем IN_PROGRESS, эпик должен стать NEW
    @Test
    void deleteSubTaskProgressToNew() {
        SubTask subTask1InEpic2ForUpdate = taskManager.getSubTaskByIdOrNull(subTask1InEpic2);
        subTask1InEpic2ForUpdate.setName("SubTask1");
        subTask1InEpic2ForUpdate.setDescription("description");
        subTask1InEpic2ForUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTask1InEpic2ForUpdate);

        // другой эпик с 1 подзадачей, DONE
        SubTask subTask2InEpic2ForUpdate = taskManager.getSubTaskByIdOrNull(subTask2InEpic2);
        subTask2InEpic2ForUpdate.setName("SubTask1");
        subTask2InEpic2ForUpdate.setDescription("description");
        subTask2InEpic2ForUpdate.setStatus(Status.DONE);

        taskManager.deleteSubTask(subTask1InEpic2);
        assertEquals(Status.NEW, taskManager.getEpicTaskByIdOrNull(epic1ID).getStatus());

        // 2 IN_PROGRESS, один удалить, эпик остается IN_PROGRESS
        SubTask subTask4InEpic1ForUpdate = taskManager.getSubTaskByIdOrNull(subTask4InEpic1);
        subTask4InEpic1ForUpdate.setName("SubTask4");
        subTask4InEpic1ForUpdate.setDescription("description");
        subTask4InEpic1ForUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTask4InEpic1ForUpdate);

        SubTask subTask2InEpic1ForUpdate = taskManager.getSubTaskByIdOrNull(subTask2InEpic1);
        subTask2InEpic1ForUpdate.setName("SubTask2");
        subTask2InEpic1ForUpdate.setDescription("description");
        subTask2InEpic1ForUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTask2InEpic1ForUpdate);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIdOrNull(epic1ID).getStatus());
        taskManager.deleteSubTask(subTask4InEpic1);
        assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIdOrNull(epic1ID).getStatus());

        // Проверить, что ID сабтаски удален из списка в её эпике
        ArrayList<Long> subTasksOfEpic1 = taskManager.getEpicTaskByIdOrNull(epic1ID).getSubTasksIds();
        assertFalse(subTasksOfEpic1.contains(subTask4InEpic1));
    }

    // 2 IN_PROGRESS, один удалить, эпик остается IN_PROGRESS
    @Test
    void deleteSubTaskProgressStaysProgress() {
        // один эпик с 2 подзадачами, IN_PROGRESS
        SubTask subTask1InEpic1ForUpdate = taskManager.getSubTaskByIdOrNull(subTask1InEpic1);
        subTask1InEpic1ForUpdate.setName("SubTask1");
        subTask1InEpic1ForUpdate.setDescription("description");
        subTask1InEpic1ForUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTask1InEpic1ForUpdate);

        // другой эпик с 1 подзадачей, DONE
        SubTask subTask1InEpic2ForUpdate = taskManager.getSubTaskByIdOrNull(subTask1InEpic2);
        subTask1InEpic2ForUpdate.setName("SubTask1");
        subTask1InEpic2ForUpdate.setDescription("description");
        subTask1InEpic2ForUpdate.setStatus(Status.DONE);

        SubTask subTask4InEpic1ForUpdate = taskManager.getSubTaskByIdOrNull(subTask4InEpic1);
        subTask4InEpic1ForUpdate.setName("SubTask4");
        subTask4InEpic1ForUpdate.setDescription("description");
        subTask4InEpic1ForUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTask4InEpic1ForUpdate);

        SubTask subTask2InEpic1ForUpdate = taskManager.getSubTaskByIdOrNull(subTask2InEpic1);
        subTask2InEpic1ForUpdate.setName("SubTask2");
        subTask2InEpic1ForUpdate.setDescription("description");
        subTask2InEpic1ForUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTask2InEpic1ForUpdate);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIdOrNull(epic1ID).getStatus());
        taskManager.deleteSubTask(subTask2InEpic1);
        assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIdOrNull(epic1ID).getStatus());

        //проверка на отсутствие повторов:
        Set<Task> historySet = new HashSet<>(taskManager.getHistory());
        assertEquals(historySet.size(), taskManager.getHistory().size());
    }
}