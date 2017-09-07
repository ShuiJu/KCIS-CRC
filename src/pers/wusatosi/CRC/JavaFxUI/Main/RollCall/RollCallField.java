package pers.wusatosi.CRC.JavaFxUI.Main.RollCall;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import pers.wusatosi.CRC.CRCApi.ConnectionCheck;
import pers.wusatosi.CRC.CRCApi.ErrorResponse;
import pers.wusatosi.CRC.CRCApi.IDFormatException;
import pers.wusatosi.CRC.CRCApi.MapGenerater;
import pers.wusatosi.CRC.CRCApi.NoSuchPersonInDataBaseException;
import pers.wusatosi.CRC.CRCApi.NoSuchPersonOnServerException;
import pers.wusatosi.CRC.CRCApi.PersonNotFoundException;
import pers.wusatosi.CRC.CRCApi.RollCallUtil;
import pers.wusatosi.CRC.CRCApi.StanderStudent;
import pers.wusatosi.CRC.CRCApi.StudentIdentify;
import pers.wusatosi.CRC.CRCApi.loginedUser;
import pers.wusatosi.CRC.CRCApi.RollCallUtil.NotTheDayException;
import pers.wusatosi.CRC.CRCApi.StudentIdentify.StudentID;
import pers.wusatosi.CRC.Util.StudentImageLibrary;

public class RollCallField extends AnchorPane{
	
	static{
		tan90=new Image(RollCallField.class.getResourceAsStream("tan90.jpg"));
		Default_0=new Image(RollCallField.class.getResourceAsStream("Default_0.jpg"));
		Senior=new Image(RollCallField.class.getResourceAsStream("乖巧.JPG"));
		Default_1=new Image(RollCallField.class.getResourceAsStream("Default_1.jpg"));
		logger=Logger.getLogger("UI");
	}

	private static final String layoutPath="RollCallField.fxml";
	
	public RollCallField(){}
	
