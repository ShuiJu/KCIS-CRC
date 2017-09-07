package pers.wusatosi.CRC.tests;

import javafx.application.Application;
import javafx.stage.Stage;
import pers.wusatosi.CRC.JavaFxUI.UIEventProcesser;
import pers.wusatosi.CRC.JavaFxUI.UIEventProcesser.FriendlyTask;
import static java.lang.System.out;

public class TaskT extends Application{

	public static void main(String[] args) throws Throwable{
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		out.println("launched");
		FriendlyTask<Void> t = UIEventProcesser.getInstance().submit(() -> {
			System.err.println("Runed");
			return null;
		});
		t.stateProperty().addListener((a,b,c) -> out.println(c));
		for (int i=0;i<10;i++) UIEventProcesser.getInstance().execute(t);;
	}

}
