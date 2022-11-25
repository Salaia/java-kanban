import Tasks.EpicTask;
import Tasks.Status;
import Tasks.SubTask;
import Tasks.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
    Тестов - их много. Хочется как-то организовать, чтобы при необходимости поменять что-то в тесте их было проще искать
    Как лучше это сделать?
 */

class TaskManagerTest {
    TaskManager taskManager = new TaskManager();

    // создание объектов классов

    @Test
    void createSimpleTask() {
        long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        Assertions.assertEquals("SimpleTaskName", taskManager.getSimpleTaskByIDorNull(taskID).getName());
        Assertions.assertEquals("SimpleTaskDescription", taskManager.getSimpleTaskByIDorNull(taskID).getDescription());
        Assertions.assertEquals(Status.NEW, taskManager.getSimpleTaskByIDorNull(taskID).getStatus());
    }

    @Test
    void createEpicAndSubtask() {
        long epicID = taskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        Assertions.assertEquals("EpicName", taskManager.getEpicTaskByIDorNull(epicID).getName());
        Assertions.assertEquals("EpicDescription", taskManager.getEpicTaskByIDorNull(epicID).getDescription());
        long subTaskID = taskManager.recordSubTask(new SubTask("SubTaskName", "SubTaskDescription", epicID));
        Assertions.assertEquals("SubTaskName", taskManager.getSubTaskByIDorNull(subTaskID).getName());
        Assertions.assertEquals("SubTaskDescription", taskManager.getSubTaskByIDorNull(subTaskID).getDescription());
    }

