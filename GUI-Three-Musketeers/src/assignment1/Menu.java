package assignment1;

import java.io.IOException;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

public class Menu extends GridPane {
	private final View view;
	protected TextField textField;
	private Label label; 
	private Label errorLabel = new Label("");

    // Creates the Menu where users can enter their username, and year of birth (if account has not already been made) 
    public Menu(View view, String s) {
        this.view = view;
        this.setAlignment(Pos.CENTER);
        this.setVgap(10);

        menuElementsCreator(s);
    }

    // Creates the TextFields, Labels, and Buttons on the Menu 
    // Same code can be used to display both the username and birth year menu (facade pattern)
    private void menuElementsCreator(String s){
    	// Textfields
    	textField = new TextField(s); 
    	
    	// Labels 
    	label = new Label("Please Enter a " + s);
        Font usernameFont = new Font(20); // sets the size of the text 
        label.setFont(usernameFont);
    	label.setStyle("-fx-text-fill: #e8e6e3;");
    	
    	// Button that user clicks after entering username and birth year
    	Button button = new Button("Submit");
        button.setPrefSize(300, 50);  // sets the size of the button 
        button.setFont(new Font(15)); // sets the font size of the text inside the button 
        button.setStyle("-fx-background-color: #404040; -fx-text-fill: white;");
        
        // Button click handler depends on whether user submitted username or birth year
        if (s == "Username") {
        	button.setOnAction(e -> {
				try {
					this.view.usernameButton();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
        }
        else {
        	button.setOnAction(e -> {
				try {
					this.view.birthButton();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
        }
        
        // Adds the label, textfields, and buttons to the gridpane
        this.add(label, 0, 0);
        this.add(textField, 0, 1);
        this.add(button, 0, 2);
        this.add(errorLabel, 0, 3);
  
    }
    
    // Sets the text in the label underneath the textfields when username or birth year is invalid
    protected void setErrorLabel(String s) {
    	this.errorLabel.setText(s);
    	
    	
    	
    }
    
    
    
    
    
}
