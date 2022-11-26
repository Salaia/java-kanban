package managers;

import managers.InMemoryTaskManager;
import tasks.EpicTask;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
    Тестов - их много. Хочется как-то организовать, чтобы при необходимости поменять что-то в тесте их было проще искать
    Как лучше это сделать?
 */

class InMemoryTaskManagerTest {
    InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();

    // создание объектов классов

    @Test
    void createSimpleTask() {
        long taskID = inMemoryTaskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        Assertions.assertEquals("SimpleTaskName", inMemoryTaskManager.getSimpleTaskByIDorNull(taskID).getName());
        Assertions.assertEquals("SimpleTaskDescription", inMemoryTaskManager.getSimpleTaskByIDorNull(taskID).getDescription());
        Assertions.assertEquals(Status.NEW, inMemoryTaskManager.getSimpleTaskByIDorNull(taskID).getStatus());
    }

    @Test
    void createEpicAndSubtask() {
        long epicID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        Assertions.assertEquals("EpicName", inMemoryTaskManager.getEpicTaskByIDorNull(epicID).getName());
        Assertions.assertEquals("EpicDescription", inMemoryTaskManager.getEpicTaskByIDorNull(epicID).getDescription());
        long subTaskID = inMemoryTaskManager.recordSubTask(new SubTask("SubTaskName", "SubTaskDescription", epicID));
        Assertions.assertEquals("SubTaskName", inMemoryTaskManager.getSubTaskByIDorNull(subTaskID).getName());
        Assertions.assertEquals("SubTaskDescription", inMemoryTaskManager.getSubTaskByIDorNull(subTaskID).getDescription());
    }

    @Test
    void updateSimpleTask() {
        long taskID = inMemoryTaskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        inMemoryTaskManager.updateSimpleTask(new Task("Task1 name1", "Some description Task1"), taskID, Status.IN_PROGRESS);
        Assertions.assertEquals("Task1 name1", inMemoryTaskManager.getSimpleTaskByIDorNull(taskID).getName());
        Assertions.assertEquals("Some description Task1", inMemoryTaskManager.getSimpleTaskByIDorNull(taskID).getDescription());
        Assertions.assertEquals(Status.IN_PROGRESS, inMemoryTaskManager.getSimpleTaskByIDorNull(taskID).getStatus());
    }

    @Test
    void updateEpicTask() {
        long epicID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        inMemoryTaskManager.updateEpicTask(new EpicTask("NewName", "NewDescription"), epicID);
        Assertions.assertEquals("NewName", inMemoryTaskManager.getEpicTaskByIDorNull(epicID).getName());
        Assertions.assertEquals("NewDescription", inMemoryTaskManager.getEpicTaskByIDorNull(epicID).getDescription());
        Assertions.assertEquals(Status.NEW, inMemoryTaskManager.getEpicTaskByIDorNull(epicID).getStatus());
    }

    @Test
    void updateSubTaskProgress() {
        long epicID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        long subTaskID = inMemoryTaskManager.recordSubTask(new SubTask("SubTaskName", "SubTaskDescription", epicID));
        inMemoryTaskManager.updateSubTask(new SubTask("NewName", "NewDescription", epicID), subTaskID, Status.IN_PROGRESS);
        var subtask = inMemoryTaskManager.getSubTaskByIDorNull(subTaskID);
        Assertions.assertEquals("NewName", subtask.getName());
        Assertions.assertEquals("NewDescription", subtask.getDescription());
        Assertions.assertEquals(Status.IN_PROGRESS, subtask.getStatus());
        Assertions.assertEquals(Status.IN_PROGRESS, inMemoryTaskManager.getEpicTaskByIDorNull(epicID).getStatus());
    }

    @Test
    void updateSubTaskDone() {
        long epicID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        long subTaskID = inMemoryTaskManager.recordSubTask(new SubTask("SubTaskName", "SubTaskDescription", epicID));
        inMemoryTaskManager.updateSubTask(new SubTask("NewName", "NewDescription", epicID), subTaskID, Status.DONE);
        var subtask = inMemoryTaskManager.getSubTaskByIDorNull(subTaskID);
        Assertions.assertEquals("NewName", subtask.getName());
        Assertions.assertEquals("NewDescription", subtask.getDescription());
        Assertions.assertEquals(Status.DONE, subtask.getStatus());
        Assertions.assertEquals(Status.DONE, inMemoryTaskManager.getEpicTaskByIDorNull(epicID).getStatus());
    }

