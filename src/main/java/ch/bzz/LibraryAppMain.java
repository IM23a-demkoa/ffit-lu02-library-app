package ch.bzz;

import java.io.*;
import java.sql.*;
import java.util.*;

public class LibraryAppMain {

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Konfigurationsdatei.");
            e.printStackTrace();
        }
        return props;
    }

    public static List<Book> fetchBooks() {
        List<Book> books = new ArrayList<>();
        Properties props = loadProperties();

        String url = props.getProperty("DB_URL");
        String user = props.getProperty("DB_USER");
        String password = props.getProperty("DB_PASSWORD");

        String query = "SELECT id, isbn, title, author, publication_year FROM books";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("id"),
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("publication_year")
                );
                books.add(book);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    // Neue Methode zum Einlesen von TSV-Dateien
    public static List<Book> importBooksFromTSV(String filePath) {
        List<Book> books = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length != 5) continue;
                Book book = new Book(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        parts[2],
                        parts[3],
                        Integer.parseInt(parts[4])
                );
                books.add(book);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Speichern der Bücher in der Datenbank (Upsert)
    public static void saveBooksToDatabase(List<Book> books) {
        Properties props = loadProperties();
        String url = props.getProperty("DB_URL");
        String user = props.getProperty("DB_USER");
        String password = props.getProperty("DB_PASSWORD");

        String query = "INSERT INTO books (id, isbn, title, author, publication_year) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE isbn = VALUES(isbn), title = VALUES(title), author = VALUES(author), publication_year = VALUES(publication_year)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            for (Book book : books) {
                pstmt.setInt(1, book.id);
                pstmt.setString(2, book.isbn);
                pstmt.setString(3, book.title);
                pstmt.setString(4, book.author);
                pstmt.setInt(5, book.year);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        List<Book> books = fetchBooks();
        String[] commands = {"help", "quit", "cool", "bye", "say", "listBooks", "importBooks"};
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            String[] parts = input.split(" ", 2); // Befehl + Argument
            String command = parts[0];

            if (command.equals("quit")) {
                break;
            } else if (command.equals("help")) {
                System.out.println(Arrays.toString(commands));
            } else if (command.equals("listBooks")) {
                books.forEach(System.out::println);
            } else if (command.equals("importBooks") && parts.length == 2) {
                String filePath = parts[1];
                List<Book> importedBooks = importBooksFromTSV(filePath);
                saveBooksToDatabase(importedBooks);
                System.out.println("Import abgeschlossen: " + importedBooks.size() + " Bücher importiert.");
                books = fetchBooks();
            } else {
                System.out.println("Eingabe nicht als Befehl erkannt: " + command);
                System.out.println("mochtest du hinzufugen Yes/No");
                if (scanner.nextLine().equals("Yes")) {
                    System.out.println("Type new command");
                    String newString = scanner.nextLine();
                    String[] newArray = Arrays.copyOf(commands, commands.length + 1);
                    newArray[newArray.length - 1] = newString;
                    commands = newArray;
                }
            }
        }
    }
}
