package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static class Node {
        Node next;
        Node prev;
        Task data;

        public Node(Node prev, Node next, Task data) {
            this.next = next;
            this.prev = prev;
            this.data = data;
        }
    }

    HashMap<Integer, Node> history = new HashMap<>();

    Node first;
    Node last;

    @Override
    public void add(Task task) {
        remove(task.getId());
        Node oldLast = last;
        Node newNode = new Node(last, null, task);
        history.put(task.getId(), newNode);
        last = newNode;
        if (oldLast == null) {
            first = newNode;
        } else {
            oldLast.next = newNode;
        }
    }

    @Override
    public List<Task> getHistory() {
        ArrayList<Task> historyList = new ArrayList<>(history.size());
        Node node = first;
        while (node != null) {
            historyList.add(node.data);
            node = node.next;
        }
        return historyList;
    }

    @Override
    public void remove(int taskId) {
        Node node = history.remove(taskId);
        if (node == null)
            return;
        if (node.prev == null) {
            first = node.next;
        } else {
            node.prev.next = node.next;
        }

        if (node.next == null) {
            last = node.prev;
        } else {
            node.next.prev = node.prev;
        }
    }
}
