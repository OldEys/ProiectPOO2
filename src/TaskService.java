import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class TaskService extends AbstractJdbcService<Task> {
    private static final TaskService INSTANTA = new TaskService();

    private TaskService() {
    }

    public static TaskService getInstance() {
        return INSTANTA;
    }

    @Override
    public void creeaza(Task task) {
        valideazaProiect(task);
        scriere.executaActualizare(
                "insert into tasks (id, project_id, title, description, completed, created_at, completed_at, due_date, employee_id, status, difficulty) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                task.getId(),
                task.getProjectId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getCompletedAt(),
                task.getDueDate(),
                task.getEmployee() == null ? null : task.getEmployee().getId(),
                task.getStatus(),
                task.getDifficulty()
        );
        inregistreazaAudit("creeaza_task");
    }

    @Override
    public Optional<Task> citesteDupaId(int id) {
        Optional<Task> rezultat = citire.citesteUnu(
                "select id, project_id, title, description, completed, created_at, completed_at, due_date, employee_id, status, difficulty from tasks where id = ?",
                this::mapeazaTask,
                id
        );
        inregistreazaAudit("citeste_task");
        return rezultat;
    }

    @Override
    public List<Task> citesteToate() {
        List<Task> rezultate = citire.citesteLista(
                "select id, project_id, title, description, completed, created_at, completed_at, due_date, employee_id, status, difficulty from tasks order by id",
                this::mapeazaTask
        );
        inregistreazaAudit("citeste_taskuri");
        return rezultate;
    }

    public List<Task> citestePentruProiect(int projectId) {
        List<Task> rezultate = citire.citesteLista(
                "select id, project_id, title, description, completed, created_at, completed_at, due_date, employee_id, status, difficulty from tasks where project_id = ? order by id",
                this::mapeazaTask,
                projectId
        );
        inregistreazaAudit("citeste_taskuri_proiect");
        return rezultate;
    }

    @Override
    public void actualizeaza(Task task) {
        valideazaProiect(task);
        scriere.executaActualizare(
                "update tasks set project_id = ?, title = ?, description = ?, completed = ?, created_at = ?, completed_at = ?, due_date = ?, employee_id = ?, status = ?, difficulty = ? where id = ?",
                task.getProjectId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getCompletedAt(),
                task.getDueDate(),
                task.getEmployee() == null ? null : task.getEmployee().getId(),
                task.getStatus(),
                task.getDifficulty(),
                task.getId()
        );
        inregistreazaAudit("actualizeaza_task");
    }

    @Override
    public void sterge(int id) {
        scriere.executaActualizare("delete from tasks where id = ?", id);
        inregistreazaAudit("sterge_task");
    }

    private void valideazaProiect(Task task) {
        if (task.getProjectId() == null) {
            throw new IllegalArgumentException("Task-ul trebuie sa apartina unui proiect inainte de persistenta");
        }
    }

    private Task mapeazaTask(ResultSet resultSet) throws SQLException {
        Date createdAt = resultSet.getDate("created_at");
        Date completedAt = resultSet.getDate("completed_at");
        Date dueDate = resultSet.getDate("due_date");
        Task task = new Task(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                dueDate == null ? null : dueDate.toLocalDate(),
                resultSet.getInt("difficulty"),
                TaskStatus.valueOf(resultSet.getString("status")),
                resultSet.getBoolean("completed"),
                createdAt == null ? null : createdAt.toLocalDate(),
                completedAt == null ? null : completedAt.toLocalDate(),
                resultSet.getInt("project_id")
        );

        int employeeId = resultSet.getInt("employee_id");
        if (!resultSet.wasNull()) {
            task.setEmployee(EmployeeService.getInstance().citesteDupaId(employeeId).orElse(null));
        }
        return task;
    }
}
