package Lab8;


import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class Exercise18 {

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

	/** Interface trich xuat tu CommandProcessor. */
	interface CommandProcessor {
		void consume(UndoableCommand pCommand);
		void undoLast();
		void redoLast();
	}

	static class BasicCommandProcessor implements CommandProcessor {
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

	/** Program nhan CommandProcessor qua constructor; client goi add/remove/clear/undo/redo. */
	static class Program {
		private final EnumMap<Day, Show> aShows = new EnumMap<>(Day.class);
		private final CommandProcessor aProcessor;

		public Program(CommandProcessor pProcessor) {
			aProcessor = pProcessor;
			doClear();
		}

		public void add(Show pShow, Day pDay) { aProcessor.consume(createAddCommand(pShow, pDay)); }
		public void remove(Day pDay)          { aProcessor.consume(createRemoveCommand(pDay)); }
		public void clear()                   { aProcessor.consume(createClearCommand()); }
		public void undo()                    { aProcessor.undoLast(); }
		public void redo()                    { aProcessor.redoLast(); }

		private UndoableCommand createAddCommand(Show pShow, Day pDay) {
			return new UndoableCommand() {
				public void execute() { doAdd(pShow, pDay); }
				public void undo() { doRemove(pDay); }
			};
		}

		private UndoableCommand createRemoveCommand(Day pDay) {
			return new UndoableCommand() {
				Show show = aShows.get(pDay);

				public void execute() {
					show = aShows.get(pDay);
					doRemove(pDay);
				}

				public void undo() { doAdd(show, pDay); }
			};
		}

		private UndoableCommand createClearCommand() {
			return new UndoableCommand() {
				EnumMap<Day, Show> previousShows = new EnumMap<>(aShows);

				public void execute() {
					previousShows = new EnumMap<>(aShows);
					doClear();
				}

				public void undo() { aShows.putAll(previousShows); }
			};
		}

		private void doClear() {
			for (Day day : Day.values()) {
				aShows.put(day, NULL_SHOW);
			}
		}

		private void doAdd(Show pShow, Day pDay) {
			aShows.put(pDay, pShow);
		}

		private void doRemove(Day pDay) {
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

	public static void main(String[] args) {
		Program program = new Program(new BasicCommandProcessor());

		program.add(new Movie("Movie 1", 2000, 120), Day.MONDAY);
		program.add(new Movie("Movie 2", 2010, 90), Day.WEDNESDAY);
		System.out.println("== Sau 2 lan add ==\n" + program);

		program.clear();
		System.out.println("== Sau clear ==\n" + program);

		program.undo();
		System.out.println("== Sau undo clear ==\n" + program);

		program.undo();
		System.out.println("== Sau undo add Movie 2 ==\n" + program);

		program.redo();
		System.out.println("== Sau redo (Movie 2 tro lai) ==\n" + program);
	}
}