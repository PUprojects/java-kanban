package service.memory;

import model.Epic;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import service.HistoryManager;
import service.Managers;
import service.TaskManager;
import service.exeptions.NotFoundException;
import service.exeptions.TaskTimeValidateException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();

    protected final Map<Integer, Epic> epics = new HashMap<>();

    protected final Map<Integer, SubTask> subTasks = new HashMap<>();

    protected int newTaskId = 0;
    private final HistoryManager historyManager;

    protected final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

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
        checkTaskTime(task);
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null)
            prioritizedTasks.add(task);
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
        Epic epic = epics.get(subTask.getEpicId());
        if (epic == null) {
            throw new NotFoundException("Не найден эпик " + subTask.getEpicId());
        }

        checkTaskTime(subTask);
        subTask.setId(generateId());
        subTasks.put(subTask.getId(), subTask);
        if (subTask.getStartTime() != null)
            prioritizedTasks.add(subTask);
        addSubTaskToEpic(epic, subTask);
        updateEpicStatus(epic);
        return subTask;
    }

    @Override
    public Task get(int id) {
        final Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Не найдена задача " + id);
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        final Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Не найден эпик " + id);
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public SubTask getSubTask(int id) {
        final SubTask subTask = subTasks.get(id);
        if (subTask == null) {
            throw new NotFoundException("Не найдена подзадача " + id);
        }
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public Task update(Task task) {
        checkTaskTime(task);

        Task saved = tasks.get(task.getId());
        if (saved == null) {
            throw new NotFoundException("Не найдена задача " + task.getId());
        }

        prioritizedTasks.remove(saved);
        saved.setDescription(task.getDescription());
        saved.setName(task.getName());
        saved.setTaskStatus(task.getTaskStatus());
        saved.setDuration(task.getDuration());
        saved.setStartTime(task.getStartTime());
        if (saved.getStartTime() != null) {
            prioritizedTasks.add(saved);
        }

        return saved;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic saved = epics.get(epic.getId());
        if (saved == null) {
            throw new NotFoundException("Не найден эпик " + epic.getId());
        }
        saved.setName(epic.getName());
        saved.setDescription(epic.getDescription());
        return saved;
    }

    @Override
    public SubTask updateSubTask(SubTask subTask) {
        checkTaskTime(subTask);

        SubTask saved = subTasks.get(subTask.getId());
        if (saved == null) {
            throw new NotFoundException("Не найдена подзадача " + subTask.getId());
        }

        prioritizedTasks.remove(saved);
        saved.setDescription(subTask.getDescription());
        saved.setName(subTask.getName());
        saved.setTaskStatus(subTask.getTaskStatus());
        saved.setDuration(subTask.getDuration());
        saved.setStartTime(subTask.getStartTime());
        if (saved.getStartTime() != null) {
            prioritizedTasks.add(subTask);
        }
        updateEpicStatus(getEpic(saved.getEpicId()));

        return saved;
    }

    @Override
    public Task delete(int id) {
        historyManager.remove(id);
        Task removed = tasks.remove(id);
        prioritizedTasks.remove(removed);
        return removed;
    }

    @Override
    public Epic deleteEpic(int id) {
        Epic epic = epics.remove(id);
        historyManager.remove(id);
        if (epic == null)
            return null;
        for (Integer subTaskId : epic.getSubTasksIds()) {
            subTasks.remove(subTaskId);
            historyManager.remove(subTaskId);
        }
        return epic;
    }

    @Override
    public SubTask deleteSubTask(int id) {
        SubTask subTask = subTasks.remove(id);
        prioritizedTasks.remove(subTask);
        historyManager.remove(id);
        if (subTask == null)
            return null;
        removeSubTaskFromEpic(getEpic(subTask.getEpicId()), subTask.getId());
        updateEpicStatus(getEpic(subTask.getEpicId()));
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
    public TreeSet<Task> getPrioritizedTasks() {
        TreeSet<Task> result = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        result.addAll(prioritizedTasks);
        return result;
    }

    @Override
    public List<SubTask> getEpicSubtasks(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Не найден эпик " + id);
        }

        return epic.getSubTasksIds().stream()
                .map(subTasks::get)
                .toList();
    }

    @Override
    public void clearTasks() {
        for (Task t : tasks.values()) {
            historyManager.remove(t.getId());
            prioritizedTasks.remove(t);
        }
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        for (Task task : epics.values()) {
            historyManager.remove(task.getId());
        }
        for (Task task : subTasks.values()) {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        }

        epics.clear();
        subTasks.clear();
    }

    @Override
    public void clearSubTasks() {
        for (Task task : subTasks.values()) {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        }
        subTasks.clear();
        for (Epic epic : epics.values()) {
            removeAllSubtasksFromEpic(epic);
        }
    }

    protected void updateEpicStatus(Epic epic) {
        if (epic == null)
            return;

        List<Integer> subTasksIds = epic.getSubTasksIds();
        if (subTasksIds.isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(LocalDateTime.MIN);
        } else {
            updateEpicWithSubtasks(epic, subTasksIds);
        }
    }

    private void updateEpicWithSubtasks(Epic epic, List<Integer> subTasksIds) {
        LocalDateTime minStartTime = LocalDateTime.MAX;
        LocalDateTime maxEndTime = LocalDateTime.MIN.plusYears(1);
        long duration = 0;
        TaskStatus currentSubTaskStatus = null;
        boolean isAllSame = true;

        for (Integer subTasksId : subTasksIds) {

            SubTask subTask = subTasks.get(subTasksId);

            if (subTask.getStartTime().isBefore(minStartTime)) {
                minStartTime = subTask.getStartTime();
            }

            if (subTask.getEndTime().isAfter(maxEndTime)) {
                maxEndTime = subTask.getEndTime();
            }

            duration += subTask.getDuration().toMinutes();

            if (currentSubTaskStatus == null) {
                currentSubTaskStatus = subTask.getTaskStatus();
            } else {
                isAllSame = currentSubTaskStatus == subTask.getTaskStatus();
            }
        }

        epic.setStartTime(minStartTime);
        epic.setDuration(Duration.ofMinutes(duration));
        epic.setEndTime(maxEndTime);
        if (isAllSame) {
            epic.setTaskStatus(currentSubTaskStatus);
        } else {
            epic.setTaskStatus(TaskStatus.IN_PROGRESS);
        }
    }

    protected void checkTaskTime(Task task) {
        if (task.getStartTime() == null) {
            return;
        }
        prioritizedTasks.stream()
                .filter(t -> (!t.equals(task) && t.isCrossed(task)))
                .findFirst()
                .ifPresent(t -> {
                    throw new TaskTimeValidateException("Пересечение с задачей " + task.getName());
                });
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
        if (epic == null)
            return null;
        List<Integer> subTaskIds = epic.getSubTasksIds();
        Integer subTaskId = subTask.getId();
        if (!subTaskIds.contains(subTaskId))
            subTaskIds.add(subTaskId);
        updateEpicStatus(epic);
        return subTask;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
