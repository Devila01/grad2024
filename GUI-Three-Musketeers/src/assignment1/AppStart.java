package assignment1;

import javafx.application.Application;
import javafx.stage.Stage;

public class AppStart extends Application {

	View view;
	
	public void start(Stage stage) throws Exception {
        this.view = new View(stage);
		
	}
	
	public static void main(String[] string) {
		launch();

	}
}
