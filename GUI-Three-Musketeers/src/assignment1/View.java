package assignment1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class View {
    Stage stage;
    BorderPane borderPane;
    Label titleLabel; 
    Button startButton;
    Menu menu;
    StartGameMenu startGameMenu; 
    protected String username;
    protected String won;
    protected String lost;

    public View(Stage stage) {
        this.stage = stage;
        initialize();
    }

    private void initialize() {
    	// Creates a borderPane, which divides the window into five sections: top, center, bottom, left, and right
        borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: #557BAF;");  // background color of welcome window 
        
        // Sets the title of the window 
        titleLabel = new Label("Play Three Musketeers");
        Font titleFont = new Font(40);  // sets the font size 
        titleLabel.setFont(titleFont);
        titleLabel.setStyle("-fx-text-fill: #e8e6e3");  // sets the font color 

        
        VBox label = new VBox(titleLabel);
        label.setAlignment(Pos.CENTER);
        borderPane.setTop(label);
        
        // Creates the Start button on the welcome screen 
        startButton = new Button("Start");
        startButton.setPrefSize(150, 50);
        borderPane.setCenter(startButton);
        startButton.setOnAction(e -> startButton());
        
        // Shows the window
        var scene = new Scene(borderPane, 800, 500);
        stage.setScene(scene);
        stage.show();
    
    }
    
    // Handles the Start button click in the welcome window 
    private void startButton() {
    	this.displayMenu("Username");
    }
    
    // displays the menu where the user can enter a username and birth year 
    private void displayMenu(String s){
        // Changes window background color 
    	borderPane.setStyle("-fx-background-color: #7855AF");
    	
    	// Displays username menu using Menu class
    	this.menu = new Menu(this, s);
        VBox vBox = new VBox(50, menu);
        vBox.setAlignment(Pos.CENTER);
        borderPane.setCenter(vBox);

    }
    
    // Displays the startGameMenu where user can see their win/lose ratio (if existing player) or 
    // the rules of the game (if new user)
    // Also shows a play game button 
    private void displayStartGameMenu(boolean existingUser) {
        // Changes window background color 
    	borderPane.setStyle("-fx-background-color: #4D3179;");
    	
    	// Displays username menu using Menu class
    	this.startGameMenu = new StartGameMenu(this, existingUser);
        VBox vBox = new VBox(50, startGameMenu);
        vBox.setAlignment(Pos.CENTER);
        borderPane.setCenter(vBox);

    }
    
    
    
    // Handles the button clicks of the user entering in username
    protected void usernameButton() throws IOException{
    	// Check if valid username (atleast  3 characters long), otherwise display and error message  
    	String input = this.menu.textField.getText();
    	if (input.length() < 3) {
    		this.menu.setErrorLabel("Please enter a username with atleast 3 characters.");
    	}
    	
    	
    	else {
    		this.username = input;
    		// Check if user already exists in .txt file 
    		// - If returning user:
    		if (this.userExists(input)) {
    			// Display GUI with win/lose ratio, and play game button
    			this.displayStartGameMenu(true);
    		}
    
    		// - If new user: 
    		else {
    			// Call on addUserData method 
    			this.addUserData(input, "USER:");
    			
    			// Display menu where user can enter birth year
    			this.displayMenu("Birth Year");
    		}
    	}    	
    }
    
    // Handles the button clicks of the user entering in birth year
    protected void birthButton() throws IOException {
    	// Check if valid birth year (between 1900 and 2021), otherwise display and error message  
    	String input = this.menu.textField.getText();
    	if (!input.chars().allMatch( Character::isDigit )){  // input isn't a number 
    		this.menu.setErrorLabel("Invalid year."); 
    	}
    	
    	else {
    		int year = Integer.parseInt(input);
    		if (year < 1900 || year > 2021) {   // year doesn't make sense 
    			this.menu.setErrorLabel("Invalid year.");
    		}
    		
    		else {
    			// Call on addUserData method 
    			this.addUserData(input, "BIRTH YEAR:");
    			
    			// Display screen with welcome and play button 
    			this.displayStartGameMenu(false);
    		}
    	}

    }
    
    // Returns whether the user already exists in userData.txt
    private boolean userExists(String username) throws FileNotFoundException {
    	
    	// The useData.txt file is in the form, where each user has four lines dedicated to them: 
    	// USER: 
    	// BIRTH YEAR: 
    	// WIN:
    	// LOSE:
    	// ... 
    	
    	File file = new File("userData.txt"); 
    	
    	Scanner scanner = new Scanner(file);
    	
    	while (scanner.hasNextLine()) {
    		String line = scanner.nextLine();
    		
    		if (line.startsWith("USER:")) {
    			if (line.contains(username)) { // problems could arise if user sets username as USER: 
    				// Store the games won and games lost for the user, to use in StartGameMenu:
    				scanner.nextLine();
    				this.won = scanner.nextLine();
    				this.lost = scanner.nextLine();
    				
    				return true;
    			}
    		}
    	}
    	scanner.close();
    
    	return false;
    	
    }
    
    // Adds the username/birth year of a new user into a .txt file. 
    // Also creates the games won/lost section 
    // Parameter <type> is either "USER:" or "BIRTH YEAR:" 
    // Code to extract user text and put into txt file is nearly identical for the birth year and username (facade pattern)
    private void addUserData(String input, String type) throws IOException {
    	
    	// Adds username or birth year 
    	FileWriter writer = new FileWriter("userData.txt", true); // the "true" appends text to end of file 
    	writer.write("\n" + type + " " + input);
    	
    	// If entering in birth year, add win/lose underneath as well 
    	if (type == "BIRTH YEAR:") {
    		writer.write("\n" + "WON: 0" );
    		writer.write("\n" + "LOST: 0" );
    	}
    	
    	writer.close();
    	
    }
    
    protected void closeGUI() {
    	this.stage.close();
    }
    
}
