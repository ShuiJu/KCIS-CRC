package pers.wusatosi.CRC.JavaFxUI.Login;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import pers.wusatosi.CRC.CRCApi.IDFormatException;
import pers.wusatosi.CRC.CRCApi.PersonNotFoundException;
import pers.wusatosi.CRC.CRCApi.StudentIdentify;
import pers.wusatosi.CRC.CRCApi.StudentIdentify.StudentID;
import pers.wusatosi.CRC.CRCApi.UnuseableLoginException.RefusedLoginException;
import pers.wusatosi.CRC.CRCApi.loginedUser;
import pers.wusatosi.CRC.JavaFxUI.UIEventProcesser;
import pers.wusatosi.CRC.JavaFxUI.Login.ConnectionCheck.ConnectionCheckHelper;
import pers.wusatosi.CRC.Util.CachedSession;
import pers.wusatosi.CRC.Util.CachedSession.SessionInfo;;

public class loginController {
	
	//For FXML loader
	public loginController(){}
	
	public loginedUser loginedUser;
	
	@FXML
	public BorderPane MainPane;
	
	@FXML 
	private TextField IDField;
	
	@FXML
	private Text IDReminder;
	
	@FXML
	private Text passwdReminder;
	
	@FXML
	private PasswordField passwd;
	
	@FXML
	private CheckBox RemeberMe;
	
	@FXML
	private Button loginButton;
	
	@FXML
	private Text FeedBackText;
	
	@FXML
	private void loginClicked(){
		login(false);
	}
	
	private class isCachedSession{
		Boolean Value = false;
	}
	
	private Boolean connectionChecked=false;
	
	private void login(Boolean isAutoLogin){
		
		if (!connectionChecked) return;
		
		if (ChangeLoginWay.isSelected()) CardIDLogin();
		
		if (!isAutoLogin) FeedBack("正在登陆pwp",Color.BLUE,1000);
		if (!isAutoLogin&&CachedSession.doHaveInfomation()&&((isCachedSession) passwd.getUserData()).Value){
			System.out.println("Enter");
			Task<loginedUser> task = 
				UIEventProcesser.getInstance().submit(()->{
					return CachedSession.getInstance().getLoginedUserByCookie();
				});
			task.setOnSucceeded((state)->{
				try {
					this.loginedUser = task.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new Error(e);
				}
				FeedBack("登录成功!pwp",Color.BLUE,-1);
				loginCallBack(true);
			});
			task.setOnFailed((failed) -> {
				Throwable t = task.getException();
				if (t instanceof Error) {
					Thread current = Thread.currentThread();
					current.getUncaughtExceptionHandler().uncaughtException(current, (Error) t); 
				}
				if (t instanceof Exception) {
					Exception e = (Exception) t;
					e.printStackTrace();
					if (e instanceof IOException)
						IOExceptionS((IOException) e,true);
					if (e instanceof IDFormatException) {
						FeedBack("缓存不可用?这可能是个假的缓存?...",Color.RED,4000);
						passwd.setText("");
					}
					FeedBack(e.toString(), Color.RED, -1);
					throw new Error(e);
				}
			});
			return;
		}
		
		System.out.println("Try Login In ID:"+IDField.getText()+" isAutoLogin:"+isAutoLogin);
		if (!isAutoLogin) FeedBack("检查中...\\(^o^)/~",Color.BROWN,1000);
		String ID=IDField.getText();
		String password = this.passwd.getText();
		if ((!pers.wusatosi.CRC.CRCApi.loginedUser.StudentIDChecker(ID))||password.indexOf(" ")!=-1) {
			if (!isAutoLogin){
				IDField.setText("");
				clearPasswdField();
				FeedBack("这个显然不能用吧喂W(￣_￣?)W",Color.RED,4000);
				return;
			}
		}
		
		UIEventProcesser.getInstance()
				.submit(()->{
					if (!isAutoLogin) passwd.setDisable(true);
					StudentID iid = StudentID.Build(ID);
					return new loginedUser(iid, password.toCharArray());
				})
			.setOnSucceededReallyFriendly((event) -> {
				passwd.setDisable(false);
				try {
					loginedUser = event.getThis().get();
				} catch (InterruptedException | ExecutionException e) {
					throw new Error(e);
				}
				loginCallBack(false);
			})
			.setDoLetUncaughtExceptionHandlerHandleThrowableExceptException(true)
			.setOnFailedByExceptionReallyFriendly((e)->{
			passwd.setDisable(false);
			if (e instanceof RefusedLoginException) 
				if (!isAutoLogin) {
					clearPasswdField();
					switch(((RefusedLoginException) e).getErrorType()) {
					case UnverifiedUser:
						FeedBack("你是假的车长(￣_,￣ )(未授权?)",Color.RED,5000);
						clearIDField();
						break;
					case WrongUserNameOrPassword:
						FeedBack("错误的账户或密码〒▽〒?", Color.RED, 5000);
						clearPasswdField();
						break;
					default:
						FeedBack(((RefusedLoginException) e).getErrorType().toString(),Color.RED,5000);
						clearPasswdField();
					}
				}
			if (e instanceof IOException) {
				FeedBack("网络错误？？Σ( ° △ °|||)︴",Color.RED,5000);
			}
			if (e instanceof IDFormatException) {
				if (!isAutoLogin){
					IDField.setText("");
					clearPasswdField();
					FeedBack("这个显然不能用吧喂W(￣_￣?)W",Color.RED,4000);
					return;
				}
			}
		});
	}

