package ru.kpfu.itis.dto.enums;

/**
 * Created by timur on 10.07.15.
 */
public enum Responses {

    USER_NOT_FOUND("User with requested id not found", 100),
    USER_INFO_NOT_FOUND("Information for user with requested id not found", 101),
    TASK_NOT_FOUND("Task doesn't found", 102),
    EMPTY_FILE("file is empty", 103),
    TASK_ALREADY_TAKEN("This user has already taken this task", 104),
    NOT_VALID_DATA("Not valid data sended", 105),
    TASK_NOT_AVAILABLE("Task not available for current user", 106),
    ENROLLED("Task has been taken", 107),
    TASK_ALREADY_EXISTS("Задание с таким именем уже существует", 107),
    EMPTY_NAME("Name of task not presented", 108),
    NOT_RIGHT_BADGE("Задача прикреплена к неправильному бейджу", 109),
    NOT_ENOUGH_POINTS("Не хватает баллов в бейдже для создания задачи", 110),
    SUBJECT_NOT_FOUND("Subject not founded", 111),
    CATEGORY_NOT_FOUND("Category of task not presented", 112);


    private String msg;
    private int code;

    Responses(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }
}
