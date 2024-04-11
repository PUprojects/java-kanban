package service;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();

    private final HashMap<Integer, Epic> epics = new HashMap<>();

    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();

    private int newTaskId = 0;


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
        updateEpicStatus(epic);
        return epic;
    }

    public SubTask createSubTask(SubTask subTask) {
        subTask.setId(generateId());
        subTasks.put(subTask.getId(), subTask);
        addSubTaskToEpic(subTask.getEpic(), subTask);
        updateEpicStatus(subTask.getEpic());
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
        Task saved = tasks.get(task.getId());
        if (saved == null)
            return null;
        saved.setDescription(task.getDescription());
        saved.setName(task.getName());
        saved.setTaskStatus(task.getTaskStatus());
        return saved;
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
        if (saved == null)
            return null;
        saved.setDescription(subTask.getDescription());
        saved.setName(subTask.getName());
        saved.setTaskStatus(subTask.getTaskStatus());
        updateEpicStatus(saved.getEpic());
        return saved;
    }

    public Task delete(int id) {
        return tasks.remove(id);
    }

    public Epic deleteEpic(int id) {
        Epic epic = epics.remove(id);
        for (Integer subTaskId : epic.getSubTasksIds()) {
            subTasks.remove(subTaskId);
        }
        return epic;
    }

    public SubTask deleteSubTask(int id) {
        SubTask subTask = subTasks.remove(id);
        removeSubTaskFromEpic(subTask.getEpic(), subTask.getId());
        updateEpicStatus(subTask.getEpic());
        return subTask;
    }

    public HashMap<Integer, Task> getTasks() {
        HashMap<Integer, Task> tasksClone = new HashMap<>();
        for (Task task : tasks.values()) {
            tasksClone.put(task.getId(), task);
        }
        return tasksClone;
    }

    public HashMap<Integer, Epic> getEpics() {
        HashMap<Integer, Epic> epicsClone = new HashMap<>();
        for (Epic epic : epics.values()) {
            epicsClone.put(epic.getId(), epic);
        }
        return epicsClone;
    }

    public HashMap<Integer, SubTask> getSubTasks() {
        HashMap<Integer, SubTask> subTasksClone = new HashMap<>();
        for (SubTask subTask : subTasks.values()) {
            subTasksClone.put(subTask.getId(), subTask);
        }
        return subTasksClone;
    }

    public List<SubTask> getEpicSubTasks(int id) {
        List<SubTask> result = null;
        Epic epic = epics.get(id);
        if (epic != null) {
            result = new ArrayList<>();
            for (Integer subTaskId : epic.getSubTasksIds()) {
                result.add(subTasks.get(subTaskId));
            }
        }
        return result;
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        epics.clear();
        subTasks.clear();
    }

    public void clearSubTasks() {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            removeAllSubtasksFromEpic(epic);
        }
    }

    private void updateEpicStatus(Epic epic) {
        List<Integer> subTasksIds = epic.getSubTasksIds();
        if (subTasksIds.isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
        } else {
            TaskStatus currentSubTaskStatus = null;
            boolean isAllSame = true;
            for (Integer subTaskId : subTasksIds) {
                if (currentSubTaskStatus == null) {
                    currentSubTaskStatus = subTasks.get(subTaskId).getTaskStatus();
                } else {
                    isAllSame = currentSubTaskStatus == subTasks.get(subTaskId).getTaskStatus();
                }
                if (!isAllSame) {
                    break;
                }
            }

            if (isAllSame) {
                epic.setTaskStatus(currentSubTaskStatus);
            } else {
                epic.setTaskStatus(TaskStatus.IN_PROGRESS);
            }
        }
    }

    public void removeSubTaskFromEpic(Epic epic, Integer subTaskId) {
        epic.getSubTasksIds().remove(subTaskId);
        updateEpicStatus(epic);
    }

    public void removeAllSubtasksFromEpic(Epic epic) {
        epic.getSubTasksIds().clear();
        updateEpicStatus(epic);
    }

    public SubTask addSubTaskToEpic(Epic epic, SubTask subTask) {
        epic.getSubTasksIds().add(subTask.getId());
        updateEpicStatus(epic);
        return subTask;
    }

}
