package service;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.List;
import java.util.TreeSet;

public interface TaskManager {
    Task create(Task task);

    Epic createEpic(Epic epic);

    SubTask createSubTask(SubTask subTask);

    Task get(int id);

    Epic getEpic(int id);

    SubTask getSubTask(int id);

    Task update(Task task);

    Epic updateEpic(Epic epic);

    SubTask updateSubTask(SubTask subTask);

    Task delete(int id);

    Epic deleteEpic(int id);

    SubTask deleteSubTask(int id);

    List<Task> getTasks();

    List<Epic> getEpics();

    List<SubTask> getSubtasks();

    List<SubTask> getEpicSubtasks(int id);

    TreeSet<Task> getPrioritizedTasks();

    void clearTasks();

    void clearEpics();

    void clearSubTasks();

    void removeSubTaskFromEpic(Epic epic, Integer subTaskId);

    void removeAllSubtasksFromEpic(Epic epic);

    SubTask addSubTaskToEpic(Epic epic, SubTask subTask);

    List<Task> getHistory();
}
