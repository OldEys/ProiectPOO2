import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface Tranzaction {
    void executa(Connection conexiune) throws SQLException;
}
