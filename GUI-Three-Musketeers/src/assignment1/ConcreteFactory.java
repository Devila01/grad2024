package assignment1;

public class ConcreteFactory extends Factory{
	Board board;
	public ConcreteFactory(Board board) {
		this.board = board;
	}
	public Agent getAgent(int number) {
		
		if (number == 1) {
			RandomAgent rAgent = new RandomAgent(board);
			return rAgent;
		}
		else {
			GreedyAgent gAgent = new GreedyAgent(board);	
			return gAgent;
		}
	}
}
