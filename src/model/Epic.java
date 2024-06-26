package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subTasksIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW, LocalDateTime.MIN.plusYears(1), Duration.ZERO);
    }

    public List<Integer> getSubTasksIds() {
        return subTasksIds;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", taskStatus=" + taskStatus +
                ", subTasksCount=" + subTasksIds.size() +
                '}';
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
