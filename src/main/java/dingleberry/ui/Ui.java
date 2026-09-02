package dingleberry.ui;

import java.util.Scanner;

import dingleberry.model.Task;
import dingleberry.model.TaskList;

/**
 * Handles all interaction with the user: reading raw command lines from
 * standard input and printing all messages (banners, task updates, and
 * errors) to standard output.
 */
public class Ui {
    /** Separates consecutive output blocks. */
    private static final String SEPARATOR =
            "____________________________________________________________";

    /** Reads input from the standard input stream. */
    private final Scanner scanner;

    /** Creates a UI bound to standard input. */
    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    /** Prints the startup banner and greeting. */
    public void showWelcome() {
        final String banner = String.join("\n",
            "____________________________________________________________",
            " ____  _             _      _                          ",
            "|  _ \\(_)_ __   __ _| | ___| |__   ___ _ __ _ __ _   " + "_   _ ",
            "| | | | | '_ \\ / _` | |/ _ \\ '_ \\ / _ \\ '__| '__| | | |",
            "| |_| | | | | | | (_| | |  __/ |_) |  __/ |  | |  | |_| |",
            "|____/|_|_| |_|\\__, |_|\\___|_.__/ \\___|_|  |_|   \\__, |",
            "               |___/                              |___|",
            "",
            "Hey There! I'm Dingleberry",
            "What can I do for you?",
            "____________________________________________________________",
            "");
        System.out.println(banner);
    }

    /**
     * Returns whether there is another line of input to read.
     *
     * @return true when an input line is available.
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next raw command line typed by the user.
     *
     * @return the next user input line.
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Prints the horizontal separator line used to frame each message block.
     */
    public void showLine() {
        System.out.println(SEPARATOR);
    }

    /**
     * Prints the current task list, numbered from 1.
     *
     * @param tasks the tasks to display.
     */
    public void showTaskList(final TaskList tasks) {
        showLine();
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf("%d.%s\n", i + 1, tasks.get(i));
        }
        showLine();
    }

    /**
     * Confirms that a task was added and reports the new list size.
     *
     * @param task the added task.
     * @param totalTaskCount the task count after the addition.
     */
    public void showTaskAdded(final Task task, final int totalTaskCount) {
        showLine();
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + totalTaskCount
            + " tasks in the list.");
        showLine();
    }

    /**
     * Confirms that a task was deleted and reports the new list size.
     *
     * @param task the deleted task.
     * @param totalTaskCount the task count after the deletion.
     */
    public void showTaskDeleted(final Task task, final int totalTaskCount) {
        showLine();
        System.out.println("Noted. I've removed this task: " + task);
        System.out.println("Now you have " + totalTaskCount
            + " tasks in the list");
        showLine();
    }

    /**
     * Reports an unrecognized command to the user.
     *
     * @param message the explanation of the invalid command.
     */
    public void showWrongCommandError(final String message) {
        showLine();
        System.out.println(
            "Oops, Dingleberry doesn't know that command: " + message);
        System.out.println("Please try again with the correct command and"
            + " parameters.");
        showLine();
    }

    /**
     * Reports a recognized command with invalid or missing parameters.
     *
     * @param message the explanation of the invalid parameters.
     */
    public void showIncorrectParametersError(final String message) {
        showLine();
        System.out.println(
            "Oops, Dingleberry found incorrect parameters: " + message);
        System.out.println("Please try again with the correct command and"
            + " parameters.");
        showLine();
    }

    /**
     * Reports that the saved task list could not be loaded from disk.
     *
     * @param message the reason loading failed.
     */
    public void showLoadingError(final String message) {
        showLine();
        System.out.println("Couldn't load saved tasks (" + message + ")."
            + " Starting with an empty list.");
        showLine();
    }

    /**
     * Reports that the task list could not be saved to disk.
     *
     * @param message the reason saving failed.
     */
    public void showSavingError(final String message) {
        showLine();
        System.out.println("Couldn't save tasks to disk (" + message + ").");
        showLine();
    }

    /** Prints the farewell message shown when the program ends. */
    public void showGoodbye() {
        System.out.println("Bya hope to see your berries again!");
    }

    /** Releases the input scanner's resources. */
    public void close() {
        scanner.close();
    }
}
