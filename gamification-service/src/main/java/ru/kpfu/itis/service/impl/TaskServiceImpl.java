package ru.kpfu.itis.service.impl;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.kpfu.itis.dao.*;
import ru.kpfu.itis.dto.*;
import ru.kpfu.itis.dto.enums.Error;
import ru.kpfu.itis.mapper.BadgeMapper;
import ru.kpfu.itis.mapper.TaskInfoMapper;
import ru.kpfu.itis.mapper.TaskMapper;
import ru.kpfu.itis.model.*;
import ru.kpfu.itis.model.classifier.TaskCategory;
import ru.kpfu.itis.model.enums.ActivityType;
import ru.kpfu.itis.model.enums.BadgeAchievementStatus;
import ru.kpfu.itis.model.enums.EntityType;
import ru.kpfu.itis.model.enums.StudyTaskType;
import ru.kpfu.itis.security.SecurityService;
import ru.kpfu.itis.service.ActivityService;
import ru.kpfu.itis.service.RatingService;
import ru.kpfu.itis.service.TaskService;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

/**
 * Created by timur on 17.06.15.
 */
@Service("taskService")
public class TaskServiceImpl implements TaskService {


    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private AccountTaskDao accountTaskDao;

    @Autowired
    private AccountBadgeDao accountBadgeDao;

    @Autowired
    private TaskCategoryDao taskCategoryDao;

    @Autowired
    private SimpleDao simpleDao;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    @Qualifier("simpleBadgeMapper")
    private BadgeMapper simpleBadgeMapper;

    @Autowired
    @Qualifier("badgeMapper")
    private BadgeMapper badgeMapper;

    @Autowired
    @Qualifier("studentTaskInfoMapper")
    private TaskInfoMapper studentTaskInfoMapper;

    @Autowired
    @Qualifier("adminTaskInfoMapper")
    private TaskInfoMapper adminTaskInfoMapper;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private RatingDao ratingDao;

    @Override
    @Transactional
    public Task submitTask(Task task) {
        return taskDao.submitTask(task);
    }

    @Override
    @Transactional
    public Task save(Account account, TaskEditorDto taskDto) {
        if (taskDto.getId() < 0) {
            Task task = new Task();
            if (StringUtils.isEmpty(taskDto.getName())) {
                throw new IllegalStateException("Name of task not presented");
            }
            task.setName(taskDto.getName());
            task.setCreateTime(new Date());

            task.setStartDate(DateUtils.truncate(taskDto.getDate_from(), Calendar.DATE));
            task.setEndDate(DateUtils.truncate(taskDto.getDate_to(), Calendar.DATE));
            task.setDescription(taskDto.getDescription() != null ? taskDto.getDescription() : "");
            task.setMaxMark(taskDto.getMaxMark());

            Account creator = null;
            AccountDto creatorDto = taskDto.getCreator();
            if (Objects.nonNull(creatorDto)) {
                creator = simpleDao.findById(Account.class, creatorDto.getId());
            }

            task.setAuthor(creator != null ? creator : account);

            Subject taskSubject = simpleDao.findById(Subject.class, taskDto.getSubject().getId());
            if (Objects.isNull(taskSubject)) {
                throw new IllegalStateException("Subject not founded");
            }
            task.setSubject(taskSubject);

            TaskCategory category = simpleDao.findById(TaskCategory.class, taskDto.getCategory().getId());
            if (Objects.isNull(category)) {
                throw new IllegalStateException("Category of task not presented");
            }
            task.setCategory(category);

            List<CourseOrGroupDto> courseOrGroupDtos = taskDto.getCoursesAndGroups();
            Set<AcademicGroup> academicGroups = new HashSet<>();
            for (CourseOrGroupDto courseOrGroupDto : courseOrGroupDtos) {
                if (courseOrGroupDto.isGroup()) {
                    AcademicGroup academicGroup = simpleDao.findById(AcademicGroup.class, courseOrGroupDto.getId());
                    academicGroups.add(academicGroup);
                } else {
                    StudyCourse course = simpleDao.findById(StudyCourse.class, courseOrGroupDto.getId());
                    academicGroups.addAll(course.getAcademicGroups().stream().collect(Collectors.toList()));
                }
            }
            academicGroups.forEach(task::addAcademicGroup);

            List<AccountDto> performers = taskDto.getPerformers();
            extractPermformers(performers, task);


            return taskDao.submitTask(task);

        }
        return null;
    }

