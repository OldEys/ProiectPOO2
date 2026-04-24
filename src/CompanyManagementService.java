import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class CompanyManagementService {
    private final Company company;
    private final Map<Integer, Department> departments = new LinkedHashMap<>();
    private final Map<Integer, Employee> employees = new LinkedHashMap<>();
    private final Map<Integer, Project> projects = new LinkedHashMap<>();
    private final Map<Integer, Client> clients = new LinkedHashMap<>();

    public CompanyManagementService(Company company) {
        this.company = company;
    }

    public void addDepartment(Department department) {
        departments.put(department.getId(), department);
    }

    public void addClient(Client client) {
        clients.put(client.getId(), client);
    }

    public void addProject(Project project) {
        projects.put(project.getId(), project);
        if (project.getClient() != null) {
            project.getClient().registerProject(project);
        }
    }

    public void hireEmployee(Employee employee, int departmentId) {
        Department department = departments.get(departmentId);
        if (department == null) {
            throw new IllegalArgumentException("Department not found");
        }
        employees.put(employee.getId(), employee);
        department.addEmployee(employee);
    }

    public void appointManager(int departmentId, int managerId) {
        Department department = departments.get(departmentId);
        Employee employee = employees.get(managerId);

        if (department == null) throw new IllegalArgumentException("Department not found");
        if (!(employee instanceof Manager)) throw new IllegalArgumentException("Employee is not manager");

        department.appointManager((Manager) employee);
    }

    public void assignEmployeeToProject(int employeeId, int projectId) {
        Employee employee = employees.get(employeeId);
        Project project = projects.get(projectId);
        if (employee == null || project == null) {
            throw new IllegalArgumentException("Assignment failed");
        }
        project.addMember(employee);
    }

    public void addTaskToProject(int projectId, Task task) {
        Project project = projects.get(projectId);
        if (project == null) throw new IllegalArgumentException("Project not found");
        project.addTask(task);
    }

    public void assignTaskToEmployee(int projectId, int taskId, int employeeId) {
        Project project = projects.get(projectId);
        Employee employee = employees.get(employeeId);
        if (project == null || employee == null) throw new IllegalArgumentException("Assignment failed");
        project.assignTask(taskId, employee);
    }

    public void completeTask(int projectId, int taskId) {
        Project project = projects.get(projectId);
        if (project == null) throw new IllegalArgumentException("Project not found");
        project.completeTask(taskId);
    }

    public List<Task> getHighPriorityTasks(int projectId) {
        Project project = projects.get(projectId);
        if (project == null) return new ArrayList<>();

        return project.getTasks().values().stream()
                .sorted(Comparator
                        .comparingInt(Task::getPriority).reversed()
                        .thenComparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public double calculateProjectCost(int projectId) {
        Project project = projects.get(projectId);
        if (project == null) throw new IllegalArgumentException("Project not found");

        double teamCost = project.getEmployees().stream()
                .mapToDouble(Employee::getSalary)
                .sum();

        double taskCost = project.getTasks().values().stream()
                .mapToDouble(t -> {
                    double base = t.getEstimatedHours() * 50.0;
                    double priorityExtra = t.getPriority() * 100.0;
                    return base + priorityExtra;
                })
                .sum();

        return teamCost + taskCost;
    }

    public double calculateProjectProfit(int projectId) {
        Project project = projects.get(projectId);
        return project.getBudget() - calculateProjectCost(projectId);
    }

    public boolean isProjectAtRisk(int projectId) {
        Project project = projects.get(projectId);
        if (project == null) throw new IllegalArgumentException("Project not found");

        double progress = project.progress();
        long daysLeft = project.getDeadline() == null
                ? Long.MAX_VALUE
                : ChronoUnit.DAYS.between(LocalDate.now(), project.getDeadline());

        boolean tooManyBlockedTasks = project.getTasks().values().stream()
                .filter(t -> t.getStatus() == TaskStatus.BLOCKED)
                .count() >= 2;

        return (daysLeft < 14 && progress < 50)
                || tooManyBlockedTasks
                || calculateProjectProfit(projectId) < 0;
    }

    public double calculatePerformanceScore(int employeeId) {
        Employee employee = employees.get(employeeId);
        if (employee == null) throw new IllegalArgumentException("Employee not found");

        int assigned = 0;
        int completed = 0;
        int late = 0;
        int blocked = 0;
        int priorityPoints = 0;

        for (Project project : projects.values()) {
            for (Task task : project.getTasks().values()) {
                if (employee.equals(task.getEmployee())) {
                    assigned++;
                    priorityPoints += task.getPriority();
                    if (task.getStatus() == TaskStatus.DONE) completed++;
                    if (task.getStatus() == TaskStatus.BLOCKED) blocked++;
                    if (task.isOverdue() && task.getStatus() != TaskStatus.DONE) late++;
                }
            }
        }

        double completionRate = assigned == 0 ? 0 : (completed * 100.0 / assigned);
        double tenureMonths = ChronoUnit.MONTHS.between(employee.getHireDate(), LocalDate.now());
        double tenureBonus = Math.min(tenureMonths * 0.8, 12.0);

        double skillBonus = 0;
        if (employee instanceof Developer developer) {
            skillBonus = developer.getSkillCount() * 2.5;
        }

        double score = completionRate * 0.55
                + priorityPoints * 3
                + tenureBonus
                + skillBonus
                - late * 8
                - blocked * 5;

        return Math.max(0, Math.min(100, score));
    }

    public boolean isEligibleForPromotion(int employeeId) {
        return calculatePerformanceScore(employeeId) >= 80;
    }

    public Manager promoteDeveloperToManager(int employeeId, int teamSize, double bonus) {
        Employee employee = employees.get(employeeId);
        if (!(employee instanceof Developer dev)) {
            throw new IllegalArgumentException("Only a Developer can be promoted to Manager in this flow.");
        }

        double score = calculatePerformanceScore(employeeId);
        if (score < 80) {
            throw new IllegalStateException("Employee is not eligible for promotion. Score = " + score);
        }
        Manager newManager = new Manager(
                dev.getId(),
                dev.getFullName(),
                dev.getEmail(),
                dev.getSalary(),
                dev.getDepartment(),
                teamSize,
                bonus
        );

        Department department = dev.getDepartment();
        if (department != null) {
            department.removeEmployee(dev);
            department.addEmployee(newManager);
            department.appointManager(newManager);
        }

        for (Project project : projects.values()) {
            if (project.getEmployees().contains(dev)) {
                project.removeMember(dev);
                project.addMember(newManager);
            }
            for (Task task : project.getTasks().values()) {
                if (dev.equals(task.getEmployee())) {
                    task.assignTo(newManager);
                }
            }
        }

        employees.put(employeeId, newManager);
        return newManager;
    }

    public Employee assignBestDeveloperToTask(int projectId, int taskId) {
        Project project = projects.get(projectId);
        if (project == null) throw new IllegalArgumentException("Project not found");

        Task task = project.getTasks().values().stream()
                .filter(t -> t.getId() == taskId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        return employees.values().stream()
                .filter(e -> e instanceof Developer)
                .max(Comparator.comparingDouble(e -> scoreDeveloperForTask((Developer) e, project, task)))
                .orElseThrow(() -> new IllegalStateException("No developers available"));
    }

    public List<Employee> recommendOptimalTeam(int projectId, int maxMembers) {
        Project project = projects.get(projectId);
        if (project == null) throw new IllegalArgumentException("Project not found");

        return employees.values().stream()
                .sorted(Comparator.comparingDouble(
                        (Employee e) -> scoreEmployeeForProject(e, project)
                ).reversed())
                .limit(maxMembers)
                .collect(Collectors.toList());
    }
    public List<Employee> employeesSortedBySalaryDesc() {
        return employees.values().stream()
                .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
                .collect(Collectors.toList());
    }

    public void printSummary() {
        System.out.println("Company: " + company.getName());
        System.out.println("Departments: " + departments.size());
        System.out.println("Employees: " + employees.size());
        System.out.println("Projects: " + projects.size());
        System.out.println("Clients: " + clients.size());
        System.out.println("Payroll: " + totalPayroll());
    }

    public double totalPayroll() {
        return employees.values().stream()
                .mapToDouble(Employee::getSalary)
                .sum();
    }

    private double scoreDeveloperForTask(Developer developer, Project project, Task task) {
        double score = calculatePerformanceScore(developer.getId());

        if (task.getPriority() > 40) score += 15;

        score -= project.getEmployees().size() * 2;
        return score;
    }

    private double scoreEmployeeForProject(Employee employee, Project project) {
        double score = calculatePerformanceScore(employee.getId());

        if (employee instanceof Manager) {
            score += 10;
        }

        if (employee instanceof Developer developer) {
            for (String req : project.getRequiredSkills()) {
                if (developer.hasSkill(req)) {
                    score += 8;
                }
            }
        }

        long workload = projects.values().stream()
                .filter(p -> p.getEmployees().contains(employee))
                .count();

        score -= workload * 4;
        return score;
    }

    public double evaluateEmployeePerformance(int employeeId)
    {
        Employee employee=employees.get(employeeId);
        if(employee == null)
        {
            throw new IllegalArgumentException("Employee not found");
        }
        int completedTasks=0;
        int lateTasks=0;

        for (Project project: projects.values())
        {
            for(Task task: project.getTasks().values())
            {
                if(employee.equals(task.getEmployee()))
                {
                    if(task.getStatus()==TaskStatus.DONE)
                    {
                        completedTasks++;
                    }
                    if(task.isOverdue() && task.getStatus() != TaskStatus.DONE)
                    {
                        lateTasks++;
                    }
                }
            }
        }

        int skillPoints=0;
        if(employee instanceof Developer dev)
        {
            skillPoints=dev.getSkillCount()*5;
        }

        double score=completedTasks * 10 -lateTasks * 8 + skillPoints;

        return Math.max(0,Math.min(100,score));
    }

    public LocalDate estimateCompletionDate(int projectId)
    {
        Project project = projects.get(projectId);
        if(project == null)
        {
            throw new IllegalArgumentException("Project not found");
        }

        List<Task> tasks = new ArrayList<>(project.getTasks().values());
        if(tasks.isEmpty())
        {
            throw new IllegalArgumentException("No tasks found");
        }

        double totalRemainingWork=0;
        int blockedTasks = 0;
        int overdueTasks = 0;

        for(Task task: tasks)
        {
            if(task.getStatus()==TaskStatus.DONE)
            {
                continue;
            }
            double weight=task.getDifficulty();

            if(task.getStatus() == TaskStatus.BLOCKED)
            {
                blockedTasks++;
            }
            if(task.isOverdue())
            {
                overdueTasks++;
            }

        }
        double teamVelocity =0;

        for(Employee e : project.getEmployees()) {
            double performance = evaluateEmployeePerformance(e.getId());

            double skillFactor = 1.0;

            if (e instanceof Developer developer) {
                skillFactor += developer.getSkillCount() * 0.1;
            }
            if (e instanceof Manager manager) {
                skillFactor += 0.5;
            }

            teamVelocity += (performance / 100.0) * skillFactor;

            if (teamVelocity == 0) {
                teamVelocity = 1;
            }
        }
            double estimatedDays = totalRemainingWork / teamVelocity;
            double penalty = 0;
            penalty += blockedTasks * 2;
            penalty += overdueTasks * 1.5;

            if (tasks.size() > 10) {
                penalty += tasks.size() * 0.3;
            }

            if (isProjectAtRisk(projectId)) {
                penalty *= 1.5;
            }

            double finalDays = Math.ceil(estimatedDays + penalty);

            finalDays=Math.max(finalDays,1);

            return LocalDate.now().plusDays((long) finalDays);
    }
}