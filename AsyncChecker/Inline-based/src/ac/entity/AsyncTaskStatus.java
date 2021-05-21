package ac.entity;

/**
 * Indicates the current status of the task. Each status will be set only once
 * during the lifetime of a task.
 */
public enum AsyncTaskStatus {
    /**
     * Indicates that the task has not been executed yet.
     */
    PENDING,
    /**
     * Indicates that the task is running.
     */
    RUNNING,
    /**
     * Indicates that the task has been cancelled
     */
    CANCEL
}