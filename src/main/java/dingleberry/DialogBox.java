package dingleberry;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

/**
 * Represents a single chat row. User messages are compact, right-aligned
 * bubbles with no avatar, while bot messages are wider, left-aligned cards
 * with a small mascot avatar, so alignment and width alone identify the
 * sender.
 */
public final class DialogBox extends HBox {
    /** Location of the cropped mascot artwork shown beside bot replies. */
    private static final String MASCOT_IMAGE_PATH =
            "/dingleberry/dingleberry_mascot_crop.png";
    /** Widest a user bubble may grow before wrapping, as a row fraction. */
    private static final double USER_MAX_WIDTH_RATIO = 0.55;
    /** Widest a bot card may grow before wrapping, as a row fraction. */
    private static final double BOT_MAX_WIDTH_RATIO = 0.88;
    /** Diameter of the mascot avatar shown beside bot replies. */
    private static final int BOT_AVATAR_SIZE = 28;
    /** Horizontal gap between a bot card and its avatar. */
    private static final int ROW_SPACING = 8;
    /** Mascot artwork shared by every bot dialog row's avatar. */
    private static final Image MASCOT_IMAGE = new Image(
            DialogBox.class.getResourceAsStream(MASCOT_IMAGE_PATH));

    private DialogBox(final String text, final boolean isUser) {
        final Label bubble = new Label(text);
        bubble.getStyleClass().addAll(
                "bubble", isUser ? "user-bubble" : "bot-bubble");
        bubble.setWrapText(true);
        // Cap the bubble at a fraction of this row's width, which itself
        // stretches to match the conversation pane, so it wraps instead of
        // overflowing as the window is resized, without forcing short
        // messages to stretch and fill the extra space.
        bubble.maxWidthProperty().bind(this.widthProperty().multiply(
                isUser ? USER_MAX_WIDTH_RATIO : BOT_MAX_WIDTH_RATIO));

        this.getStyleClass().add("dialog-box");
        this.setSpacing(ROW_SPACING);
        if (isUser) {
            this.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(bubble);
        } else {
            this.setAlignment(Pos.TOP_LEFT);
            this.getChildren().addAll(createBotAvatar(), bubble);
        }
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
     * Creates a compact, right-aligned dialog row representing the user's
     * message.
     *
     * @param text the message the user typed.
     * @return the assembled dialog row.
     */
    public static DialogBox getUserDialog(final String text) {
        return new DialogBox(text, true);
    }

    /**
     * Creates a wider, left-aligned dialog row representing a chatbot
     * response.
     *
     * @param text the message the chatbot replied with.
     * @return the assembled dialog row.
     */
    public static DialogBox getBotDialog(final String text) {
        return new DialogBox(text, false);
    }
}
