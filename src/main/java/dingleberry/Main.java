package dingleberry;

import java.io.IOException;

import dingleberry.command.Command;
import dingleberry.exception.DingleberryException;
import dingleberry.model.Task;
import dingleberry.model.TaskList;
import dingleberry.parser.Parser;
import dingleberry.persistence.Storage;
import dingleberry.ui.Ui;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * A JavaFX-based chatbot UI for the Dingleberry task app.
 */
public final class Main extends Application {
    /** Default storage location used by the GUI app. */
    private static final String DEFAULT_DATA_FILE_PATH =
            "./data/dingleberry.txt";
    /** Location of the stylesheet applied to the chat window. */
    private static final String STYLESHEET_PATH =
            "/dingleberry/dingleberry.css";
    /** Vertical gap between speech bubbles in the chat log. */
    private static final int DIALOG_SPACING = 6;
    /** Padding around the chat log and the window's outer border. */
    private static final int DIALOG_PADDING = 8;
    /** Padding around the whole window content. */
    private static final int ROOT_PADDING = 12;
    /** Horizontal gap between the input field and the send button. */
    private static final int INPUT_BOX_SPACING = 8;
    /** Initial window width, in pixels. */
    private static final int WINDOW_WIDTH = 720;
    /** Initial window height, in pixels. */
    private static final int WINDOW_HEIGHT = 520;

    /** Stores the current in-memory task list. */
    private final TaskList tasks;
    /** Persists the task list to disk. */
    private final Storage storage;
    /** Displays output in the GUI instead of the console. */
    private final ChatUi chatUi;
    /** Holds the ordered speech bubbles shown in the chat log. */
    private final VBox dialogContainer = new VBox(DIALOG_SPACING);
    /** Scrolls the dialog container and keeps the latest message visible. */
    private final ScrollPane chatScrollPane = new ScrollPane(dialogContainer);
    /** User input field for commands. */
    private final TextField inputField = new TextField();
    /** Provides the stage for closing the app after an exit command. */
    private Stage stage;

    /**
     * Creates the GUI app and loads saved tasks if they exist.
     */
    public Main() {
        this.storage = new Storage(DEFAULT_DATA_FILE_PATH);
        TaskList loadedTasks;
        try {
            loadedTasks = new TaskList(storage.load());
        } catch (IOException e) {
            loadedTasks = new TaskList();
        }
        this.tasks = loadedTasks;
        this.chatUi = new ChatUi(dialogContainer);
    }

    @Override
    public void start(final Stage primaryStage) {
        this.stage = primaryStage;

        final Label titleLabel = new Label("Dingleberry");
        titleLabel.getStyleClass().add("title-label");

        dialogContainer.setPadding(new Insets(DIALOG_PADDING));
        dialogContainer.getStyleClass().add("dialog-container");

        chatScrollPane.setFitToWidth(true);
        chatScrollPane.getStyleClass().add("chat-scroll-pane");
        chatScrollPane.vvalueProperty()
                .bind(dialogContainer.heightProperty());

        final Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("send-button");
        inputField.setPromptText("Type a command...");
        inputField.getStyleClass().add("input-field");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        final HBox inputBox = new HBox(
                INPUT_BOX_SPACING, inputField, sendButton);

        final BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        root.setPadding(new Insets(ROOT_PADDING));
        root.setTop(titleLabel);
        root.setCenter(chatScrollPane);
        root.setBottom(inputBox);
        BorderPane.setMargin(titleLabel,
                new Insets(0, 0, DIALOG_PADDING, 0));
        BorderPane.setMargin(inputBox,
                new Insets(DIALOG_PADDING, 0, 0, 0));

        sendButton.setOnAction(event -> handleUserInput());
        inputField.setOnAction(event -> handleUserInput());

        final Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(
                getClass().getResource(STYLESHEET_PATH).toExternalForm());
        primaryStage.setTitle("Dingleberry");
        primaryStage.setScene(scene);
        primaryStage.show();

        chatUi.showWelcome();
    }

    /**
     * Handles a command typed into the JavaFX input field.
     */
    private void handleUserInput() {
        final String input = inputField.getText();
        if (input == null) {
            return;
        }

        final String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) {
            chatUi.showIncorrectParametersError(
                    "Please give me a command or a task description.");
            inputField.clear();
            return;
        }

        dialogContainer.getChildren()
                .add(DialogBox.getUserDialog(trimmedInput));
        inputField.clear();

        try {
            final Command command = Parser.parse(trimmedInput);
            command.execute(tasks, chatUi, storage);
            if (command.isExit()) {
                chatUi.showGoodbye();
                stage.close();
            }
        } catch (DingleberryException e) {
            if (e.isWrongCommand()) {
                chatUi.showWrongCommandError(e.getMessage());
            } else {
                chatUi.showIncorrectParametersError(e.getMessage());
            }
        }
    }

    /**
     * A UI implementation that renders responses as bot speech bubbles.
     */
    private static final class ChatUi extends Ui {
        /** Chat log that new bot dialog rows are appended to. */
        private final VBox dialogLog;

        /**
         * Creates a GUI-backed UI that appends messages to the given dialog
         * log.
         *
         * @param chatLog the chat log to append bot messages to
         */
        private ChatUi(final VBox chatLog) {
            this.dialogLog = chatLog;
        }

        /**
         * Adds a bot speech bubble containing the given message.
         *
         * @param message the text to display inside the bubble
         */
        private void appendMessage(final String message) {
            dialogLog.getChildren().add(DialogBox.getBotDialog(message));
        }

        @Override
        public void showWelcome() {
            appendMessage("Hello! I'm Dingleberry.\nWhat can I do for you?");
        }

        @Override
        public void showLine() {
            // No separator line is needed between GUI speech bubbles.
        }

        @Override
        public void showTaskList(final TaskList tasks) {
            final StringBuilder builder = new StringBuilder();
            builder.append("Here are your tasks:");
            for (int i = 0; i < tasks.size(); i++) {
                builder.append(System.lineSeparator())
                        .append(i + 1)
                        .append(". ")
                        .append(tasks.get(i));
            }
            appendMessage(builder.toString());
        }

        @Override
        public void showTaskAdded(final Task task,
                                  final int totalTaskCount) {
            appendMessage("Got it. I've added this task:\n  " + task
                    + "\nNow you have " + totalTaskCount
                    + " tasks in the list.");
        }

        @Override
        public void showTaskDeleted(final Task task,
                                    final int totalTaskCount) {
            appendMessage("Noted. I've removed this task: " + task
                    + "\nNow you have " + totalTaskCount
                    + " tasks in the list.");
        }

        @Override
        public void showWrongCommandError(final String message) {
            appendMessage(
                    "Oops, Dingleberry doesn't know that command: "
                            + message);
        }

        @Override
        public void showIncorrectParametersError(final String message) {
            appendMessage(
                    "Oops, Dingleberry found incorrect parameters: "
                            + message);
        }

        @Override
        public void showLoadingError(final String message) {
            appendMessage("Couldn't load saved tasks (" + message
                    + "). Starting with an empty list.");
        }

        @Override
        public void showSavingError(final String message) {
            appendMessage("Couldn't save tasks to disk (" + message + ").");
        }

        @Override
        public void showGoodbye() {
            appendMessage("Bye! Hope to see your berries again!");
        }
    }
}
