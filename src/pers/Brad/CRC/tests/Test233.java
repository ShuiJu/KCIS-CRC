package pers.Brad.CRC.tests;

import javafx.application.Application;
import javafx.stage.Stage;
import pers.Brad.CRC.InternetAcessPart.RollCallUtil;
import pers.Brad.CRC.InternetAcessPart.loginedUser;
import pers.Brad.CRC.JavaFxUI.Main.MainController;

public class Test233 extends Application{
	public static void main(String[] args){
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		new MainController().start(primaryStage,new RollCallUtil(new loginedUser("08426")));
		primaryStage.show();
	}
}