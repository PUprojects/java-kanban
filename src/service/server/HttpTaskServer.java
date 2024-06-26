package service.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import service.Managers;
import service.TaskManager;
import service.converter.DurationAdapter;
import service.converter.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {

    public static final int PORT = 8080;
    private final HttpServer httpServer;

    public HttpTaskServer() {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager taskManager) {
        Gson gson = HttpTaskServer.getGson();
        ErrorHandler errorHandler = new ErrorHandler(gson);
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        httpServer.createContext("/tasks", new TasksHttpHandler(taskManager, gson, errorHandler));
        httpServer.createContext("/subtasks", new SubtaskHttpHandler(taskManager, gson, errorHandler));
        httpServer.createContext("/epics", new EpicsHttpHandler(taskManager, gson, errorHandler));
        httpServer.createContext("/history", new HistoryHttpHandler(taskManager, gson, errorHandler));
        httpServer.createContext("/prioritized", new PrioritizedHttpHandler(taskManager, gson, errorHandler));
    }

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        builder.registerTypeAdapter(Duration.class, new DurationAdapter());
        return builder.create();
    }

    public void start() {
        System.out.println("Запускаем сервер менеджера задач на порту " + PORT);
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("Отстановили сервер на порту " + PORT);
    }

    public static void main(String[] args) {
        HttpTaskServer httpTaskServer = new HttpTaskServer();
        httpTaskServer.start();
    }
}