    @Test
    void deleteAllSimpleTasks() {
        long task1ID = inMemoryTaskManager.recordSimpleTask(new Task("Task1 name1", "Some description Task1"));
        long task2ID = inMemoryTaskManager.recordSimpleTask(new Task("Task2 name2", "Some description Task2"));
        Assertions.assertEquals(2, inMemoryTaskManager.getSimpleTasks().size());

        inMemoryTaskManager.deleteAllSimpleTasks();
        Assertions.assertEquals(0, inMemoryTaskManager.getSimpleTasks().size());
    }

    @Test
    void deleteAllEpicTasks() {
        // один эпик с 2 подзадачами
        long epic1ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        long subTask1InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        long subTask2InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        // другой эпик с 1 подзадачей
        long epic2ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        long subTask1InEpic2 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        Assertions.assertEquals(2, inMemoryTaskManager.getEpicTasks().size());
        Assertions.assertEquals(3, inMemoryTaskManager.getSubTasks().size());

        inMemoryTaskManager.deleteAllEpicTasks();
        Assertions.assertEquals(0, inMemoryTaskManager.getEpicTasks().size());
        Assertions.assertEquals(0, inMemoryTaskManager.getSubTasks().size());
    }

    @Test
    void deleteAllSubTasks() {
        long epic1ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        long subTask1InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        long subTask2InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        inMemoryTaskManager.updateSubTask(new SubTask("SubTask1", "description", epic1ID), subTask1InEpic1, Status.IN_PROGRESS);
        Assertions.assertEquals(Status.IN_PROGRESS, inMemoryTaskManager.getEpicTaskByIDorNull(epic1ID).getStatus());
        // другой эпик с 1 подзадачей
        long epic2ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        long subTask1InEpic2 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        Assertions.assertEquals(2, inMemoryTaskManager.getEpicTasks().size());
        Assertions.assertEquals(3, inMemoryTaskManager.getSubTasks().size());

        inMemoryTaskManager.deleteAllSubTasks();
        Assertions.assertEquals(2, inMemoryTaskManager.getEpicTasks().size());
        Assertions.assertEquals(0, inMemoryTaskManager.getSubTasks().size());
        for (EpicTask epicTask : inMemoryTaskManager.getEpicTasks().values()) {
            Assertions.assertEquals(Status.NEW, epicTask.getStatus());
        }
    }

    @Test
    void deleteSimpleTaskByID() {
        long taskID = inMemoryTaskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        long task1ID = inMemoryTaskManager.recordSimpleTask(new Task("Task1 name1", "Some description Task1"));
        long task2ID = inMemoryTaskManager.recordSimpleTask(new Task("Task2 name2", "Some description Task2"));
        Assertions.assertEquals(3, inMemoryTaskManager.getSimpleTasks().size());
        inMemoryTaskManager.deleteSimpleTask(task1ID);
        Assertions.assertEquals(2, inMemoryTaskManager.getSimpleTasks().size());
        Assertions.assertNull(inMemoryTaskManager.getSimpleTaskByIDorNull(task1ID));
        Assertions.assertNotNull(inMemoryTaskManager.getSimpleTaskByIDorNull(task2ID));
    }

