package assignment1;

// The Composite Design Pattern: treat the board and components of the board (cells) in the same way. 
// Has all the methods that the board and all the cells in the board share. 
public interface BoardComponent {

	// Implementation with GUI: change color of board/cells using a similar approach as A2
	// Implementation w/o GUI: change the color of the board in the console output. Issues:
	// -- Will no longer need to use composite design pattern 
	// -- Eclipse does not support console text color change
	public void changeColor(); 
	
}
