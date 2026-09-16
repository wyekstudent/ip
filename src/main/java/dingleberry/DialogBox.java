package dingleberry;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Represents a single chat row. Each row is rendered according to an
 * explicit {@link DialogType} rather than by styling based on message
 * text: user messages are compact, right-aligned bubbles with no avatar;
 * bot messages are wider, left-aligned cards with a small mascot avatar;
 * and error messages are dedicated cards with a warning marker, a short
 * heading, and the underlying detail message, so invalid commands and
 * parameters are never confused with an ordinary bot reply.
 */
public final class DialogBox extends HBox {
    /** Distinguishes the visual treatment applied to a dialog row. */
    private enum DialogType {
        /** A compact, right-aligned bubble with no avatar. */
        USER,
        /** A wide, left-aligned card with a mascot avatar. */
        BOT,
        /** A wide, left-aligned card with a heading and warning marker. */
        ERROR
    }

    /** Location of the cropped mascot artwork shown beside bot replies. */
    private static final String MASCOT_IMAGE_PATH =
            "/dingleberry/dingleberry_mascot_crop.png";
    /** Compact marker shown beside error cards. */
    private static final String WARNING_ICON_TEXT = "\u26A0";
    /** Widest a user bubble may grow before wrapping, as a row fraction. */
    private static final double USER_MAX_WIDTH_RATIO = 0.55;
    /** Widest a bot or error card may grow before wrapping, as a row
     *  fraction. */
    private static final double WIDE_MAX_WIDTH_RATIO = 0.88;
    /** Diameter of the mascot avatar shown beside bot replies. */
    private static final int BOT_AVATAR_SIZE = 28;
    /** Diameter of the warning marker shown beside error cards. */
    private static final int WARNING_ICON_SIZE = 28;
    /** Horizontal gap between a card and its avatar or marker. */
    private static final int ROW_SPACING = 8;
    /** Vertical gap between an error card's heading and its message. */
    private static final int ERROR_CARD_SPACING = 3;
    /** Mascot artwork shared by every bot dialog row's avatar. */
    private static final Image MASCOT_IMAGE = new Image(
            DialogBox.class.getResourceAsStream(MASCOT_IMAGE_PATH));

    private DialogBox(final DialogType type, final String heading,
                      final String text) {
        final Region card = createCard(type, heading, text);
        // Cap the card at a fraction of this row's width, which itself
        // stretches to match the conversation pane, so it wraps instead of
        // overflowing as the window is resized, without forcing short
        // messages to stretch and fill the extra space.
        card.maxWidthProperty().bind(this.widthProperty().multiply(
                type == DialogType.USER
                        ? USER_MAX_WIDTH_RATIO : WIDE_MAX_WIDTH_RATIO));

        this.getStyleClass().add("dialog-box");
        this.setSpacing(ROW_SPACING);
        switch (type) {
        case USER:
            this.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(card);
            break;
        case ERROR:
            this.setAlignment(Pos.TOP_LEFT);
            this.getChildren().addAll(createWarningIcon(), card);
            break;
        default:
            this.setAlignment(Pos.TOP_LEFT);
            this.getChildren().addAll(createBotAvatar(), card);
            break;
        }
    }

    /**
     * Builds the message card for the given dialog type: a plain wrapping
     * label for user and bot messages, or a heading-plus-message card for
     * errors.
     *
     * @param type the dialog type to render.
     * @param heading the error heading, ignored for non-error types.
     * @param text the message body.
     * @return the assembled card node.
     */
    private static Region createCard(final DialogType type,
                                     final String heading,
                                     final String text) {
        if (type == DialogType.ERROR) {
            final Label headingLabel = new Label(heading);
            headingLabel.getStyleClass().add("error-heading");
            headingLabel.setWrapText(true);

            final Label messageLabel = new Label(text);
            messageLabel.getStyleClass().add("error-message");
            messageLabel.setWrapText(true);

            final VBox card = new VBox(
                    ERROR_CARD_SPACING, headingLabel, messageLabel);
            card.getStyleClass().addAll("bubble", "error-bubble");
            return card;
        }

        final Label bubble = new Label(text);
        bubble.getStyleClass().addAll("bubble",
                type == DialogType.USER ? "user-bubble" : "bot-bubble");
        bubble.setWrapText(true);
        return bubble;
    }

    /**
     * Creates the small circular mascot avatar shown beside bot replies.
     *
     * @return the assembled avatar image view.
     */
    private static ImageView createBotAvatar() {
        final ImageView avatar = new ImageView(MASCOT_IMAGE);
        avatar.setFitWidth(BOT_AVATAR_SIZE);
        avatar.setFitHeight(BOT_AVATAR_SIZE);
        avatar.setPreserveRatio(true);
        avatar.setSmooth(true);
        avatar.setClip(new Circle(BOT_AVATAR_SIZE / 2.0,
                BOT_AVATAR_SIZE / 2.0, BOT_AVATAR_SIZE / 2.0));
        avatar.getStyleClass().add("bot-avatar");
        avatar.setAccessibleText("Dingleberry mascot");
        return avatar;
    }

    /**
     * Creates the compact warning marker shown beside error cards.
     *
     * @return the assembled warning marker label.
     */
    private static Label createWarningIcon() {
        final Label icon = new Label(WARNING_ICON_TEXT);
        icon.getStyleClass().add("warning-icon");
        icon.setMinSize(WARNING_ICON_SIZE, WARNING_ICON_SIZE);
        icon.setMaxSize(WARNING_ICON_SIZE, WARNING_ICON_SIZE);
        icon.setAlignment(Pos.CENTER);
        icon.setAccessibleText("Warning");
        return icon;
    }

    /**
     * Creates a compact, right-aligned dialog row representing the user's
     * message.
     *
     * @param text the message the user typed.
     * @return the assembled dialog row.
     */
    public static DialogBox getUserDialog(final String text) {
        return new DialogBox(DialogType.USER, null, text);
    }

    /**
     * Creates a wider, left-aligned dialog row representing a chatbot
     * response.
     *
     * @param text the message the chatbot replied with.
     * @return the assembled dialog row.
     */
    public static DialogBox getBotDialog(final String text) {
        return new DialogBox(DialogType.BOT, null, text);
    }

    /**
     * Creates a dedicated error dialog row with a warning marker, a short
     * heading, and the underlying detail message, distinct from an
     * ordinary bot reply.
     *
     * @param heading a short description of the error, such as
     *     "Command not understood".
     * @param message the detail message explaining what went wrong.
     * @return the assembled dialog row.
     */
    public static DialogBox getErrorDialog(final String heading,
                                           final String message) {
        return new DialogBox(DialogType.ERROR, heading, message);
    }
}

