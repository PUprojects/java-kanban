package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected Integer id;
    protected String name;
    protected String description;
    protected TaskStatus taskStatus;
    protected Duration duration;
    protected LocalDateTime startTime;
    protected LocalDateTime endTime;

    public Task(String name, String description, TaskStatus taskStatus, LocalDateTime startTime, Duration duration) {
        this.id = null;
        this.name = name;
        this.description = description;
        this.taskStatus = taskStatus;
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = this.startTime.plus(this.duration);
    }

    public Task(Integer id, String name, String description, TaskStatus taskStatus, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.taskStatus = taskStatus;
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = this.startTime.plus(this.duration);
    }

    public Task(Task task) {
        this.id = task.id;
        this.name = task.name;
        this.description = task.description;
        this.taskStatus = task.taskStatus;
        this.startTime = task.startTime;
        this.duration = task.duration;
        this.endTime = task.endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", taskStatus=" + taskStatus +
                ", epicId=" + getEpicId() +
                ", duration=" + duration.toMinutes() +
                ", startTime=" + startTime +
                "}";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public Integer getEpicId() {
        return 0;
    }

    public boolean compareAllFields(Task task) {
        return (Objects.equals(id, task.id)) && (name.equals(task.name)) && (description.equals(task.description)) &&
                (taskStatus == task.taskStatus) && (getType() == task.getType()) &&
                duration.equals(task.getDuration()) && startTime.equals(task.getStartTime()) &&
                endTime.equals(task.getEndTime());
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
        this.endTime = this.startTime.plus(this.duration);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        this.endTime = this.startTime.plus(this.duration);
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isCrossed(Task task) {
        return (startTime.isBefore(task.getEndTime()) && endTime.isAfter(task.getStartTime()));
    }
}