	public RollCallField(RollCallUtil rcl) throws IOException{
		super();
		assert rcl!=null;
		this.rcu=rcl;
		FXMLLoader fxmll=new FXMLLoader(getClass().getResource(layoutPath));
		fxmll.setController(this);
		fxmll.setRoot(this);
		fxmll.load();
		
		//--------------set property listeners-----------
		
		//infomation change cause by RollCalledList selecting change
		
		//RollCalledList
		RollCalledList.getSelectionModel().selectedItemProperty().addListener((v,o,rsi)->{
			try {
				if (rsi!=null&&rsi!=onShowing)
					changeInfoDisplay(rsi.stu);
			} catch (IOException e) {
				IOExceptionH(e);
			}			
		});
		
		//onCarList
		onCarList.getSelectionModel().selectedItemProperty().addListener((v,o,rsi)->{
			try {
				if (rsi!=null&&rsi!=onShowing)
					changeInfoDisplay(onCarList.getSelectionModel().getSelectedItem().stu);
			} catch (IOException e) {
				IOExceptionH(e);
			}
		});
		
		//onStudentInfo - ShowPic auto resize height
		Pic.imageProperty().addListener((v,old,newI)->{
			AnchorPane.setTopAnchor(onStudentInfo, 425-(400-Pic.getBoundsInLocal().getHeight()));
		});
		
		//----------TableColumn value setting--------
		
		//RollCalledList
		NameColumn.setCellValueFactory(new PropertyValueFactory<RollcalledStudentInfo0,String>("Name"));
		IDColunm.setCellValueFactory(new PropertyValueFactory<RollcalledStudentInfo0,String>("ID"));
		TemporaryColumn.setCellValueFactory(new PropertyValueFactory<RollcalledStudentInfo0,String>("IsTemporary"));
		
		//onCarList
		Name_OnCar.setCellValueFactory(new PropertyValueFactory<RollcalledStudentInfo0,String>("Name"));
		ID_OnCar.setCellValueFactory(new PropertyValueFactory<RollcalledStudentInfo0,String>("ID"));
		OnCar.setCellValueFactory(new PropertyValueFactory<RollcalledStudentInfo0,String>("IsOnCar"));
		
		//onCarList init variables
		onCarObs=FXCollections.observableArrayList();
		List<StanderStudent> shouldBelist=rcl.getShouldBeStudentInfoList();
		if (shouldBelist.size()!=0) {
			Boolean doCheckAlreadyInList=!shouldBelist.get(0).getProperty(loginedUser.onCarDefinitionKey).equals(loginedUser.NOT_CHECKED);
			for (StanderStudent stu:shouldBelist){
				RollcalledStudentInfo0 in=buildInfo(stu);
				if (rcu.user.ID.equals(stu.getID()))
					in.setIsOnCar(true);
				if (doCheckAlreadyInList&&stu.getProperty(loginedUser.onCarDefinitionKey).equals(loginedUser.Checked_ON_CAR)) {
					in.setIsOnCar(true);
					RollCalledList.getItems().add(in);
				}
				onCarObs.add(in);
			}
		}
		onCarList.itemsProperty().bind(new ReadOnlyObjectWrapper<ObservableList<RollcalledStudentInfo0>>(onCarObs));

		//infomation showing changing while TabPane switching
		TabPane.getSelectionModel().selectedIndexProperty().addListener((obsV,o,n)->{
			switch (n.intValue()){
			case 0:{
				DeleteFromTheList.setText("从名单中踢掉!");
				if (RollCalledList.getSelectionModel().getSelectedItem()!=onShowing){
					ObservableList<RollcalledStudentInfo0> list = RollCalledList.getItems();
					int listS=list.size();
					if (listS==1){
						RollCalledList.getSelectionModel().clearSelection();
						RollCalledList.getSelectionModel().select(0);;
					}else
						for (int i=0;i<listS;i++)
							if (list.get(i)==onShowing){
								RollCalledList.getSelectionModel().select(i);
								RollCalledList.scrollTo(i);
								break;
							}
				}
				break;
			}
			case 1:{
				DeleteFromTheList.setText("加入已点名列表pwp");
				if (!(onCarList.getSelectionModel().getSelectedItem()==onShowing&&onShowing.LineID.equals(rcu.LineID))){
					int listS=onCarObs.size();
					if (listS==1){
						onCarList.getSelectionModel().clearSelection();
						onCarList.getSelectionModel().select(0);
					}else
						for (int i=0;i<listS;i++){
							if (onCarObs.get(i)==onShowing){
								onCarList.getSelectionModel().select(i);
								onCarList.scrollTo(i);
								break;
							}
						}
				}
			}
			}
		});
		
		//add user to the list
		add(rcl.user.ID);
		logger.log(Level.FINER, "Roll call field loaded");
	}
	
	private RollCallUtil rcu;
	
	private static final Image tan90;
	private static final Logger logger;
	private static final Image Default_0;
	private static final Image Default_1;
	private static final Image Senior;
	
	private static final HashMap<StanderStudent,RollcalledStudentInfo0> bufferMap=new HashMap<StanderStudent,RollcalledStudentInfo0>(50);
	
	private ObservableList<RollcalledStudentInfo0> onCarObs;
	
	private RollcalledStudentInfo0 onShowing;
	
	@FXML
	private Pane InfoDisplayPane;
	
	@FXML
	private PasswordField CardNumberHandler;
	
	@FXML
	private Button CardNumberConfirm;
		
	@FXML
	private Button DeleteFromTheList;
	
	@FXML
	private TableView<RollcalledStudentInfo0> RollCalledList;
	
	@FXML
	private Button Upload;
	
	@FXML
	private TabPane TabPane;

	@FXML
	private TableColumn<RollcalledStudentInfo0,String> NameColumn;
	
	@FXML
	private TableColumn<RollcalledStudentInfo0,String> IDColunm;
	
	@FXML
	private TableColumn<RollcalledStudentInfo0,String> TemporaryColumn;
	
	@FXML
	private ImageView Pic;
	
	@FXML
	private ListView<String> onStudentInfo;
	
	@FXML
	private ToggleButton ShowPic;
	
	public class RollcalledStudentInfo0{
		
