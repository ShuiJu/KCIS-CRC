package pers.Brad.CRC.JavaFxUI.Main;


import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.stage.Stage;
import pers.Brad.CRC.CRC.RollCallUtil;
import pers.Brad.CRC.JavaFxUI.Main.RollCall.RollCallField;
import pers.Brad.CRC.Util.StudentImageLibrary;

public class MainController{
	private static final String layoutFilePath="mainlayout.fxml";

	public void start(Stage stage,RollCallUtil rcl) throws Exception {
		rcl.getShouldBeStudentInfoList().forEach((stu)->{
				StudentImageLibrary.preloadStudentImage(stu.getID());
		});
		//setup confirm
		if (VerticalBackgroundImage==null||HorizontalBackgroundImage==null) throw new java.lang.ExceptionInInitializerError();
		FXMLLoader fxmll=new FXMLLoader(getClass().getResource(layoutFilePath));
		fxmll.setController(this);
		fxmll.load();
		stage.setScene(new Scene(MainPane));
		stage.setResizable(true);
		stage.setMinHeight(500);
		stage.setMinWidth(600);
		MainPane.setBackground(new Background(new BackgroundImage(
				VerticalBackgroundImage,
				BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER,
				new BackgroundSize(670,820,false,false,false,false)))); //2976 3968
		MainPane.widthProperty().addListener((oa)->OnMainPaneSizeChange());
		MainPane.heightProperty().addListener((oa)->OnMainPaneSizeChange());
		MainSplitPane.applyCss();
		loginedUserBlock.setText(rcl.user.getUserName());
		ObservableList<MenuItem> Items = loginedUserBlock.getItems();
		CustomMenuItem Custom = (CustomMenuItem) Items.get(0);
		Custom.setHideOnClick(false);
		//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		loginedUserBlock.getItems().clear();
		AnchorPane rollcallfield=(AnchorPane) new RollCallField(rcl);
		rollcallfield.prefWidthProperty().bind(ApplicationDisplay.widthProperty());
		rollcallfield.prefHeightProperty().bind(ApplicationDisplay.heightProperty());
		ApplicationDisplay.getChildren().add(rollcallfield);
	}
	
	private final Image VerticalBackgroundImage=new Image(getClass().getResourceAsStream("Background_vertical.jpg"));;
	private final Image HorizontalBackgroundImage=new Image(getClass().getResourceAsStream("Background_horizontal.jpg"));
	
	private void OnMainPaneSizeChange(){
		final double width=MainPane.getWidth();
		final double height=MainPane.getHeight();
		if (height>width)
			MainPane.setBackground(new Background(new BackgroundImage(
					VerticalBackgroundImage,
					BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,
					BackgroundPosition.CENTER,
					new BackgroundSize(width, height, false, false, false, false)
			)));
		else
			MainPane.setBackground(new Background(new BackgroundImage(
					HorizontalBackgroundImage,
					BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,
					BackgroundPosition.CENTER,
					new BackgroundSize(width, height, false, false, false, false)
				)));
		ApplicationDisplay.setPrefHeight(MainPane.getHeight());
		ApplicationDisplay.setPrefWidth(MainPane.getWidth());
	}
	
    @FXML
    private MenuButton loginedUserBlock;

    @FXML
    private Button rollCallButton;

    @FXML
    private AnchorPane MainPane;

    @FXML
    public Button timeRecordButton;
    
    @FXML
    private SplitPane MainSplitPane;
    
    @FXML
    private SplitPane OutmostSplitPane;
    
    @FXML
    private AnchorPane ApplicationDisplay;
    
    @FXML
    private void RollCallCallOnAction(){
    	System.out.println("RollCall");
    }
    
    @FXML
    private void TimeRecordCallOnAction(){
    	System.out.println("Time");
    }
}