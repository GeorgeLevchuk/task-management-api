package com.example.task_management_api.exception;

public class NotValidAttributeTaskException extends RuntimeException {
    public NotValidAttributeTaskException() {
        super("Title or Description are not valid!");
    }
}
