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
import dingleberry.command.MarkCommand;
import dingleberry.command.UnmarkCommand;
import dingleberry.exception.DingleberryException;

/** Tests command recognition and validation performed by {@link Parser}. */
class ParserTest {
    @Test
    void parse_bye_returnsExitCommand() throws DingleberryException {
        Command command = Parser.parse("bye");

        assertInstanceOf(ExitCommand.class, command);
        assertTrue(command.isExit());
    }

    @Test
    void parse_list_returnsListCommand() throws DingleberryException {
        Command command = Parser.parse("list");

        assertInstanceOf(ListCommand.class, command);
        assertFalse(command.isExit());
    }

    @Test
    void parse_todoWithDescription_returnsAddCommand()
            throws DingleberryException {
        Command command = Parser.parse("todo read lecture notes");

        assertInstanceOf(AddCommand.class, command);
    }

    @Test
    void parse_deadlineWithDate_returnsAddCommand() throws DingleberryException {
        Command command = Parser.parse(
                "deadline submit report /by 2026-08-25 1800");

        assertInstanceOf(AddCommand.class, command);
    }

    @Test
    void parse_eventWithTimes_returnsAddCommand() throws DingleberryException {
        Command command = Parser.parse(
                "event team meeting /from 2026-08-25 1400 /to 2026-08-25 1500");

        assertInstanceOf(AddCommand.class, command);
    }

    @Test
    void parse_deleteWithTaskNumber_returnsDeleteCommand()
            throws DingleberryException {
        Command command = Parser.parse("delete 2");

        assertInstanceOf(DeleteCommand.class, command);
    }

    @Test
    void parse_markWithTaskNumber_returnsMarkCommand()
            throws DingleberryException {
        Command command = Parser.parse("mark 1");

        assertInstanceOf(MarkCommand.class, command);
        assertFalse(command.isExit());
    }

    @Test
    void parse_markWithoutNumber_throwsIncorrectParameters() {
        assertThrows(DingleberryException.class, () -> Parser.parse("mark"));
    }

    @Test
    void parse_markWithNonNumericNumber_throwsIncorrectParameters() {
        assertThrows(DingleberryException.class,
                () -> Parser.parse("mark one"));
    }

    @Test
    void parse_unmarkWithTaskNumber_returnsUnmarkCommand()
            throws DingleberryException {
        Command command = Parser.parse("unmark 1");

        assertInstanceOf(UnmarkCommand.class, command);
        assertFalse(command.isExit());
    }

    @Test
    void parse_unmarkWithoutNumber_throwsIncorrectParameters() {
        assertThrows(DingleberryException.class, () -> Parser.parse("unmark"));
    }

    @Test
    void parse_unmarkWithNonNumericNumber_throwsIncorrectParameters() {
        assertThrows(DingleberryException.class,
                () -> Parser.parse("unmark one"));
    }

    @Test
    void parse_findWithKeyword_returnsFindCommand() throws DingleberryException {
        Command command = Parser.parse("find lecture");

        assertInstanceOf(FindCommand.class, command);
    }

    @Test
    void parse_blankInput_throwsIncorrectParameters() {
        DingleberryException exception = assertThrows(
                DingleberryException.class, () -> Parser.parse("   "));

        assertEquals("Please give me a command or a task description.",
                exception.getMessage());
        assertFalse(exception.isWrongCommand());
    }

    @Test
    void parse_unknownCommand_throwsWrongCommand() {
        DingleberryException exception = assertThrows(
                DingleberryException.class, () -> Parser.parse("archive notes"));

        assertTrue(exception.isWrongCommand());
    }

    @Test
    void parse_listWithParameters_throwsIncorrectParameters() {
        DingleberryException exception = assertThrows(
                DingleberryException.class, () -> Parser.parse("list now"));

        assertEquals("'list' does not accept parameters.",
                exception.getMessage());
    }

    @Test
    void parse_todoWithoutDescription_throwsIncorrectParameters() {
        assertThrows(DingleberryException.class, () -> Parser.parse("todo   "));
    }

    @Test
    void parse_deleteWithoutNumber_throwsIncorrectParameters() {
        assertThrows(DingleberryException.class, () -> Parser.parse("delete"));
    }

    @Test
    void parse_deleteWithNonNumericNumber_throwsIncorrectParameters() {
        assertThrows(DingleberryException.class,
                () -> Parser.parse("delete two"));
    }

    @Test
    void parse_findWithoutKeyword_throwsIncorrectParameters() {
        DingleberryException exception = assertThrows(
                DingleberryException.class, () -> Parser.parse("find   "));

        assertEquals("'find' needs a keyword.", exception.getMessage());
    }

    @Test
    void parse_deadlineWithoutDate_throwsIncorrectParameters() {
        DingleberryException exception = assertThrows(
                DingleberryException.class,
                () -> Parser.parse("deadline submit report"));

        assertEquals(
                "A deadline needs a description and '/by <date>' in the"
                        + " format yyyy-MM-dd HHmm, e.g. 2019-12-02 1800.",
                exception.getMessage());
    }

    @Test
    void parse_deadlineWithInvalidDate_throwsIncorrectParameters() {
        DingleberryException exception = assertThrows(
                DingleberryException.class,
                () -> Parser.parse("deadline submit report /by tomorrow"));

        assertEquals(
                "I couldn't understand that date/time. Please use the format"
                        + " yyyy-MM-dd HHmm, e.g. 2019-12-02 1800.",
                exception.getMessage());
    }

    @Test
    void parse_eventWithoutEndTime_throwsIncorrectParameters() {
        DingleberryException exception = assertThrows(
                DingleberryException.class,
                () -> Parser.parse("event team meeting /from 2026-08-25 1400"));

        assertEquals(
                "An event needs a description, '/from <time>', and '/to <time>'"
                        + " in the format yyyy-MM-dd HHmm, e.g. 2019-12-02 1800.",
                exception.getMessage());
    }

    @Test
    void parse_eventWithInvalidDate_throwsIncorrectParameters() {
        DingleberryException exception = assertThrows(
                DingleberryException.class,
                () -> Parser.parse(
                        "event team meeting /from tomorrow /to 2026-08-25 1500"));

        assertEquals(
                "I couldn't understand that date/time. Please use the format"
                        + " yyyy-MM-dd HHmm, e.g. 2019-12-02 1800.",
                exception.getMessage());
    }
}
