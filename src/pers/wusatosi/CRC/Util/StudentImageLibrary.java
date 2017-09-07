package pers.wusatosi.CRC.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import pers.wusatosi.CRC.CRCApi.IDFormatException;
import pers.wusatosi.CRC.CRCApi.NoSuchPersonOnServerException;
import pers.wusatosi.CRC.CRCApi.StudentIdentify.StudentID;

public final class StudentImageLibrary {
	
	private StudentImageLibrary(){throw new Error("No instance!");}
	
	static{
//		Need to read from a config file
		CacheFilePoisition=new File(System.getProperty("user.home")+"\\CRL\\ImageBuffer.zip");
		
//		It should just need resize twice at max usage
		cacheMap=new HashMap<>(50,2);
		
//		for initialization
		firstTime=true;
		init();
	}
	
	
	private static final File CacheFilePoisition;
	
//	will throw IOException when construct, so construct it in init method
	private static ZipFile zipObj;
	
	private static final String StudentImageURL="http://portal.kcisec.com/rollcall/Images/student/";
	private static final String ImageExtensionName=".jpeg";
	private static Boolean localCacheUseable=true;
	
//	Thread pool need to access cacheMap from another thread
	private static volatile Map<StudentID,Future<Image>> cacheMap;
	
	private static Boolean firstTime;
	private static void init(){
		if (!CacheFilePoisition.exists()){
//			it's not that important actually, it will be throne as IOException if we try to access anyway, it just to fill in spaces
			try {
				System.getSecurityManager().checkWrite(CacheFilePoisition.getPath());
			} catch (SecurityException e) {
				System.err.println("Write Permission Denied local Cache Unuseable");
				localCacheUseable=false;
			}
			return;
		}
		
		try {
			
			if (zipObj==null)//construct zipObj
				zipObj=new ZipFile(CacheFilePoisition);
			
			ZipEntry entry;
			Enumeration<? extends ZipEntry> entries = zipObj.entries();
			while (entries.hasMoreElements()){
				entry=entries.nextElement();
				String name=entry.getName(); //it should look like 08***.jpeg
				int poistion;
				name=(poistion=name.lastIndexOf(".jpeg"))!=-1?name.substring(0, poistion):name; //08***.jpeg -> 08***
				try {
					StudentID ID=StudentID.Build(name);
					if (firstTime) 
						cacheMap.put(ID,CompletableFuture.completedFuture(new Image(zipObj.getInputStream(entry),0,0,false, true)));
					else // maybe repeat in if it's not the first time
						if (!cacheMap.keySet().contains(ID))
							cacheMap.put(ID,CompletableFuture.completedFuture(new Image(zipObj.getInputStream(entry),0,0,false, true)));
				} catch (IDFormatException e) { // if something shouldn't exists in the zip file exist
					continue;
				}
				
			}
		} catch (Exception e) {
			localCacheUseable=false;
			e.printStackTrace();
		} finally {
			firstTime=false;
		}
	}
	
	public static Image getStudentImage(StudentID StudentID, Image DefaultImageWhenStudentDoNotHaveImageOnServer) throws IOException {
		try {
			return getStudentImage(StudentID);
		} catch (NoSuchPersonOnServerException e) {
			return DefaultImageWhenStudentDoNotHaveImageOnServer;
		}
	}
	
	public static Image getStudentImage(StudentID StudentID) throws IOException, NoSuchPersonOnServerException{
		Objects.requireNonNull(StudentID);
		try {
			Image image=getCachedImage(StudentID); //if the Image already been cached
			if (image!=null) return image;
		} catch (IOException e) {} //retry if the download failed
		return response(preloadStudentImage(StudentID));
	}
	
	public static Image getCachedImage(StudentID ID, Image DefaultImageWhenStudentDoNotHaveImageOnServerOrNotCached) throws IOException {
		Image image;
		try {
			return (image=getCachedImage(ID))==null?DefaultImageWhenStudentDoNotHaveImageOnServerOrNotCached:image;
		} catch (NoSuchImageOnServerException e) {
			return DefaultImageWhenStudentDoNotHaveImageOnServerOrNotCached;
		}
	}
	
	public static Image getCachedImage(StudentID ID) throws NoSuchImageOnServerException, IOException{
		Objects.requireNonNull(ID);
		return response(cacheMap.get(ID));//if it's null, response() will deal with it.
	}
	
