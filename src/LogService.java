import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class LogService {
    private static final LogService INSTANTA = new LogService();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final Path caleFisier;

    private LogService() {
        String cale = System.getenv("AUDIT_CSV_PATH");
        if (cale == null || cale.isBlank()) {
            cale = "audit.csv";
        }
        this.caleFisier = Path.of(cale);
    }

    public static LogService getInstance() {
        return INSTANTA;
    }

    public synchronized void inregistreaza(String numeActiune) {
        try {
            Path parinte = caleFisier.getParent();
            if (parinte != null) {
                Files.createDirectories(parinte);
            }
            boolean fisierNou = Files.notExists(caleFisier) || Files.size(caleFisier) == 0;
            try (BufferedWriter writer = Files.newBufferedWriter(
                    caleFisier,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            )) {
                if (fisierNou) {
                    writer.write("nume_actiune,timestamp");
                    writer.newLine();
                }
                writer.write(formateazaCsv(numeActiune));
                writer.write(",");
                writer.write(formateazaCsv(LocalDateTime.now().format(FORMATTER)));
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Scrierea auditului a esuat: " + exception.getMessage(), exception);
        }
    }

    private String formateazaCsv(String valoare) {
        String text = valoare == null ? "" : valoare;
        boolean necesitaGhilimele = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
        if (!necesitaGhilimele) {
            return text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
