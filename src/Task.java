import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.DoubleStream;

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

    public Task(int id, String title, String description,LocalDate dueDate,int difficulty) {
        if(id<=0)
        {
            throw new IllegalArgumentException("Invalid ID");
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
        return LocalDate.now().isAfter(dueDate);
    }

    public int getDifficulty() {
        return this.difficulty;
    }

    public void complete() {
        this.completed = true;
        this.completedAt = LocalDate.now();

    }

    public void assignTo(Employee employee) {
        if(employee.equals(this.employee)) return;

        if(this.status==TaskStatus.DONE)
        {
            throw new IllegalArgumentException("Cannot assign to this task that has already been done");

        }
        this.employee = employee;
        if(this.status == TaskStatus.TODO)
        {
            this.status=TaskStatus.IN_PROGRESS;
        }
    }
    public Employee getEmployee() {
        return employee;
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
        this.status = TaskStatus.BLOCKED;
    }

    public void reopen() {
        if(this.status!=TaskStatus.DONE)
        {
            this.status = TaskStatus.REVIEW;
        }

    }
}
