package service;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();

    private final Map<Integer, Epic> epics = new HashMap<>();

    private final Map<Integer, SubTask> subTasks = new HashMap<>();

    private int newTaskId = 0;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        historyManager = Managers.getDefaultHistory();
    }

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    private int generateId() {
        return ++newTaskId;
    }

    @Override
    public Task create(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
        return epic;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        subTask.setId(generateId());
        subTasks.put(subTask.getId(), subTask);
        addSubTaskToEpic(subTask.getEpic(), subTask);
        updateEpicStatus(subTask.getEpic());
        return subTask;
    }

    @Override
    public Task get(int id) {
        final Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        final Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public SubTask getSubTask(int id) {
        final SubTask subTask = subTasks.get(id);
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public Task update(Task task) {
        Task saved = tasks.get(task.getId());
        if (saved == null)
            return null;
        saved.setDescription(task.getDescription());
        saved.setName(task.getName());
        saved.setTaskStatus(task.getTaskStatus());
        return saved;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic saved = epics.get(epic.getId());
        if (saved == null)
            return null;
        saved.setName(epic.getName());
        saved.setDescription(epic.getDescription());
        return saved;
    }

    @Override
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

    @Override
    public Task delete(int id) {
        return tasks.remove(id);
    }

    @Override
    public Epic deleteEpic(int id) {
        Epic epic = epics.remove(id);
        for (Integer subTaskId : epic.getSubTasksIds()) {
            subTasks.remove(subTaskId);
        }
        return epic;
    }

    @Override
    public SubTask deleteSubTask(int id) {
        SubTask subTask = subTasks.remove(id);
        removeSubTaskFromEpic(subTask.getEpic(), subTask.getId());
        updateEpicStatus(subTask.getEpic());
        return subTask;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getSubtasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public List<SubTask> getEpicSubtasks(int id) {
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

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void clearSubTasks() {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            removeAllSubtasksFromEpic(epic);
        }
    }

    private void updateEpicStatus(Epic epic) {
        if(epic == null)
            return;

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

    @Override
    public void removeSubTaskFromEpic(Epic epic, Integer subTaskId) {
        epic.getSubTasksIds().remove(subTaskId);
        updateEpicStatus(epic);
    }

    @Override
    public void removeAllSubtasksFromEpic(Epic epic) {
        epic.getSubTasksIds().clear();
        updateEpicStatus(epic);
    }

    @Override
    public SubTask addSubTaskToEpic(Epic epic, SubTask subTask) {
        if(epic == null)
            return null;
        List<Integer> subTaskIds = epic.getSubTasksIds();
        Integer subTaskId = subTask.getId();
        if (!subTaskIds.contains(subTaskId))
            subTaskIds.add(subTaskId);
        updateEpicStatus(epic);
        return subTask;
    }

    @Override
    public List<Task> getHistory(){
        return historyManager.getHistory();
    }
}
