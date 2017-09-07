package pers.wusatosi.CRC.tests;

import java.util.concurrent.ExecutionException;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.stage.Stage;
import pers.wusatosi.CRC.JavaFxUI.UIEventProcesser;

public class TaskTest extends Application{
	
	public static void main(String[] String) throws Throwable, ExecutionException {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		System.out.println("installed");
		Task<String> task = UIEventProcesser.getInstance().submit(()->{
			Thread.sleep(1000);
			System.out.println("Runed on " + Thread.currentThread());
			throw new Exception();
		});
		task.stateProperty().addListener((state,o,n)-> System.out.println("State changed, from "+o+" to "+n));
		task.workDoneProperty().addListener((state,o,n)-> System.out.println("work done property from "+o+" to "+n));
		try {
			task.get();
		} catch (Exception e) {
			System.err.println(e);
		}
		System.out.println("To the back");
	}
	
}
