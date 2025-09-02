package ch.bzz;

import java.io.FileInputStream;
import java.io.IOException;
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

    // JDBC-Methode in separater Klasse oder inner class
    public static List<Book> fetchBooks() {
        List<Book> books = new ArrayList<>();
        Properties props = loadProperties();   // ⬅️ hier die Properties laden

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

    public static void main(String[] args) {
        List<Book> books = fetchBooks();
        String[] commands = {"help", "quit", "cool", "bye", "say"};
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String command = scanner.nextLine();

            if (command.equals("quit")) {
                break;
            } else if (command.equals("help")) {
                System.out.println(Arrays.toString(commands));
            } else if (command.equals("listBooks")) {
                books.forEach(System.out::println);
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
