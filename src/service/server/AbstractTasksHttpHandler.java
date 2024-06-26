package service.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

import java.io.IOException;
import java.util.regex.Pattern;

public abstract class AbstractTasksHttpHandler extends BaseHttpHandler {
    private final ErrorHandler errorHandler;
    private final String sectionName;
    protected final TaskManager taskManager;
    protected final Gson gson;

    public AbstractTasksHttpHandler(TaskManager taskManager, Gson gson, ErrorHandler errorHandler, String sectionName) {
        this.sectionName = sectionName;
        this.errorHandler = errorHandler;
        this.taskManager = taskManager;
        this.gson = gson;
    }

    abstract String getAll();

    abstract String getOne(int id);

    abstract String getElse(String path);

    abstract String createOrUpdate(String jsonText);

    abstract void delete(int id);

    protected void processGet(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String response = "";

        if (Pattern.matches("^/" + sectionName + "$", path)) {
            response = getAll();
        }

        if (Pattern.matches("^/" + sectionName + "/\\d+$", path)) {
            int id = Integer.parseInt(path.replaceFirst("/" + sectionName + "/", ""));
            response = getOne(id);
        }

        if (response.isEmpty())
            response = getElse(path);

        if (!response.isEmpty()) {
            sendText(httpExchange, response, 200);
        } else {
            httpExchange.sendResponseHeaders(400, 0);
        }
    }

    protected void processPost(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        if (Pattern.matches("^/" + sectionName + "$", path)) {
            String response = createOrUpdate(readText(httpExchange));
            sendText(httpExchange, response, 200);
        } else {
            httpExchange.sendResponseHeaders(400, 0);
        }
    }

    protected void processDelete(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        if (Pattern.matches("^/" + sectionName + "/\\d+$", path)) {
            int id = Integer.parseInt(path.replaceFirst("/" + sectionName + "/", ""));
            delete(id);
            httpExchange.sendResponseHeaders(204, -1);
        } else {
            httpExchange.sendResponseHeaders(400, 0);
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        try (httpExchange) {
            String method = httpExchange.getRequestMethod();
            try {
                switch (method) {
                    case "GET" -> processGet(httpExchange);
                    case "POST" -> processPost(httpExchange);
                    case "DELETE" -> processDelete(httpExchange);
                    default -> httpExchange.sendResponseHeaders(405, 0);
                }
            } catch (Exception e) {
                errorHandler.handle(httpExchange, e);
            }
        }
    }
}
