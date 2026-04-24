import java.util.*;

public class Company {

    private final String name;

    private final Map<Integer, Employee> employees = new LinkedHashMap<>();
    private final Map<Integer, Project> projects = new LinkedHashMap<>();
    private final Map<Integer, Client> clients = new LinkedHashMap<>();

    public Company(String name) {
        this.name = name;
    }

    public void addEmployee(Employee e) {
        if (e == null) throw new IllegalArgumentException("Employee null");
        employees.put(e.getId(), e);
    }

    public void addProject(Project p) {
        if (p == null) throw new IllegalArgumentException("Project null");
        projects.put(p.getId(), p);
    }

    public void addClient(Client c) {
        if (c == null) throw new IllegalArgumentException("Client null");
        clients.put(c.getId(), c);
    }
    
    public Collection<Employee> getEmployees() {
        return Collections.unmodifiableCollection(employees.values());
    }

    public Collection<Project> getProjects() {
        return Collections.unmodifiableCollection(projects.values());
    }

    public Collection<Client> getClients() {
        return Collections.unmodifiableCollection(clients.values());
    }
    
    public void optimizeResourceAllocation() {

        for (Project project : projects.values()) {

            // 🔹 identifică proiecte în risc
            boolean tooManyBlocked = project.getTasks().values().stream()
                    .filter(t -> t.getStatus() == TaskStatus.BLOCKED)
                    .count() >= 2;

            boolean lowProgress = project.calculateWeightedProgress() < 50;

            if (!(tooManyBlocked || lowProgress)) {
                continue;
            }

            Employee weakest = null;
            double minScore = Double.MAX_VALUE;

            for (Employee e : project.getEmployees()) {
                double perf = evaluateEmployeePerformance(e);

                if (perf < minScore) {
                    minScore = perf;
                    weakest = e;
                }
            }

            Employee bestCandidate = null;
            double maxScore = 0;

            for (Employee e : employees.values()) {

                // nu e deja în proiect
                if (project.getEmployees().contains(e)) continue;

                double perf = evaluateEmployeePerformance(e);

                if (perf > maxScore) {
                    maxScore = perf;
                    bestCandidate = e;
                }
            }

            // 🔹 înlocuire (dacă are sens)
            if (weakest != null && bestCandidate != null && maxScore > minScore) {

                project.removeMember(weakest);
                project.addMember(bestCandidate);

                System.out.println("Reassigned: " + weakest.getFullName()
                        + " -> " + bestCandidate.getFullName()
                        + " in project " + project.getName());
            }
        }
    }

    private double evaluateEmployeePerformance(Employee e) {

        double score = 50;

        if (e instanceof Developer dev) {
            score += dev.getSkillCount() * 5;
        }

        if (e instanceof Manager manager) {
            score += manager.getTeamSize() * 2;
        }

        return Math.min(100, score);
    }

    public double getCompanyPerformanceIndex() {

        if (projects.isEmpty()) return 0;

        double total = 0;

        for (Project p : projects.values()) {
            total += p.calculateWeightedProgress();
        }

        return total / projects.size();
    }

    @Override
    public String toString() {
        return "Company{" +
                "name='" + name + '\'' +
                ", employees=" + employees.size() +
                ", projects=" + projects.size() +
                ", clients=" + clients.size() +
                ", performanceIndex=" + String.format("%.2f", getCompanyPerformanceIndex()) +
                '}';
    }

    public String getName() {
        return this.name;
    }
}