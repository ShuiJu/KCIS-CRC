package pers.Brad.CRC.JavaFxUI.Login;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pers.Brad.CRC.InternetAcessPart.PersonNotFoundException;
import pers.Brad.CRC.InternetAcessPart.RollCallUtil;
import pers.Brad.CRC.InternetAcessPart.loginedUser;
import pers.Brad.CRC.InternetAcessPart.Exceptions.IDFormatException;
import pers.Brad.CRC.InternetAcessPart.Exceptions.UnuseableLoginException;

public class loginController implements Runnable{
	
	public loginController(){}
	
	public loginedUser loginedUser;
	
	private static LoginSuccessListener loginlistener;	
	public static void setListener(LoginSuccessListener listener){
		loginlistener=listener;
	}
	
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
	
	private Boolean connectionChecked=false;
	private RemeberedLogin RemeberMeCacheUser;
	
	private void login(Boolean isAutoLogin){
		if (!connectionChecked) return;
		if (ChangeLoginWay.isSelected()) CardIDLogin();
		if (!isAutoLogin) FeedBack("正在登陆pwp",Color.BLUE,-1);
		if (!isAutoLogin&&RemeberMeCacheUser!=null&&RemeberMeCacheUser.doHaveRemeberedLogin!=false){
			try {
				this.loginedUser=RemeberMeCacheUser.login();
				loginCallBack();
			} catch (IOException e) {
				IOExceptionS(e,true);
			} catch (IDFormatException e) {
				FeedBack("缓存不可用?这可能是个假的缓存?...",Color.RED,4000);
				passwd.setText("");
			}
			return;
		}
		System.out.println("Try Login In ID:"+IDField.getText()+" isAutoLogin:"+isAutoLogin);
		if (!isAutoLogin) FeedBack("检查中...\\(^o^)/~",Color.BROWN,1000);
		String ID=IDField.getText();
		try{
			Integer.parseInt(ID);
			if (ID.length()!=5) throw new Exception();
			try {
				if (passwd.getText().indexOf(" ")!=-1){
					FeedBack("肯定错误的密码?�▽�?", Color.RED, 5000);
					return;
				}
				loginedUser=new loginedUser(ID,passwd.getText().toCharArray());
				loginCallBack();
			} catch (UnuseableLoginException e) {
				if (!isAutoLogin) clearPasswdField();
				if (!isAutoLogin){
					if (e.msg.equals("密碼錯誤!")){
						FeedBack("错误的账户或密码〒▽〒?", Color.RED, 5000);
						return;
					}
					if (e.msg.equals("還未進行程式授權")){
						FeedBack("你是假的车长(￣_,￣ )(未授权?)",Color.RED,5000);
						clearIDField();
						return;
					}
					FeedBack(e.msg,Color.BROWN,5000);
				}
			} catch (IOException e) {
				FeedBack("网络错误？？Σ( ° △ °|||)︴",Color.RED,5000);
			}//Train Station Lullaby (Lullatone Remix) - remix
		}catch (NumberFormatException e){
			if (!isAutoLogin){
				IDField.setText("");
				passwd.setText("");
				FeedBack("这个显然不能用吧喂W(￣_￣?)W",Color.RED,4000);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void loginCallBack(){
		FeedBack("登陆成功O(∩_∩)O?!",Color.BLUE,-1);
		try {
			RemeberedLogin.writeIn(loginedUser, passwd.getText().length(), RemeberMe.isSelected());
		} catch (IOException e1) {
			FeedBack("登录成功但无法将用户缓存写入本地",Color.RED,-1);
		}
		System.out.println(loginlistener);
		if (loginlistener!=null) loginlistener.loginEvent(loginedUser);
		Platform.runLater(()->{ms.close();});
	}
	
	private Boolean KeyReleased=false;
	
	@FXML
	private void IDFieldOnKeyReleased(KeyEvent event){
		if (RemeberMeCacheUser!=null&&RemeberMeCacheUser.doHaveRemeberedLogin){
			if (event.getCode().equals(KeyCode.ENTER)) login(false);
			passwd.setText("");
		}
		if (!KeyReleased&&IDField.getText()!=null&&IDField.getText().length()==5){
			passwd.requestFocus();
			KeyReleased=true;
		}
	}
	
	private int lastTimeLength=-1;	
	@FXML
	private void PasswdOnKeyReleased(KeyEvent event){
		if (FeedBackText.getText().equals("肯定错误的密码?�▽�?")) cancelFeedBack();
		if (passwd.getText()==null) return;
		int length=passwd.getText().length();
		if (event.getCode().equals(KeyCode.ENTER)){
			login(false);
			lastTimeLength=length;
		}else{
			if (RemeberMeCacheUser!=null){
				RemeberMeCacheUser=null;
				passwd.setText("");
			}
		}
		if (length==lastTimeLength){
			lastTimeLength=length;
			return;
		}
		lastTimeLength=length;
		new Thread("Auto Login By Time Cycle"){
			public void run(){
				String str=passwd.getText();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e){}
				if (passwd.getText()!=null&&passwd.getText().equals(str)) login(true);
			}
		}.start();;
	}
	
	Boolean OnAction=false;
	
	public void FeedBack(String msg,Color color,int time){
		if (OnAction) while(OnAction);
		FeedBackText.setVisible(false);
		FeedBackText.setFill(color); 
		FeedBackText.setText(msg);
		FeedBackText.setVisible(true);
		if (time!=-1)
			new Thread(){
				public void run(){
					try {
						String correctCheck=msg;
						sleep(time);
						if (correctCheck.equals(FeedBackText.getText()))FeedBackText.setVisible(false);
						OnAction=false;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}.start();;
	}
	
	public void cancelFeedBack(){
		FeedBackText.setVisible(false);
	}
	
	public void cancelFeedBack(int delay){
		new Thread("Delaied CancelFeedBack"){
			public void run(){
				try {
					String str=FeedBackText.getText();
					Thread.sleep(delay);
					if (FeedBackText.getText().equals(str)) cancelFeedBack();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
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
		// TODO Auto-generated method stub
		long time=System.currentTimeMillis();
		FXMLLoader fxmll=new FXMLLoader(getClass().getResource("login.fxml"));
		fxmll.setController(this);
		fxmll.load();
		System.out.println("FXMLload time:"+(System.currentTimeMillis()-time));
		time=System.currentTimeMillis();
		stage.setScene(new Scene(MainPane));
		stage.setHeight(820);
		stage.setWidth(670);
		stage.setResizable(false);
		stage.setTitle("KCIS车长点名系统");
		System.out.println("Set Scene properties:"+(System.currentTimeMillis()-time));
		time=System.currentTimeMillis();
		ObservableList<Image> a = stage.getIcons();
		a.add(new Image(getClass().getResourceAsStream("Icon.jpg")));
		System.out.println("Show:"+(System.currentTimeMillis()-time));
		time=System.currentTimeMillis();
		IDField.requestFocus();
		IDField.setText("");
		FeedBackText.setText("");
		connectionCheck();
		new Thread(){
			public void run(){
				try {
					RemeberMeCacheUser=new RemeberedLogin(IDField,passwd);
					if (RemeberMeCacheUser.doHaveRemeberedLogin) KeyReleased=true;
				} catch (IOException e) {FeedBack("硬盘残忍地拒绝了我们的缓存读取请求pwp",Color.BLACK,3000);}}
		}.start();
		passwd.focusedProperty().addListener((FocusListener)->{
			if (passwd.isFocused()){
				if (RemeberMeCacheUser!=null){
					RemeberMeCacheUser=null;
					passwd.setText("");
				}
			}
		});
		stage.showingProperty().addListener((lol,o,n)->{
			MainPane.setBackground(new Background(new BackgroundImage(
					new Image(getClass().getResourceAsStream("realbackground.jpg")),BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,
					BackgroundPosition.DEFAULT,BackgroundSize.DEFAULT)));
		});
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
			if (doNeedConnectionCheck) connectionCheck();
		}else{
			FeedBack("出错了??喵喵喵????:"+e.getClass().getName(),Color.RED,-1);
		}
	}
	
	@FXML
	private Text VersionTEXT;
	
	//Internet Check
	
	@FXML
	private SplitPane ConnectionCheckSplitPane;
	
	@FXML
	private Text CCText;
	
	@FXML
	private ProgressIndicator ProgressO;
	
	
	@FXML
	private Text SCCText;
	
	@FXML
	private ProgressIndicator ProgressS;
	
	
	private int toSchoolDelay0=-1;
	private int toSchoolDelay1=-1;
	private int tobaiduDelay=-1;	
	private Boolean toSchooled=false;
	private int stateCounter=2;
	
	private void connectionCheck(){
		toSchoolDelay0=-1;
		toSchoolDelay1=-1;
		tobaiduDelay=-1;
		toSchooled=false;
		stateCounter=2;
		if (!connectionChecked) FeedBack("正在测试网络...",Color.BROWN,-1);
		connectionChecked=false;
		new Thread(this).start();
		new Thread(this).start();
	}
	
	@Override
	public void run(){
		if (toSchooled){
			try{
				toSchoolDelay0=pers.Brad.CRC.InternetAcessPart.loginedUser.portalCheck();
				ProgressS.setProgress(0.5);
				toSchoolDelay1=pers.Brad.CRC.InternetAcessPart.loginedUser.orderingCheck();
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
				tobaiduDelay=pers.Brad.CRC.InternetAcessPart.loginedUser.baiduCheck();
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
	}
	
	//extend feature
	
	@FXML
	private ToggleButton ChangeLoginWay;
	
	@FXML
	private PasswordField CardIDLoginTextField;
	
	@FXML
	private Text CardIDLoginReminder;
	
	@FXML
	private void ChangeLoginWayOnAction(){
		if (RemeberMeCacheUser!=null){
			RemeberMeCacheUser=null;
			passwd.setText("");
		}
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
			String ID=CardIDLoginTextField.getText();
			try{
				if (ID.length()!=10) throw new NumberFormatException(ID);
				ID=RollCallUtil.cardIDToID(ID);
			}catch (NumberFormatException num){
				FeedBack("憋瞎(咽口水)打(っ´Ι`)っ", Color.RED, 2000);
				return;
			} catch (PersonNotFoundException e) {
				FeedBack("并没有找到此人,失败惹", Color.RED, 2000);
				return;
			}
			if (ID==null){
				FeedBack("查无此人",Color.RED,-1);
				CardIDLoginTextField.setText("");
			}
			FeedBack("尝试登陆ID:"+ID,Color.BROWN,5000);
			final String innerID=ID;
			new Thread(()->{
				try {
					loginedUser=new loginedUser(innerID);
					Platform.runLater(()->{FeedBack("登陆成功;-) ID:"+innerID,Color.BROWN,2000);});
				} catch (IOException e) {
					IOExceptionS(e,true);
				} catch (IDFormatException e) {
					FeedBack("登陆失败 ID:"+innerID,Color.BROWN,2000);
				}
			},"Login Service");
			loginedUser=new loginedUser(ID);
		} catch (IDFormatException e) {
			FeedBack("憋瞎(咽口水)打(っ´Ι`)っ", Color.RED, 2000);
		} catch (IOException e){
			IOExceptionS(e,true);
		}
	}
}
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
			Throwable t=e.getCause();
			try{
				throw t;
			}catch (IOException ioe){
				throw new IOException(t);
			}catch (Throwable t1){
				return null;
			}
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
		pre.put(registryIDKey, us.ID);
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