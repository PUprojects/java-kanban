package service.converter;

import model.Task;

public class TaskConverter {
    private TaskConverter() {
    }

    public static String toString(Task task) {
        return task.getId() + "," + task.getType() + "," + task.getName() + "," + task.getTaskStatus() + "," +
                task.getDescription() + "," + task.getEpicId() + "," + task.getDuration().toMinutes() + "," +
                task.getStartTime();
    }
}
