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
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
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
    /** Location of the cropped mascot artwork shown in the header. */
    private static final String MASCOT_IMAGE_PATH =
            "/dingleberry/dingleberry_mascot_crop.png";
    /** Fixed height of the branded header bar, in pixels. */
    private static final int HEADER_HEIGHT = 60;
    /** Diameter of the circular mascot avatar in the header, in pixels. */
    private static final int HEADER_AVATAR_SIZE = 40;
    /** Horizontal gap between the avatar and the title block. */
    private static final int HEADER_SPACING = 10;
    /** Vertical gap between speech bubbles in the chat log. */
    private static final int DIALOG_SPACING = 4;
    /** Padding around the chat log and the window's outer border. */
    private static final int DIALOG_PADDING = 6;
    /** Padding around the whole window content. */
    private static final int ROOT_PADDING = 8;
    /** Horizontal gap between the input field and the send button. */
    private static final int INPUT_BOX_SPACING = 8;
    /** Initial window width, in pixels. */
    private static final int WINDOW_WIDTH = 720;
    /** Initial window height, in pixels. */
    private static final int WINDOW_HEIGHT = 520;
    /** Smallest usable window width, in pixels. */
    private static final int MIN_WINDOW_WIDTH = 400;
    /** Smallest usable window height, in pixels. */
    private static final int MIN_WINDOW_HEIGHT = 320;
    /** Compact width reserved for the send button so it never grows. */
    private static final int SEND_BUTTON_WIDTH = 92;

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

        final HBox header = createHeader();

        dialogContainer.setPadding(new Insets(DIALOG_PADDING));
        dialogContainer.getStyleClass().add("dialog-container");

        chatScrollPane.setFitToWidth(true);
        chatScrollPane.getStyleClass().add("chat-scroll-pane");
        chatScrollPane.vvalueProperty()
                .bind(dialogContainer.heightProperty());

        final Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("send-button");
        // Fixed so the button stays compact while the input field absorbs
        // any extra width from resizing.
        sendButton.setMinWidth(SEND_BUTTON_WIDTH);
        sendButton.setPrefWidth(SEND_BUTTON_WIDTH);
        // Disabled whenever there is nothing meaningful to send, so it
        // cannot be clicked on blank or whitespace-only input.
        sendButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> inputField.getText().trim().isEmpty(),
                inputField.textProperty()));
        inputField.setPromptText("Type a command...");
        inputField.getStyleClass().add("input-field");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        final HBox inputBox = new HBox(
                INPUT_BOX_SPACING, inputField, sendButton);
        inputBox.getStyleClass().add("composer-bar");

        final BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        root.setPadding(new Insets(ROOT_PADDING));
        root.setTop(header);
        root.setCenter(chatScrollPane);
        root.setBottom(inputBox);
        BorderPane.setMargin(header,
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
        // The window is resizable by default; give it a floor so the
        // header, conversation, and composer stay usable at small sizes.
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.show();

        chatUi.showWelcome();
    }

    /**
     * Builds the compact branded header bar shown above the conversation:
     * a circular mascot avatar next to the app name and a short subtitle.
     *
     * @return the assembled header bar.
     */
    private HBox createHeader() {
        final Image mascotImage = new Image(
                getClass().getResourceAsStream(MASCOT_IMAGE_PATH));
        final ImageView mascotView = new ImageView(mascotImage);
        mascotView.setFitWidth(HEADER_AVATAR_SIZE);
        mascotView.setFitHeight(HEADER_AVATAR_SIZE);
        mascotView.setPreserveRatio(true);
        mascotView.setSmooth(true);
        // Clip to a circle so the square artwork reads as a round avatar.
        mascotView.setClip(new Circle(HEADER_AVATAR_SIZE / 2.0,
                HEADER_AVATAR_SIZE / 2.0, HEADER_AVATAR_SIZE / 2.0));
        mascotView.setAccessibleText("Dingleberry mascot");

        final Label titleLabel = new Label("Dingleberry");
        titleLabel.getStyleClass().add("title-label");

        final Label subtitleLabel = new Label("Task assistant");
        subtitleLabel.getStyleClass().add("subtitle-label");

        final VBox titleBlock = new VBox(titleLabel, subtitleLabel);
        titleBlock.setAlignment(Pos.CENTER_LEFT);

        final HBox header = new HBox(
                HEADER_SPACING, mascotView, titleBlock);
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMinHeight(HEADER_HEIGHT);
        header.setPrefHeight(HEADER_HEIGHT);
        header.setMaxHeight(HEADER_HEIGHT);
        return header;
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
            inputField.requestFocus();
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
                return;
            }
        } catch (DingleberryException e) {
            if (e.isWrongCommand()) {
                chatUi.showWrongCommandError(e.getMessage());
            } else {
                chatUi.showIncorrectParametersError(e.getMessage());
            }
        }
        // Keep the caret in the input field after every command so the
        // user can keep typing without an extra click, whether they sent
        // it with Enter or the Send button.
        inputField.requestFocus();
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
         * Adds a calm success card confirming a task change, with the
         * heading and body kept as separate structured content.
         *
         * @param heading a short description of the change
         * @param body the confirmation details shown below the heading
         */
        private void appendSuccessMessage(final String heading,
                                          final String body) {
            dialogLog.getChildren()
                    .add(DialogBox.getSuccessDialog(heading, body));
        }

        /**
         * Adds a calm information card for a neutral reply, with the
         * heading and body kept as separate structured content.
         *
         * @param heading a short description of the reply
         * @param body the reply content shown below the heading
         */
        private void appendInformationMessage(final String heading,
                                              final String body) {
            dialogLog.getChildren()
                    .add(DialogBox.getInformationDialog(heading, body));
        }

        /**
         * Adds a dedicated error card with the given heading and detail
         * message, styled distinctly from an ordinary bot reply.
         *
         * @param heading a short description of the error
         * @param message the detail message explaining what went wrong
         */
        private void appendErrorMessage(final String heading,
                                        final String message) {
            dialogLog.getChildren()
                    .add(DialogBox.getErrorDialog(heading, message));
        }

        @Override
        public void showWelcome() {
            final DialogBox welcomeDialog = DialogBox.getInformationDialog(
                    "Welcome to Dingleberry", WELCOME_MESSAGE);
            // Keeps the ASCII-art banner's spacing aligned, unlike the
            // proportional font used by every other card.
            welcomeDialog.getStyleClass().add("welcome-dialog");
            dialogLog.getChildren().add(welcomeDialog);
        }

        @Override
        public void showLine() {
            // No separator line is needed between GUI speech bubbles.
        }

        @Override
        public void showTaskList(final TaskList tasks) {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < tasks.size(); i++) {
                if (i > 0) {
                    builder.append(System.lineSeparator());
                }
                builder.append(i + 1).append(". ").append(tasks.get(i));
            }
            appendInformationMessage("Your tasks", builder.toString());
        }

        @Override
        public void showTaskAdded(final Task task,
                                  final int totalTaskCount) {
            appendSuccessMessage("Task added", task
                    + System.lineSeparator() + "You now have "
                    + totalTaskCount + " tasks in your list.");
        }

        @Override
        public void showTaskDeleted(final Task task,
                                    final int totalTaskCount) {
            appendSuccessMessage("Task deleted", task
                    + System.lineSeparator() + "You now have "
                    + totalTaskCount + " tasks in your list.");
        }

        @Override
        public void showTaskMarked(final Task task) {
            appendSuccessMessage("Task completed", task.toString());
        }

        @Override
        public void showTaskUnmarked(final Task task) {
            appendSuccessMessage("Task reopened", task.toString());
        }

        @Override
        public void showWrongCommandError(final String message) {
            appendErrorMessage("Command not understood", message);
        }

        @Override
        public void showIncorrectParametersError(final String message) {
            appendErrorMessage("Missing or invalid details", message);
        }

        @Override
        public void showLoadingError(final String message) {
            appendErrorMessage("Could not load tasks",
                    message + " Starting with an empty list.");
        }

        @Override
        public void showSavingError(final String message) {
            appendErrorMessage("Could not save tasks", message);
        }

        @Override
        public void showGoodbye() {
            appendInformationMessage("Goodbye",
                    "Bye, hope to see your berries again!");
        }
    }
}
