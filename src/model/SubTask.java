package model;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(int epicId, String name, String description, TaskStatus status) {
        super(name, description, status);

        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", taskStatus=" + taskStatus +
                ", epicId=" + epicId + '}';
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public boolean compareAllFields(Task task) {
        return super.compareAllFields(task) && (epicId == task.getEpicId());
    }
}
