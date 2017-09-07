package pers.wusatosi.CRC.tests;

import java.awt.Button;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class FXMLTest extends Application{

	public static void main(String[] args) {
		launch();
	}

	private static Boolean t = false;
	
	public FXMLTest() {
		System.out.println("Called");
		if (t) {
			throw new Error();
		}
		t = true;
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader fxmll = new FXMLLoader(FXMLTest.class.getResource("T.fxml"));
		fxmll.load();
	}
	
	@FXML
	private Button Button; 
	
	
}
