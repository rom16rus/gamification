package ru.kpfu.itis.dao;

import ru.kpfu.itis.model.Account;
import ru.kpfu.itis.model.Task;
import ru.kpfu.itis.model.TaskStatus;

import java.util.List;

/**
 * Created by timur on 23.06.15.
 */
public interface TaskDao {

    Task submitTask(Task task);

    Task findById(Long id);

    Task findByName(String name);

    List<Task> getActualTasks();

    List<Task> getTasksByUser(Long userId);

    List<Task> getTasksByUser(Long userId, Integer offset, Integer limit, TaskStatus.TaskStatusType status);

    List<Task> getCreatedTasks(Account user, Integer offset, Integer limit, String query);

    boolean isTaskAvailableForUser(Account user, Long taskId);
}
