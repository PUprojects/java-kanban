package service.server;

import com.google.gson.Gson;
import model.SubTask;
import service.TaskManager;

public class SubtaskHttpHandler extends AbstractTasksHttpHandler {
    public SubtaskHttpHandler(TaskManager taskManager, Gson gson, ErrorHandler errorHandler) {
        super(taskManager, gson, errorHandler, "subtasks");
    }

    @Override
    String getAll() {
        return gson.toJson(taskManager.getSubtasks());
    }

    @Override
    String getOne(int id) {
        return gson.toJson(taskManager.getSubTask(id));
    }

    @Override
    String getElse(String path) {
        return "";
    }

    @Override
    String createOrUpdate(String jsonText) {
        SubTask subTask = gson.fromJson(jsonText, SubTask.class);
        if (subTask.getId() == null)
            subTask = taskManager.createSubTask(subTask);
        else
            subTask = taskManager.updateSubTask(subTask);
        return gson.toJson(subTask);
    }

    @Override
    void delete(int id) {
        taskManager.deleteSubTask(id);
    }
}
