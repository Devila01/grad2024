package assignment1;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javafx.application.Application;
import java.util.Random;

public class ThreeMusketeers {

    private final Board board;
    private Agent musketeerAgent, guardAgent;
    private final Scanner scanner = new Scanner(System.in);
    private final List<Move> moves = new ArrayList<>();
    private final List<Move> undoneMoves = new ArrayList<>();
    private final List<BoardMemento> mementos = new ArrayList<>();
    private final AppWindow appWindow = new AppWindow();

    // All possible game modes W/ ADDTION OF HUMAN RANDOM MODE
    public enum GameMode {
        Human("Human vs Human"),
        HumanRandom("Human vs Computer (Random)"),
        HumanGreedy("Human vs Computer (Greedy)"),
        HumanRandomMode("Human vs Randomized Mode");

        private final String gameMode;
        GameMode(final String gameMode) {
            this.gameMode = gameMode;
        }
    }

    /**
     * Default constructor to load Starter board
     */
    public ThreeMusketeers() {
        this.board = new Board();
    }

    /**
     * Constructor to load custom board
     * @param boardFilePath filepath of custom board
     */
    public ThreeMusketeers(String boardFilePath) {
        this.board = new Board(boardFilePath);
    }

    /**
     * Play game with human input mode selector
     */
    public void play(){
        System.out.println("Welcome! \n");
        System.out.println("[DEBUG] Launching timer window...");
    	//Application.launch(AppWindow.class, new String[]{});

    	/*if (this.appWindow == null) {
    		System.out.println("It's null.");
    	}
    	else {
    		System.out.println("It's not null.");
    	}*/
    	
        final GameMode mode = getModeInput();
        System.out.println("Playing " + mode.gameMode);
        play(mode);
    }

    /**
     * Play game without human input mode selector
     * @param mode the GameMode to run
     */
    public void play(GameMode mode){
        selectMode(mode);
        runGame();
    }

    /**
     * Mode selector sets the correct agents based on the given GameMode
     * @param mode the selected GameMode
     */
    private void selectMode(GameMode mode) {
        switch (mode) {
            case Human -> {
                musketeerAgent = new HumanAgent(board);
                guardAgent = new HumanAgent(board);
            }
            case HumanRandom -> {
                String side = getSideInput();
                
                // The following statement may look weird, but it's what is known as a ternary statement.
                // Essentially, it sets musketeerAgent equal to a new HumanAgent if the value M is entered,
                // Otherwise, it sets musketeerAgent equal to a new RandomAgent
                musketeerAgent = side.equals("M") ? new HumanAgent(board) : new RandomAgent(board);
                guardAgent = side.equals("G") ? new HumanAgent(board) : new RandomAgent(board);
            }
            case HumanGreedy -> {
                String side = getSideInput();
                musketeerAgent = side.equals("M") ? new HumanAgent(board) : new GreedyAgent(board);
                guardAgent = side.equals("G") ? new HumanAgent(board) : new GreedyAgent(board);
            }
        }
    }

    /**
     * Runs the game, handling human input for move actions
     * Handles moves for different agents based on current turn 
     */
    private void runGame() {
        while(!board.isGameOver()) {
            System.out.println("\n" + board);

            final Agent currentAgent;
            if (board.getTurn() == Piece.Type.MUSKETEER)
                currentAgent = musketeerAgent;
            else
                currentAgent = guardAgent;

            if (currentAgent instanceof HumanAgent) // Human move
                switch (getInputOption()) {
                    case "M":
                    	mementos.clear();
                        move(currentAgent);
                        break;
                    case "U":
                        if (moves.size() == 0) {
                            System.out.println("No moves to undo.");
                            continue;
                        }
                        else if (moves.size() == 1 || isHumansPlaying()) {
                            undoMove();
                        }
                        else {
                            undoMove();
                            undoMove();
                        }
                        break;
                    case "S":
                        board.saveBoard();
                        break;
                    case "R":
                    	if (mementos.size() == 0) {
                    		System.out.println("No undone moves to redo.");
                    		continue;
                    	}
                    	else if (mementos.size() == 1 || isHumansPlaying()) {
                    		redoMove();
                    	}
                    	else {
                    		redoMove();
                    		redoMove();
                    	}
                    case "F":
                    	changeAgentType();
                    	break;
                }
            else { // Computer move
                System.out.printf("[%s] Calculating move...\n", currentAgent.getClass().getSimpleName());
                move(currentAgent);
            }
        }

        System.out.println(board);
        System.out.printf("\n%s won!%n", board.getWinner().getType());
    }

    /**
     * Gets a move from the given agent, adds a copy of the move using the copy constructor to the moves stack, and
     * does the move on the board.
     * @param agent Agent to get the move from.
     */
    protected void move(final Agent agent) {
        final Move move = agent.getMove();
        moves.add(new Move(move));
        board.move(move);
    }

