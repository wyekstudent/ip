package dingleberry.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import dingleberry.model.Deadlines;
import dingleberry.model.Events;
import dingleberry.model.Task;
import dingleberry.model.TaskList;
import dingleberry.model.ToDos;

/**
 * Loads and saves the task list to a plain-text file at a relative path, using
 * one pipe-delimited line per task (e.g. "T | 0 | read book"). Creates the
 * containing folder and file automatically if they don't exist yet.
 */
public class Storage {
    private final Path filePath;

    public Storage(String relativeFilePath) {
        this.filePath = Path.of(relativeFilePath);
    }

    /**
     * Reads the data file into a task list, creating an empty file (and its
     * parent folder) first if it doesn't already exist. Lines that don't match
     * the expected format are skipped with a warning rather than aborting load.
     */
    public ArrayList<Task> load() throws IOException {
        ArrayList<Task> tasks = new ArrayList<>();

        File parentDir = filePath.toFile().getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
            return tasks;
        }

        List<String> lines = Files.readAllLines(filePath);
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            Task task = parseLine(line);
            if (task == null) {
                System.err.println("Skipping unreadable line in data file: " + line);
                continue;
            }
            tasks.add(task);
        }
        return tasks;
    }

    /**
     * Overwrites the data file with the current task list, one task per line.
     */
    public void save(TaskList tasks) throws IOException {
        File parentDir = filePath.toFile().getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            for (int i = 0; i < tasks.size(); i++) {
                writer.write(tasks.get(i).toSaveFormat());
                writer.newLine();
            }
        }
    }

    private Task parseLine(String line) {
        String[] parts = line.split("\\s*\\|\\s*");
        try {
            String type = parts[0];
            boolean isDone = parts[1].equals("1");
            String description = parts[2];

            Task task;
            switch (type) {
            case "T":
                task = new ToDos(description);
                break;
            case "D":
                task = new Deadlines(description, LocalDateTime.parse(parts[3], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                break;
            case "E":
                task = new Events(description,
                        LocalDateTime.parse(parts[3], DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        LocalDateTime.parse(parts[4], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                break;
            default:
                return null;
            }
            if (isDone) {
                task.markAsDone();
            }
            return task;
        } catch (ArrayIndexOutOfBoundsException | DateTimeParseException e) {
            return null;
        }
    }
}
