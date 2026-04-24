import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Client {
    private final int id;
    private String name;
    private String email;
    private String industry;

    private final Set<Project> projects = new LinkedHashSet<>();

    public Client(int id, String name, String email, String industry) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid client ID");
        }
        this.id = id;
        this.name = name;
        this.email = email;
        this.industry = industry;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getIndustry() {
        return industry;
    }

    public Set<Project> getProjects() {
        return Collections.unmodifiableSet(projects);
    }

    public void updateContact(String newName, String newEmail) {
        if (newName != null && !newName.isBlank()) {
            this.name = newName.trim();
        }
        if (newEmail != null && !newEmail.isBlank()) {
            this.email = newEmail.trim();
        }
    }

    public void changeCompany(String newCompanyName) {
        if (newCompanyName != null && !newCompanyName.isBlank()) {
            this.name = newCompanyName.trim();
        }
    }

    public void addProject(Project project) {
        if (project != null) {
            projects.add(project);
        }
    }

    public void removeProject(Project project) {
        projects.remove(project);
    }

    public int getActiveProjectsCount() {
        int count = 0;
        for (Project project : projects) {
            if (project.getStatus() != ProjectStatus.COMPLETED
                    && project.getStatus() != ProjectStatus.CANCELLED) {
                count++;
            }
        }
        return count;
    }

    public int getCompletedProjectsCount() {
        int count = 0;
        for (Project project : projects) {
            if (project.getStatus() == ProjectStatus.COMPLETED) {
                count++;
            }
        }
        return count;
    }

    public int getRiskyProjectsCount() {
        int count = 0;
        for (Project project : projects) {
            if (project.isOverdue()) {
                count++;
            }
        }
        return count;
    }

    public double getTotalPortfolioBudget() {
        double total = 0.0;
        for (Project project : projects) {
            total += project.getBudget();
        }
        return total;
    }

    public double calculateAccountHealthScore() {
        if (projects.isEmpty()) {
            return 0.0;
        }

        double score = 50.0;
        double progressSum = 0.0;
        int completedProjects = 0;
        int activeProjects = 0;
        int riskyProjects = 0;

        for (Project project : projects) {
            double progress = project.calculateWeightedProgress();
            progressSum += progress;

            if (project.getStatus() == ProjectStatus.COMPLETED) {
                completedProjects++;
                score += 8;
            } else if (project.getStatus() == ProjectStatus.ACTIVE) {
                activeProjects++;
                score += 2;
            } else if (project.getStatus() == ProjectStatus.ON_HOLD) {
                score -= 3;
            }

            if (project.isOverdue()) {
                riskyProjects++;
                score -= 10;
            }

            long blockedTasks = project.getTasks().values().stream()
                    .filter(t -> t.getStatus() == TaskStatus.BLOCKED)
                    .count();

            long lateTasks = project.getTasks().values().stream()
                    .filter(t -> t.isOverdue())
                    .count();

            score -= blockedTasks * 2.5;
            score -= lateTasks * 1.5;

            if (progress >= 90) {
                score += 6;
            } else if (progress >= 70) {
                score += 3;
            } else if (progress < 30) {
                score -= 4;
            }

            score += Math.min(project.getEmployees().size(), 6);
        }

        double avgProgress = progressSum / projects.size();

        score += avgProgress * 0.20;
        score += Math.min(getTotalPortfolioBudget() / 100000.0, 10.0);
        score += completedProjects * 4;
        score += activeProjects * 1.5;
        score -= riskyProjects * 5;

        return Math.max(0.0, Math.min(100.0, score));
    }

    public String getAccountTier() {
        double score = calculateAccountHealthScore();

        if (score >= 85) {
            return "PLATINUM";
        }
        if (score >= 70) {
            return "GOLD";
        }
        if (score >= 50) {
            return "SILVER";
        }
        return "BRONZE";
    }

    @Override
    public String toString() {
        return "Client{id=" + id +
                ", name='" + name + '\'' +
                ", industry='" + industry + '\'' +
                ", projects=" + projects.size() +
                ", healthScore=" + String.format("%.2f", calculateAccountHealthScore()) +
                ", tier=" + getAccountTier() +
                '}';
    }

    public void registerProject(Project project) {

    }
}