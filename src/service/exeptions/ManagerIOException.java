package service.exeptions;

import java.io.IOException;

public class ManagerIOException extends RuntimeException {
    public ManagerIOException(String message, IOException e) {
        super(message, e);
    }
}
