import java.time.LocalDate;
import java.util.Objects;

public class Task {
    private final int id;
    private String title;
    private String description;
    private Boolean completed;
    private LocalDate createdAt;
    private LocalDate completedAt;
    private LocalDate dueDate;
    private Employee employee;
    private TaskStatus status;
    private final int difficulty;
    private Integer projectId;

    public Task(int id, String title, String description,LocalDate dueDate,int difficulty) {
        if(id<=0)
        {
            throw new IllegalArgumentException("ID invalid");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = false;
        this.createdAt = LocalDate.now();
        this.difficulty = difficulty;
        this.dueDate=dueDate;
        this.status=TaskStatus.TODO;
    }

    public Task(int id, String title, String description, LocalDate dueDate, int difficulty, TaskStatus status,
                Boolean completed, LocalDate createdAt, LocalDate completedAt, Integer projectId) {
        this(id, title, description, dueDate, difficulty);
        this.status = status == null ? TaskStatus.TODO : status;
        this.completed = completed != null && completed;
        this.createdAt = createdAt == null ? LocalDate.now() : createdAt;
        this.completedAt = completedAt;
        this.projectId = projectId;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public boolean isOverdue() {
        if (dueDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate);
    }

    public int getDifficulty() {
        return this.difficulty;
    }

    public void complete() {
        this.completed = true;
        this.completedAt = LocalDate.now();
        this.status = TaskStatus.DONE;

    }

    public void assignTo(Employee employee) {
        if(Objects.equals(employee, this.employee)) return;

        if (employee == null) {
            this.employee = null;
            return;
        }

        if(this.status==TaskStatus.DONE)
        {
            throw new IllegalArgumentException("Task-ul finalizat nu poate fi reasignat");

        }
        this.employee = employee;
        if(employee != null && this.status == TaskStatus.TODO)
        {
            this.status=TaskStatus.IN_PROGRESS;
        }
    }
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public int getPriority() {
        int priorityScore = 0;

        priorityScore += this.difficulty;

        if (dueDate != null) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(), dueDate);
            priorityScore += Math.max(0,50-(int)daysLeft*5);
        }
        if (status == TaskStatus.BLOCKED) {
            priorityScore += 10;
        } else if (status == TaskStatus.IN_PROGRESS) {
            priorityScore += 5;
        }
        if (isOverdue()) {
            priorityScore += 20;
        }
        return priorityScore;
    }

    public int getEstimatedHours() {

        int baseHours = difficulty * 2;

        double statusMultiplier = switch (status) {
            case TODO -> 1.0;
            case IN_PROGRESS -> 0.8;
            case REVIEW -> 0.5;
            case BLOCKED -> 1.2;
            case DONE -> 0.0;
        };

        double estimated = baseHours * statusMultiplier;

        return (int) Math.ceil(estimated);
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setTitle(String newTitle)
    {
        this.title=newTitle;
    }

    public void block() {
        if (this.status == TaskStatus.DONE) {
            throw new IllegalArgumentException("Task-ul finalizat nu poate fi blocat");
        }
        this.completed = false;
        this.completedAt = null;
        this.status = TaskStatus.BLOCKED;
    }

    public void reopen() {
        if(this.status!=TaskStatus.DONE)
        {
            this.status = this.employee == null ? TaskStatus.TODO : TaskStatus.IN_PROGRESS;
        }

    }

    public boolean isCompleted() {
        return completed != null && completed;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public LocalDate getCompletedAt() {
        return completedAt;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public void setStatus(TaskStatus status) {
        this.status = status == null ? TaskStatus.TODO : status;
        this.completed = this.status == TaskStatus.DONE;
        if (this.completed && this.completedAt == null) {
            this.completedAt = LocalDate.now();
        }
        if (!this.completed) {
            this.completedAt = null;
        }
    }
}
