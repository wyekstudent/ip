package dingleberry;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Represents a single chat row: a speech-bubble label paired with a round
 * icon, styled to sit on either the left (bot) or right (user) side of the
 * chat log.
 */
public final class DialogBox extends HBox {
    /** Emoji shown inside the user's icon circle. */
    private static final String USER_ICON_TEXT = "\uD83E\uDDD1";
    /** Emoji shown inside the chatbot's icon circle, evoking a berry. */
    private static final String BOT_ICON_TEXT = "\uD83E\uDECB";
    /** Fraction of the row's available width a speech bubble may fill. */
    private static final double MAX_BUBBLE_WIDTH_RATIO = 0.75;
    /** Horizontal gap between a bubble and its icon. */
    private static final int ROW_SPACING = 8;

    private DialogBox(final String text, final String iconText,
                      final String bubbleStyleClass,
                      final String iconStyleClass, final boolean isUser) {
        final Label bubble = new Label(text);
                bubble.getStyleClass().addAll("bubble", bubbleStyleClass);
        bubble.setWrapText(true);
        HBox.setHgrow(bubble, Priority.SOMETIMES);
        // Cap the bubble at a fraction of this row's width, which itself
        // stretches to match the conversation pane, so it wraps instead of
        // overflowing as the window is resized.
        bubble.maxWidthProperty().bind(
                this.widthProperty().multiply(MAX_BUBBLE_WIDTH_RATIO));

        final Label icon = new Label(iconText);
                icon.getStyleClass().addAll("icon", iconStyleClass);

        this.getStyleClass().add("dialog-box");
        this.setSpacing(ROW_SPACING);
        this.setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
        if (isUser) {
            this.getChildren().addAll(bubble, icon);
        } else {
            this.getChildren().addAll(icon, bubble);
        }
    }

    /**
     * Creates a right-aligned dialog row representing the user's message.
     *
     * @param text the message the user typed.
     * @return the assembled dialog row.
     */
    public static DialogBox getUserDialog(final String text) {
        return new DialogBox(text, USER_ICON_TEXT, "user-bubble",
                "user-icon", true);
    }

    /**
     * Creates a left-aligned dialog row representing a chatbot response.
     *
     * @param text the message the chatbot replied with.
     * @return the assembled dialog row.
     */
    public static DialogBox getBotDialog(final String text) {
        return new DialogBox(text, BOT_ICON_TEXT, "bot-bubble",
                "bot-icon", false);
    }
}
