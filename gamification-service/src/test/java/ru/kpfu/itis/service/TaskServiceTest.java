package ru.kpfu.itis.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kpfu.itis.dao.AccountTaskDao;
import ru.kpfu.itis.model.Account;
import ru.kpfu.itis.model.AccountTask;
import ru.kpfu.itis.model.Task;
import ru.kpfu.itis.model.TaskStatus;
import ru.kpfu.itis.model.classifier.TaskCategory;
import ru.kpfu.itis.processing.SimpleService;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Roman on 27.06.2015.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/spring/applicationContext.xml"})
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AccountTaskDao accountTaskDao;

    @Autowired
    private SimpleService simpleService;

    @Before
    public void setUp() throws Exception {


    }


    @Test
    public void submittingTask() {
        Account testAccount = TestToolkit.fakeAccount();
        simpleService.save(testAccount);
        assertNotNull(testAccount);
        TaskCategory taskCategory = TestToolkit.fakeTaskCategory();
        taskCategory = taskService.save(taskCategory);
        assertNotNull(taskCategory);

        Task task = new Task();
        task.setAuthor(testAccount);
        task.setParticipantsCount(10);
        task.setMaxMark(5);
        task.setName("XO");
        task.setDescription("desctiption");
        task.setCategory(taskCategory);
        task.setCreateTime(new Date());
        task.setStartDate(new Date());
        task.setEndDate(new Date());
        task.setBadge(null);
        task = taskService.submitTask(task);

        Task submittedTask = simpleService.findById(Task.class, task.getId());

        assertNotNull(submittedTask);

        AccountTask accountTask = new AccountTask();
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setAccountTask(accountTask);
        taskStatus.setCreateTime(new Date());
        taskStatus.setType(TaskStatus.TaskStatusType.ASSIGNED);

        accountTask.setCreateTime(new Date());
        accountTask.setAccount(testAccount);
        accountTask.setTask(task);
        accountTask.setAttemptsCount(1);
        accountTask.setAvailability(false);
        accountTask.setNewStatus(taskStatus);

        simpleService.save(accountTask);

        AccountTask saved = accountTaskDao.findByTaskAndAccount(task.getId(), testAccount.getId());

        List<Task> tasks = taskService.getTasksByUser(testAccount.getId());
        assertEquals(tasks.size(), 1);


    }


}
