package pers.Brad.CRC.InternetAcessPart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;


public class MapGenerater {
	
	private static final String hashURL="http://kcis-crl.oss-cn-shanghai.aliyuncs.com/ID-Name_HashTable";
	private static final String confirmURL="http://kcis-crl.oss-cn-shanghai.aliyuncs.com/ID-Name_Confirm";
	private static final String CacheFolder=System.getProperty("user.home")+"\\CRL";
	private static final String CacheFile=CacheFolder+"\\NameHashToID_Table.cfg";
	
	private static final String InFile_Length="length:"; 
	private static final String lineSeparator=System.lineSeparator();
	
	private static Map<Integer,String> NameToIDMap=null;
	private static List<String> IDList=null;
	
	public static Boolean updatedlist=false;
	public static Boolean needUpdate=false;
	private static Boolean needReconect=false;
	private static Boolean worminged=false;
	
	static{
		if (NameToIDMap==null){
			try {
				init();
				if(needReconect)throw new IOException("Need update");
			} catch (IOException e) {
				if (!e.getMessage().equals("Need update")) System.err.println(e);
				new Thread(){
					public void run(){
						while (needReconect){
							try {
								init();
								if(needReconect)throw new IOException("Need update");
							} catch (IOException e) {
								System.err.println("TRY UPDATE MAP FAILED");
							}
							try {Thread.sleep(5000);} catch (InterruptedException e) {}
						}
					}
				}.start();
			}
		}
	}
	
	private MapGenerater(){}
	
	public static void init() throws IOException{
		needReconect=false;
		try{
			try {
				if (!confirmCacheFile()) throw new IOException("File Has Been Change");
			} catch (IOException e) {
				if (!e.getMessage().equals("File Has Been Change")) System.err.println(e);;
				try{
					downloadList();
				}catch (IOException ioe){
					needUpdate=true;
					if (!worminged){
						System.err.println(ioe);
						System.err.println("WORMING! THE LIST GENERATER IS USING THE DEFAULT LIST");
						worminged=true;
					}
					loadDefault();
				}
			}
			try{
				if (!isLocalCacheLatest()){
					downloadList();
				}else{
					try{
						reader(new FileInputStream(CacheFile));
					}catch (IllegalArgumentException ia){
						downloadList();
					}
				}
			}catch (IOException e){
				if (!worminged){
					System.err.println(e);
					System.err.println("WORMING! THE LIST GENERATER IS USING THE DEFAULT LIST");
					worminged=true;
				}
				loadDefault();
				throw e;
			}
		}catch (IOException e2){
			if (e2.getMessage().equals("Can not load default list")) throw e2;
			else{
				loadDefault();
				throw e2;
			}
		}
	}
	
	public static Map<Integer,String> getHashedNameToIDMap(){
		return Collections.unmodifiableMap(NameToIDMap);
	}
	
	public static List<String> getIDList(){
		return Collections.unmodifiableList(IDList);
	}
	
	private static Boolean isLocalCacheLatest() throws IOException{
		HttpURLConnection hcon=(HttpURLConnection) new URL(confirmURL).openConnection();
		hcon.addRequestProperty("Referer", "http://portal.kcisec.com/rollcall/CarRollCall");
		String TMD5=new BufferedReader(new InputStreamReader(hcon.getInputStream())).readLine();
//		System.out.println("Online MD5:"+TMD5);
		File f=new File(CacheFile);
		if (!f.exists()) return false;
		InputStream in=new FileInputStream(f);
		byte[] buffer=new byte[1024];
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			for (int num=0;(num=in.read(buffer))>0;){
				md.update(buffer,0,num);
			}
			String LMD5=new BigInteger(md.digest()).toString();
//			System.out.println("local MD5"+LMD5);
			if (LMD5.equals(TMD5)){
				in.close();
				return true;
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		in.close();
		return false;
	}
		
	private static final String registryKey="Cache_MD5";
	
	private static Boolean confirmCacheFile() throws IOException{
		File f=new File(CacheFolder);
		if (!f.exists()) f.mkdirs();
		f=new File(CacheFile);
		if (!f.exists()) return false;
		InputStream in=new FileInputStream(f);
		byte[] buffer=new byte[1024];
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			for (int num=0;(num=in.read(buffer))>0;){
				md.update(buffer,0,num);
			}
			try{
				Preferences pre=Preferences.userNodeForPackage(MapGenerater.class);
				if (pre.get(registryKey, "null").equals(new BigInteger(md.digest())+"")){
					in.close();
					return true;
				}else{
					in.close();
					return false;
				}
			}catch (java.lang.UnsupportedOperationException uns){}
		} catch (NoSuchAlgorithmException e) {
			throw new InternalError(e);
		} finally{
			in.close();
		}
		return false;
	}
	