	private void loginCallBack(Boolean isCachedSession){
		FeedBack("登陆成功O(∩_∩)O?!",Color.BLUE,-1);
		if (!isCachedSession)
			UIEventProcesser.quickSubmit(() ->
				System.out.println(CachedSession.getInstance().write(new SessionInfo(loginedUser, passwd.getText().length()))));
		new Timeline(new KeyFrame(new Duration(500), (action) -> ms.close())).play();
	}
	
	private Boolean KeyReleased=false;
	
	@FXML
	private void IDFieldOnKeyReleased(KeyEvent event){
		if (CachedSession.doHaveInfomation()){
			if (event.getCode().equals(KeyCode.ENTER)) login(false);
		}
		if (!KeyReleased&&IDField.getText()!=null&&IDField.getText().length()==5){
			passwd.requestFocus();
			KeyReleased=true;
		}
	}
	
	private int lastTimeLength=-1;	
	@FXML
	private void PasswdOnKeyReleased(KeyEvent event){
		if (event.getCode().equals(KeyCode.TAB)) return;
		if (FeedBackText.getText().equals("肯定错误的密码?�▽�?")) cancelFeedBack();
		if (passwd.getText() == null) return;
		int length=passwd.getText().length();
		if (event.getCode().equals(KeyCode.ENTER)){
			login(false);
			lastTimeLength=length;
			return;
		}
		((isCachedSession) passwd.getUserData()).Value = false;
		if (length==lastTimeLength){
			lastTimeLength=length;
			return;
		}
		lastTimeLength=length;
		final String str = passwd.getText();
		UIEventProcesser.quickSubmit(()->{
			System.out.println("Getin check");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e){}
			if (passwd.getText()!=null&&passwd.getText().equals(str)) login(true);
		});
	}
	
	public void FeedBack(final String msg,Color color,int time){
		FeedBackText.setVisible(false);
		FeedBackText.setFill(color); 
		FeedBackText.setText(msg);
		FeedBackText.setVisible(true);
		if (time > 0)
			cancelFeedBack(time);
	}
	
	public void cancelFeedBack(){
		FeedBackText.setVisible(false);
	}
	
	public void cancelFeedBack(int delay){
		if (delay < 1) {
			cancelFeedBack();
		}else {
			final String str = FeedBackText.getText();
			new Timeline(new KeyFrame(new Duration(delay), (action) ->  {
				if (str.equals(FeedBackText.getText()))FeedBackText.setVisible(false);
			})).play();
		}
	}
	
	void clearIDField(){
		IDField.setText("");
		passwd.setText("");
	}
	
	void clearPasswdField(){
		passwd.setText("");
	}

	private Stage ms;
	
	public loginedUser start(Stage stage) throws IOException {
		
		long time=System.currentTimeMillis();
		
		FXMLLoader fxmll=new FXMLLoader(loginController.class.getResource("login.fxml"));
		fxmll.setController(this);
		fxmll.load();
		
		System.out.println("FXMLload time:"+(System.currentTimeMillis()-time));
		time=System.currentTimeMillis();
		
		stage.setScene(new Scene(MainPane));
		stage.setHeight(820);
		stage.setWidth(670);
		stage.setResizable(false);
		stage.setTitle("KCIS车长点名系统");
		
		ObservableList<Image> a = stage.getIcons();
		a.add(new Image(getClass().getResourceAsStream("Icon.jpg")));
		
		System.out.println("Set Scene properties:"+(System.currentTimeMillis()-time));
		time=System.currentTimeMillis();
		
		IDField.requestFocus();
		IDField.setText("");
		FeedBackText.setText("");
		
		ConnectionCheckHelper.BuildConnectionHelper((e) -> IOExceptionS(e,false), ConnectionCheckPane).addListener((li) ->{
			connectionChecked = true;
		});
		
		passwd.setUserData(new isCachedSession());
		UIEventProcesser.getInstance().execute(() ->{
			if (CachedSession.doHaveInfomation() && IDField.getText().equals("")) {
				IDField.setText(CachedSession.getInstance().getID().getValue());
				int length = CachedSession.getInstance().getPasswordLength();
				StringBuilder sb = new StringBuilder(length);
				for (int i=0;i<length;i++) {
					sb.append("*");
				}
				passwd.setText(sb.toString());
				((isCachedSession) passwd.getUserData()).Value = true;
			}
		});
		
		MainPane.setBackground(new Background(new BackgroundImage(
				new Image(loginController.class.getResourceAsStream("realbackground.jpg")),
				BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.DEFAULT,BackgroundSize.DEFAULT)));
		
		ms=stage;
		stage.showAndWait();
		return loginedUser;
	}
	
	private void IOExceptionS(IOException e,Boolean doNeedConnectionCheck){
		cancelFeedBack();
		if (e.getMessage()!=null){
			StringBuilder message=new StringBuilder(e.getMessage());
			if (message.indexOf(".com")!=-1){
				FeedBack("网络连接失败qaq",Color.RED,3000);
				return;
			}
			try{
				int javaS=message.indexOf("java.")+"java.".length();
				if (javaS==-1) throw new Exception();
				message=new StringBuilder(message.substring(javaS+1));
				message=new StringBuilder(message.substring(message.indexOf(".")+1, message.indexOf(":")));
			}catch (Exception e1){}
			if (message.length()>35){
				message.setLength(32);
				message.append("...");
			}
			message.insert(0, "网络连接失败qaq:");
			FeedBack(message.toString(),Color.RED,-1);
			if (doNeedConnectionCheck) 
				ConnectionCheckHelper.BuildConnectionHelper((ei) -> {}, ConnectionCheckPane);
		}else{
			FeedBack("出错了??喵喵喵????:"+e.getClass().getName(),Color.RED,-1);
		}
	}
	
	@FXML
	private Text VersionTEXT;
	
	//Internet Check
	
	@FXML
	private AnchorPane ConnectionCheckPane;
	
	/*
	private void connectionCheck(){
		ConnectionCheckHelper helper
			= ConnectionCheckHelper.BuildConnectionHelper((e) -> IOExceptionS(e,false), ConnectionCheckPane);
		
		
		/*
		toSchoolDelay0=-1;
		toSchoolDelay1=-1;
		tobaiduDelay=-1;
		toSchooled=false;
		stateCounter=2;
		if (!connectionChecked) FeedBack("正在测试网络...",Color.BROWN,-1);
		connectionChecked=false;
		Runnable target = () -> {
			if (toSchooled){
				try{
					toSchoolDelay0=pers.wusatosi.CRC.CRCApi.loginedUser.portalCheck();
					ProgressS.setProgress(0.5);
					toSchoolDelay1=pers.wusatosi.CRC.CRCApi.loginedUser.orderingCheck();
					ProgressS.setProgress(1);
					SCCText.setFont(Font.font(14));
					SCCText.setText("校系统连接延迟:"+toSchoolDelay0+"ms, "+toSchoolDelay1+"ms");
				}catch (IOException e){
					SCCText.setFont(Font.font(14));
					SCCText.setText("校系统连接失败?");
					IOExceptionS(e,false);
				}
			}else{
				toSchooled=true;
				try{
					tobaiduDelay=pers.wusatosi.CRC.CRCApi.loginedUser.baiduCheck();
					ProgressO.setProgress(1);
					CCText.setFont(Font.font(14));
					CCText.setText("外网连接延迟:"+tobaiduDelay+"ms");
				}catch (IOException e){
					CCText.setFont(Font.font(14));
					CCText.setText("外网连接失败");
					IOExceptionS(e,false);
				}
			}
			stateCounter--;
			if (stateCounter==0) {
				if (toSchoolDelay0!=-1&&toSchoolDelay1!=-1&&tobaiduDelay!=-1){
					connectionChecked=true;
					new Thread(){
						public void run(){
							//FeedBack("测试成功!<(￣︶�?)>",Color.RED,6000);
							cancelFeedBack();
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							ConnectionCheckSplitPane.setVisible(false);
						}
					}.start();
				}else{
					if (toSchoolDelay0==-1&&toSchoolDelay1==-1&&tobaiduDelay==-1){
						if (!connectionChecked) FeedBack("全网连接失败，请检查网络连接(?_?)",Color.RED,10000);
						new Thread(){
							public void run(){
								System.err.println("RECONNECT");
								try {Thread.sleep(3000);} catch (InterruptedException e1) {e1.printStackTrace();}
								for (int i=5;i>=0;i--){
									CCText.setText(i+"秒后尝试重连?");
									SCCText.setText(i+"秒后尝试重连?");
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								try {Thread.sleep(400);} catch (InterruptedException e) {e.printStackTrace();}
								//init connection check
								toSchoolDelay0=-1;
								toSchoolDelay1=-1;
								tobaiduDelay=-1;
								toSchooled=false;
								stateCounter=2;
								connectionCheck();
							}
						}.start();
					}
					else{
						if (toSchoolDelay0!=-1&&toSchoolDelay1!=-1&&tobaiduDelay==-1){
							if (!connectionChecked) FeedBack("诡异的网络环境(￢_￢?)",Color.RED,-1);
							new Thread(){
								public void run(){
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									ConnectionCheckSplitPane.setVisible(false);
								}
							}.start();
						}else{
							if ((toSchoolDelay0==-1||toSchoolDelay1==-1)&&tobaiduDelay!=-1){
								SCCText.setFont(Font.font(14));
								SCCText.setText("校系统连接失败");
								if (!connectionChecked) FeedBack("可能校服务器炸了?",Color.RED,-1);
								new Thread(){
									public void run(){
										System.err.println("RECONNECT");
										try {
											Thread.sleep(3000);
										} catch (InterruptedException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
										for (int i=5;i>=0;i--){
											SCCText.setText(i+"秒后尝试重连?");
											try {
												Thread.sleep(1000);
											} catch (InterruptedException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
										try {
											Thread.sleep(400);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										//init connection check	private 
										toSchoolDelay0=-1;
										toSchoolDelay1=-1;
										tobaiduDelay=-1;	
										toSchooled=false;
										stateCounter=2;
										connectionCheck();
									}
								}.start();
							}
						}
					}
				}
			}
		};
		UIEventProcesser.getInstance().execute(target);
		UIEventProcesser.getInstance().execute(target);
		
		*/