		private RollcalledStudentInfo0(StanderStudent incomeStudent){
			ID=incomeStudent.getID();
			Name=incomeStudent.getName();
			LineID=incomeStudent.getLineID()==null?"原不乘车":incomeStudent.getLineID();
			stu=incomeStudent;
			bufferMap.put(incomeStudent, this);
//			民办新北郊15节5班
		}
		
		private final StudentID ID;
		private final String Name;
		private final String LineID;
		private Boolean isOn=false;
		public final StanderStudent stu;
		
		public String getID(){
			return ID.getValue();
		}
		
		public String getName(){
			return Name;
		}
		
		public String getIsTemporary(){
			return (LineID.equals(rcu.user.lineID)?"":LineID);
		}
		
		public String getIsOnCar(){
			return isOn?"是滴":"并没有";
		}
		
		public void setIsOnCar(Boolean isOn){
			this.isOn=isOn;
		}
		
	}
	
	@FXML
	private void CardIDTypied(KeyEvent ke){
		String CardID=CardNumberHandler.getText();
		if (ke.getCode().equals(KeyCode.ENTER))
			add(CardID);
	}
	
	@FXML
	private void CardNumberConfirmOnAction(){
		String CardID=CardNumberHandler.getText();
		add(CardID);
	}
	
	@FXML
	private void UploadOnAction(){
		new Thread(()->{
			try {
				try {
					rcu.send();
					Platform.runLater(()->{
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setHeaderText("成功!");
						alert.setContentText("成功了耶嘿!");
						alert.showAndWait();
					});
				}catch (NotTheDayException e) {
					Platform.runLater(()->{
						Alert alert=new Alert(AlertType.WARNING);
						alert.setHeaderText("别闹了啦");
						alert.setContentText("今天不是点名的日子啦,偷窥就算了还要捣乱 ( ｰ̀дｰ́ )！");
						onStudentInfo.setItems(FXCollections.observableArrayList("今天不是点名的日子啦,偷窥就算了还要捣乱 ( ｰ̀дｰ́ )！"));
						alert.showAndWait();
					});
				}
			} catch (IOException e) {
				try{
					ConnectionCheck.orderingCheck();
					ConnectionCheck.baiduCheck();
				}catch (IOException e1){
					Alert info=new Alert(Alert.AlertType.WARNING);
					info.setHeaderText("IO错误,请检查网络连接");
					info.setContentText("full info:"+e1.toString());
					info.show();
				}
				try {
					try {
						rcu.send();
					} catch (NotTheDayException e1) {
						throw new InternalError(e1);
					}
				} catch (IOException e1) {
					Alert info=new Alert(Alert.AlertType.ERROR);
					info.setHeaderText("系统貌似炸了");
					info.setContentText("是的就是这样, 应该不会有什么卵用的full detail:"+e1.toString()+"\n可以尝试过会重新来下?");
					info.show();
				}
			}
		},"Send").start();;
	}
	
	@FXML
	private void KickOutOnAction(){
		switch (TabPane.getSelectionModel().getSelectedIndex()){
		case 0:{
			removeOnSelect(0);
			break;
		}
		
		case 1:{
			StanderStudent stu=onCarList.getSelectionModel().getSelectedItem().stu;
			if (stu!=null)
				add(stu.getID());
		}
		
		}
	}
	
	public void add(String CardID) {
		try {
			add(StudentIdentify.Build(CardID));
		}  catch (IDFormatException e) {
			logger.log(Level.INFO, CardID+" failed to add to roll call util because of "+e.getClass().getSimpleName());
			onStudentInfo.setItems(FXCollections.observableArrayList("错误输入|ू･ω･` )"));
		}
	}
	
