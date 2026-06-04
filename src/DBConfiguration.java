public final class DBConfiguration {
    private final String url;
    private final String utilizator;
    private final String parola;

    private DBConfiguration() {
        this.url = citesteVariabila("DB_URL", "jdbc:postgresql://localhost:5432/proiect_poo2");
        this.utilizator = citesteVariabila("DB_USER", "proiect_user");
        this.parola = citesteVariabila("DB_PASSWORD", "proiect_password");
    }

    public static DBConfiguration incarca() {
        return new DBConfiguration();
    }

    private static String citesteVariabila(String nume, String valoareImplicita) {
        String valoare = System.getenv(nume);
        if (valoare == null || valoare.isBlank()) {
            return valoareImplicita;
        }
        return valoare.trim();
    }

    public String getUrl() {
        return url;
    }

    public String getUtilizator() {
        return utilizator;
    }

    public String getParola() {
        return parola;
    }
}
