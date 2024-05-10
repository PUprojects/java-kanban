import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import service.Managers;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        Task simpleTask = taskManager.create(new Task("Задача номер 1", "Простая задача", TaskStatus.NEW));
        System.out.println("Создана задача: " + simpleTask);
        simpleTask = taskManager.create(new Task("Задача номер 2", "Ещё простая задача", TaskStatus.NEW));
        System.out.println("Создана задача: " + simpleTask);

        Epic epic = taskManager.createEpic(new Epic("Эпик номер 1", "Первый эпик"));
        System.out.println("Создан эпик: " + epic);

        SubTask sub1 = taskManager.createSubTask(new SubTask(taskManager.getEpic(epic.getId()), "Первая подзадача",
                "Описание первой подзадачи", TaskStatus.NEW));
        System.out.println("Создана подзадача: " + sub1);
        System.out.println("Состояние эпика: " + epic);

        SubTask sub2 = taskManager.createSubTask(new SubTask(taskManager.getEpic(epic.getId()), "Вторая подзадача",
                "Описание второй подзадачи", TaskStatus.NEW));
        System.out.println("Создана подзадача: " + sub2);
        System.out.println("Состояние эпика: " + epic);

        SubTask subNew = new SubTask(taskManager.getEpic(epic.getId()), "Вторая подзадача",
                "Описание второй подзадачи", TaskStatus.IN_PROGRESS);
        subNew.setId(sub2.getId());
        sub2 = taskManager.updateSubTask(subNew);

        System.out.println("Обновлена подзадача: " + sub2);
        System.out.println("Состояние эпика: " + epic);

        subNew = new SubTask(taskManager.getEpic(epic.getId()), "Вторая подзадача",
                "Описание второй подзадачи", TaskStatus.DONE);
        subNew.setId(sub2.getId());
        taskManager.updateSubTask(subNew);

        subNew = new SubTask(taskManager.getEpic(epic.getId()), "Первая подзадача",
                "Описание первой подзадачи", TaskStatus.DONE);

        subNew.setId(sub1.getId());
        sub1 = taskManager.updateSubTask(subNew);

        Epic epicNew = new Epic("Просто эпик", "Обновлённые данные");
        epicNew.setId(epic.getId());

        epic = taskManager.updateEpic(epicNew);

        System.out.println("Состояние эпика после обновлений: " + epic);

        sub1 = taskManager.deleteSubTask(sub1.getId());
        System.out.println("Задача удалена:" + sub1);
        System.out.println("Состояние эпика: " + epic);

        Epic epic2 = taskManager.createEpic(new Epic("Эпик номер 2", "Второй эпик"));
        System.out.println("Создан эпик: " + epic2);

        subNew = new SubTask(taskManager.getEpic(epic2.getId()), "Подзадача для второго эпика",
                "Описание подзадачи", TaskStatus.IN_PROGRESS);

        taskManager.createSubTask(subNew);
        System.out.println("Состояние эпика2: " + epic2);

        printAllTasks(taskManager);
    }
    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
