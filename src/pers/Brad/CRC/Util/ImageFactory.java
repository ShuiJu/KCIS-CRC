package pers.Brad.CRC.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import pers.Brad.CRC.InternetAcessPart.MapGenerater;
import pers.Brad.CRC.InternetAcessPart.NoSuchPersonOnServerException;
import pers.Brad.CRC.InternetAcessPart.loginedUser;
import pers.Brad.CRC.InternetAcessPart.Exceptions.IDFormatException;

public class ImageFactory {
	
	private ImageFactory(){}
	
	static{
		CacheFilePoisition=new File(System.getProperty("user.home")+"\\CRL\\ImageBuffer.zip");
		StudentImageURL="http://portal.kcisec.com/rollcall/Images/student/";
		ImageExtensionName=".jpeg";
		cacheMap=new HashMap<Integer,ObservableValue<Image>>(50);
		firstTime=true;
		init();
	}
	
	private static final File CacheFilePoisition;
	private static final String StudentImageURL;
	private static final String ImageExtensionName;
	private static ZipFile zipObj;
	private static Boolean localCacheUseable=true;
	private static volatile Map<Integer,ObservableValue<Image>> cacheMap;
	private static final Image defaultLoadingImage=new Image(ImageFactory.class.getResourceAsStream("Default.jpg"));
	private static final Image[] defaultExceptionImageWhileProloadingImage=new Image[0];
	
	private static Boolean firstTime;
	private static void init(){
		if (!CacheFilePoisition.exists()){
			localCacheUseable=false;
			return;
		}
		try {
			zipObj=new ZipFile(CacheFilePoisition);
			ZipEntry entry;
			Enumeration<? extends ZipEntry> loop = zipObj.entries();
			while (loop.hasMoreElements()){
				entry=(ZipEntry) loop.nextElement();
				String name=entry.getName();
				name=name.indexOf(".jpeg")!=-1?name.split(".jpeg")[0]:name;
				if (firstTime) cacheMap.put(Integer.parseInt(name), new SimpleObjectProperty<Image>(new Image(zipObj.getInputStream(entry),0,0,false, true)));
				else{
					final String[] nameCopy=new String[1];
					final Boolean[] booleanHandle=new Boolean[1];
					nameCopy[0]=name;
					booleanHandle[0]=false;
					cacheMap.keySet().forEach((IDHandler)->{
						if (IDHandler.equals(Integer.parseInt(nameCopy[0]))){
							booleanHandle[0]=true;
						}
					});
					if (!booleanHandle[0]){
						cacheMap.put(Integer.parseInt(name), new SimpleObjectProperty<Image>(new Image(zipObj.getInputStream(entry),0,0,false, true)));
					}
				}
			}
		} catch (IOException e) {
			localCacheUseable=false;
		} catch (NumberFormatException e){
			clear();
			localCacheUseable=false;
		} finally {
			firstTime=false;
		}
	}
	
	public static Image getStudentImageNow(String StudentID) throws IOException, NoSuchPersonOnServerException,IDFormatException{
		mapCheck();
		if (!loginedUser.StudentIDChecker(StudentID))
			throw new IDFormatException(StudentID);
		Image image=getCachedImage(StudentID);
		if (image!=null&&image!=defaultLoadingImage) return image;
		try {
			HttpURLConnection hcon=(HttpURLConnection) new URL(new StringBuilder(60).append(StudentImageURL).append(StudentID)
					.append(ImageExtensionName).toString()).openConnection();
			if (hcon.getResponseCode()==404){
				throw new NoSuchPersonOnServerException(StudentID);
			}
			image=new Image(hcon.getInputStream(),0,0,false,true);
			if (!cacheMap.containsKey(StudentID.hashCode())) cacheMap.put(StudentID.hashCode(), new SimpleObjectProperty<Image>(image));
			else{
				((SimpleObjectProperty<Image>)cacheMap.get(StudentID.hashCode())).set(image);
			}
			return image;
		} catch (MalformedURLException e) {
			throw new InternalError(e);
		}
	}
	
	public static Image getCachedImage(String name){
		if (localCacheUseable){
			synchronized (cacheMap){
				mapCheck();
				final Image[] image=new Image[1];
				cacheMap.forEach((Name,Image)->{
					if (Name.equals(name.hashCode())){
						image[0]=Image.getValue();
					}
				});
				if (image[0]!=null){
					return image[0];
				}
			}
		}
		return null;
	}
	
	public static Image getCachedImage(String name,Image defaultImg){
		Image handle=getCachedImage(name);
		return handle==null?defaultImg:handle;
	}
	
	public static void add(String key,Image image){
		mapCheck();
		if (image.getProgress()==1)
			synchronized (cacheMap){
				cacheMap.put(key.hashCode(), new SimpleObjectProperty<Image>(image));
			}
	}
	
	public static void makeBuffer() throws IOException{
		mapCheck();
		System.out.println("makingBuffer");
		File secondTemp=new File(CacheFilePoisition.getPath()+"0");
		if (!localCacheUseable){
			init();
			if (!localCacheUseable) throw new IOException("local cache unavailable");
		}
		ZipOutputStream zipout=new ZipOutputStream(new FileOutputStream(secondTemp));
		final IOException[] exceptionHandler=new IOException[1];
		exceptionHandler[0]=null;
		synchronized (cacheMap){
			cacheMap.forEach((name,image)->{
				if (exceptionHandler[0]==null)
					try {
						zipout.putNextEntry(new ZipEntry(name.toString()));
						ImageIO.write(SwingFXUtils.fromFXImage(image.getValue(), null), "jpeg", zipout);
						zipout.flush();
					} catch (IOException e) {
						exceptionHandler[0]=e;
					} catch (Exception e) {
						throw new InternalError(e);
					}
			});
		}
		zipout.close();
		if (exceptionHandler[0]!=null)
			throw exceptionHandler[0];
		CacheFilePoisition.delete();
		secondTemp.renameTo(CacheFilePoisition);
		new File(CacheFilePoisition.getPath()+"0").delete();
	}
	
	public static void clear(){
		CacheFilePoisition.delete();
		cacheMap.clear();
	}
	
	private static ExecutorService exc=Executors.newFixedThreadPool(2);
	
	public static void preloadStudentImage(final String StudentID) throws IDFormatException {
		if (!loginedUser.StudentIDChecker(StudentID)) throw new IDFormatException(StudentID);
		exc.execute(()->{
			cacheMap.put(StudentID.hashCode(), new SimpleObjectProperty<Image>(defaultLoadingImage));
			for (;;) {
				try {
					if (MapGenerater.getIDList().indexOf(StudentID)==-1) throw new NoSuchPersonOnServerException(null);
					getStudentImageNow(StudentID);
					break;
				} catch (NoSuchPersonOnServerException e) {
					if (defaultExceptionImageWhileProloadingImage.length!=0)
						((SimpleObjectProperty<Image>)cacheMap.get(StudentID.hashCode()))
							.set(defaultExceptionImageWhileProloadingImage[new java.util.Random().nextInt(defaultExceptionImageWhileProloadingImage.length)]);
					else
						((SimpleObjectProperty<Image>)cacheMap.get(StudentID.hashCode())).set(defaultLoadingImage);
					break;
				} catch (IOException e) {
				} catch (IDFormatException e) {
					throw new InternalError("ID already checked",e);
				}
			}
		});
	}
	
	private static void mapCheck(){
		cacheMap.forEach((k,v)->{
			if (v.getValue().getException()!=null) cacheMap.remove(k);
		});
	}
}