    @Test
    void deleteEpicTaskByID() {
        // один эпик с 2 подзадачами
        long epic1ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        long subTask1InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        long subTask2InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        // другой эпик с 1 подзадачей
        long epic2ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        long subTask1InEpic2 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));

        inMemoryTaskManager.deleteEpicTask(epic2ID);
        Assertions.assertEquals(1, inMemoryTaskManager.getEpicTasks().size()); // проверять длину мапы -
        Assertions.assertEquals(2, inMemoryTaskManager.getSubTasks().size()); // моя изначальная идея

        // от Виталия
        Assertions.assertNull(inMemoryTaskManager.getSubTaskByIDorNull(subTask1InEpic2)); // Subtask этого эпика действительно удалена
        Assertions.assertNotNull(inMemoryTaskManager.getSubTaskByIDorNull(subTask1InEpic1)); // чужая сабтаска была не тронута
        Assertions.assertNull(inMemoryTaskManager.getEpicTaskByIDorNull(epic2ID)); // epic2 deleted
        Assertions.assertNotNull(inMemoryTaskManager.getEpicTaskByIDorNull(epic1ID)); // epic1 still here
    }

    // В эпике 2 задачи, NEW и IN_PROGRESS, удаляем IN_PROGRESS, эпик должен стать NEW
    @Test
    void deleteSubTaskProgressToNew() {
        // один эпик с 2 подзадачами, IN_PROGRESS
        long epic1ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        long subTask1InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        long subTask2InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        inMemoryTaskManager.updateSubTask(new SubTask("SubTask1", "description", epic1ID), subTask1InEpic1, Status.IN_PROGRESS);

        // другой эпик с 1 подзадачей, DONE
        long epic2ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        long subTask1InEpic2 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        inMemoryTaskManager.updateSubTask(new SubTask("SubTask1", "description", epic2ID), subTask1InEpic2, Status.DONE);

        inMemoryTaskManager.deleteSubTask(subTask1InEpic1);
        Assertions.assertEquals(Status.NEW, inMemoryTaskManager.getEpicTaskByIDorNull(epic1ID).getStatus());

        // 2 IN_PROGRESS, один удалить, эпик остается IN_PROGRESS
        long subTask3InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask3 in epic1", "some description", epic1ID));
        long subTask4InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask4 in epic1", "some description", epic1ID));
        inMemoryTaskManager.updateSubTask(new SubTask("SubTask4", "description", epic1ID), subTask4InEpic1, Status.IN_PROGRESS);
        inMemoryTaskManager.updateSubTask(new SubTask("SubTask2", "description", epic1ID), subTask2InEpic1, Status.IN_PROGRESS);
        Assertions.assertEquals(Status.IN_PROGRESS, inMemoryTaskManager.getEpicTaskByIDorNull(epic1ID).getStatus());
        inMemoryTaskManager.deleteSubTask(subTask4InEpic1);
        Assertions.assertEquals(Status.IN_PROGRESS, inMemoryTaskManager.getEpicTaskByIDorNull(epic1ID).getStatus());
    }

    // 2 IN_PROGRESS, один удалить, эпик остается IN_PROGRESS
    @Test
    void deleteSubTaskProgressStaysProgress() {
        // один эпик с 2 подзадачами, IN_PROGRESS
        long epic1ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        long subTask1InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        long subTask2InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        inMemoryTaskManager.updateSubTask(new SubTask("SubTask1", "description", epic1ID), subTask1InEpic1, Status.IN_PROGRESS);

        // другой эпик с 1 подзадачей, DONE
        long epic2ID = inMemoryTaskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        long subTask1InEpic2 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        inMemoryTaskManager.updateSubTask(new SubTask("SubTask1", "description", epic2ID), subTask1InEpic2, Status.DONE);

        long subTask3InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask3 in epic1", "some description", epic1ID));
        long subTask4InEpic1 = inMemoryTaskManager.recordSubTask(new SubTask("SubTask4 in epic1", "some description", epic1ID));
        inMemoryTaskManager.updateSubTask(new SubTask("SubTask4", "description", epic1ID), subTask4InEpic1, Status.IN_PROGRESS);
        inMemoryTaskManager.updateSubTask(new SubTask("SubTask2", "description", epic1ID), subTask2InEpic1, Status.IN_PROGRESS);
        Assertions.assertEquals(Status.IN_PROGRESS, inMemoryTaskManager.getEpicTaskByIDorNull(epic1ID).getStatus());
        inMemoryTaskManager.deleteSubTask(subTask4InEpic1);
        Assertions.assertEquals(Status.IN_PROGRESS, inMemoryTaskManager.getEpicTaskByIDorNull(epic1ID).getStatus());
    }
}