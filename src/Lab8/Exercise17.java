package Lab8;


import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class Exercise17 {

	interface Command {
		void execute();
	}

	interface UndoableCommand extends Command {
		void undo();
	}

	interface Show {
		String description();
	}

	record Movie(String title, int year, int time) implements Show {
		public String description() {
			return title + " (" + year + ", " + time + " min)";
		}
	}

	enum Day {
		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
	}

	static final Show NULL_SHOW = () -> "";

	static class Program {
		private final EnumMap<Day, Show> aShows = new EnumMap<>(Day.class);

		public Program() {
			clear();
		}

		public UndoableCommand createAddCommand(Show pShow, Day pDay) {
			return new UndoableCommand() {
				public void execute() { add(pShow, pDay); }
				public void undo() { remove(pDay); }
			};
		}

		public UndoableCommand createRemoveCommand(Day pDay) {
			return new UndoableCommand() {
				Show show = aShows.get(pDay);

				public void execute() {
					show = aShows.get(pDay);
					remove(pDay);
				}

				public void undo() { add(show, pDay); }
			};
		}

		public UndoableCommand createClearCommand() {
			return new UndoableCommand() {
				EnumMap<Day, Show> previousShows = new EnumMap<>(aShows);

				public void execute() {
					previousShows = new EnumMap<>(aShows);
					clear();
				}

				public void undo() { aShows.putAll(previousShows); }
			};
		}

		private void clear() {
			for (Day day : Day.values()) {
				aShows.put(day, NULL_SHOW);
			}
		}

		private void add(Show pShow, Day pDay) {
			aShows.put(pDay, pShow);
		}

		private void remove(Day pDay) {
			aShows.put(pDay, NULL_SHOW);
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			for (Day day : aShows.keySet()) {
				result.append(String.format("%9s", day.name()))
					.append(": ")
					.append(aShows.get(day).description())
					.append("\n");
			}
			return result.toString();
		}
	}

	/** Them redo: stack thu hai chua cac lenh da undo. */
	static class CommandProcessor {
		private final List<UndoableCommand> aExecutedCommands = new ArrayList<>();
		private final List<UndoableCommand> aUndoneCommands = new ArrayList<>();

		public void consume(UndoableCommand pCommand) {
			pCommand.execute();
			aExecutedCommands.add(pCommand);
		}

		public void undoLast() {
			assert !aExecutedCommands.isEmpty();
			UndoableCommand command = aExecutedCommands.remove(aExecutedCommands.size() - 1);
			command.undo();
			aUndoneCommands.add(command);
		}

		public void redoLast() {
			assert !aUndoneCommands.isEmpty();
			consume(aUndoneCommands.remove(aUndoneCommands.size() - 1));
		}
	}

	public static void main(String[] args) {
		Program program = new Program();
		CommandProcessor processor = new CommandProcessor();

		processor.consume(program.createAddCommand(new Movie("Movie 1", 2000, 120), Day.MONDAY));
		processor.consume(program.createAddCommand(new Movie("Movie 2", 2010, 90), Day.WEDNESDAY));
		System.out.println("== Sau 2 lenh add ==\n" + program);

		processor.undoLast();
		System.out.println("== Sau undoLast ==\n" + program);

		processor.redoLast();
		System.out.println("== Sau redoLast (Movie 2 tro lai) ==\n" + program);
	}
}