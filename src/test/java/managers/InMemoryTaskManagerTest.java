package managers;

import tasks.EpicTask;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class InMemoryTaskManagerTest {
    TaskManager taskManager = Managers.getDefault();
    //static TaskManager taskManager = new FileBackedTaskManager(new File("src/main/java/storage/TaskManagerSaved.csv"));


    @Test
    void createSimpleTask() {
        Long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        Assertions.assertEquals("SimpleTaskName", taskManager.getSimpleTaskByIdOrNull(taskID).getName());
        Assertions.assertEquals("SimpleTaskDescription", taskManager.getSimpleTaskByIdOrNull(taskID).getDescription());
        Assertions.assertEquals(Status.NEW, taskManager.getSimpleTaskByIdOrNull(taskID).getStatus());
    }

    @Test
    void createEpicAndSubtask() {
        Long epicID = taskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        Assertions.assertEquals("EpicName", taskManager.getEpicTaskByIdOrNull(epicID).getName());
        Assertions.assertEquals("EpicDescription", taskManager.getEpicTaskByIdOrNull(epicID).getDescription());
        Long subTaskID = taskManager.recordSubTask(new SubTask("SubTaskName", "SubTaskDescription", epicID));
        Assertions.assertEquals("SubTaskName", taskManager.getSubTaskByIdOrNull(subTaskID).getName());
        Assertions.assertEquals("SubTaskDescription", taskManager.getSubTaskByIdOrNull(subTaskID).getDescription());
    }

    @Test
    void updateSimpleTask() {
        Long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        Task simpleTaskForUpdate = taskManager.getSimpleTaskByIdOrNull(taskID);
        simpleTaskForUpdate.setStatus(Status.IN_PROGRESS);
        simpleTaskForUpdate.setName("Task1 name1");
        simpleTaskForUpdate.setDescription("Some description Task1");
        taskManager.updateSimpleTask(simpleTaskForUpdate);
        Assertions.assertEquals("Task1 name1", taskManager.getSimpleTaskByIdOrNull(taskID).getName());
        Assertions.assertEquals("Some description Task1", taskManager.getSimpleTaskByIdOrNull(taskID).getDescription());
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getSimpleTaskByIdOrNull(taskID).getStatus());
    }

    @Test
    void updateEpicTask() {
        Long epicID = taskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        EpicTask epicTaskForUpdate = taskManager.getEpicTaskByIdOrNull(epicID);
        epicTaskForUpdate.setName("NewName");
        epicTaskForUpdate.setDescription("NewDescription");
        taskManager.updateEpicTask(epicTaskForUpdate);
        Assertions.assertEquals("NewName", taskManager.getEpicTaskByIdOrNull(epicID).getName());
        Assertions.assertEquals("NewDescription", taskManager.getEpicTaskByIdOrNull(epicID).getDescription());
        Assertions.assertEquals(Status.NEW, taskManager.getEpicTaskByIdOrNull(epicID).getStatus());
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
        Assertions.assertEquals("NewName", subtask.getName());
        Assertions.assertEquals("NewDescription", subtask.getDescription());
        Assertions.assertEquals(Status.IN_PROGRESS, subtask.getStatus());
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIdOrNull(epicID).getStatus());
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
        Assertions.assertEquals("NewName", subtask.getName());
        Assertions.assertEquals("NewDescription", subtask.getDescription());
        Assertions.assertEquals(Status.DONE, subtask.getStatus());
        Assertions.assertEquals(Status.DONE, taskManager.getEpicTaskByIdOrNull(epicID).getStatus());
    }

    @Test
    void deleteAllSimpleTasks() {
        Long task1ID = taskManager.recordSimpleTask(new Task("Task1 name1", "Some description Task1"));
        Long task2ID = taskManager.recordSimpleTask(new Task("Task2 name2", "Some description Task2"));
        Assertions.assertEquals(2, taskManager.getSimpleTasks().size());

        taskManager.deleteAllSimpleTasks();
        Assertions.assertEquals(0, taskManager.getSimpleTasks().size());
    }

    @Test
    void deleteAllEpicTasks() {
        // один эпик с 2 подзадачами
        Long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        Long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        Long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        // другой эпик с 1 подзадачей
        Long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        Long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        Assertions.assertEquals(2, taskManager.getEpicTasks().size());
        Assertions.assertEquals(3, taskManager.getSubTasks().size());

        taskManager.deleteAllEpicTasks();
        Assertions.assertEquals(0, taskManager.getEpicTasks().size());
        Assertions.assertEquals(0, taskManager.getSubTasks().size());
    }

    @Test
    void deleteAllSubTasks() {
        Long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        Long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        Long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        SubTask subTask1InEpic1ForUpdate = taskManager.getSubTaskByIdOrNull(subTask1InEpic1);
        subTask1InEpic1ForUpdate.setName("SubTask1");
        subTask1InEpic1ForUpdate.setDescription("description");
        subTask1InEpic1ForUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTask1InEpic1ForUpdate);
        //taskManager.updateSubTask(new SubTask("SubTask1", "description", epic1ID), subTask1InEpic1, Status.IN_PROGRESS);
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIdOrNull(epic1ID).getStatus());
        // другой эпик с 1 подзадачей
        Long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        Long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        Assertions.assertEquals(2, taskManager.getEpicTasks().size());
        Assertions.assertEquals(3, taskManager.getSubTasks().size());

        taskManager.deleteAllSubTasks();
        Assertions.assertEquals(2, taskManager.getEpicTasks().size());
        Assertions.assertEquals(0, taskManager.getSubTasks().size());
        for (EpicTask epicTask : taskManager.getEpicTasks()) {
            Assertions.assertEquals(Status.NEW, epicTask.getStatus());
        }
    }

    @Test
    void deleteSimpleTaskByID() {
        Long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        Long task1ID = taskManager.recordSimpleTask(new Task("Task1 name1", "Some description Task1"));
        Long task2ID = taskManager.recordSimpleTask(new Task("Task2 name2", "Some description Task2"));
        Assertions.assertEquals(3, taskManager.getSimpleTasks().size());
        taskManager.deleteSimpleTask(task1ID);
        Assertions.assertEquals(2, taskManager.getSimpleTasks().size());
        Assertions.assertNull(taskManager.getSimpleTaskByIdOrNull(task1ID));
        Assertions.assertNotNull(taskManager.getSimpleTaskByIdOrNull(task2ID));
    }

    @Test
    void deleteEpicTaskByID() {
        // один эпик с 2 подзадачами
        Long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        Long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        Long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        // другой эпик с 1 подзадачей
        Long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        Long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));

        taskManager.deleteEpicTask(epic2ID);
        Assertions.assertEquals(1, taskManager.getEpicTasks().size()); // проверять длину мапы -
        Assertions.assertEquals(2, taskManager.getSubTasks().size()); // моя изначальная идея

        // от Виталия
        Assertions.assertNull(taskManager.getSubTaskByIdOrNull(subTask1InEpic2)); // Subtask этого эпика действительно удалена
        Assertions.assertNotNull(taskManager.getSubTaskByIdOrNull(subTask1InEpic1)); // чужая сабтаска была не тронута
        Assertions.assertNull(taskManager.getEpicTaskByIdOrNull(epic2ID)); // epic2 deleted
        Assertions.assertNotNull(taskManager.getEpicTaskByIdOrNull(epic1ID)); // epic1 still here
    }

    // В эпике 2 задачи, NEW и IN_PROGRESS, удаляем IN_PROGRESS, эпик должен стать NEW
    @Test
    void deleteSubTaskProgressToNew() {
        // один эпик с 2 подзадачами, IN_PROGRESS
        Long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        Long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        Long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));

        SubTask subTask1InEpic1ForUpdate = taskManager.getSubTaskByIdOrNull(subTask1InEpic1);
        subTask1InEpic1ForUpdate.setName("SubTask1");
        subTask1InEpic1ForUpdate.setDescription("description");
        subTask1InEpic1ForUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTask1InEpic1ForUpdate);

        // другой эпик с 1 подзадачей, DONE
        Long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        Long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));

        SubTask subTask1InEpic2ForUpdate = taskManager.getSubTaskByIdOrNull(subTask1InEpic2);
        subTask1InEpic2ForUpdate.setName("SubTask1");
        subTask1InEpic2ForUpdate.setDescription("description");
        subTask1InEpic2ForUpdate.setStatus(Status.DONE);

        taskManager.deleteSubTask(subTask1InEpic1);
        Assertions.assertEquals(Status.NEW, taskManager.getEpicTaskByIdOrNull(epic1ID).getStatus());

        // 2 IN_PROGRESS, один удалить, эпик остается IN_PROGRESS
        Long subTask3InEpic1 = taskManager.recordSubTask(new SubTask("SubTask3 in epic1", "some description", epic1ID));
        Long subTask4InEpic1 = taskManager.recordSubTask(new SubTask("SubTask4 in epic1", "some description", epic1ID));

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

        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIdOrNull(epic1ID).getStatus());
        taskManager.deleteSubTask(subTask4InEpic1);
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIdOrNull(epic1ID).getStatus());

        // Проверить, что ID сабтаски удален из списка в её эпике
        ArrayList<Long> subTasksOfEpic1 = taskManager.getEpicTaskByIdOrNull(epic1ID).getSubTasksIds();
        Assertions.assertFalse(subTasksOfEpic1.contains(subTask4InEpic1));
    }

    // 2 IN_PROGRESS, один удалить, эпик остается IN_PROGRESS
    @Test
    void deleteSubTaskProgressStaysProgress() {
        // один эпик с 2 подзадачами, IN_PROGRESS
        Long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        Long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        Long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));

        SubTask subTask1InEpic1ForUpdate = taskManager.getSubTaskByIdOrNull(subTask1InEpic1);
        subTask1InEpic1ForUpdate.setName("SubTask1");
        subTask1InEpic1ForUpdate.setDescription("description");
        subTask1InEpic1ForUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(subTask1InEpic1ForUpdate);

        // другой эпик с 1 подзадачей, DONE
        Long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        Long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));

        SubTask subTask1InEpic2ForUpdate = taskManager.getSubTaskByIdOrNull(subTask1InEpic2);
        subTask1InEpic2ForUpdate.setName("SubTask1");
        subTask1InEpic2ForUpdate.setDescription("description");
        subTask1InEpic2ForUpdate.setStatus(Status.DONE);

        Long subTask3InEpic1 = taskManager.recordSubTask(new SubTask("SubTask3 in epic1", "some description", epic1ID));
        Long subTask4InEpic1 = taskManager.recordSubTask(new SubTask("SubTask4 in epic1", "some description", epic1ID));

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

        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIdOrNull(epic1ID).getStatus());
        taskManager.deleteSubTask(subTask4InEpic1);
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIdOrNull(epic1ID).getStatus());

        //проверка на отсутствие повторов:
        Set<Task> historySet = new HashSet<>(taskManager.getHistory());
        Assertions.assertEquals(historySet.size(), taskManager.getHistory().size());

    }
}