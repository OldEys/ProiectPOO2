import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final DBConnection INSTANTA = new DBConnection();
    private final DBConfiguration configuratie;

    private DBConnection() {
        this.configuratie = DBConfiguration.incarca();
    }

    public static DBConnection getInstance() {
        return INSTANTA;
    }

    public Connection deschideConexiune() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(
                    configuratie.getUrl(),
                    configuratie.getUtilizator(),
                    configuratie.getParola()
            );
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Driverul JDBC pentru PostgreSQL nu este disponibil.", exception);
        } catch (SQLException exception) {
            throw new IllegalStateException("Conexiunea la baza de date a esuat: " + exception.getMessage(), exception);
        }
    }
}