//	}
	
	
	//extend feature
	
	@FXML
	private ToggleButton ChangeLoginWay;
	
	@FXML
	private PasswordField CardIDLoginTextField;
	
	@FXML
	private Text CardIDLoginReminder;
	
	@FXML
	private void ChangeLoginWayOnAction(){
		if (ChangeLoginWay.isSelected()){
			IDField.setVisible(false);
			passwd.setVisible(false);
			IDReminder.setVisible(false);
			passwdReminder.setVisible(false);
			CardIDLoginTextField.setVisible(true);
			CardIDLoginReminder.setVisible(true);
			CardIDLoginTextField.requestFocus();
		}else{
			IDField.setVisible(true);
			passwd.setVisible(true);
			IDReminder.setVisible(true);
			passwdReminder.setVisible(true);
			CardIDLoginTextField.setVisible(false);
			CardIDLoginReminder.setVisible(false);
		}
	}
	
	@FXML
	private void CardIDLoginOnKeyReleased(KeyEvent ke){
		if (ke.getCode().equals(KeyCode.ENTER)) CardIDLogin();
	}
	
	private void CardIDLogin(){
		try {
			StudentIdentify ID;
			try{
				ID=StudentIdentify.Build(CardIDLoginTextField.getText());
				ID=ID.getStudentID();
			}catch (IDFormatException num){
				FeedBack("憋瞎(咽口水)打(っ´Ι`)っ", Color.RED, 2000);
				return;
			} catch (PersonNotFoundException e) {
				FeedBack("并没有找到此人,失败惹", Color.RED, 2000);
				CardIDLoginTextField.setText("");
				return;
			}
			FeedBack("尝试登陆ID:"+ID,Color.BROWN,5000);
			final StudentID innerID=(StudentID) ID;
			UIEventProcesser.quickSubmit(()->{
				try {
					loginedUser=new loginedUser(innerID);
					Platform.runLater(()->{FeedBack("登陆成功;-) ID:"+innerID.getValue(),Color.BROWN,2000);});
				} catch (IOException e) {
					IOExceptionS(e,true);
				} 
			});
		} catch (IOException e){
			IOExceptionS(e,true);
		}
	}
}
/*
class RemeberedLogin{
	
	public RemeberedLogin(TextField tf1,PasswordField tf2) throws IOException{
		IOException IOE=null;
		try {
			cookies=registryReader(tf1,tf2);
		} catch (BackingStoreException e) {e.printStackTrace();}
		try{
			//cookies=null;
			if (cookies==null)cookies=fileReader(tf1,tf2);
		}catch (IOException e){IOE=e;}
		if (cookies==null){
			tf2.setText("");
			if (IOE!=null) throw IOE;
			doHaveRemeberedLogin=false;
		}else doHaveRemeberedLogin=true;
	}
	
	public final Boolean doHaveRemeberedLogin;
	private Map<String,String> cookies=null;
	
	public loginedUser login() throws IOException, IDFormatException{
		try {
			return new loginedUser(cookies);
		} catch (UnuseableLoginException e) {
			if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
			throw new IOException(e);
		}
	}
	
	public static void writeIn(loginedUser us,int length,Boolean isSeleted) throws IOException{
		Boolean wrote=false;
		try {
			registryWriter(us,length,isSeleted);
			wrote=true;
		} catch (BackingStoreException e) {}
		try{
			fileWrite(us,length,isSeleted);
			wrote=true;
		}catch (IOException e){}
		if (wrote==false) throw new IOException();
	}
	
	private final static String registryIDKey="ID";
	private final static String registryPasswdLength="Password length";
	private final static String registryCookieKey="Cookie";
	private final static String registryConfirm=".Confirm";

	private static Map<String,String> registryReader(TextField tf1, TextField tf2) throws BackingStoreException{
		Preferences pre=Preferences.userNodeForPackage(loginController.class);
		try{
			String ID=pre.get(registryIDKey, null);
			if(ID==null) pre.removeNode();
			tf1.setText(ID);
			int passwordL=pre.getInt(registryPasswdLength,-1);
			if (passwordL==-1) pre.removeNode();
			StringBuilder sb=new StringBuilder();
			sb.append("2");
			for (int i=0;i<passwordL-1;i++){
				sb.append("3");
			}
			tf2.setText(sb.toString());
			String CookieKey=pre.get(registryCookieKey, null);
			if (CookieKey==null) pre.removeNode();
			Map<String,String> back=new HashMap<String,String>();
			{
				int pointer;
				while ((pointer=CookieKey.indexOf(","))!=-1){
					String cah=CookieKey.substring(0, pointer);
					CookieKey=CookieKey.substring(pointer+1);
					String cahV=pre.get(cah, null);
					if (cahV==null) continue;
					int cahC=pre.getInt(cah+registryConfirm,-1);
					if (cahV.hashCode()!=cahC){
						pre.removeNode();
						back=null;
						break;
					};
					back.put(cah, cahV);
				}
				String v=pre.get(CookieKey, null);
				if (v!=null){
					back.put(CookieKey, v);
				}
				back.put(loginedUser.MapLoginDefinitier, ID);
			}
			return back;
		}catch (IllegalStateException e){
			return null;
		}catch (java.lang.UnsupportedOperationException e1){
			e1.printStackTrace();
			return null;
		}
	}
	
	private static void registryWriter(loginedUser us,int length,Boolean isSeleted) throws BackingStoreException{
		Preferences pre=Preferences.userNodeForPackage(loginController.class);
		if (!isSeleted){
			pre.removeNode();
			return;
		}
		pre.put(registryIDKey, us.ID.getValue());
		pre.putInt(registryPasswdLength, length);
		Map<String, String> cookie = us.getCookie();
		StringBuilder sb=new StringBuilder();
		for (String str:cookie.keySet()){
			String value=cookie.get(str);
			pre.put(str, value);
			pre.putInt(str+registryConfirm, value.hashCode());
			sb.append(str);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		pre.put(registryCookieKey, sb.toString());
		sb=null;
		pre.flush();
	}

	private static final String FileFolder=System.getProperty("user.home")+"\\CRL";
	private static final String FilePath=FileFolder+"\\login.cfg";
	private static final String InFileIDKey="ID";
	private static final String InFilePasswdLength="Password length";
	private static final String InFileCookieStartNoting="cookie--->";
	private static final String InFileCookieEndNoting="<---";
	private static final String InFileCookieConfirm="|Confirm|";
	private static final String separator=System.lineSeparator();
	
	private static Map<String,String> fileReader(TextField tf1,TextField tf2) throws IOException{
		try {
			File inputConfigFile=new File(FilePath);
			if (!inputConfigFile.exists()) return null;
			BufferedReader buf=new BufferedReader(new InputStreamReader(new FileInputStream(inputConfigFile),"utf-8"));
			try{
				Map<String,String> back=new HashMap<String,String>();
				String str=buf.readLine();
				int pointer;
				if ((pointer=str.indexOf(InFileIDKey))==-1){
					inputConfigFile.delete();
					buf.close();
					return null;
				}
				str=str.substring(pointer+InFileIDKey.length()+1);
				tf1.setText(str);
				back.put(loginedUser.MapLoginDefinitier,str);
				{
					str=buf.readLine();
					if ((pointer=str.indexOf(InFilePasswdLength))==-1){
						inputConfigFile.delete();
						buf.close();
						return null;
					}
					str=str.substring(InFilePasswdLength.length()+1);
					try{
						int times=Integer.parseInt(str);
						StringBuilder sb=new StringBuilder(2);
						for (int i=0;i<times;i++){
							sb.append(3);
						}
						tf2.setText(sb.toString());
					}catch (NumberFormatException e){
						inputConfigFile.delete();
						buf.close();
						return null;
					}
				}
				//cookie
				str=buf.readLine();
				if (str.indexOf(InFileCookieStartNoting)==-1){
					inputConfigFile.delete();
					buf.close();
					return null;
				}
				while (((str=buf.readLine()).indexOf(InFileCookieEndNoting))==-1){
					pointer=str.indexOf(":");
					if (pointer==-1){
						inputConfigFile.delete();
						buf.close();
						return null;
					}
					int confirmer=str.indexOf(InFileCookieConfirm);
					if (confirmer==-1){
						inputConfigFile.delete();
						buf.close();
						return null;
					}
					String v=str.substring(pointer+1,confirmer);
					try{
						int confirmCode=Integer.parseInt(str.substring(confirmer+InFileCookieConfirm.length()));
						if (v.hashCode()!=confirmCode) throw new Exception();
						back.put(str.substring(0, pointer), v);
					}catch (Exception e){
						inputConfigFile.delete();
						buf.close();
						return null;
					}
				}
				return back;
			}finally{
				buf.close();
			}
		} catch (UnsupportedEncodingException|FileNotFoundException e) {return null;}
	}
	
	private static void fileWrite(loginedUser us,int length,Boolean isSeleted) throws IOException{
		File onWriteFile=new File(FileFolder);
		onWriteFile.mkdirs();
		onWriteFile=new File(FilePath);
		if (!isSeleted){
			onWriteFile.delete();
		}
		if (!onWriteFile.exists()) onWriteFile.createNewFile();
		OutputStreamWriter writer=new OutputStreamWriter(new FileOutputStream(onWriteFile),"utf-8");
		try{
			StringBuilder sb=new StringBuilder();
			sb.append(InFileIDKey);
			sb.append(":");
			sb.append(us.ID);
			sb.append(separator);
			sb.append(InFilePasswdLength);
			sb.append(":");
			sb.append(length);
			sb.append(separator);
			writer.write(sb.toString());
			sb=new StringBuilder();
			{
				//For cookie
				writer.write(InFileCookieStartNoting+separator);
				Map<String,String> map=us.getCookie();
				for (String str:map.keySet()){
					System.out.println(str);
					sb.append(str);
					sb.append(":");
					String v=map.get(str);
					sb.append(map.get(str));
					sb.append(InFileCookieConfirm);
					sb.append(v.hashCode());
					sb.append(separator);
					writer.write(sb.toString());
					writer.flush();
					sb.setLength(0);
				}
				writer.write(InFileCookieEndNoting);
				writer.flush();
			}
		}finally{
			writer.close();
		}
	}
}
*/