	public void add(StudentIdentify CardID){
		logger.log(Level.INFO, CardID+" adding");
		try {
			if (tan90.getException()==null&&tan90.getProgress()==1) Pic.setImage(tan90);
			StanderStudent studentInfo=rcu.add(CardID);
			RollcalledStudentInfo0 rsi=buildInfo(studentInfo);
			rsi.setIsOnCar(true);
			RollCalledList.getItems().add(rsi);
			onCarList.refresh();
			changeInfoDisplay(studentInfo);
			logger.log(Level.INFO, studentInfo.getID()+" sucessfully added to roll call util");
		} catch (IOException e) {
			IOExceptionH(e);
		} catch (RollCallUtil.PersonAlreadyExistsException e){
			ObservableList<RollcalledStudentInfo0> list = RollCalledList.getItems();
			for (int i=0;i<list.size();i++){
				if (e.ID.equals(list.get(i).ID)){
					try {
						changeInfoDisplay(list.get(i).stu);
					} catch (IOException e1) {
						IOExceptionH(e1);
					}
				}
			}
		} catch (NoSuchPersonInDataBaseException e) {
			logger.log(Level.INFO, CardID+" failed to add to roll call util because of "+e.getClass().getSimpleName(),e);
			onStudentInfo.setItems(FXCollections.observableArrayList("∑(っ°Д°;)っ卧槽，数据库没有这个人"));
			Alert info = new Alert(Alert.AlertType.ERROR);
			info.setHeaderText("此工具数据库中没有此人,但次人在学校系统中存在");
			info.setContentText("正在查看情况并尝试更新....┐(´～｀；)");
			info.show();
			try {
				MapGenerater.init();
				try{
					try {
						CardID=RollCallUtil.cardIDToID(CardID);
						add(CardID);
						info.setContentText("已成功更新数据库 并添加了名单 ヾﾉ≧∀≦)o");
					} catch (PersonNotFoundException e1) {
						info.setContentText("刷新本地数据库成功但是依旧没有找到数据 (*｀皿´*)ﾉ ,请直接输入学生的学号(。-ω-)zzz");
						return;
					}
				}catch (IOException ioe){
					try{
						CardID=RollCallUtil.cardIDToID(CardID);
						add(CardID);
						info.setContentText("已成功更新数据库 并添加了名单");
					}catch (Exception innerE){
						info.setContentText("刷新本地数据库成功但是依旧发生了错误(艹皿艹),请直接输入学生的学号(。-ω-)zzz");
						return;
					}
				}
			} catch (IOException e1) {
				info.setContentText("更新失败,请检查网络连接\n请直接输入同学的学号,对数据库的不稳定连接表示抱歉(；′⌒`)");
				return;
			}
		} catch (NoSuchPersonOnServerException e) {
			logger.log(Level.INFO, CardID+" failed to add to roll call util because of "+e.getClass().getSimpleName(),e);
			onStudentInfo.setItems(FXCollections.observableArrayList("这个人不存在的 tan90!","","",e.getClass().getSimpleName()));
			if (tan90!=null&&tan90.getException()==null) Pic.setImage(tan90);
			if (!CardID.isStudentID()){
				try {
					StanderStudent stu=RollCallUtil.getStanderStudentByCardIDFromOrdering(CardID);
					try{
						Pic.setImage(StudentImageLibrary.getStudentImage(stu.getID()));
					} catch (PersonNotFoundException e2){
						Pic.setImage(Default_0);
					}
					onStudentInfo.setItems(FXCollections.observableArrayList(stu.getID().getValue(),stu.getName(),
							"你的信息在查餐系统上存在但是无法获取的具体信息,一般情况下TA应该为临时乘车,TA的信息仍会上传"));
				} catch (PersonNotFoundException e1) {
				} catch (IOException e1) {
					IOExceptionH(e1);
				}
			}else{
				try {
					StanderStudent stu=RollCallUtil.getStanderStudentByCardIDFromOrdering(CardID);
					try{
						Pic.setImage(StudentImageLibrary.getStudentImage(stu.getID()));
					} catch (PersonNotFoundException e2){
						Pic.setImage(Default_0);
					}
					onStudentInfo.setItems(FXCollections.observableArrayList(stu.getID().getValue(),stu.getName(),"","-----------------",
							"你的信息在查餐系统上存在","但是无法获取的具体信息","一般情况下TA应该为临时乘车","TA的信息仍会上传","ゞ(o｀Д´o)"));
				} catch (PersonNotFoundException e1) {
				}  catch (IOException e1) {
					IOExceptionH(e1);
				}
			}
		} catch (ErrorResponse ere) {
			ere.printStackTrace();
		} finally {
			CardNumberHandler.setText("");
		}
	}
	
