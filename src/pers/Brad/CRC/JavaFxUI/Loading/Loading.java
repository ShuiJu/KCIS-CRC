package pers.Brad.CRC.JavaFxUI.Loading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pers.Brad.CRC.Util.bilibiliGIFAnalyzer;

public class Loading {
	
	private final Stage thisStage;
	
	public Loading(Stage stage){
		this.thisStage=stage;
		new Thread(()->{
			GIFS.getImages();
		}).start();
	}
	
	static{
		localList=GIFS.getImages();
		initURLS();
	}
	
	private static String FXMLLocation="LoadingFXML.fxml";
	private static final List<bilibiliGIFAnalyzer.SorceGIFInfo> urlList=new ArrayList<>(600);
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
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {}
			List<bilibiliGIFAnalyzer.SorceGIFInfo> List=new ArrayList<bilibiliGIFAnalyzer.SorceGIFInfo>(600);
			try {
				List.addAll(bilibiliGIFAnalyzer.getURLs());
				synchronized (urlList){
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
		Platform.runLater(()->{
			thisStage.show();
		});
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
		try{
			final String nameSummaryFile="Images/ImageNames";
			System.out.println(Loading.class);
			System.out.println(Loading.class.getResourceAsStream(nameSummaryFile));
			if (Loading.class.getResourceAsStream(nameSummaryFile)==null){
				throw new InternalError("Name Summary File losts");
			}
			ArrayList<Image> LocalImages=new ArrayList<Image>(10);
			BufferedReader buf=new BufferedReader(new InputStreamReader(Loading.class.getResourceAsStream(nameSummaryFile),"utf-8"));
			String str;
			while ((str=buf.readLine())!=null){
				InputStream in=Loading.class.getResourceAsStream("Images/"+str);
				if (in==null) throw new InternalError(str+" Pic losts");
				LocalImages.add(new Image(in));
			}
			images=LocalImages;
		}catch (Exception e){
			throw new InternalError("Error while get GIFs"+e);
		}
	}
	
	private static final List<Image> images;
	
	public static List<Image> getImages(){
		return images;
	}
	
}