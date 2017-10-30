package client.view;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Screen extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			VBox root = new VBox();
			HBox buttons = new HBox();
			HBox attempts = new HBox();
			HBox wordInfos = new HBox();
			HBox scoreInfos = new HBox();
			HBox guess = new HBox();
			HBox next = new HBox();
			Label toDoNext = new Label("Please connect to server");
			TextField guessField = new TextField();
			Button guessButton = new Button("Guess");
			Label wordLabel = new Label("Word = ");
			Label word = new Label();
			Label scoreLabel = new Label("Score = ");
			Label score = new Label("0");
			Label remainingFailedAttempts = new Label("Number of remaining failed attempts = ");
			Label numberOfRemainingFailedAttempts = new Label("No value");
			Button start = new Button("Start game");
			Button serverConnexion = new Button("Server connexion");
	        root.getChildren().add(wordInfos);
	        root.getChildren().add(attempts);
	        root.getChildren().add(scoreInfos);
	        root.getChildren().add(guess);
	        root.getChildren().add(buttons);
	        root.getChildren().add(next);
	        guess.getChildren().add(guessField);
	        guess.getChildren().add(guessButton);
	        wordInfos.getChildren().add(wordLabel);
	        wordInfos.getChildren().add(word);
	        scoreInfos.getChildren().add(scoreLabel);
	        scoreInfos.getChildren().add(score);
	        attempts.getChildren().add(remainingFailedAttempts);
	        attempts.getChildren().add(numberOfRemainingFailedAttempts);
	        buttons.getChildren().add(serverConnexion);
	        buttons.getChildren().add(start);
	        next.getChildren().add(toDoNext);
	        Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("The Hangman Game");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void start(String[] args) {
		launch(args);
	}
}
