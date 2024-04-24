package service;

import model.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final int HISTORY_LENGTH = 10;
    private final LinkedList<Task> history = new LinkedList<>();
    @Override
    public void add(Task task) {
        if(history.size() == HISTORY_LENGTH) {
            history.removeFirst();
        }

        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
