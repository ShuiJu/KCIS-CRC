package pers.wusatosi.CRC.tests;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pers.wusatosi.CRC.JavaFxUI.Login.ConnectionCheck.ConnectionCheckHelper;

public class ConnectionHelperTest extends Application{
	
	public static void main(String[] args) {
		launch();
		System.exit(0);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		ConnectionCheckHelper helper = ConnectionCheckHelper.BuildConnectionHelper((e) -> {});
		primaryStage.setScene(new Scene(helper));
		primaryStage.showingProperty().addListener(System.out::println);
		primaryStage.show();
		helper.addListener((a) -> Platform.exit());
		
	}
	
}