    @Test
    void updateSimpleTask() {
        long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        taskManager.updateSimpleTask(new Task("Task1 name1", "Some description Task1"), taskID, Status.IN_PROGRESS);
        Assertions.assertEquals("Task1 name1", taskManager.getSimpleTaskByIDorNull(taskID).getName());
        Assertions.assertEquals("Some description Task1", taskManager.getSimpleTaskByIDorNull(taskID).getDescription());
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getSimpleTaskByIDorNull(taskID).getStatus());
    }

    @Test
    void updateEpicTask() {
        long epicID = taskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        taskManager.updateEpicTask(new EpicTask("NewName", "NewDescription"), epicID);
        Assertions.assertEquals("NewName", taskManager.getEpicTaskByIDorNull(epicID).getName());
        Assertions.assertEquals("NewDescription", taskManager.getEpicTaskByIDorNull(epicID).getDescription());
        Assertions.assertEquals(Status.NEW, taskManager.getEpicTaskByIDorNull(epicID).getStatus());
    }

    @Test
    void updateSubTaskProgress() {
        long epicID = taskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        long subTaskID = taskManager.recordSubTask(new SubTask("SubTaskName", "SubTaskDescription", epicID));
        taskManager.updateSubTask(new SubTask("NewName", "NewDescription", epicID), subTaskID, Status.IN_PROGRESS);
        var subtask = taskManager.getSubTaskByIDorNull(subTaskID);
        Assertions.assertEquals("NewName", subtask.getName());
        Assertions.assertEquals("NewDescription", subtask.getDescription());
        Assertions.assertEquals(Status.IN_PROGRESS, subtask.getStatus());
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIDorNull(epicID).getStatus());
    }

    @Test
    void updateSubTaskDone() {
        long epicID = taskManager.recordEpicTask(new EpicTask("EpicName", "EpicDescription"));
        long subTaskID = taskManager.recordSubTask(new SubTask("SubTaskName", "SubTaskDescription", epicID));
        taskManager.updateSubTask(new SubTask("NewName", "NewDescription", epicID), subTaskID, Status.DONE);
        var subtask = taskManager.getSubTaskByIDorNull(subTaskID);
        Assertions.assertEquals("NewName", subtask.getName());
        Assertions.assertEquals("NewDescription", subtask.getDescription());
        Assertions.assertEquals(Status.DONE, subtask.getStatus());
        Assertions.assertEquals(Status.DONE, taskManager.getEpicTaskByIDorNull(epicID).getStatus());
    }

    @Test
    void deleteAllSimpleTasks() {
        long task1ID = taskManager.recordSimpleTask(new Task("Task1 name1", "Some description Task1"));
        long task2ID = taskManager.recordSimpleTask(new Task("Task2 name2", "Some description Task2"));
        Assertions.assertEquals(2, taskManager.getSimpleTasks().size());

        taskManager.deleteAllSimpleTasks();
        Assertions.assertEquals(0, taskManager.getSimpleTasks().size());
    }

    @Test
    void deleteAllEpicTasks() {
        // один эпик с 2 подзадачами
        long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        // другой эпик с 1 подзадачей
        long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        Assertions.assertEquals(2, taskManager.getEpicTasks().size());
        Assertions.assertEquals(3, taskManager.getSubTasks().size());

        taskManager.deleteAllEpicTasks();
        Assertions.assertEquals(0, taskManager.getEpicTasks().size());
        Assertions.assertEquals(0, taskManager.getSubTasks().size());
    }

    @Test
    void deleteAllSubTasks() {
        long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        taskManager.updateSubTask(new SubTask("SubTask1", "description", epic1ID), subTask1InEpic1, Status.IN_PROGRESS);
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIDorNull(epic1ID).getStatus());
        // другой эпик с 1 подзадачей
        long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        Assertions.assertEquals(2, taskManager.getEpicTasks().size());
        Assertions.assertEquals(3, taskManager.getSubTasks().size());

        taskManager.deleteAllSubTasks();
        Assertions.assertEquals(2, taskManager.getEpicTasks().size());
        Assertions.assertEquals(0, taskManager.getSubTasks().size());
        for (EpicTask epicTask : taskManager.getEpicTasks().values()) {
            Assertions.assertEquals(Status.NEW, epicTask.getStatus());
        }
    }

    @Test
    void deleteSimpleTaskByID() {
        long taskID = taskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
        long task1ID = taskManager.recordSimpleTask(new Task("Task1 name1", "Some description Task1"));
        long task2ID = taskManager.recordSimpleTask(new Task("Task2 name2", "Some description Task2"));
        Assertions.assertEquals(3, taskManager.getSimpleTasks().size());
        taskManager.deleteSimpleTask(task1ID);
        Assertions.assertEquals(2, taskManager.getSimpleTasks().size());
        Assertions.assertNull(taskManager.getSimpleTaskByIDorNull(task1ID));
        Assertions.assertNotNull(taskManager.getSimpleTaskByIDorNull(task2ID));
    }

    @Test
    void deleteEpicTaskByID() {
        // один эпик с 2 подзадачами
        long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        // другой эпик с 1 подзадачей
        long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));

        taskManager.deleteEpicTask(epic2ID);
        Assertions.assertEquals(1, taskManager.getEpicTasks().size()); // проверять длину мапы -
        Assertions.assertEquals(2, taskManager.getSubTasks().size()); // моя изначальная идея

        // от Виталия
        Assertions.assertNull(taskManager.getSubTaskByIDorNull(subTask1InEpic2)); // Subtask этого эпика действительно удалена
        Assertions.assertNotNull(taskManager.getSubTaskByIDorNull(subTask1InEpic1)); // чужая сабтаска была не тронута
        Assertions.assertNull(taskManager.getEpicTaskByIDorNull(epic2ID)); // epic2 deleted
        Assertions.assertNotNull(taskManager.getEpicTaskByIDorNull(epic1ID)); // epic1 still here
    }

    // В эпике 2 задачи, NEW и IN_PROGRESS, удаляем IN_PROGRESS, эпик должен стать NEW
    @Test
    void deleteSubTaskProgressToNew() {
        // один эпик с 2 подзадачами, IN_PROGRESS
        long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        taskManager.updateSubTask(new SubTask("SubTask1", "description", epic1ID), subTask1InEpic1, Status.IN_PROGRESS);

        // другой эпик с 1 подзадачей, DONE
        long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        taskManager.updateSubTask(new SubTask("SubTask1", "description", epic2ID), subTask1InEpic2, Status.DONE);

        taskManager.deleteSubTask(subTask1InEpic1);
        Assertions.assertEquals(Status.NEW, taskManager.getEpicTaskByIDorNull(epic1ID).getStatus());

        // 2 IN_PROGRESS, один удалить, эпик остается IN_PROGRESS
        long subTask3InEpic1 = taskManager.recordSubTask(new SubTask("SubTask3 in epic1", "some description", epic1ID));
        long subTask4InEpic1 = taskManager.recordSubTask(new SubTask("SubTask4 in epic1", "some description", epic1ID));
        taskManager.updateSubTask(new SubTask("SubTask4", "description", epic1ID), subTask4InEpic1, Status.IN_PROGRESS);
        taskManager.updateSubTask(new SubTask("SubTask2", "description", epic1ID), subTask2InEpic1, Status.IN_PROGRESS);
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIDorNull(epic1ID).getStatus());
        taskManager.deleteSubTask(subTask4InEpic1);
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIDorNull(epic1ID).getStatus());
    }

    // 2 IN_PROGRESS, один удалить, эпик остается IN_PROGRESS
    @Test
    void deleteSubTaskProgressStaysProgress() {
        // один эпик с 2 подзадачами, IN_PROGRESS
        long epic1ID = taskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
        long subTask1InEpic1 = taskManager.recordSubTask(new SubTask("SubTask1 in epic1", "some description", epic1ID));
        long subTask2InEpic1 = taskManager.recordSubTask(new SubTask("SubTask2 in epic1", "some description", epic1ID));
        taskManager.updateSubTask(new SubTask("SubTask1", "description", epic1ID), subTask1InEpic1, Status.IN_PROGRESS);

        // другой эпик с 1 подзадачей, DONE
        long epic2ID = taskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
        long subTask1InEpic2 = taskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
        taskManager.updateSubTask(new SubTask("SubTask1", "description", epic2ID), subTask1InEpic2, Status.DONE);

        long subTask3InEpic1 = taskManager.recordSubTask(new SubTask("SubTask3 in epic1", "some description", epic1ID));
        long subTask4InEpic1 = taskManager.recordSubTask(new SubTask("SubTask4 in epic1", "some description", epic1ID));
        taskManager.updateSubTask(new SubTask("SubTask4", "description", epic1ID), subTask4InEpic1, Status.IN_PROGRESS);
        taskManager.updateSubTask(new SubTask("SubTask2", "description", epic1ID), subTask2InEpic1, Status.IN_PROGRESS);
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIDorNull(epic1ID).getStatus());
        taskManager.deleteSubTask(subTask4InEpic1);
        Assertions.assertEquals(Status.IN_PROGRESS, taskManager.getEpicTaskByIDorNull(epic1ID).getStatus());
    }
}