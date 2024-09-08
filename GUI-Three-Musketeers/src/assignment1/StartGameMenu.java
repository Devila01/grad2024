package assignment1;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import assignment1.ThreeMusketeers;

public class StartGameMenu extends GridPane {
	private final View view;
	private Button playButton;
	private Label label;
	private Label ratioLabel; // win/lose ratio for returning player
	private Label rulesLabel; // displays the rules to a new player 
	private boolean isExistingUser;

    // Creates the Menu where users can enter their username, and year of birth (if account has not already been made) 
    public StartGameMenu(View view, boolean isExistingUser) {
        this.isExistingUser = isExistingUser;
    	
    	this.view = view;
        this.setAlignment(Pos.CENTER);
        this.setVgap(10);
        
        // Creates play game button 
    	Button playButton = new Button("Start Game");
        playButton.setPrefSize(300, 50);  // sets the size of the button 
        playButton.setFont(new Font(15)); // sets the font size of the text inside the button 
        playButton.setStyle("-fx-background-color: #404040; -fx-text-fill: white;");
        playButton.setOnAction(e -> this.playGame());  // start the game on the console using the three musketeer class 
        
        // Creates a label 
    	label = new Label("");
        Font usernameFont = new Font(30); // sets the size of the text 
        label.setFont(usernameFont);
    	label.setStyle("-fx-text-fill: #e8e6e3;");
        
        // Adds the buttons and labels to the gridpane 
        this.add(playButton, 0, 2);
        this.add(label, 0, 0);
        
        if (this.isExistingUser) {
        	this.existingUser();
        }
        
        else {
        	this.newUser();
        }
    }
    
    // Displays the win/lose ratio of the existing user, and a play game button
    private void existingUser() {
    	this.setLabel("Welcome Back " + view.username + "!");
    	
    	// Show win/lose ratio 
    	ratioLabel = new Label(this.view.won + "\n" + this.view.lost);
        Font font = new Font(20); // sets the size of the text 
        ratioLabel.setFont(font);
    	ratioLabel.setStyle("-fx-text-fill: #8E8E8E;");
        
    	this.add(ratioLabel, 0, 1);
    	
    }
    
    // Displays a window with the rules of the game, plus a play button 
    private void newUser() {
    	this.setLabel("Welcome " + view.username + "!");
    	
    	// Show rules of the game
    	rulesLabel = new Label("How to Play:" + '\n' + "...insert rules here...");
        Font font = new Font(10); // sets the size of the text 
        rulesLabel.setFont(font);
    	rulesLabel.setStyle("-fx-text-fill: #8E8E8E;");
    	
    	this.add(rulesLabel, 0, 1);
    }
    
    private void setLabel(String s) {
    	this.label.setText(s);
    }
    
    // Handles the click of the play game button
    private void playGame() {
    	this.view.closeGUI();
    	assignment1.ThreeMusketeers.main(null);
    }
    
    
    
    
    
    
    
    
}