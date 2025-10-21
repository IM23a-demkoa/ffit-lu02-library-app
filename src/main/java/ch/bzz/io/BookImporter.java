package ch.bzz.io;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ch.bzz.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class BookImporter {

    private static final Logger log = LoggerFactory.getLogger(BookImporter.class);

    private final String url;
    private final String user;
    private final String password;

    public BookImporter() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Laden der config.properties", e);
        }
        this.url = props.getProperty("DB_URL");
        this.user = props.getProperty("DB_USER");
        this.password = props.getProperty("DB_PASSWORD");
    }

    /**
     * Liest eine TSV-Datei ein und speichert die Bücher direkt in die Datenbank.
     */
    public void importFromTSV(String filePath) {
        List<Book> books = readFromTSV(filePath);
        saveBooksToDB(books);
        log.info("Import abgeschlossen: {} Bücher aus Datei {}", books.size(), filePath);
    }

    private List<Book> readFromTSV(String filePath) {
        List<Book> books = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] tokens = line.split("\t", -1);
                if (tokens.length < 5) continue;

                try {
                    int id = Integer.parseInt(tokens[0]);
                    String isbn = tokens[1];
                    String title = tokens[2];
                    String author = tokens[3];
                    int year = Integer.parseInt(tokens[4]);

                    books.add(new Book(id, isbn, title, author, year));
                } catch (NumberFormatException e) {
                    log.warn("Ungültige Zahl in Zeile: {} - {}", line, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Fehler beim Einlesen der Datei: {}", filePath, e);
        }
        return books;
    }

    private void saveBooksToDB(List<Book> books) {
        String sql = "INSERT INTO books (id, isbn, title, author, publication_year) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "isbn = EXCLUDED.isbn, " +
                "title = EXCLUDED.title, " +
                "author = EXCLUDED.author, " +
                "publication_year = EXCLUDED.publication_year";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Book book : books) {
                stmt.setInt(1, book.getId());
                stmt.setString(2, book.getIsbn());
                stmt.setString(3, book.getTitle());
                stmt.setString(4, book.getAuthor());
                stmt.setInt(5, book.getYear());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            log.error("Fehler beim Speichern der Bücher in der Datenbank", e);
        }
    }
}


