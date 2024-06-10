package service.file;

import model.*;
import service.HistoryManager;
import service.Managers;
import service.converter.TaskConverter;
import service.exeptions.AlreadyExistException;
import service.exeptions.ManagerSaveException;
import service.exeptions.NotFoundExeption;
import service.memory.InMemoryTaskManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final int FIELD_ID = 0;
    private static final int FIELD_TYPE = 1;
    private static final int FIELD_NAME = 2;
    private static final int FIELD_STATUS = 3;
    private static final int FIELD_DESCRIPTION = 4;
    private static final int FIELD_EPIC = 5;

    private final Path file;

    public FileBackedTaskManager(HistoryManager historyManager, Path file) {
        super(historyManager);
        this.file = file;

        readFromFile();
    }

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file.toFile()))) {
            bw.write("id,type,name,status,description,epic");
            bw.newLine();
            for (Task task : getTasks()) {
                bw.write(TaskConverter.toString(task));
                bw.newLine();
            }
            for (Epic epic : getEpics()) {
                bw.write(TaskConverter.toString(epic));
                bw.newLine();
            }
            for (SubTask subTask : getSubtasks()) {
                bw.write(TaskConverter.toString(subTask));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи файла " + file, e);
        }
    }

    private void readFromFile() {
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла " + file, e);
        }

        newTaskId = 0;
        for (int i = 1; i < lines.size(); i++) {

            Task task = fromString(lines.get(i));

            if (task.getId() > newTaskId) {
                newTaskId = task.getId();
            }

            switch (task.getType()) {
                case EPIC -> addEpic((Epic) task);
                case SUBTASK -> addSubTask((SubTask) task);
                default -> add(task);
            }
        }

    }

    static FileBackedTaskManager loadFromFile(Path file) {
        return new FileBackedTaskManager(Managers.getDefaultHistory(), file);
    }

    private Task fromString(String line) {
        String[] fields = line.split(",");
        Task task;
        TaskType taskType;
        try {
            taskType = TaskType.valueOf(fields[FIELD_TYPE]);
        } catch (IllegalArgumentException e) {
            throw new NotFoundExeption("Не найден тип задачи: " + fields[FIELD_TYPE]);
        }

        switch (taskType) {
            case EPIC -> task = new Epic(fields[FIELD_NAME], fields[FIELD_DESCRIPTION]);
            case SUBTASK ->
                    task = new SubTask(Integer.parseInt(fields[FIELD_EPIC]), fields[FIELD_NAME], fields[FIELD_DESCRIPTION], TaskStatus.valueOf(fields[FIELD_STATUS]));
            default ->
                    task = new Task(fields[FIELD_NAME], fields[FIELD_DESCRIPTION], TaskStatus.valueOf(fields[FIELD_STATUS]));
        }

        task.setId(Integer.parseInt(fields[FIELD_ID]));

        return task;
    }

    private void add(Task task) {
        Task saved = tasks.get(task.getId());
        if (saved != null) {
            throw new AlreadyExistException("Задача " + task.getId() + "уже существует");
        }
        tasks.put(task.getId(), task);
    }

    private void addEpic(Epic epic) {
        Epic saved = epics.get(epic.getId());
        if (saved != null) {
            throw new AlreadyExistException("Эпик " + epic.getId() + "уже существует");
        }
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    private void addSubTask(SubTask subTask) {
        SubTask saved = subTasks.get(subTask.getId());
        if (saved != null) {
            throw new AlreadyExistException("Подзадача " + subTask.getId() + "уже существует");
        }
        subTasks.put(subTask.getId(), subTask);
        addSubTaskToEpic(getEpic(subTask.getEpicId()), subTask);
        updateEpicStatus(getEpic(subTask.getEpicId()));
    }

    @Override
    public Task create(Task task) {
        Task result = super.create(task);
        save();
        return result;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic result = super.createEpic(epic);
        save();
        return result;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        SubTask result = super.createSubTask(subTask);
        save();
        return result;
    }

    @Override
    public Task update(Task task) {
        Task result = super.update(task);
        save();
        return result;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic result = super.updateEpic(epic);
        save();
        return result;
    }

    @Override
    public SubTask updateSubTask(SubTask subTask) {
        SubTask result = super.updateSubTask(subTask);
        save();
        return result;
    }

    @Override
    public Task delete(int id) {
        Task result = super.delete(id);
        save();
        return result;
    }

    @Override
    public Epic deleteEpic(int id) {
        Epic result = super.deleteEpic(id);
        save();
        return result;
    }

    @Override
    public SubTask deleteSubTask(int id) {
        SubTask result = super.deleteSubTask(id);
        save();
        return result;
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubTasks() {
        super.clearSubTasks();
        save();
    }
}