	Alert removeAlert=new Alert(Alert.AlertType.CONFIRMATION);
	
	private void removeOnSelect(int TabIndex){
		if (TabIndex>0 || TabIndex>TabPane.getTabs().size()) TabIndex=TabPane.getSelectionModel().getSelectedIndex();
		RollcalledStudentInfo0 handle=null;
		int RollCalledListIndex=-1;
		switch (TabIndex){
		case 0:
			handle = RollCalledList.getSelectionModel().getSelectedItem();
			RollCalledListIndex=RollCalledList.getSelectionModel().getSelectedIndex();
			if (handle==null) return;
			break;
		case 1:
			handle = onCarList.getSelectionModel().getSelectedItem();
			if (handle==null||!handle.isOn) return;
			RollCalledListIndex = RollCalledList.getItems().indexOf(handle);
			break;
		}
		if (handle==null||RollCalledListIndex==-1){
			logger.log(Level.WARNING, "Can't find the selected object");
			return;
		}
		if (!handle.getName().equals(rcu.user.UserName)){
			removeAlert.setHeaderText("确定要把这个人踢出表?");
			removeAlert.setContentText(handle.getID()+": "+handle.getName());
			Optional<ButtonType> back = removeAlert.showAndWait();
			if (back.get().getButtonData().equals(ButtonData.CANCEL_CLOSE))
				return;
			try {
				rcu.remove(StudentID.Build(handle.getID()));
				RollCalledList.getItems().remove(RollCalledListIndex);
				bufferMap.get(handle.stu).setIsOnCar(false);;
				onCarList.refresh();
			} catch (IDFormatException e) {
				throw new InternalError(e);
			}
		}else{
			removeAlert.setAlertType(AlertType.INFORMATION);
			removeAlert.setHeaderText("你并不能把自己踢出列表");
			removeAlert.setContentText("想啥呐你(￣▽￣)／");
			removeAlert.showAndWait();
		}
	}

	private void changeInfoDisplay(StanderStudent stu) throws IOException{
		onStudentInfo.getItems().clear();
		try {
			if (!stu.getID().getValue().equals("06265")) Pic.setImage(StudentImageLibrary.getStudentImage(stu.getID()));
			else Pic.setImage(Senior);
		} catch (NoSuchPersonOnServerException e) {
			if (stu.getImageURL()!=null){
				Pic.setImage(new Image(((HttpURLConnection)stu.getImageURL().openConnection()).getInputStream()));
			}else{
				Pic.setImage(Default_0);
			}
		}
		ObservableList<String> info = FXCollections.observableArrayList(
				stu.getID().getValue(),stu.getName(),
				stu.getLineID()!=StanderStudent.NA?stu.getLineID():"不在任何车上",
				stu.getLineID()==null?!stu.getLineID().equals(StanderStudent.NA)?
				(stu.getID().equals(rcu.user.ID)?"车长大人":(stu.getLineID().equals(rcu.LineID)?"自己人自己人":("零食乘车! 原味(原为):"+stu.getLineID())))
				:"不在任何名单上":"");
		if (stu.getLineID().equals(StanderStudent.NA)) info.remove(0);
		onStudentInfo.setItems(info);
		CardNumberHandler.clear();
		RollcalledStudentInfo0 rsi=bufferMap.get(stu);
		onShowing=rsi;
		switch (TabPane.getSelectionModel().getSelectedIndex()){
		case 0:
			if (RollCalledList.getSelectionModel().getSelectedItem()!=null&&RollCalledList.getSelectionModel().getSelectedItem().ID.equals(stu.getID()))
				return;
			ObservableList<RollcalledStudentInfo0> Obs = RollCalledList.getItems();
			int listS=Obs.size();
			for (int i=0;i<listS;i++){
				if (Obs.get(i).ID.equals(stu.getID())){
					RollCalledList.getSelectionModel().select(i);
					RollCalledList.scrollTo(i);
					return;
				}
			}
			break;
		
		case 1:
			if (onCarList.getSelectionModel().getSelectedItem()!=null&&onCarList.getSelectionModel().getSelectedItem().ID.equals(stu.getID()))
				return;
			listS=onCarObs.size();
			for (int i=0;i<listS;i++){
				if (onCarObs.get(i).stu==stu){
					onCarList.getSelectionModel().select(i);
					onCarList.scrollTo(i);
				}
			}
		}
	}
	
