package service;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, SubTask> subTasks;
    private int newTaskId = 0;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
    }

    private int generateId() {
        return ++newTaskId;
    }

    public Task create(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        epic.updateStatus();
        return epic;
    }

    public SubTask createSubTask(SubTask subTask) {
        subTask.setId(generateId());
        subTasks.put(subTask.getId(), subTask);
        subTask.getEpic().addSubTask(subTask);
        return subTask;
    }

    public Task get(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public SubTask getSubTask(int id) {
        return subTasks.get(id);
    }

    public Task update(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic updateEpic(Epic epic) {
        Epic saved = epics.get(epic.getId());
        if (saved == null)
            return null;
        saved.setName(epic.getName());
        saved.setDescription(epic.getDescription());
        return saved;
    }

    public SubTask updateSubTask(SubTask subTask) {
        SubTask saved = subTasks.get(subTask.getId());
        saved.setDescription(subTask.getDescription());
        saved.setName(subTask.getName());
        saved.setTaskStatus(subTask.getTaskStatus());
        saved.getEpic().updateStatus();
        return saved;
    }

    public Task delete(int id) {
        return tasks.remove(id);
    }

    public Epic deleteEpic(int id) {
        Epic epic = epics.remove(id);
        for (SubTask subTask : epic.getSubTasks()) {
            subTasks.remove(subTask.getId());
        }
        return epic;
    }

    public SubTask deleteSubTask(int id) {
        SubTask subTask = subTasks.remove(id);
        subTask.getEpic().removeSubTask(subTask);
        return subTask;
    }

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    public List<SubTask> getEpicSubTasks(int id) {
        List<SubTask> result = null;
        Epic epic = epics.get(id);
        if (epic != null) {
            result = epic.getSubTasks();
        }
        return result;
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        epics.clear();
    }

    public void clearSubTasks() {
        subTasks.clear();
    }
}