    /**
     * Removes a move from the top of the moves stack and undoes the move on the board.
     */
    private void undoMove() {
    	undoneMoves.add(moves.remove(moves.size() - 1));
    	mementos.add(board.getMemento());
        board.undoMove(undoneMoves.get(undoneMoves.size() - 1));
        System.out.println("Undid the previous move.");
    }
    
    private void redoMove() {
    	moves.add(undoneMoves.remove(undoneMoves.size() - 1));
	    board.restore(mementos.get(mementos.size() - 1));
		mementos.remove(mementos.size() - 1);
		
    }

    /**
     * Get human input for move action
     * @return the selected move action, 'M': move, 'U': undo, and 'S': save
     */
    private String getInputOption() {
        System.out.printf("[%s] Enter 'M' to move, 'U' to undo, 'R' to redo, 'F' to switch modes, and 'S' to save: ", board.getTurn().getType());
        while (!scanner.hasNext("[MURFSmurfs]")) {
            System.out.print("Invalid option. Enter 'M', 'U', 'R', 'F', or 'S': ");
            scanner.next();
        }
        return scanner.next().toUpperCase();
    }

    /**
     * Returns whether both sides are human players
     * @return True if both sides are Human, False if one of the sides is a computer
     */
    private boolean isHumansPlaying() {
        return musketeerAgent instanceof HumanAgent && guardAgent instanceof HumanAgent;
    }

    /**
     * Get human input for side selection
     * @return the selected Human side for Human vs Computer games,  'M': Musketeer, G': Guard
     */
    private String getSideInput() {
        System.out.print("Enter 'M' to be a Musketeer or 'G' to be a Guard: ");
        while (!scanner.hasNext("[MGmg]")) {
            System.out.println("Invalid option. Enter 'M' or 'G': ");
            scanner.next();
        }
        return scanner.next().toUpperCase();
    }

    /**
     * Get human input for selecting the game mode
     * @return the chosen GameMode
     * 
     * ADDED ADDITIONAL OPTIONS IN MENU
     */
    private GameMode getModeInput() {
        System.out.println("""
                    0: Human vs Human
                    1: Human vs Computer (Random)
                    2: Human vs Computer (Greedy)
                    3: Random mode(Human vs Computer (Random) OR Human vs Computer (Greedy))""");
        System.out.print("Choose a game mode to play i.e. enter a number: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid option. Enter 0, 1, 2, or 3: ");
            scanner.next();
        }
        final int mode = scanner.nextInt();
        if (mode < 0 || mode > 3) {
            System.out.println("Invalid option.");
            return getModeInput();
        }
        if (mode == 3) {
        	ConcreteFactory factory = new ConcreteFactory(board);
        	Agent agent = getRandomizedAgent(factory);
        	setRandomizedAgent(agent);
        }
        return GameMode.values()[mode];
    }
    private void changeAgentType() {
    	Agent otherAgent;
    	String s = "";
    	if (board.getTurn() == Piece.Type.MUSKETEER) {
    		otherAgent = guardAgent;
    		s = "musketeer";
    	}
        else {
            otherAgent = musketeerAgent;
            s = "guard";
        }
    	ConcreteFactory fac = new ConcreteFactory(board);

    	if (otherAgent instanceof RandomAgent) {
    		if (s == "musketeer") {
    			guardAgent = fac.getAgent(2);
    		}
    		else {
    			musketeerAgent = fac.getAgent(2);
    		}
			System.out.println("Mode has been changed to GreedyAgent");
    	}
    	else if(otherAgent instanceof GreedyAgent) {
    		if (s == "guard") {
    			musketeerAgent = fac.getAgent(1);
    		}
    		else {
    			guardAgent = fac.getAgent(1);
    		}
    		System.out.println("Mode has been changed to RandomAgent");
    	}
    	else {
    		System.out.println("Can only change agents if Human vs Computer");
    	}
    }
    public void setRandomizedAgent(Agent agent) {
    	Boolean chosen = false;
    	while(! chosen) {
    		String side = getSideInput();
    		if (side.equals("M")) {
    			musketeerAgent = new HumanAgent(board);
    			guardAgent = agent;
    			chosen = true;
    		}
    		else if (side.equals("G")) {
    			musketeerAgent = agent;
    			guardAgent = new HumanAgent(board);
    			chosen = true;
    		}
    	}    	
    }
    public Agent getRandomizedAgent(Factory factory) {
    	Random rand = new Random();
    	int randomNum = rand.nextInt(2) + 1;
    	Agent agent = factory.getAgent(randomNum);
    	return agent;
    	
    }
    public static void main(String[] args) {
        String boardFileName = "Boards/Starter.txt";
        ThreeMusketeers game = new ThreeMusketeers(boardFileName);
        game.play();
    }
}
