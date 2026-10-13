package dingleberry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dingleberry.model.TaskList;
import dingleberry.persistence.Storage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Tests the GUI through its displayed controls with isolated task storage.
 */
class MainTest {
    /** Bounds waits so a JavaFX failure cannot hang the test suite. */
    private static final int FX_TIMEOUT_SECONDS = 10;
    /** Supplies enough responses to overflow the conversation viewport. */
    private static final int OVERFLOW_TASK_COUNT = 12;
    /** Identifies the list response after welcome, warning, and user input. */
    private static final int LIST_RESPONSE_INDEX = 3;

    /** Provides a separate storage directory for each test. */
    @TempDir
    private Path tempDir;
    /** Holds the window closed after each test. */
    private Stage stage;
    /** Provides the layout used to locate the displayed controls. */
    private BorderPane root;
    /** Receives commands through the same field used by the application. */
    private TextField inputField;
    /** Sends commands through the displayed button. */
    private Button sendButton;
    /** Contains the responses displayed by the application. */
    private VBox dialogContainer;
    /** Exposes the conversation's scroll position. */
    private ScrollPane chatScrollPane;

    @BeforeAll
    static void initializeJavaFx() throws InterruptedException {
        CountDownLatch toolkitReady = new CountDownLatch(1);
        Runnable initializeToolkit = () -> {
            Platform.setImplicitExit(false);
            toolkitReady.countDown();
        };
        try {
            Platform.startup(initializeToolkit);
        } catch (IllegalStateException alreadyStarted) {
            // DialogBoxTest may have already initialized the shared toolkit.
            Platform.runLater(initializeToolkit);
        }
        assertTrue(toolkitReady.await(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @AfterEach
    void closeWindow() throws Exception {
        runOnFxThread(() -> {
            if (stage != null) {
                stage.close();
            }
        });
    }

    @Test
    void start_corruptedRecord_showsWelcomeBeforeWarning() throws Exception {
        Path taskFile = tempDir.resolve("tasks.txt");
        Files.writeString(taskFile, "T | 0 | recovered task\ninvalid record\n");
        startApplication(taskFile);

        runOnFxThread(() -> {
            assertEquals("Welcome to Dingleberry", headingAt(0));
            assertEquals("Some tasks were skipped", headingAt(1));
            inputField.setText("list");
            sendButton.fire();
            assertTrue(bodyAt(LIST_RESPONSE_INDEX).contains("recovered task"));
        });
    }

    @Test
    void start_unreadableStorage_showsLoadErrorAndAcceptsCommands()
            throws Exception {
        Path taskFile = tempDir.resolve("directory");
        Files.createDirectory(taskFile);
        startApplication(taskFile);

        runOnFxThread(() -> {
            assertEquals("Welcome to Dingleberry", headingAt(0));
            assertEquals("Could not load tasks", headingAt(1));
            inputField.setText("list");
            sendButton.fire();
            assertEquals("Your taskies 📋", headingAt(LIST_RESPONSE_INDEX));
            assertEquals("", bodyAt(LIST_RESPONSE_INDEX));
        });
    }

    @Test
    void sendButton_blankInput_remainsDisabled() throws Exception {
        startApplication(tempDir.resolve("tasks.txt"));

        runOnFxThread(() -> {
            assertTrue(sendButton.isDisabled());
            inputField.setText("   ");
            assertTrue(sendButton.isDisabled());
            inputField.setText("list");
            assertFalse(sendButton.isDisabled());
        });
    }

    @Test
    void submit_sendAndEnter_addsAndPersistsBothTasks() throws Exception {
        Path taskFile = tempDir.resolve("tasks.txt");
        startApplication(taskFile);

        runOnFxThread(() -> {
            inputField.setText("todo sent with button");
            sendButton.fire();
            inputField.setText("todo sent with Enter");
            inputField.fireEvent(new ActionEvent());
            assertEquals("", inputField.getText());
            assertEquals(inputField, stage.getScene().getFocusOwner());
        });

        TaskList savedTasks = new TaskList(
                new Storage(taskFile.toString()).load());
        assertEquals(2, savedTasks.size());
        assertEquals("sent with button", savedTasks.get(0).getDescription());
        assertEquals("sent with Enter", savedTasks.get(1).getDescription());
    }

    @Test
    void submit_newMessages_scrollsToLatestResponse() throws Exception {
        startApplication(tempDir.resolve("tasks.txt"));
        runOnFxThread(() -> {
            for (int i = 0; i < OVERFLOW_TASK_COUNT; i++) {
                inputField.setText("todo task " + i);
                sendButton.fire();
            }
            chatScrollPane.setVvalue(0.0);
            root.applyCss();
            root.layout();
        });

        runOnFxThread(() -> {
            assertTrue(dialogContainer.getHeight()
                    > chatScrollPane.getViewportBounds().getHeight());
            assertEquals(1.0, chatScrollPane.getVvalue());
        });
    }

    /**
     * Starts a window on the JavaFX thread and locates its displayed controls.
     *
     * @param taskFile the isolated file used by the application.
     * @throws Exception if JavaFX startup or task loading fails unexpectedly.
     */
    private void startApplication(final Path taskFile) throws Exception {
        runOnFxThread(() -> {
            stage = new Stage();
            new Main(new Storage(taskFile.toString())).start(stage);
            root = (BorderPane) stage.getScene().getRoot();
            HBox composer = (HBox) root.getBottom();
            inputField = (TextField) composer.getChildren().get(0);
            sendButton = (Button) composer.getChildren().get(1);
            chatScrollPane = (ScrollPane) root.getCenter();
            dialogContainer = (VBox) chatScrollPane.getContent();
        });
    }

    /**
     * Returns the heading displayed in a bot response.
     *
     * @param index the response's position in the conversation.
     * @return the displayed heading.
     */
    private String headingAt(final int index) {
        return ((Label) dialogContainer.getChildren().get(index)
                .lookup(".message-heading")).getText();
    }

    /**
     * Returns the body displayed in a bot response.
     *
     * @param index the response's position in the conversation.
     * @return the displayed body.
     */
    private String bodyAt(final int index) {
        return ((Label) dialogContainer.getChildren().get(index)
                .lookup(".message-body")).getText();
    }

    /**
     * Runs scene operations on the JavaFX thread and propagates test failures.
     *
     * @param action the scene operations or assertions to perform.
     * @throws Exception if the action fails or JavaFX stops responding.
     */
    private static void runOnFxThread(final Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
