package application;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import pers.wusatosi.CRC.CRCApi.RollCallUtil;
import pers.wusatosi.CRC.CRCApi.loginedUser;
import pers.wusatosi.CRC.JavaFxUI.LoadStage.Loading;
import pers.wusatosi.CRC.JavaFxUI.Login.loginController;
import pers.wusatosi.CRC.JavaFxUI.Main.MainController;


public class Main extends Application {
	
	@Override
	public void start(Stage stage) throws Exception {
		Loading loadingSign=new Loading(new Stage());
		loadingSign.start();
		loginController lc=new loginController();
		loginedUser lu=lc.start(new Stage());
		loadingSign.show();
		if (lu==null) System.exit(0);
		RollCallUtil rcu = new RollCallUtil(lu);
		MainController mc=new MainController();
		mc.start(stage, rcu);;
		stage.show();
		loadingSign.close();
		stage.setOnCloseRequest((a)->{
			Platform.exit();
			System.exit(0);
		});
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
