package dingleberry.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import dingleberry.command.AddCommand;
import dingleberry.command.Command;
import dingleberry.command.DeleteCommand;
import dingleberry.command.ExitCommand;
import dingleberry.command.FindCommand;
import dingleberry.command.ListCommand;
import dingleberry.exception.DingleberryException;

/** Tests command recognition and validation performed by {@link Parser}. */
class ParserTest {
    @Test
    void parseByeReturnsExitCommand() throws DingleberryException {
        Command command = Parser.parse("bye");

        assertInstanceOf(ExitCommand.class, command);
        assertTrue(command.isExit());
    }

    @Test
    void parseListReturnsListCommand() throws DingleberryException {
        Command command = Parser.parse("list");

        assertInstanceOf(ListCommand.class, command);
        assertFalse(command.isExit());
    }

    @Test
        void parseTodoWithDescriptionReturnsAddCommand()
            throws DingleberryException {
        Command command = Parser.parse("todo read lecture notes");

        assertInstanceOf(AddCommand.class, command);
    }

    @Test
    void parseDeadlineWithDateReturnsAddCommand() throws DingleberryException {
        Command command = Parser.parse(
            "deadline submit report /by 2026-08-25 1800");

        assertInstanceOf(AddCommand.class, command);
    }

    @Test
    void parseEventWithTimesReturnsAddCommand() throws DingleberryException {
        Command command = Parser.parse(
            "event team meeting /from 2026-08-25 1400 /to 2026-08-25 1500");

        assertInstanceOf(AddCommand.class, command);
    }

    @Test
        void parseDeleteWithTaskNumberReturnsDeleteCommand()
            throws DingleberryException {
        Command command = Parser.parse("delete 2");

        assertInstanceOf(DeleteCommand.class, command);
    }

    @Test
    void parseFindWithKeywordReturnsFindCommand() throws DingleberryException {
        Command command = Parser.parse("find lecture");

        assertInstanceOf(FindCommand.class, command);
    }

    @Test
    void parseBlankInputThrowsIncorrectParameters() {
        DingleberryException exception = assertThrows(
            DingleberryException.class, () -> Parser.parse("   "));

        assertEquals("Please give me a command or a task description.",
            exception.getMessage());
        assertFalse(exception.isWrongCommand());
    }

    @Test
    void parseUnknownCommandThrowsWrongCommand() {
        DingleberryException exception = assertThrows(
            DingleberryException.class, () -> Parser.parse("archive notes"));

        assertTrue(exception.isWrongCommand());
    }

    @Test
    void parseListWithParametersThrowsIncorrectParameters() {
        DingleberryException exception = assertThrows(
            DingleberryException.class, () -> Parser.parse("list now"));

        assertEquals("'list' does not accept parameters.",
            exception.getMessage());
    }

    @Test
    void parseTodoWithoutDescriptionThrowsIncorrectParameters() {
        assertThrows(DingleberryException.class, () -> Parser.parse("todo   "));
    }

    @Test
    void parseDeleteWithoutNumberThrowsIncorrectParameters() {
        assertThrows(DingleberryException.class, () -> Parser.parse("delete"));
    }

    @Test
    void parseDeleteWithNonNumericNumberThrowsIncorrectParameters() {
        assertThrows(DingleberryException.class,
            () -> Parser.parse("delete two"));
    }

    @Test
    void parseFindWithoutKeywordThrowsIncorrectParameters() {
        DingleberryException exception = assertThrows(
            DingleberryException.class, () -> Parser.parse("find   "));

        assertEquals("'find' needs a keyword.", exception.getMessage());
    }

    @Test
    void parseDeadlineWithoutDateThrowsIncorrectParameters() {
        assertThrows(DingleberryException.class,
            () -> Parser.parse("deadline submit report"));
    }

    @Test
    void parseDeadlineWithInvalidDateThrowsIncorrectParameters() {
        assertThrows(DingleberryException.class,
                () -> Parser.parse("deadline submit report /by tomorrow"));
    }

    @Test
    void parseEventWithoutEndTimeThrowsIncorrectParameters() {
        assertThrows(DingleberryException.class,
                () -> Parser.parse(
                    "event team meeting /from 2026-08-25 1400"));
    }

    @Test
    void parseEventWithInvalidDateThrowsIncorrectParameters() {
        assertThrows(DingleberryException.class,
                () -> Parser.parse(
                    "event team meeting /from tomorrow /to 2026-08-25 1500"));
    }
}
