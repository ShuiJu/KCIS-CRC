package pers.wusatosi.CRC.JavaFxUI.LoadStage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import com.sun.javafx.tk.Toolkit;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pers.wusatosi.CRC.Util.BilibiliGIFAnalyzer;

public class Loading {
	
	private final Stage thisStage;
	
	public Loading(Stage stage){
		this.thisStage=stage;
	}
	
	static{
		localList=GIFS.getImages();
		initURLS();
	}
	
	private static String FXMLLocation="LoadingFXML.fxml";
	private static final List<BilibiliGIFAnalyzer.SorceGIFInfo> urlList=new ArrayList<>(600);
	private static final List<Image> localList;
	
	private static Image nextBiliBiliGif(){
		if (urlList.size()>0)
			try {
				return new Image(urlList.get(new Random().nextInt(urlList.size()-1)).getURL().openConnection().getInputStream());
			} catch (IOException e) {}
		return localList.get(new Random().nextInt(localList.size()-1));
	}
	
	public static void initURLS(){
		new Thread(()->{
			List<BilibiliGIFAnalyzer.SorceGIFInfo> List=new ArrayList<BilibiliGIFAnalyzer.SorceGIFInfo>(600);
			try {
				List.addAll(BilibiliGIFAnalyzer.getURLs());
				synchronized (urlList){
					urlList.clear();
					urlList.addAll(List);
				}
			} catch (IOException e) {}
			
		});
	}
	
	public Loading start(Stage stage) throws IOException{
		FXMLLoader fxmll=new FXMLLoader(Loading.class.getResource(FXMLLocation));
		fxmll.setController(this);
		fxmll.load();
		stage.setScene(new Scene(fxmll.getRoot()));
		System.out.println(stage.getScene());
		Info.setText(".");
		updatePic();
		
		new Thread(()->{
			for (;;){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
				String text=Info.getText();
				Boolean moded=true;
				if (!text.equals("")){
					StringBuilder sb=new StringBuilder(text.length());
					for (int i=0;i<text.length();i++)
						sb.append(".");
					if (text.equals(sb.toString())) moded=false;
				}else moded=false;
				if (!moded){
					Info.setText(Info.getText()+".");
					if (Info.getText().length()>=20)
						Info.setText(".");
				}
			}
		}).start();
		
		return this;
	}
	
	public Loading start() throws IOException{
		return start(thisStage);
	}
	
	public void show(){
		if (Toolkit.getToolkit().isFxUserThread())
			thisStage.show();
		else Platform.runLater(()->{thisStage.show();});
	}
	
	public void close(){
		Platform.runLater(()->{
			thisStage.close();;
		});
	}
	
	public void updatePic(){
		Pic.setImage(nextBiliBiliGif());
	}
	
	public void updateInfo(String info){
		Info.setText(info);
	}
	
	@FXML
	private Text Info;
	
	@FXML
	private ImageView Pic;
	
}
class GIFS{
	
	static{
		cdl=new CountDownLatch(1);
		new Thread(()-> {
			try{
				ArrayList<Image> LocalImages=new ArrayList<Image>(10);
				BufferedReader buf=new BufferedReader(new InputStreamReader(Loading.class.getResourceAsStream("Images"),"utf-8"));
				String str;
				while ((str=buf.readLine())!=null){
					InputStream in=Loading.class.getResourceAsStream("Images/"+str);
					if (in==null) throw new InternalError(str+" Pic losts");
					LocalImages.add(new Image(in));
				}
				images=LocalImages;
			}catch (Exception e){
				throw new InternalError("Error while get GIFs ",e);
			} finally {
				GIFS.cdl.countDown();
			}
		}).start();;
	}
	
	private static volatile List<Image> images;
	
	private static final CountDownLatch cdl;
	
	public static List<Image> getImages(){
		if (images==null)
			try {
				cdl.await();
			} catch (InterruptedException e) {
				throw new Error(e);
			}
		assert images != null;
		return images;
	}
	
}