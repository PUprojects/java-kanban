package service.server;

import com.google.gson.Gson;
import model.Epic;
import service.TaskManager;

import java.util.regex.Pattern;

public class EpicsHttpHandler extends AbstractTasksHttpHandler {
    public EpicsHttpHandler(TaskManager taskManager, Gson gson, ErrorHandler errorHandler) {
        super(taskManager, gson, errorHandler, "epics");
    }

    @Override
    String getAll() {
        return gson.toJson(taskManager.getEpics());
    }

    @Override
    String getOne(int id) {
        return gson.toJson(taskManager.getEpic(id));
    }

    @Override
    String getElse(String path) {
        if (Pattern.matches("^/epics/\\d+/subtasks$", path)) {
            int id = Integer.parseInt(path.replaceFirst("/epics/", "")
                    .replaceFirst("/subtasks", ""));
            return gson.toJson(taskManager.getEpicSubtasks(id));
        }

        return "";
    }

    @Override
    String createOrUpdate(String jsonText) {
        Epic epic = gson.fromJson(jsonText, Epic.class);
        return gson.toJson(taskManager.createEpic(epic));
    }

    @Override
    void delete(int id) {
        taskManager.deleteEpic(id);
    }
}
