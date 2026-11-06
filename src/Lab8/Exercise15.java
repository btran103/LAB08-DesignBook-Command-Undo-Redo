package Lab8;

import java.util.EnumMap;

public class Exercise15 {

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

	static void print(String pTitle, Program pProgram) {
		System.out.println("== " + pTitle + " ==");
		System.out.println(pProgram);
	}

	public static void main(String[] args) {
		Program program = new Program();

		UndoableCommand command = program.createAddCommand(new Movie("Movie 1", 2000, 120), Day.MONDAY);
		command.execute();
		print("Sau add Movie 1 (MONDAY)", program);
		command.undo();
		print("Sau undo add Movie 1", program);
		command.execute();
		print("Sau execute lai add Movie 1", program);

		command = program.createAddCommand(new Movie("Movie 2", 2010, 90), Day.WEDNESDAY);
		command.execute();
		print("Sau add Movie 2 (WEDNESDAY)", program);

		command = program.createRemoveCommand(Day.MONDAY);
		command.execute();
		print("Sau remove MONDAY", program);
		command.undo();
		print("Sau undo remove MONDAY", program);

		command = program.createClearCommand();
		command.execute();
		print("Sau clear", program);
		command.undo();
		print("Sau undo clear", program);
	}
}