import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DBReadService {
    private static final DBReadService INSTANTA = new DBReadService();

    private DBReadService() {
    }

    public static DBReadService getInstance() {
        return INSTANTA;
    }

    public <T> Optional<T> citesteUnu(String sql, RowMapper<T> mapper, Object... parametri) {
        List<T> rezultate = citesteLista(sql, mapper, parametri);
        if (rezultate.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rezultate.get(0));
    }

    public <T> List<T> citesteLista(String sql, RowMapper<T> mapper, Object... parametri) {
        try (Connection conexiune = DBConnection.getInstance().deschideConexiune()) {
            return citesteLista(conexiune, sql, mapper, parametri);
        } catch (SQLException exception) {
            throw new IllegalStateException("Citirea din baza de date a esuat: " + exception.getMessage(), exception);
        }
    }

    public <T> List<T> citesteLista(Connection conexiune, String sql, RowMapper<T> mapper, Object... parametri) {
        try (PreparedStatement statement = conexiune.prepareStatement(sql)) {
            SqlParams.seteaza(statement, parametri);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> rezultate = new ArrayList<>();
                while (resultSet.next()) {
                    rezultate.add(mapper.map(resultSet));
                }
                return rezultate;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Citirea din baza de date a esuat: " + exception.getMessage(), exception);
        }
    }
}