	private static void writeLocalCache() throws IOException{
		File f=new File(CacheFolder);
		if (!f.exists()) f.mkdirs();
		f=new File(CacheFile);
		f.createNewFile();
		OutputStreamWriter out=new OutputStreamWriter(new FileOutputStream(f),"utf-8");
		try{
			out.write(InFile_Length);
			out.write(NameToIDMap.size()+"");
			out.write(lineSeparator);
			for (int key:NameToIDMap.keySet()){
				out.write(key+"");
				out.write("\t");
				out.write(NameToIDMap.get(key));
				out.write(lineSeparator);
				out.flush();
			}
		}catch (IOException e){
			throw e;
		}finally{
			out.close();
		}
		InputStream in=new FileInputStream(f);
		byte[] buffer=new byte[1024];
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			for (int num=0;(num=in.read(buffer))>0;){
				md.update(buffer,0,num);
			}
			try{
				Preferences pre=Preferences.userNodeForPackage(MapGenerater.class);
				pre.put(registryKey, new BigInteger(md.digest())+"");
			}catch (java.lang.UnsupportedOperationException uns){}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e){
			in.close();
			throw e;
		}
		in.close();
	}
	
	private static void downloadList() throws IOException{
		try{
			HttpURLConnection hcon=(HttpURLConnection) new URL(hashURL).openConnection();
			hcon.addRequestProperty("Referer", "http://portal.kcisec.com/rollcall/CarRollCall");
			reader(hcon.getInputStream());
			updatedlist=true;
			writeLocalCache();
		}catch (IOException ioe){
			throw ioe;
		}
	}
	
	private static void loadDefault() throws IOException{
		needReconect=true;
		URL url=MapGenerater.class.getResource("Default_Table");
		if (url==null) throw new IOException("Can not load default list");
		try{reader(new FileInputStream(url.getFile()));}catch (IOException e){e.printStackTrace();}
		catch (IllegalArgumentException iae){throw new InternalError(iae);}
	}
	
	private static void reader(InputStream in) throws IOException{
		BufferedReader buf=new BufferedReader(new InputStreamReader(in));
		String str;
		int pointer=-1;
		while ((str=buf.readLine())!=null&&(pointer=str.indexOf(InFile_Length))==-1);
		if (str==null||pointer==-1) throw new IllegalArgumentException();
		try{
			int length=Integer.parseInt(str.substring(pointer+InFile_Length.length()));
			NameToIDMap=new HashMap<Integer,String>(length);
			IDList=new ArrayList<String>(length);
		}catch (NumberFormatException e){
			throw new IllegalArgumentException();
		}
		try{
			while ((str=buf.readLine())!=null){
				pointer=str.indexOf("\t");
				NameToIDMap.put(Integer.parseInt(str.substring(0, pointer)), str.substring(pointer+1));
				IDList.add(str.substring(pointer+1));
			}
		}catch (NumberFormatException e){
			throw new IllegalArgumentException();
		}
		buf.close();
	}
}