	private void IOExceptionH(IOException e){
		logger.log(Level.WARNING, " IOException: ",e);
		Platform.runLater(()->{
			Alert info=new Alert(Alert.AlertType.WARNING);
			info.setHeaderText("IO错误,检查网络连接然后重试一下吧");
			info.setContentText("Full info:"+e.toString());
			info.show();
			onStudentInfo.setItems(FXCollections.observableArrayList("网络错误"));
			new Thread(()->{
				Boolean islocalInternetError=false;
				Boolean isOrderingSeverError=false;
				Boolean isPortalServerError=false;
				try{
					islocalInternetError=true;
					int baidu=ConnectionCheck.baiduCheck();
					info.setContentText(info.getContentText()+"\nBaidu.com耗时:"+baidu);
					islocalInternetError=false;
					isOrderingSeverError=true;
					info.setContentText(info.getContentText()+"\n查餐系统耗时::"+ConnectionCheck.orderingCheck());
					isOrderingSeverError=false;
					isPortalServerError=true;
					ConnectionCheck.portalCheck();
					isPortalServerError=false;
				}catch (IOException e1){
					final Boolean[] ErrorSet=new Boolean[3];
					ErrorSet[0]=islocalInternetError;
					ErrorSet[1]=isOrderingSeverError;
					ErrorSet[2]=isPortalServerError;
					Platform.runLater(()->{
						info.setContentText(
								ErrorSet[0]?"本地网络错误,检查网络连接▼o･ェ･o▼":(ErrorSet[1]?"查餐体统错误,有的时候查餐系统会间歇发疯,再试一次?"
										+ "\n如果还是不行的话还是自己在网页上点名或者直接输入学号点名吧(=`ｪ´=；)ゞ":
											(ErrorSet[2]?"点名系统错误...重试一下?还是不行的话那就真的没有办法了，我能怎么办我也很绝望":"??? 重试试试看? 现在检查下来好像没问题了...")));
						});
				}
			},"IOException Check").start();;
		});
	}
	
	@FXML
	private TableView<RollcalledStudentInfo0> onCarList;
	
	@FXML
	private TableColumn<RollcalledStudentInfo0,String> Name_OnCar;
	
	@FXML
	private TableColumn<RollcalledStudentInfo0,String> ID_OnCar;
	
	@FXML
	private TableColumn<RollcalledStudentInfo0,String> OnCar;
	
	private RollcalledStudentInfo0 buildInfo(StanderStudent stu){
		RollcalledStudentInfo0 back=bufferMap.get(stu);
		if (back!=null) return back;
		return new RollcalledStudentInfo0(stu);
	}
	
	@FXML
	private void onCarListKeyRelease(KeyEvent ke){
		if (!removeAlert.isShowing())
			switch (ke.getCode()){
			case ENTER:
				StanderStudent stu=onCarList.getSelectionModel().getSelectedItem().stu;
				if (stu!=null)
					add(stu.getID());
				break;
			case DELETE:
				removeOnSelect(1);
				break;
			default:
				break;
			}
	}
	
	@FXML
	private void RollCalledListKeyRelease(KeyEvent ke){
		if (!removeAlert.isShowing())
			switch (ke.getCode()){
			case DELETE:
				removeOnSelect(0);
				break;
			default:
				break;
			}
	}
	
	
	/**
	 * Hook for Util.ImageFactory
	 * @return
	 */
	public static Image[] getDefaultImages() {
		Image[] back=new Image[3];
		back[0]=Default_0;
		back[1]=Default_1;
		back[2]=tan90;
		return back;
	}
}
