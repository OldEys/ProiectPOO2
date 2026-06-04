import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

final class SqlParams {
    private SqlParams() {
    }

    static void seteaza(PreparedStatement statement, Object... parametri) throws SQLException {
        for (int index = 0; index < parametri.length; index++) {
            Object valoare = parametri[index];
            int pozitie = index + 1;
            if (valoare instanceof LocalDate data) {
                statement.setDate(pozitie, Date.valueOf(data));
            } else if (valoare instanceof LocalDateTime dataOra) {
                statement.setTimestamp(pozitie, Timestamp.valueOf(dataOra));
            } else if (valoare instanceof Enum<?> enumerare) {
                statement.setString(pozitie, enumerare.name());
            } else {
                statement.setObject(pozitie, valoare);
            }
        }
    }
}
