package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<SubTask> subTasks = new ArrayList<>();

    public Epic(String name, String description, TaskStatus taskStatus) {
        this(0, name, description, taskStatus);
    }

    public Epic(String name, String description) {
        this(0, name, description, null);

    }

    public Epic(int id, String name, String description, TaskStatus taskStatus) {
        super(id, name, description, taskStatus);
        updateStatus();
    }

    public Epic(int id, String name, String description) {
        this(id, name, description, null);
    }

    public List<SubTask> getSubTasks() {
        return subTasks;
    }

    public void updateStatus() {
        if (subTasks.isEmpty()) {
            taskStatus = TaskStatus.NEW;
        } else {
            taskStatus = TaskStatus.IN_PROGRESS;
            boolean isAllDone = true;
            boolean isAllNew = true;
            for (SubTask subTask : subTasks) {
                isAllDone = isAllDone && (subTask.getTaskStatus() == TaskStatus.DONE);
                isAllNew = isAllNew && (subTask.getTaskStatus() == TaskStatus.NEW);
            }

            if (isAllNew) {
                taskStatus = TaskStatus.NEW;
            } else if (isAllDone) {
                taskStatus = TaskStatus.DONE;
            }
        }
    }

    public void removeSubTask(SubTask subTask) {
        subTasks.remove(subTask);
        updateStatus();
    }

    public SubTask addSubTask(SubTask subTask) {
        subTasks.add(subTask);
        updateStatus();
        return subTask;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", taskStatus=" + taskStatus +
                ", subTasksCount=" + subTasks.size() +
                '}';
    }
}
