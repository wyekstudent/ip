package dingleberry.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    /** Defines the fixed Tuesday used as the parser reference date. */
    private static final LocalDate TUESDAY = LocalDate.of(2026, 9, 8);
    /** Defines the Monday used for relative weekday boundary tests. */
    private static final LocalDate MONDAY = LocalDate.of(2026, 9, 14);
    /** Defines midnight on the Monday following the reference Tuesday. */
    private static final LocalDateTime NEXT_MONDAY_MIDNIGHT =
            LocalDateTime.of(2026, 9, 14, 0, 0);
    /** Defines a weekday timestamp with a dotted afternoon time. */
    private static final LocalDateTime NEXT_MONDAY_AFTERNOON =
            LocalDateTime.of(2026, 9, 14, 14, 30);
    /** Defines midnight on the Monday after the current Monday. */
    private static final LocalDateTime FOLLOWING_MONDAY_MIDNIGHT =
            LocalDateTime.of(2026, 9, 21, 0, 0);
    /** Defines a time-only value resolved against an inherited date. */
    private static final LocalDateTime INHERITED_THREE_PM =
            LocalDateTime.of(2026, 9, 14, 15, 0);
    /** Defines an inherited date with a twelve-hour morning time. */
    private static final LocalDateTime INHERITED_TWO_THIRTY_AM =
            LocalDateTime.of(2026, 9, 14, 2, 30);

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
    void parseDeadlineWithNaturalWeekdayReturnsAddCommand()
            throws DingleberryException {
        Command command = Parser.parse(
                "deadline submit report /by Mon");

        assertInstanceOf(AddCommand.class, command);
    }

    @Test
    void parseEventWithTimesReturnsAddCommand() throws DingleberryException {
        Command command = Parser.parse(
                "event team meeting /from 2026-08-25 1400 /to"
                        + " 2026-08-25 1500");

        assertInstanceOf(AddCommand.class, command);
    }

    @Test
    void parseEventWithNaturalTimesReturnsAddCommand()
            throws DingleberryException {
        Command command = Parser.parse(
                "event team meeting /from next Mon 2pm /to 4pm");

        assertInstanceOf(AddCommand.class, command);
    }

    @Test
    void parseDeleteWithTaskNumberReturnsDeleteCommand()
            throws DingleberryException {
        Command command = Parser.parse("delete 2");

        assertInstanceOf(DeleteCommand.class, command);
    }

    @Test
    void parseMarkWithTaskNumberReturnsMarkCommand()
            throws DingleberryException {
        Command command = Parser.parse("mark 1");

        assertInstanceOf(MarkCommand.class, command);
        assertFalse(command.isExit());
    }

    @Test
    void parseMarkWithoutNumberThrowsIncorrectParameters() {
        assertThrows(DingleberryException.class, () -> Parser.parse("mark"));
    }

    @Test
    void parseMarkWithNonNumericNumberThrowsIncorrectParameters() {
        assertThrows(DingleberryException.class,
                () -> Parser.parse("mark one"));
    }

    @Test
    void parseUnmarkWithTaskNumberReturnsUnmarkCommand()
            throws DingleberryException {
        Command command = Parser.parse("unmark 1");

        assertInstanceOf(UnmarkCommand.class, command);
        assertFalse(command.isExit());
    }

    @Test
    void parseUnmarkWithoutNumberThrowsIncorrectParameters() {
        assertThrows(DingleberryException.class, () -> Parser.parse("unmark"));
    }

    @Test
    void parseUnmarkWithNonNumericNumberThrowsIncorrectParameters() {
        assertThrows(DingleberryException.class,
                () -> Parser.parse("unmark one"));
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
                DingleberryException.class,
                () -> Parser.parse("archive notes"));

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
        assertThrows(DingleberryException.class,
                () -> Parser.parse("todo   "));
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
        DingleberryException exception = assertThrows(
                DingleberryException.class,
                () -> Parser.parse("deadline submit report"));

        assertEquals(
                "A deadline needs a description and '/by <date>' in the"
                        + " format yyyy-MM-dd HHmm, e.g. 2019-12-02 1800.",
                exception.getMessage());
    }

    @Test
    void parseDeadlineWithInvalidDateThrowsIncorrectParameters() {
        DingleberryException exception = assertThrows(
                DingleberryException.class,
                () -> Parser.parse("deadline submit report /by tomorrow"));

        assertEquals(
                "I couldn't understand that date/time. Use yyyy-MM-dd HHmm,"
                        + " a weekday such as Mon or next Monday, or a time"
                        + " such as 2:30pm.", exception.getMessage());
    }

    @Test
    void parseEventWithoutEndTimeThrowsIncorrectParameters() {
        DingleberryException exception = assertThrows(
                DingleberryException.class,
                () -> Parser.parse(
                        "event team meeting /from 2026-08-25 1400"));

        assertEquals(
                "An event needs a description, '/from <time>',"
                        + " and '/to <time>' in the format"
                        + " yyyy-MM-dd HHmm, e.g. 2019-12-02 1800.",
                exception.getMessage());
    }

    @Test
    void parseEventWithInvalidDateThrowsIncorrectParameters() {
        DingleberryException exception = assertThrows(
                DingleberryException.class,
                () -> Parser.parse(
                        "event team meeting /from tomorrow"
                                + " /to 2026-08-25 1500"));

        assertEquals(
                "I couldn't understand that date/time. Use yyyy-MM-dd HHmm,"
                        + " a weekday such as Mon or next Monday, or a time"
                        + " such as 2:30pm.", exception.getMessage());
    }

    @Test
    void parseDateTimeWithNaturalWeekdayReturnsNextOccurrence()
            throws DingleberryException {
        assertEquals(NEXT_MONDAY_MIDNIGHT,
                Parser.parseDateTime("Mon", TUESDAY, null));
        assertEquals(NEXT_MONDAY_AFTERNOON,
                Parser.parseDateTime("monday 2.30pm", TUESDAY, null));
    }

    @Test
    void parseDateTimeWithRelativeWeekdayHonorsThisAndNext()
            throws DingleberryException {
        assertEquals(NEXT_MONDAY_MIDNIGHT,
                Parser.parseDateTime("this Mon", MONDAY, null));
        assertEquals(FOLLOWING_MONDAY_MIDNIGHT,
                Parser.parseDateTime("next Mon", MONDAY, null));
    }

    @Test
    void parseDateTimeWithTimeOnlyUsesInheritedDate()
            throws DingleberryException {
        assertEquals(INHERITED_THREE_PM,
                Parser.parseDateTime("3pm", TUESDAY, MONDAY));
        assertEquals(INHERITED_TWO_THIRTY_AM,
                Parser.parseDateTime("2:30am", TUESDAY, MONDAY));
    }
}
