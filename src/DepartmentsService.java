import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class DepartmentsService extends AbstractJdbcService<Department> {
    private static final DepartmentsService INSTANTA = new DepartmentsService();

    private DepartmentsService() {
    }

    public static DepartmentsService getInstance() {
        return INSTANTA;
    }

    @Override
    public void creeaza(Department departament) {
        scriere.executaActualizare(
                "insert into departments (id, name, location, manager_id) values (?, ?, ?, ?)",
                departament.getId(),
                departament.getName(),
                departament.getLocation(),
                departament.getManager() == null ? null : departament.getManager().getId()
        );
        inregistreazaAudit("creeaza_departament");
    }

    @Override
    public Optional<Department> citesteDupaId(int id) {
        Optional<Department> rezultat = citire.citesteUnu(
                "select id, name, location, manager_id from departments where id = ?",
                this::mapeazaDepartament,
                id
        );
        inregistreazaAudit("citeste_departament");
        return rezultat;
    }

    @Override
    public List<Department> citesteToate() {
        List<Department> rezultate = citire.citesteLista(
                "select id, name, location, manager_id from departments order by id",
                this::mapeazaDepartament
        );
        inregistreazaAudit("citeste_departamente");
        return rezultate;
    }

    @Override
    public void actualizeaza(Department departament) {
        scriere.executaActualizare(
                "update departments set name = ?, location = ?, manager_id = ? where id = ?",
                departament.getName(),
                departament.getLocation(),
                departament.getManager() == null ? null : departament.getManager().getId(),
                departament.getId()
        );
        inregistreazaAudit("actualizeaza_departament");
    }

    @Override
    public void sterge(int id) {
        scriere.executaActualizare("delete from departments where id = ?", id);
        inregistreazaAudit("sterge_departament");
    }

    public Optional<Integer> citesteManagerId(int id) {
        return citire.citesteUnu(
                "select manager_id from departments where id = ? and manager_id is not null",
                resultSet -> resultSet.getInt("manager_id"),
                id
        );
    }

    private Department mapeazaDepartament(ResultSet resultSet) throws SQLException {
        return new Department(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("location")
        );
    }
}
