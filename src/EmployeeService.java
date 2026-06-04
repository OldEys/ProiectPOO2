import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class EmployeeService extends AbstractJdbcService<Employee> {
    private static final EmployeeService INSTANTA = new EmployeeService();

    private EmployeeService() {
    }

    public static EmployeeService getInstance() {
        return INSTANTA;
    }

    @Override
    public void creeaza(Employee angajat) {
        scriere.executaInTranzactie(conexiune -> {
            scrieAngajat(conexiune, angajat, true);
            rescrieSkilluri(conexiune, angajat);
        });
        inregistreazaAudit("creeaza_angajat");
    }

    @Override
    public Optional<Employee> citesteDupaId(int id) {
        Optional<Employee> rezultat = citire.citesteUnu(
                "select id, full_name, email, base_salary, hire_date, department_id, role, team_size, bonus from employees where id = ?",
                this::mapeazaAngajat,
                id
        );
        inregistreazaAudit("citeste_angajat");
        return rezultat;
    }

    @Override
    public List<Employee> citesteToate() {
        List<Employee> rezultate = citire.citesteLista(
                "select id, full_name, email, base_salary, hire_date, department_id, role, team_size, bonus from employees order by id",
                this::mapeazaAngajat
        );
        inregistreazaAudit("citeste_angajati");
        return rezultate;
    }

    @Override
    public void actualizeaza(Employee angajat) {
        scriere.executaInTranzactie(conexiune -> {
            scrieAngajat(conexiune, angajat, false);
            rescrieSkilluri(conexiune, angajat);
        });
        inregistreazaAudit("actualizeaza_angajat");
    }

    @Override
    public void sterge(int id) {
        scriere.executaActualizare("delete from employees where id = ?", id);
        inregistreazaAudit("sterge_angajat");
    }

    private void scrieAngajat(Connection conexiune, Employee angajat, boolean creare) {
        Integer departmentId = angajat.getDepartment() == null ? null : angajat.getDepartment().getId();
        int teamSize = angajat instanceof Manager manager ? manager.getTeamSize() : 0;
        double bonus = angajat instanceof Manager manager ? manager.getBonus() : 0.0;
        if (creare) {
            scriere.executaActualizare(
                    conexiune,
                    "insert into employees (id, full_name, email, base_salary, hire_date, department_id, role, team_size, bonus) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    angajat.getId(),
                    angajat.getFullName(),
                    angajat.getEmail(),
                    angajat.getBaseSalary(),
                    angajat.getHireDate(),
                    departmentId,
                    angajat.getRole(),
                    teamSize,
                    bonus
            );
        } else {
            scriere.executaActualizare(
                    conexiune,
                    "update employees set full_name = ?, email = ?, base_salary = ?, hire_date = ?, department_id = ?, role = ?, team_size = ?, bonus = ? where id = ?",
                    angajat.getFullName(),
                    angajat.getEmail(),
                    angajat.getBaseSalary(),
                    angajat.getHireDate(),
                    departmentId,
                    angajat.getRole(),
                    teamSize,
                    bonus,
                    angajat.getId()
            );
        }
    }

    private void rescrieSkilluri(Connection conexiune, Employee angajat) {
        scriere.executaActualizare(conexiune, "delete from employee_skills where employee_id = ?", angajat.getId());
        if (angajat instanceof Developer developer) {
            for (String skill : developer.getSkillSet()) {
                scriere.executaActualizare(
                        conexiune,
                        "insert into employee_skills (employee_id, skill) values (?, ?)",
                        angajat.getId(),
                        skill
                );
            }
        }
    }

    private Employee mapeazaAngajat(ResultSet resultSet) throws SQLException {
        Department departament = null;
        int departmentId = resultSet.getInt("department_id");
        if (!resultSet.wasNull()) {
            departament = DepartmentsService.getInstance().citesteDupaId(departmentId).orElse(null);
        }

        int id = resultSet.getInt("id");
        String rol = resultSet.getString("role");
        Employee angajat;
        if ("Manager".equals(rol)) {
            angajat = new Manager(
                    id,
                    resultSet.getString("full_name"),
                    resultSet.getString("email"),
                    resultSet.getDouble("base_salary"),
                    departament,
                    resultSet.getInt("team_size"),
                    resultSet.getDouble("bonus")
            );
        } else if ("Developer".equals(rol)) {
            Developer developer = new Developer(
                    id,
                    resultSet.getString("full_name"),
                    resultSet.getString("email"),
                    resultSet.getDouble("base_salary"),
                    departament
            );
            for (String skill : citesteSkilluri(id)) {
                developer.learnSkill(skill);
            }
            angajat = developer;
        } else {
            throw new IllegalStateException("Rol de angajat necunoscut: " + rol);
        }

        Date dataAngajare = resultSet.getDate("hire_date");
        if (dataAngajare != null) {
            angajat.setHireDate(dataAngajare.toLocalDate());
        }
        return angajat;
    }

    private List<String> citesteSkilluri(int employeeId) {
        return citire.citesteLista(
                "select skill from employee_skills where employee_id = ? order by skill",
                resultSet -> resultSet.getString("skill"),
                employeeId
        );
    }
}
