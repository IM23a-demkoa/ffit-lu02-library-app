package ch.bzz;

import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class LibraryAppMain {
    public static void main(String[] args) {
        Book one = new Book(1,"978-3-8362-9544-4","Java ist auch eine Insel","Christian Ullenboom",2023);
        Book two = new Book(2,"978-3-658-43573-8","Grundkurs Java","Dietmar Abts", 2024);
        List<Book> booklist = new ArrayList<>();
        booklist.add(one);
        booklist.add(two);

        String[] commands = {"help", "quit", "cool", "bye", "say"};
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String command = scanner.nextLine();

            if (command.equals("quit")) {
                break;
            } else if (command.equals("help")) {
                System.out.println(Arrays.toString(commands));
            } else if (command.equals("listBooks")) {
                booklist.forEach(System.out::println);
            } else {
                System.out.println("Eingabe nicht als Befehl erkannt" + command);
                System.out.println("mochtest du hinzufugen Yes/No");
                if (scanner.nextLine().equals("Yes")) {
                    System.out.println("Type new command");
                    String newString = scanner.nextLine();
                    String[] newArray = Arrays.copyOf(commands, commands.length + 1);
                    newArray[newArray.length - 1] = newString;
                    commands = newArray;
                } else {
                    break;
                }
            }
        }
    }

}
