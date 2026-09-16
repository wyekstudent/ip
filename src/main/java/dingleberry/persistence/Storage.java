package dingleberry.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import dingleberry.model.Deadlines;
import dingleberry.model.Events;
import dingleberry.model.Task;
import dingleberry.model.TaskList;
import dingleberry.model.Todo;

/**
 * Loads and saves the task list to a plain-text file at a relative path, using
 * one pipe-delimited line per task (e.g. "T | 0 | read book"). Creates the
 * containing folder and file automatically if they don't exist yet.
 */
public class Storage {
    /** Identifies the description field in persisted task records. */
    private static final int DESCRIPTION_INDEX = 2;
    /** Identifies the first date/time field in persisted task records. */
    private static final int FIRST_DATE_TIME_INDEX = 3;
    /** Identifies the second date/time field in persisted event records. */
    private static final int SECOND_DATE_TIME_INDEX = 4;
    /** Defines the strict format used for persisted date/time values. */
    private static final DateTimeFormatter STORAGE_DATE_TIME_FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    /** Defines the UTF-8 escape used for a newline in a description. */
    private static final char NEWLINE_ESCAPE = 'n';
    /** Defines the UTF-8 escape used for a carriage return in a description. */
    private static final char CARRIAGE_RETURN_ESCAPE = 'r';

    /** Stores the data file path. */
    private final Path filePath;

    /**
     * Creates storage backed by the given relative file path.
     *
     * @param relativeFilePath the path used for persisted task data.
     */
    public Storage(final String relativeFilePath) {
        try {
            this.filePath = Path.of(relativeFilePath);
        } catch (InvalidPathException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Invalid storage path: " + relativeFilePath, e);
        }
    }

    /**
     * Reads the data file into a task list, creating an empty file (and its
     * parent folder) first if it doesn't already exist. Lines that don't match
      * the expected format are skipped with a warning rather than aborting
      * load.
      *
      * @return the tasks loaded from disk.
     */
    public ArrayList<Task> load() throws IOException {
        ArrayList<Task> tasks = new ArrayList<>();
        ensureParentDirectory();

        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw storageException("create storage file", e);
            }
            return tasks;
        }

        final List<String> lines;
        try {
            lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw storageException("load tasks", e);
        }
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            try {
                Task task = parseLine(line);
                tasks.add(task);
            } catch (IllegalArgumentException e) {
                System.err.println("Skipping corrupted task record: " + line
                        + " (" + e.getMessage() + ")");
                continue;
            }
        }
        return tasks;
    }

    /**
      * Overwrites the data file with the current task list, one task per line.
      *
      * @param tasks the task list to write to disk.
     */
    public void save(final TaskList tasks) throws IOException {
        ensureParentDirectory();
        Path absoluteFilePath = filePath.toAbsolutePath();
        Path temporaryFile;
        try {
            temporaryFile = Files.createTempFile(
                    absoluteFilePath.getParent(), ".dingleberry-", ".tmp");
        } catch (IOException e) {
            throw storageException("create temporary storage file", e);
        }

        try (var writer = Files.newBufferedWriter(temporaryFile,
                StandardCharsets.UTF_8, StandardOpenOption.WRITE)) {
            for (int i = 0; i < tasks.size(); i++) {
                writer.write(tasks.get(i).toSaveFormat());
                writer.newLine();
            }
        } catch (IOException | RuntimeException e) {
            deleteTemporaryFile(temporaryFile);
            if (e instanceof IOException ioException) {
                throw storageException("save tasks", ioException);
            }
            throw e;
        }

        try {
            moveTemporaryFile(temporaryFile, absoluteFilePath);
        } catch (IOException e) {
            deleteTemporaryFile(temporaryFile);
            throw storageException("replace storage file", e);
        } finally {
            deleteTemporaryFile(temporaryFile);
        }
    }

    private void ensureParentDirectory() throws IOException {
        Path parent = filePath.toAbsolutePath().getParent();
        if (parent == null) {
            return;
        }
        if (Files.exists(parent) && !Files.isDirectory(parent)) {
            throw new IOException("Storage parent path is not a directory: "
                    + parent);
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw storageException("create storage directory " + parent, e);
        }
    }

    private Task parseLine(final String line) {
        List<String> parts = splitEscapedFields(line);
        String type = requireField(parts, 0, "task type");
        String completionFlag = requireField(parts, 1, "completion flag");
        String description = requireField(parts, DESCRIPTION_INDEX,
                "description");
        boolean isDone;
        if ("0".equals(completionFlag)) {
            isDone = false;
        } else if ("1".equals(completionFlag)) {
            isDone = true;
        } else {
            throw new IllegalArgumentException("invalid completion flag");
        }

        Task task;
        switch (type) {
        case "T":
            requireFieldCount(parts, DESCRIPTION_INDEX + 1);
            task = new Todo(description);
            break;
        case "D":
            requireFieldCount(parts, FIRST_DATE_TIME_INDEX + 1);
            task = new Deadlines(description, parseDateTime(
                    requireField(parts, FIRST_DATE_TIME_INDEX, "deadline")));
            break;
        case "E":
            requireFieldCount(parts, SECOND_DATE_TIME_INDEX + 1);
            LocalDateTime from = parseDateTime(requireField(
                    parts, FIRST_DATE_TIME_INDEX, "event start"));
            LocalDateTime to = parseDateTime(requireField(
                    parts, SECOND_DATE_TIME_INDEX, "event end"));
            if (!to.isAfter(from)) {
                throw new IllegalArgumentException(
                        "event end must be after event start");
            }
            task = new Events(description, from, to);
            break;
        default:
            throw new IllegalArgumentException("unknown task type");
        }
        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    private static String requireField(final List<String> fields,
                                       final int index,
                                       final String fieldName) {
        if (index >= fields.size()) {
            throw new IllegalArgumentException("missing " + fieldName);
        }
        String value = fields.get(index).trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("blank " + fieldName);
        }
        return value;
    }

    private static void requireFieldCount(final List<String> fields,
                                          final int expectedCount) {
        if (fields.size() != expectedCount) {
            throw new IllegalArgumentException("expected " + expectedCount
                    + " fields but found " + fields.size());
        }
    }

    private static LocalDateTime parseDateTime(final String value) {
        try {
            return LocalDateTime.parse(value, STORAGE_DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid date/time", e);
        }
    }

    private static List<String> splitEscapedFields(final String line) {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '\\' && i + 1 < line.length()) {
                char escaped = line.charAt(++i);
                if (escaped == '|' || escaped == '\\') {
                    field.append(escaped);
                } else if (escaped == NEWLINE_ESCAPE) {
                    field.append('\n');
                } else if (escaped == CARRIAGE_RETURN_ESCAPE) {
                    field.append('\r');
                } else {
                    field.append('\\').append(escaped);
                }
            } else if (character == '|') {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }
        fields.add(field.toString().trim());
        return fields;
    }

    private void moveTemporaryFile(final Path temporaryFile,
                                   final Path destination) throws IOException {
        try {
            Files.move(temporaryFile, destination,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporaryFile, destination,
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteTemporaryFile(final Path temporaryFile) {
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException ignored) {
            // The original file is already safe if temporary cleanup fails.
        }
    }

    private IOException storageException(final String action,
                                         final IOException cause) {
        return new IOException("Could not " + action + " at " + filePath
                + ": " + cause.getMessage(), cause);
    }
}
