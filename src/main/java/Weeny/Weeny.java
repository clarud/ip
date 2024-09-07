package weeny;

import weeny.ui.Ui;
import weeny.task.Todo;
import weeny.task.Events;
import weeny.task.Deadlines;
import weeny.task.TaskList;
import weeny.task.Task;
import weeny.storage.Storage;
import weeny.parser.Parser;
import weeny.ui.WeenyGui;

import javafx.application.Application;
import javafx.stage.Stage;



/**
 * Main class for the Weeny task management application.
 * Manages user commands and handles tasks.
 */
public class Weeny extends Application {
    private final Ui ui;
    private final Storage storage;
    private final TaskList taskList;
    private final Parser parser;

    public Weeny() {
        ui = new Ui(); // UI for user interactions
        storage = new Storage(); // Manages task data storage
        parser = new Parser(); // Parses user input
        taskList = new TaskList(); // List of tasks
        try {
            // Ensure data directory and TaskList.txt file exist
            storage.createFileIfNotExist("./data", "TaskList.txt");

            // Load tasks from file
            taskList.getTasks().addAll(storage.loadTask("./data/TaskList.txt"));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void start(Stage stage) {
        WeenyGui weenyGui = new WeenyGui();
        weenyGui.start(stage);
    }


    /**
     * Starts the Weeny application.
     * Initializes UI, storage, parser, and task list. Processes user commands.
     *
     * @param input Command-line arguments (not used).
     */
    public String executeWeeny(String input) {

        boolean isFarewell = false;
        while (!isFarewell) {
            String command = parser.extractFirstWord(input);

            try {
                switch (command) {
                case "list":
                    return ui.showTaskList(taskList.getTasks());

                case "bye":
                    isFarewell = true;
                    storage.saveTask("./Data/TaskList.txt", taskList.getTasks());
                    break;

                case "mark":
                    int markIndex = parser.extractEndNumber(input) - 1;
                    validateIndex(markIndex, taskList.size(), "mark");
                    taskList.markAsDone(markIndex);
                    return ui.showMarkMessage(taskList.getTask(markIndex));

                case "unmark":
                    int unmarkIndex = parser.extractEndNumber(input) - 1;
                    validateIndex(unmarkIndex, taskList.size(), "unmark");
                    taskList.markAsNotDone(unmarkIndex);
                    return ui.showUnmarkMessage(taskList.getTask(unmarkIndex));

                case "todo":
                    validateTodoInput(input);
                    Task todoTask = new Todo(input.substring(5).trim());
                    taskList.addTask(todoTask);
                    return ui.printTaskAddedMessage(todoTask, taskList.size());

                case "event":
                    validateEventInput(input);
                    Task eventTask = new Events(parser.extractEventName(input),
                            parser.extractEventTimes(input)[0],
                            parser.extractEventTimes(input)[1]);
                    taskList.addTask(eventTask);
                    return ui.printTaskAddedMessage(eventTask, taskList.size());

                case "deadline":
                    validateDeadlineInput(input);
                    Task deadlineTask = new Deadlines(parser.extractDeadlineName(input),
                            parser.extractDeadlineTime(input));
                    taskList.addTask(deadlineTask);
                    return ui.printTaskAddedMessage(deadlineTask, taskList.size());

                case "delete":
                    int deleteIndex = parser.extractEndNumber(input) - 1;
                    validateIndex(deleteIndex, taskList.size(), "delete");
                    Task removedTask = taskList.getTask(deleteIndex);
                    taskList.deleteTask(deleteIndex);
                    return ui.showTaskDeletedMessage(removedTask, taskList.size());

                case "find":
                    String keyWord = input.substring(5);
                    return ui.showSearchResult(taskList.findTask(keyWord));

                default:
                    throw new UnsupportedOperationException("Unknown command");
                }
            } catch (IllegalArgumentException | IndexOutOfBoundsException | UnsupportedOperationException e) {
                return ui.showError(e.getMessage());
            }
        }
        storage.saveTask("./Data/TaskList.txt", taskList.getTasks());
        return ui.showGoodbyeMessage();
    }

    /**
     * Checks if the index is within the task list range.
     *
     * @param index The index to check.
     * @param size The size of the task list.
     * @param action The action being performed (e.g., "mark", "delete").
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    protected static void validateIndex(int index, int size, String action) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Invalid index for " + action + " action.");
        }
    }

    /**
     * Validates input for a "todo" command.
     *
     * @param input The command input.
     * @throws IllegalArgumentException if the input is too short.
     */
    private static void validateTodoInput(String input) {
        if (input.length() <= 5) {
            throw new IllegalArgumentException("To-Do description is too short.");
        }
    }

    /**
     * Validates input for an "event" command.
     *
     * @param input The command input.
     * @throws IllegalArgumentException if the input lacks necessary details.
     */
    private static void validateEventInput(String input) {
        if (input.length() <= 6 || !input.contains("/from") || !input.contains("/to")) {
            throw new IllegalArgumentException("Event details are incomplete.");
        }
    }

    /**
     * Validates input for a "deadline" command.
     *
     * @param input The command input.
     * @throws IllegalArgumentException if the input lacks necessary details.
     */
    private static void validateDeadlineInput(String input) {
        if (input.length() <= 9 || !input.contains("/by")) {
            throw new IllegalArgumentException("Deadline details are incomplete.");
        }
    }
}
