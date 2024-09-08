package assignment1;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AppWindow extends Application{
	
	private int time = 0;
	private TextField timerDisplay;
	//private static AppWindow instance = null;
	private Timer timer;

	@Override
	public void start(Stage stage) throws Exception {
		 System.out.printf("[DEBUG] AppWindow.start() called on thread %s%n", 
                 Thread.currentThread());
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(3));
		timerDisplay = new TextField();
		
		Scene scene = new Scene(root, 200, 40);
		
		stage.setTitle("Timer");
		stage.setScene(scene);
		stage.show();
	}
	
	/*public static AppWindow getInstance()
    {
        if (instance == null) {
            instance = new AppWindow();
        } //90% sure that bit was pointless because we'll create this when we launch the window
        return instance;
    }*/
	
	public void startTimer(int time) {
		this.time = time;
	    if (timer == null) timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	        public void run() {
	            if(AppWindow.this.time > 0)
	            {
	                Platform.runLater(() -> AppWindow.this.timerDisplay.setText(String.valueOf(AppWindow.this.time)));
	                AppWindow.this.time--;
	            }
	            else {
	                timer.cancel();
	            }
	        }
	    }, 1000,1000);
	}
	
	public void pauseTimer() {
		this.timer.cancel();
		this.timer.purge();
	}
	
	public void shutdownTimer() {
		Platform.exit();
	}

}