    @Override
    @Transactional
    public Task findByName(String name) {
        return simpleDao.findByField(Task.class, "name", name);
    }

    @Override
    @Transactional
    public Task findTaskById(Long id) {
        return simpleDao.findById(Task.class, id);
    }

    @Override
    public List<TaskCategoryDto> getAllCategories() {
        List<TaskCategory> taskCategories = simpleDao.fetchAll(TaskCategory.class);
        List<TaskCategoryDto> categoryDtos = new ArrayList<>();
        if (CollectionUtils.isEmpty(taskCategories)) {
            return categoryDtos;
        }
        for (TaskCategory taskCategory : taskCategories) {
            categoryDtos.add(new TaskCategoryDto(taskCategory.getId(), taskCategory.getName(), taskCategory.getTaskCategoryType(), taskCategory.getTaskCategoryType().getCaption()));
        }
        return categoryDtos;

    }

    @Override
    @Transactional
    public TaskCategory save(TaskCategory taskCategory) {
        return taskCategoryDao.save(taskCategory);
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
    @Transactional
    public TaskInfoDto findById(Long taskId) {
        Task task = taskDao.findById(taskId);
        return studentTaskInfoMapper.toDto(task);
    }

    @Override
    @Transactional
    public List<TaskDto> getTasksByUser(Long userId, Integer offset, Integer limit, TaskStatus.TaskStatusType status) {
        List<Task> tasksByUser = taskDao.getTasksByUser(userId, offset, limit, status);
        return tasksByUser
                .stream()
                .map(taskMapper::toDto)
                .map(it -> {
                    it.setBadge(null);
                    return it;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void stub() {
        System.out.println("hello,hello,hello");
    }

    @Override
    @Transactional
    public List<TaskInfoDto> getCreatedTasks(Long userId, Integer offset, Integer limit, String query) {
        List<Task> createdTasks = taskDao.getCreatedTasks(userId, offset, limit, query);
        return createdTasks.stream().map(adminTaskInfoMapper::toDto).collect(Collectors.toList());
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
            simpleDao.save(youngAccountTask);
            Activity activity = new Activity(EntityType.TASK, ActivityType.TASK_ENROLL, account, neededTask.getId());
            simpleDao.save(activity);
            return new ResponseEntity<>(new TaskEnrollDto(TaskEnrollDto.TaskEnrollStatus.SUCCESS), HttpStatus.OK);
        } else {
            if (TaskStatus.TaskStatusType.CANCELED.equals(accountTask.getTaskStatus().getType()) && Boolean.TRUE.equals(accountTask.getAvailability())) {
                TaskStatus newStatus = createNewStatus(accountTask);
                accountTask.setNewStatus(newStatus);
                accountTask.setAttemptsCount(accountTask.getAttemptsCount() + 1);
                simpleDao.save(accountTask);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ErrorDto(Error.TASK_ALREADY_TAKEN), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Override
    @Transactional
    public ResponseEntity checkTask(Long taskId, Long accountId, Integer mark) {
        if (mark < 0 || Objects.isNull(mark)) {
            return new ResponseEntity<>(new ErrorDto(Error.NOT_VALID_DATA), BAD_REQUEST);
        }
        AccountTask accountTask = accountTaskDao.findByTaskAndAccount(taskId, accountId);
        if (Objects.nonNull(accountTask)) {
            Hibernate.initialize(accountTask.getTaskHistory());
            setNewStatus(accountTask, TaskStatus.TaskStatusType.COMPLETED);
            accountTask.setMark(mark);
            simpleDao.save(accountTask);
            Task task = accountTask.getTask();
            Account account = accountTask.getAccount();
            Activity activity = new Activity(EntityType.TASK, ActivityType.TASK_COMPLETE, account, task.getId());
            activityService.save(activity);
            if (task.getBadge() != null) {
                AccountBadge accountBadge = accountBadgeDao.findByBadgeAndAccount(task.getBadge(), account);
                if (Objects.isNull(accountBadge)) {
                    //TODO throw exception create account badge ON enroll
                    AccountBadge notExistedAccountBadge = new AccountBadge();
                    notExistedAccountBadge.setAccount(accountTask.getAccount());
                    notExistedAccountBadge.setBadge(task.getBadge());
                    accountBadge = notExistedAccountBadge;
                }
                if (StudyTaskType.PRACTICE.equals(task.getStudyType())) {
                    accountBadge.setPractice(accountBadge.getPractice() + mark);
                } else if (StudyTaskType.THEORY.equals(task.getStudyType())) {
                    accountBadge.setTheory(accountBadge.getTheory() + mark);
                }
                accountBadge.computeProgress();
                if (accountBadge.getAchevementStatus() == BadgeAchievementStatus.COMPLETE) {
                    activity = new Activity(EntityType.BADGE, ActivityType.BADGE_COMPLETE, account, task.getBadge().getId());
                    simpleDao.save(activity);
                }
                simpleDao.saveOrUpdate(accountBadge);
            }
            AccountInfo accountInfo = account.getAccountInfo();
            Rating rating = ratingDao.getUserRating(accountInfo.getId());
            if (Objects.nonNull(rating)) {
                rating.setPoint(rating.getPoint() + mark);
                ratingDao.save(rating);
            } else {
                ratingService.createUserRating(accountInfo, Double.valueOf(mark));
            }
            ratingService.recalculateRating(accountInfo);
            return new ResponseEntity<>(OK);
        } else {
            return new ResponseEntity<>(new ErrorDto(Error.TASK_NOT_FOUND), NOT_FOUND);
        }

    }

    @Override
    @Transactional
    public List<BadgeDto> getAllBadges() {
        List<Badge> badges = simpleDao.fetchAll(Badge.class);
        return badges
                .stream()
                .map(it -> findBadgeById(it.getId(), simpleBadgeMapper))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BadgeDto findBadgeById(Long id) {
        return findBadgeById(id, badgeMapper);
    }

    @Override
    @Transactional
    public BadgeDto findBadgeById(Long id, BadgeMapper badgeMapper) {
        Badge badge = simpleDao.findById(Badge.class, id);
        Hibernate.initialize(badge.getTasks());
        Account currentUser = simpleDao.findById(Account.class, securityService.getCurrentUserId());
        AccountBadge accountBadge = accountBadgeDao.findByBadgeAndAccount(badge, currentUser);
        if (accountBadge == null) {
            AccountBadge notExistedAccountBadge = new AccountBadge();
            notExistedAccountBadge.setAccount(currentUser);
            notExistedAccountBadge.setBadge(badge);
            accountBadge = notExistedAccountBadge;
            simpleDao.save(notExistedAccountBadge);
        }
        return badgeMapper.toDto(accountBadge);
    }

    @Override
    public ErrorDto isTaskAvailableForUser(Long taskId) {
        Account currentUser = simpleDao.findById(Account.class, securityService.getCurrentUserId());
        Task task = simpleDao.findById(Task.class, taskId);
        if (currentUser == null || task == null) {
            return new ErrorDto(Error.TASK_NOT_AVAILABLE);
        }
        if (!taskDao.isTaskAvailableForUser(currentUser, taskId)) {
            return new ErrorDto(Error.TASK_NOT_AVAILABLE);
        }
        return null;
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

    public void setNewStatus(AccountTask accountTask, TaskStatus.TaskStatusType statusType) {
        TaskStatus taskStatus = new TaskStatus();
        if (accountTask.getTaskStatus() != null) {
            taskStatus = accountTask.getTaskStatus();
        }
        taskStatus.setAccountTask(accountTask);
        taskStatus.setType(statusType);
        //TODO update time
        accountTask.setNewStatus(taskStatus);
    }


    private void extractPermformers(List<AccountDto> performers, Task task) {
        List<Account> accounts = new ArrayList<>();
        for (AccountDto performer : performers) {
            accounts.add(simpleDao.findById(Account.class, performer.getId()));
        }
        for (Account account : accounts) {
            createAccountTaskLink(task, account);
        }
    }

    private void createAccountTaskLink(Task task, Account account) {
        AccountTask accountTask = new AccountTask();
        accountTask.setTask(task);
        accountTask.setCreateTime(new Date());
        accountTask.setMark(0);
        accountTask.setAvailability(true);
        accountTask.setAccount(account);

        TaskStatus taskStatus = getNewTaskStatus(TaskStatus.TaskStatusType.ASSIGNED);

        accountTask.setNewStatus(taskStatus);
        task.addAccountTask(accountTask);
    }

    private TaskStatus getNewTaskStatus(TaskStatus.TaskStatusType statusType) {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setType(statusType);
        taskStatus.setCreateTime(new Date());
        return taskStatus;
    }
}
