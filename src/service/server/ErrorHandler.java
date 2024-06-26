package service.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import service.exeptions.NotFoundException;
import service.exeptions.TaskTimeValidateException;

public class ErrorHandler {
    final Gson gson;

    public ErrorHandler(Gson gson) {
        this.gson = gson;
    }

    public void handle(HttpExchange exchange, Exception e) {
        try {
            System.out.println("Exception message: " + e.getMessage());
            if (e instanceof NotFoundException) {
                exchange.sendResponseHeaders(404, 0);
                return;
            }
            if (e instanceof TaskTimeValidateException) {
                exchange.sendResponseHeaders(406, 0);
                return;
            }
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        } catch (Exception newException) {
            newException.printStackTrace();
        }
    }
}
