package ch.bzz;

import ch.bzz.db.BookPersistor;
import ch.bzz.db.UserPersistor;
import ch.bzz.model.User;
import ch.bzz.PasswordHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Properties;
import java.util.Scanner;

public class LibraryAppMain {

    private static final Logger log = LoggerFactory.getLogger(LibraryAppMain.class);

    public static void main(String[] args) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            log.error("Fehler beim Laden von config.properties", e);
            return;
        }

        BookPersistor bookPersistor = new BookPersistor(props);
        UserPersistor userPersistor = new UserPersistor(props);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equals("quit")) {
                System.out.println("Programm wird beendet...");
                running = false;
            } else if (input.equals("help")) {
                System.out.println("Verfügbare Befehle: help, quit, listBooks, createUser");
            } else if (input.startsWith("listBooks")) {
                // deine existierende listBooks-Logik
            } else if (input.startsWith("createUser")) {
                handleCreateUser(input, userPersistor);
            } else {
                System.out.println("Unbekannter Befehl: " + input);
            }
        }

        scanner.close();
    }

    private static void handleCreateUser(String input, UserPersistor userPersistor) {
        try {
            // Erwartet: createUser Max Mustermann 1990-05-21 max@example.com geheim123
            String[] parts = input.split(" ");
            if (parts.length < 6) {
                System.out.println("Fehler: Nutzung -> createUser <Vorname> <Nachname> <Geburtsdatum> <Email> <Passwort>");
                return;
            }

            String firstname = parts[1];
            String lastname = parts[2];
            LocalDate dob = LocalDate.parse(parts[3]);
            String email = parts[4];
            String password = parts[5];

            byte[] salt = PasswordHandler.generateSalt();
            byte[] hash = PasswordHandler.hashPassword(password, salt);
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            User user = new User(firstname, lastname, dob, email);
            user.setPasswordSalt(saltBase64);
            user.setPasswordHash(hashBase64);

            userPersistor.saveUser(user);

            System.out.println("Benutzer erfolgreich angelegt: " + firstname + " " + lastname);
            log.info("User {} {} angelegt", firstname, lastname);

        } catch (NoSuchAlgorithmException e) {
            log.error("Hash-Algorithmus nicht gefunden", e);
        } catch (Exception e) {
            log.error("Fehler beim Erstellen des Users", e);
        }
    }
}
