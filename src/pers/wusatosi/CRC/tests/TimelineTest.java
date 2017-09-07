package pers.wusatosi.CRC.tests;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TimelineTest extends Application {

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Text text = new Text("YES");
		text.setFont(Font.font(20));
		Timeline tl = new Timeline(new KeyFrame(new Duration(2000),(action) -> { System.out.println("called");text.setVisible(true);}));
		tl.setCycleCount(0);
		primaryStage.setScene(new Scene(new Pane(text)));
		primaryStage.show();
		tl.playFromStart();
	}

}
