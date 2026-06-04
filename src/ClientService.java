import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class ClientService extends AbstractJdbcService<Client> {
    private static final ClientService INSTANTA = new ClientService();

    private ClientService() {
    }

    public static ClientService getInstance() {
        return INSTANTA;
    }

    @Override
    public void creeaza(Client client) {
        scriere.executaActualizare(
                "insert into clients (id, name, email, industry) values (?, ?, ?, ?)",
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getIndustry()
        );
        inregistreazaAudit("creeaza_client");
    }

    @Override
    public Optional<Client> citesteDupaId(int id) {
        Optional<Client> rezultat = citire.citesteUnu(
                "select id, name, email, industry from clients where id = ?",
                this::mapeazaClient,
                id
        );
        inregistreazaAudit("citeste_client");
        return rezultat;
    }

    @Override
    public List<Client> citesteToate() {
        List<Client> rezultate = citire.citesteLista(
                "select id, name, email, industry from clients order by id",
                this::mapeazaClient
        );
        inregistreazaAudit("citeste_clienti");
        return rezultate;
    }

    @Override
    public void actualizeaza(Client client) {
        scriere.executaActualizare(
                "update clients set name = ?, email = ?, industry = ? where id = ?",
                client.getName(),
                client.getEmail(),
                client.getIndustry(),
                client.getId()
        );
        inregistreazaAudit("actualizeaza_client");
    }

    @Override
    public void sterge(int id) {
        scriere.executaActualizare("delete from clients where id = ?", id);
        inregistreazaAudit("sterge_client");
    }

    private Client mapeazaClient(ResultSet resultSet) throws SQLException {
        return new Client(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("industry")
        );
    }
}
