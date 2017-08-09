package application;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import pers.Brad.CRC.CRC.RollCallUtil;
import pers.Brad.CRC.CRC.loginedUser;
import pers.Brad.CRC.JavaFxUI.Loading.Loading;
import pers.Brad.CRC.JavaFxUI.Login.loginController;
import pers.Brad.CRC.JavaFxUI.Main.MainController;


public class Main extends Application {
	
	@Override
	public void start(Stage stage) throws Exception {
		Loading loadingSign=new Loading(new Stage());
		loadingSign.start();
		loginController lc=new loginController();
		loginedUser lu=lc.start(new Stage());
		loadingSign.show();
		if (lu==null) System.exit(0);
		new Thread(()->{
			RollCallUtil rcu;
			try {
				rcu = new RollCallUtil(lu);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
			Platform.runLater(()->{
				try{
				MainController mc=new MainController();
				mc.start(stage, rcu);;
				stage.show();
				loadingSign.close();
				stage.setOnCloseRequest((a)->{
					System.exit(0);
				});
				}catch (Throwable e){
					throw new RuntimeException(e);
				}
			});
		}).start();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
