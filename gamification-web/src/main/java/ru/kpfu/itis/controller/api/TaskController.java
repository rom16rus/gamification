package ru.kpfu.itis.controller.api;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kpfu.itis.dto.ResponseDto;
import ru.kpfu.itis.dto.TaskCategoryDto;
import ru.kpfu.itis.dto.TaskDto;
import ru.kpfu.itis.model.AccountTask;
import ru.kpfu.itis.model.TaskStatus;
import ru.kpfu.itis.service.AccountTaskService;
import ru.kpfu.itis.service.FileService;
import ru.kpfu.itis.service.TaskService;
import ru.kpfu.itis.util.Constant;
import ru.kpfu.itis.validator.TaskValidator;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

/**
 * Created by timur on 17.06.15.
 */
@Api(value = "challenge", description = "operation with challenges")
@RequestMapping(Constant.API_URI_PREFIX + "/challenge")
@RestController("apiTaskController")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AccountTaskService accountTaskService;

    @Autowired
    private TaskValidator taskValidator;

    @InitBinder("taskDto")
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(taskValidator);
    }

    @ApiOperation("get task's information")
    @RequestMapping(value = "/{taskId:[1-9]+[0-9]*}", method = RequestMethod.GET)
    public TaskDto getTaskById(@PathVariable Long taskId) {
        return taskService.findById(taskId);
    }

    @ApiOperation("get student's tasks")
    @RequestMapping(method = RequestMethod.GET)
    public List<TaskDto> getAvailableTasks(@RequestParam(required = false) Integer offset,
                                           @RequestParam(required = false) Integer limit,
                                           @RequestParam(required = false) TaskStatus.TaskStatusType status) {
        //before we make authentication userId = 2 (user with role STUDENT)
        return taskService.getTasksByUser(2L, offset, limit, status);
    }

    @ApiOperation("get created tasks[FOR ADMIN OR TEACHER]")
    @RequestMapping(value = "/my", method = RequestMethod.GET)
    public List<TaskDto> getCreatedTasks(@RequestParam(required = false) Integer offset,
                                         @RequestParam(required = false) Integer limit) {
        //before we make authentication userId = 1
        return taskService.getCreatedTasks(1L, offset, limit);
    }

    @ApiOperation(value = "create challenge")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto, BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) throw new BindException(bindingResult);
        taskDto.setId(taskService.save(taskDto).getId());
        return new ResponseEntity<>(taskDto, HttpStatus.CREATED);
    }

    @ApiOperation("upload challenge's attachment")
    @RequestMapping(value = "/{taskId:[1-9]+[0-9]*}/attachments", method = RequestMethod.POST)
    public ResponseEntity<ResponseDto<String>> uploadAttachment(@RequestPart MultipartFile file,
                                                                @PathVariable Long taskId) {
        if (file.isEmpty())
            return new ResponseEntity<>(new ResponseDto<>("file is empty", NO_CONTENT.value()), NO_CONTENT);
        else
            try {
                String attachmentDir = fileService.uploadTaskAttachment(file, taskId);
                return new ResponseEntity<>(new ResponseDto<>("attachment uri", "/files/" + attachmentDir,
                        CREATED.value()), CREATED);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(new ResponseDto<>("Failed to upload", INTERNAL_SERVER_ERROR.value()),
                        INTERNAL_SERVER_ERROR);
            }
    }

    @Transactional
    @ApiOperation("check challenge")
    @RequestMapping(value = "/{taskId:[1-9]+[0-9]*}/user/{accountId:[1-9]+[0-9]*}", method = RequestMethod.POST)
    public ResponseEntity<ResponseDto<String>> checkTask(@PathVariable Long taskId,
                                                         @PathVariable Long accountId,
                                                         @RequestBody Integer mark) {
        AccountTask accountTask = accountTaskService.findByTaskAndAccount(taskId, accountId);
        if (accountTask != null) {
            TaskStatus taskStatus = new TaskStatus();
            taskStatus.setAccountTask(accountTask);
            taskStatus.setType(TaskStatus.TaskStatusType.COMPLETED);
            accountTask.setNewStatus(taskStatus);
            accountTask.setMark(mark);
            accountTaskService.saveOrUpdate(accountTask);
            return new ResponseEntity<>(OK);
        } else {
            return new ResponseEntity<>(new ResponseDto<>("Task doesn't found", NOT_FOUND.value()), NOT_FOUND);
        }

    }

    @ApiOperation("get challenge's attachments")
    @RequestMapping(value = "/{taskId:[1-9]+[0-9]*}/attachments", method = RequestMethod.GET)
    public List<String> getTaskAttachmentsNames(@PathVariable Long taskId) {
        return fileService.getTaskAttachmentsNames(taskId);
    }

    @ApiOperation("get available task categories")
    @RequestMapping(value = "/categories", method = RequestMethod.GET)
    public List<TaskCategoryDto> getTaskCategories() {
        return taskService.getAllCategories();
    }

}
