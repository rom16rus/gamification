package ru.kpfu.itis.service.impl;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kpfu.itis.dao.AccountDao;
import ru.kpfu.itis.dao.AccountTaskDao;
import ru.kpfu.itis.dao.TaskCategoryDao;
import ru.kpfu.itis.dao.TaskDao;
import ru.kpfu.itis.dto.ErrorDto;
import ru.kpfu.itis.dto.TaskCategoryDto;
import ru.kpfu.itis.dto.TaskDto;
import ru.kpfu.itis.dto.TaskInfoDto;
import ru.kpfu.itis.dto.enums.Error;
import ru.kpfu.itis.mapper.TaskInfoMapper;
import ru.kpfu.itis.mapper.TaskMapper;
import ru.kpfu.itis.model.Account;
import ru.kpfu.itis.model.AccountTask;
import ru.kpfu.itis.model.Task;
import ru.kpfu.itis.model.TaskStatus;
import ru.kpfu.itis.model.classifier.TaskCategory;
import ru.kpfu.itis.security.SecurityService;
import ru.kpfu.itis.service.TaskService;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by timur on 17.06.15.
 */
@Service("taskService")
public class TaskServiceImpl implements TaskService {


    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private AccountTaskDao accountTaskDao;

    @Autowired
    private TaskCategoryDao taskCategoryDao;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Autowired
    private SecurityService securityService;

    @Override
    @Transactional
    public Task submitTask(Task task) {
        return taskDao.submitTask(task);
    }

    @Override
    @Transactional
    public Task save(TaskDto taskDto) {
        Task task = taskMapper.fromDto(taskDto);
        task.setCategory(taskCategoryDao.findByName(taskDto.getCategory()));
        task.setAuthor(securityService.getCurrentUser());
        taskDao.save(task);
        return task;
    }

    @Override
    @Transactional
    public Task findByName(String name) {
        return taskDao.findByField(Task.class, "name", name);
    }

    @Override
    @Transactional
    public Task findTaskById(Long id) {
        return taskDao.findById(Task.class, id);
    }

    @Override
    public List<TaskCategoryDto> getAllCategories() {
        return taskCategoryDao.fetchAll(TaskCategory.class)
                .parallelStream().<TaskCategoryDto>map(taskCategory ->
                        new TaskCategoryDto(taskCategory.getName(), taskCategory.getTaskCategoryType()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskCategory save(TaskCategory taskCategory) {
        return taskCategoryDao.save(taskCategory);
    }

    @Override
    @Transactional
    public void setNewStatus(AccountTask accountTask, TaskStatus taskStatus) {
        accountTask.setNewStatus(taskStatus);
    }

    @Override
    @Transactional
    public List<Task> getActualTasks() {
        return taskDao.getActualTasks();
    }

    @Override
    @Transactional
    public List<Task> getTasksByUser(Long userId) {
        return taskDao.getTasksByUser(userId);
    }

    @Override
    public TaskInfoDto findById(Long taskId) {
        Task task = taskDao.findById(taskId);
        return taskInfoMapper.toDto(task);
    }

    @Override
    @Transactional
    public List<TaskDto> getTasksByUser(Long userId, Integer offset, Integer limit, TaskStatus.TaskStatusType status) {
        List<Task> tasksByUser = taskDao.getTasksByUser(userId, offset, limit, status);
        return tasksByUser.parallelStream().map(taskMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void stub() {
        System.out.println("hello,hello,hello");
    }

    @Override
    @Transactional
    public List<TaskInfoDto> getCreatedTasks(Long userId, Integer offset, Integer limit) {
        List<Task> createdTasks = taskDao.getCreatedTasks(userId, offset, limit);
        return createdTasks.stream().map(taskInfoMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResponseEntity enroll(Account account, Long taskId) {
        Validate.notNull(account);
        Task neededTask = taskDao.findById(taskId);
        if (Objects.isNull(neededTask)) {
            return new ResponseEntity<>(new ErrorDto(Error.TASK_NOT_FOUND), HttpStatus.NOT_FOUND);
        }

        AccountTask accountTask = accountTaskDao.findByTaskAndAccount(taskId, account.getId());
        if (Objects.isNull(accountTask)) {
            AccountTask youngAccountTask = createAccountTask(account, neededTask);
            accountTaskDao.save(youngAccountTask);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            if (TaskStatus.TaskStatusType.CANCELED.equals(accountTask.getTaskStatus().getType()) && Boolean.TRUE.equals(accountTask.getAvailability())) {
                TaskStatus newStatus = createNewStatus(accountTask);
                accountTask.setNewStatus(newStatus);
                accountTask.setAttemptsCount(accountTask.getAttemptsCount() + 1);
                accountTaskDao.save(accountTask);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ErrorDto(Error.TASK_ALREADY_TAKEN), HttpStatus.BAD_REQUEST);
            }
        }
    }


    public AccountTask createAccountTask(Account account, Task task) {
        AccountTask accountTask = new AccountTask();
        TaskStatus taskStatus = createNewStatus(accountTask);
        accountTask.setCreateTime(new Date());
        accountTask.setAccount(account);
        accountTask.setTask(task);
        accountTask.setAttemptsCount(1);
        accountTask.setAvailability(false);
        accountTask.setNewStatus(taskStatus);
        return accountTask;
    }

    public TaskStatus createNewStatus(AccountTask accountTask) {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setAccountTask(accountTask);
        taskStatus.setCreateTime(new Date());
        taskStatus.setType(TaskStatus.TaskStatusType.ASSIGNED);
        return taskStatus;
    }

}
