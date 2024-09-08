package assignment1;

public class BoardMemento {
	public final Cell[][] boardState;
    private Piece.Type turn;
    
	public BoardMemento(Piece.Type turn, Cell[][] board) {
		//winner doesn't need to be saved because board can recalculate it by calling isGameOver
		//size doesn't need to be saved because it's basically a constant the TAs didn't mark final,
		//nothing changes it at runtime, it will always be whatever it's hardcoded as, i.e. 5.
		this.boardState = new Cell[board.length][board[0].length];
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				this.boardState[i][j] = new Cell(board[i][j]);
			}
		}
		this.turn = turn;
	}
	
	//board can call both of these one at a time
	//remember to have Board call isGameOver after restoring from a memento to recalculate winner
	public Cell[][] getBoardState() {
		return boardState;
	}
	
	public Piece.Type getTurn() {
		return turn;
	}
}
