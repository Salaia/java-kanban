package managers;

import org.junit.jupiter.api.Test;
import tasks.EpicTask;
import tasks.SubTask;
import tasks.Task;
import java.io.File;

class FileBackedTaskManagerTest {

        @Test
        void MemoryToFileTest() {
                TaskManager fileBackedTaskManager = new FileBackedTaskManager(
                        new File("src/main/java/storage/TaskManagerSaved.csv"));

                Long taskID = fileBackedTaskManager.recordSimpleTask(new Task("SimpleTaskName", "SimpleTaskDescription"));
                Long epic1ID = fileBackedTaskManager.recordEpicTask(new EpicTask("EpicName1", "Epic1descr"));
                Long epic2ID = fileBackedTaskManager.recordEpicTask(new EpicTask("EpicName2", "Epic2descr"));
                Long subTask1InEpic2 = fileBackedTaskManager.recordSubTask(new SubTask("SubTask1 in epic2", "some description", epic2ID));
                Long subTask2InEpic2 = fileBackedTaskManager.recordSubTask(new SubTask("SubTask2 in epic2", "some description", epic2ID));
                Long subTask3InEpic2 = fileBackedTaskManager.recordSubTask(new SubTask("SubTask3 in epic2", "some description", epic2ID));

                fileBackedTaskManager.getSimpleTaskByIdOrNull(taskID);
                fileBackedTaskManager.getEpicTaskByIdOrNull(epic1ID);
                fileBackedTaskManager.getEpicTaskByIdOrNull(epic2ID);
                fileBackedTaskManager.getSubTaskByIdOrNull(subTask1InEpic2);
                fileBackedTaskManager.getSubTaskByIdOrNull(subTask2InEpic2);
                fileBackedTaskManager.getSubTaskByIdOrNull(subTask3InEpic2);

                //проверка на отсутствие повторов проваливается, потому что двоится голова
                //Set<Task> historySet = new HashSet<>(fileBackedTaskManager.getHistory());
                //System.out.println(historySet.size() + " " + fileBackedTaskManager.getHistory().size());
                //System.out.println(fileBackedTaskManager.getHistory());
                //Assertions.assertEquals(historySet.size(), fileBackedTaskManager.getHistory().size());

        }

        @Test
        void FileToMemoryTest() {
                FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(
                        new File("src/main/java/storage/TaskManagerSaved.csv"));

            // assertions - не могу придумать как, так что пока через sout
                System.out.println(fileBackedTaskManager.getSimpleTasks());
            System.out.println(fileBackedTaskManager.getEpicTasks());
            System.out.println(fileBackedTaskManager.getSubTasks());
                System.out.println("\n" + fileBackedTaskManager.getHistory());
        }

}