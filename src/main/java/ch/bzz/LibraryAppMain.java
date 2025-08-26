package ch.bzz;

import java.util.Scanner;
import java.util.Arrays;

public class LibraryAppMain {

    public static void main(String[] args) {
        System.out.println("HelloWorld");
        String[] commands = {"help", "quit", "cool", "bye", "say"};
        Scanner scanner = new Scanner(System.in); // nur einmal erstellen

        while (true) {
            String command = scanner.nextLine();

            if (command.equals("quit")) {
                break;
            } else if (command.equals("help")) {
                System.out.println(Arrays.toString(commands));
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
