package dingleberry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Verifies that {@link DialogBox} assigns a distinct, explicit visual
 * treatment per message type rather than inferring it from message text.
 */
class DialogBoxTest {

    /**
     * Starts the JavaFX toolkit once before any test constructs scene
     * graph nodes, since {@link DialogBox} loads mascot artwork via
     * {@link javafx.scene.image.Image}.
     */
    @BeforeAll
    static void initJavaFxToolkit() throws InterruptedException {
        final CountDownLatch toolkitReady = new CountDownLatch(1);
        try {
            Platform.startup(toolkitReady::countDown);
        } catch (IllegalStateException alreadyStarted) {
            toolkitReady.countDown();
        }
        toolkitReady.await();
    }

    @Test
    void getErrorDialogAppliesErrorStyleNotBotStyle() {
        final DialogBox errorDialog = DialogBox.getErrorDialog(
                "Command not understood", "bogus is not a command");

        final Node card = lastChild(errorDialog);
        assertTrue(card.getStyleClass().contains("error-bubble"));
        assertFalse(card.getStyleClass().contains("bot-bubble"));
        assertEquals(2, errorDialog.getChildren().size(),
                "an error row should show a warning marker plus the card");
    }

    @Test
    void getErrorDialogKeepsHeadingAndDetailMessageVisible() {
        final DialogBox errorDialog = DialogBox.getErrorDialog(
                "Missing or invalid details", "todo needs a description");

        final VBox card = (VBox) lastChild(errorDialog);
        final Label heading = (Label) card.getChildren().get(0);
        final Label detail = (Label) card.getChildren().get(1);
        assertEquals("Missing or invalid details", heading.getText());
        assertEquals("todo needs a description", detail.getText());
    }

    @Test
    void getBotDialogAppliesBotStyleNotError() {
        final DialogBox botDialog = DialogBox.getBotDialog("Okie dokie!");

        final Node card = lastChild(botDialog);
        assertTrue(card.getStyleClass().contains("bot-bubble"));
        assertFalse(card.getStyleClass().contains("error-bubble"));
    }

    @Test
    void getUserDialogHasNoAvatarChild() {
        final DialogBox userDialog = DialogBox.getUserDialog("list");

        assertEquals(1, userDialog.getChildren().size(),
                "a user row should show only its bubble, with no avatar");
    }

    private static Node lastChild(final DialogBox dialogBox) {
        return dialogBox.getChildren()
                .get(dialogBox.getChildren().size() - 1);
    }
}
