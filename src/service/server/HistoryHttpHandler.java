package service.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

public class HistoryHttpHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;
    private final ErrorHandler errorHandler;

    public HistoryHttpHandler(TaskManager taskManager, Gson gson, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        try (httpExchange) {
            try {
                String method = httpExchange.getRequestMethod();

                if (method.equals("GET")) {
                    String response = gson.toJson(taskManager.getHistory());
                    sendText(httpExchange, response, 200);
                } else {
                    httpExchange.sendResponseHeaders(405, 0);
                }
            } catch (Exception e) {
                errorHandler.handle(httpExchange, e);
            }
        }
    }
}
