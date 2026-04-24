import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.stream.Collectors;
import java.util.*;

public class Project {
    private final int id;
    private String name;
    private String description;
    private final LocalDate startDate;
    private double budget;
    private LocalDate deadlineDate;
    private ProjectStatus status;
    private Client client;
    private Department department;

    private final Set<Employee> employees = new LinkedHashSet<>();
    private final Map<Integer,Task> tasks = new LinkedHashMap<>();
    private final Set<String> requiredSkills = new LinkedHashSet<>();

    public Project(int id, String name, String description, Department department,
                   Client client, Task task,double budget, LocalDate deadlineDate) {
        if(id<=0)
        {
            throw new IllegalArgumentException("Invalid ID");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.department = department;
        this.client = client;
        this.startDate = LocalDate.now();
        this.budget = budget;
        this.status=ProjectStatus.PLANNED;
        this.deadlineDate = deadlineDate;

    }

    public int getId() {
        return id;
    }
    public Set<String> getRequiredSkills() {
        return Collections.unmodifiableSet(requiredSkills);
    }

    public void addRequiredSkill(String skill) {
        if (skill != null && !skill.isBlank()) {
            requiredSkills.add(skill.trim().toLowerCase());
        }
    }

    public void updateBudget(double newBudget) {
        if (newBudget >= 0) {
            this.budget = newBudget;
        }
    }

    public void addMember(Employee employee) {
        if (employee != null) {
            employees.add(employee);
        }
    }

    public void removeMember(Employee employee) {
        employees.remove(employee);
        tasks.values().forEach(task -> {
            if (employee != null && employee.equals(task.getEmployee())) {
                task.assignTo(null);
            }
        });
    }

    public void addTask(Task task) {
        if (task == null) return;
        if (tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Task duplicate id");
        }
        tasks.put(task.getId(), task);
        if (status == ProjectStatus.PLANNED) {
            status = ProjectStatus.ACTIVE;
        }
    }
    public void assignTask(int taskId, Employee employee) {
        Task task = tasks.get(taskId);
        if (task == null) throw new IllegalArgumentException("Task not found");
        task.assignTo(employee);
        if (employee != null) employees.add(employee);
    }

    public boolean completeTask(int taskId) {
        Task task = tasks.get(taskId);
        if (task == null) return false;
        task.complete();
        refreshStatus();
        return true;
    }

    public double progress() {
        if (tasks.isEmpty()) return 0.0;
        long done = tasks.values().stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        return done * 100.0 / tasks.size();
    }

    public void refreshStatus() {
        if (!tasks.isEmpty() && progress() == 100.0) {
            status = ProjectStatus.COMPLETED;
        }
    }

    public double calculateWeightedProgress()
    {
        if(tasks.isEmpty()) return 0.0;
        int totalWeight=0;
        int completedWeight=0;

        int blockedWeight=0;
        int overduePenalty=0;

        for(Task task: tasks.values())
        {
            int weight=task.getDifficulty();
            totalWeight += weight;

            if(task.getStatus() == TaskStatus.DONE)
            {
                completedWeight += weight;
            }

            else if(task.getStatus() == TaskStatus.IN_PROGRESS)
            {
                completedWeight += weight;
            }
            if(task.getStatus()==TaskStatus.BLOCKED)
            {
                blockedWeight += weight;
            }
            if(task.isOverdue() && task.getStatus() != TaskStatus.DONE)
            {
                overduePenalty +=2;
            }
        }

        double rawProgress = (completedWeight * 100.0) / totalWeight;

        double penalty = (blockedWeight * 2) +overduePenalty;

        double finalProgress = rawProgress - penalty;

        return Math.max(0,Math.min(100,finalProgress));
    }

    public String getProjectHealth()
    {
        double progress=calculateWeightedProgress();
        long blockedTasks=tasks.values().stream().filter(t->t.getStatus() == TaskStatus.BLOCKED).count();

        long overdueTasks=tasks.values().stream().filter(t->t.isOverdue()).count();

        if(progress > 80 && blockedTasks ==0)
        {
            return "Healthy";
        }
        if(progress < 50 && overdueTasks < 3)
        {
            return "STABLE";
        }
        if(blockedTasks > 2 || overdueTasks >3)
        {
            return "AT RISK";
        }

        return "CRITICAL";
    }
    @Override
    public String toString()
    {
        return "Project{id=" +  id + ", name=" + name + ", description=" + description + ", startDate=" + startDate
                + ", progress=" + String.format("%.2f",progress()) + "%, deadlineDate=" + deadlineDate + ", status=" + status + "}";
    }
    public Set<Employee> getEmployees() {
        return Collections.unmodifiableSet(employees);
    }

    public Map<Integer, Task> getTasks() {
        return Collections.unmodifiableMap(tasks);
    }
    public boolean isOverdue() {
        return LocalDate.now().isAfter(deadlineDate);
    }
    public ProjectStatus getStatus()
    {
        return this.status;
    }

    public Client getClient() {
        return this.client;
    }

    public LocalDate getDeadline() {
        return this.deadlineDate;
    }

    public double getBudget() {
        return this.budget;
    }

    public String getName() {
        return this.name;
    }

    public Department getDepartment() {
        return department;
    }

    public void reassignClient(Client client) {
        this.client = client;
    }

    public void setName(String newName) {
        name=newName;
    }

    public void changeDeadline(LocalDate deadline) {
        this.deadlineDate=deadline;
    }

    public void changeStatus(ProjectStatus status) {
        this.status=status;
    }

    public void reassignDepartment(Department department) {
        this.department=department;
    }
}
