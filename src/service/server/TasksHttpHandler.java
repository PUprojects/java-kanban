package service.server;

import com.google.gson.Gson;
import model.Task;
import service.TaskManager;

public class TasksHttpHandler extends AbstractTasksHttpHandler {
    public TasksHttpHandler(TaskManager taskManager, Gson gson, ErrorHandler errorHandler) {
        super(taskManager, gson, errorHandler, "tasks");
    }

    @Override
    String getAll() {
        return gson.toJson(taskManager.getTasks());
    }

    @Override
    String getOne(int id) {
        return gson.toJson(taskManager.get(id));
    }

    @Override
    String getElse(String path) {
        return "";
    }

    @Override
    String createOrUpdate(String jsonText) {
        Task task = gson.fromJson(jsonText, Task.class);
        if (task.getId() == null)
            task = taskManager.create(task);
        else
            task = taskManager.update(task);
        return gson.toJson(task);
    }

    @Override
    void delete(int id) {
        taskManager.delete(id);
    }
}
