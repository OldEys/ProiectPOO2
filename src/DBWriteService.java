import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class DBWriteService {
    private static final DBWriteService INSTANTA = new DBWriteService();

    private DBWriteService() {
    }

    public static DBWriteService getInstance() {
        return INSTANTA;
    }

    public int executaActualizare(String sql, Object... parametri) {
        try (Connection conexiune = DBConnection.getInstance().deschideConexiune()) {
            return executaActualizare(conexiune, sql, parametri);
        } catch (SQLException exception) {
            throw new IllegalStateException("Scrierea in baza de date a esuat: " + exception.getMessage(), exception);
        }
    }

    public int executaActualizare(Connection conexiune, String sql, Object... parametri) {
        try (PreparedStatement statement = conexiune.prepareStatement(sql)) {
            SqlParams.seteaza(statement, parametri);
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Scrierea in baza de date a esuat: " + exception.getMessage(), exception);
        }
    }

    public void executaInTranzactie(Tranzaction operatie) {
        try (Connection conexiune = DBConnection.getInstance().deschideConexiune()) {
            boolean autoCommitInitial = conexiune.getAutoCommit();
            conexiune.setAutoCommit(false);
            try {
                operatie.executa(conexiune);
                conexiune.commit();
            } catch (SQLException | RuntimeException exception) {
                conexiune.rollback();
                throw exception;
            } finally {
                conexiune.setAutoCommit(autoCommitInitial);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Tranzactia in baza de date a esuat: " + exception.getMessage(), exception);
        }
    }
}
