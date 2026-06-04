import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class ProjectService extends AbstractJdbcService<Project> {
    private static final ProjectService INSTANTA = new ProjectService();

    private ProjectService() {
    }

    public static ProjectService getInstance() {
        return INSTANTA;
    }

    @Override
    public void creeaza(Project proiect) {
        scriere.executaInTranzactie(conexiune -> {
            scrieProiect(conexiune, proiect, true);
            rescrieRelatii(conexiune, proiect);
        });
        inregistreazaAudit("creeaza_proiect");
    }

    @Override
    public Optional<Project> citesteDupaId(int id) {
        Optional<Project> rezultat = citire.citesteUnu(
                "select id, name, description, start_date, budget, deadline_date, status, client_id, department_id from projects where id = ?",
                this::mapeazaProiect,
                id
        );
        inregistreazaAudit("citeste_proiect");
        return rezultat;
    }

    @Override
    public List<Project> citesteToate() {
        List<Project> rezultate = citire.citesteLista(
                "select id, name, description, start_date, budget, deadline_date, status, client_id, department_id from projects order by id",
                this::mapeazaProiect
        );
        inregistreazaAudit("citeste_proiecte");
        return rezultate;
    }

    @Override
    public void actualizeaza(Project proiect) {
        scriere.executaInTranzactie(conexiune -> {
            scrieProiect(conexiune, proiect, false);
            rescrieRelatii(conexiune, proiect);
        });
        inregistreazaAudit("actualizeaza_proiect");
    }

    @Override
    public void sterge(int id) {
        scriere.executaActualizare("delete from projects where id = ?", id);
        inregistreazaAudit("sterge_proiect");
    }

    private void scrieProiect(Connection conexiune, Project proiect, boolean creare) {
        Integer clientId = proiect.getClient() == null ? null : proiect.getClient().getId();
        Integer departmentId = proiect.getDepartment() == null ? null : proiect.getDepartment().getId();
        if (creare) {
            scriere.executaActualizare(
                    conexiune,
                    "insert into projects (id, name, description, start_date, budget, deadline_date, status, client_id, department_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    proiect.getId(),
                    proiect.getName(),
                    proiect.getDescription(),
                    proiect.getStartDate(),
                    proiect.getBudget(),
                    proiect.getDeadline(),
                    proiect.getStatus(),
                    clientId,
                    departmentId
            );
        } else {
            scriere.executaActualizare(
                    conexiune,
                    "update projects set name = ?, description = ?, start_date = ?, budget = ?, deadline_date = ?, status = ?, client_id = ?, department_id = ? where id = ?",
                    proiect.getName(),
                    proiect.getDescription(),
                    proiect.getStartDate(),
                    proiect.getBudget(),
                    proiect.getDeadline(),
                    proiect.getStatus(),
                    clientId,
                    departmentId,
                    proiect.getId()
            );
        }
    }

    private void rescrieRelatii(Connection conexiune, Project proiect) {
        scriere.executaActualizare(conexiune, "delete from project_required_skills where project_id = ?", proiect.getId());
        for (String skill : proiect.getRequiredSkills()) {
            scriere.executaActualizare(
                    conexiune,
                    "insert into project_required_skills (project_id, skill) values (?, ?)",
                    proiect.getId(),
                    skill
            );
        }

        scriere.executaActualizare(conexiune, "delete from project_members where project_id = ?", proiect.getId());
        for (Employee angajat : proiect.getEmployees()) {
            scriere.executaActualizare(
                    conexiune,
                    "insert into project_members (project_id, employee_id) values (?, ?)",
                    proiect.getId(),
                    angajat.getId()
            );
        }
    }

    private Project mapeazaProiect(ResultSet resultSet) throws SQLException {
        Department departament = null;
        int departmentId = resultSet.getInt("department_id");
        if (!resultSet.wasNull()) {
            departament = DepartmentsService.getInstance().citesteDupaId(departmentId).orElse(null);
        }

        Client client = null;
        int clientId = resultSet.getInt("client_id");
        if (!resultSet.wasNull()) {
            client = ClientService.getInstance().citesteDupaId(clientId).orElse(null);
        }

        Date startDate = resultSet.getDate("start_date");
        Date deadlineDate = resultSet.getDate("deadline_date");
        Project proiect = new Project(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                departament,
                client,
                resultSet.getDouble("budget"),
                deadlineDate == null ? null : deadlineDate.toLocalDate(),
                startDate == null ? null : startDate.toLocalDate(),
                ProjectStatus.valueOf(resultSet.getString("status"))
        );

        for (String skill : citesteSkilluriNecesare(proiect.getId())) {
            proiect.addRequiredSkill(skill);
        }
        for (Employee angajat : citesteMembri(proiect.getId())) {
            proiect.addMember(angajat);
        }
        for (Task task : TaskService.getInstance().citestePentruProiect(proiect.getId())) {
            proiect.addTask(task);
        }
        if (client != null) {
            client.registerProject(proiect);
        }
        return proiect;
    }

    private List<String> citesteSkilluriNecesare(int projectId) {
        return citire.citesteLista(
                "select skill from project_required_skills where project_id = ? order by skill",
                resultSet -> resultSet.getString("skill"),
                projectId
        );
    }

    private List<Employee> citesteMembri(int projectId) {
        return citire.citesteLista(
                "select employee_id from project_members where project_id = ? order by employee_id",
                resultSet -> EmployeeService.getInstance().citesteDupaId(resultSet.getInt("employee_id")).orElse(null),
                projectId
        ).stream().filter(angajat -> angajat != null).toList();
    }
}