	/**
	 * Get the image from future and process Exceptions
	 * @param future
	 * @return
	 * @throws IOException
	 * @throws NoSuchImageOnServerException
	 */
	private static Image response(Future<Image> future) throws IOException, NoSuchImageOnServerException {
		if (future==null) return null;
		try {
			return future.get();
		} catch (InterruptedException e) {
			throw new Error(e); //now interrupt calls in this class to the executor
		} catch (ExecutionException e) {
			Throwable t = e.getCause();
			if (t instanceof Error) throw (Error) t;
			if (!(t instanceof Exception)) throw new Error(t);
			if (t instanceof IOException) throw (IOException) t;
			if (t instanceof NoSuchImageOnServerException) throw (NoSuchImageOnServerException) t;
			throw new Error(t);
		}
	}
	
	//serialize all the picture to local
	public synchronized static void makeBuffer() throws IOException{
		System.out.println("makingBuffer");
		File secondTemp=new File(CacheFilePoisition.getPath()+"0");//rename the file to the original afterward
		if (!localCacheUseable){
			init();
			if (!localCacheUseable) throw new IOException("local cache files unavailable");
		}
		secondTemp.createNewFile();
		secondTemp.deleteOnExit();//if crash or something bad happens
		try (ZipOutputStream zipout=new ZipOutputStream(new FileOutputStream(secondTemp));){
			synchronized (cacheMap){//make sure no modification while serializing
				for (StudentID id:cacheMap.keySet()) {
					Future<Image> f=cacheMap.get(id);
					if (!f.isDone()) continue; //don't need to wait to complete downloading, that may take too much time
					Image image;
					try {
						image = f.get();
					} catch (InterruptedException | ExecutionException e) {
						continue; //ignore exceptions
					}
					zipout.putNextEntry(new ZipEntry(id.getValue()));
					ImageIO.write(SwingFXUtils.fromFXImage(image, null), "jpeg", zipout);
				}
			}
		}
		CacheFilePoisition.delete(); 
		secondTemp.renameTo(CacheFilePoisition); //delete original file and replace as the new file
	}
	
	public static void clear(){ //I don't why anyone want to do that, but.... emmmm
		CacheFilePoisition.delete();
		cacheMap.clear();
	}
	
	private static ThreadPoolExecutor exc=(ThreadPoolExecutor) Executors.newFixedThreadPool(2);
	// need to test the number out, sometime the school network is terrible
	private static AtomicBoolean EnableAutoRedownload=new AtomicBoolean(true);
	private static final Runnable redownloadWhenNothingToDo=()->{ 
		//when thread pool is idle, it should check whether there is a image failed to download and then redownload it.
		if (EnableAutoRedownload.get()) redownloadExceptionedImages();
	};
	
	public static Future<Image> preloadStudentImage(final StudentID StudentID) {
		Objects.requireNonNull(StudentID);
		if (cacheMap.containsKey(StudentID)) return cacheMap.get(StudentID); //if the task already exists, return the existing value
		Future<Image> future;
		future = exc.submit(()->{
			HttpURLConnection hcon=(HttpURLConnection) new URL(new StringBuilder(60).append(StudentImageURL).append(StudentID.getValue())
				.append(ImageExtensionName).toString()).openConnection();
			if (hcon.getResponseCode()==404){
				throw new NoSuchImageOnServerException(StudentID);
			}
			Image image = new Image(hcon.getInputStream());
			Exception imageloade;
			if ((imageloade=image.getException())!=null){
				if (imageloade instanceof IOException)
					throw imageloade;
				else throw new IOException(imageloade);
			}
			if (exc.getActiveCount()==0) 
				exc.execute(redownloadWhenNothingToDo);
			return image;
		});
		cacheMap.put(StudentID, future);
		return future;
	}
	
	public static void redownloadExceptionedImages() {
		for (StudentID id:cacheMap.keySet()) { // get images with load exceptions
			Future<Image> f=cacheMap.get(id);
			if (!f.isDone()) continue;
			try {
				f.get();
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
				if (e.getCause() instanceof IOException) {
					cacheMap.remove(id); //remove the dead one so the preloader could redownload
					preloadStudentImage(id);
				}
			}
		}
	}
	
	public static void setAutoRedownload(Boolean enable) { //should be disable when high Internet usage
		EnableAutoRedownload.set(enable);
	}
	
	public static class NoSuchImageOnServerException extends NoSuchPersonOnServerException{
//		The image provider from school is wired, lots of students don't have their image

		/**
		 * 
		 */
		private static final long serialVersionUID = -4529077077572136329L;

		NoSuchImageOnServerException(StudentID ID, Throwable e) {
			super(ID, e);
		}

		NoSuchImageOnServerException(StudentID ID) {
			super(ID);
		}
		
	}
}
