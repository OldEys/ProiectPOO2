public abstract class AbstractJdbcService<T> implements GenericServiceInterface<T> {
    protected final DBReadService citire = DBReadService.getInstance();
    protected final DBWriteService scriere = DBWriteService.getInstance();
    protected final LogService audit = LogService.getInstance();

    protected void inregistreazaAudit(String numeActiune) {
        audit.inregistreaza(numeActiune);
    }
}
