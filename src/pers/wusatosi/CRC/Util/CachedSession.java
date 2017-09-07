package pers.wusatosi.CRC.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import pers.wusatosi.CRC.CRCApi.IDFormatException;
import pers.wusatosi.CRC.CRCApi.StudentIdentify.StudentID;
import pers.wusatosi.CRC.CRCApi.UnuseableLoginException;
import pers.wusatosi.CRC.CRCApi.loginedUser;

public final class CachedSession {
	
	private CachedSession() {}

	private static CachedSession Instance;
	
	public synchronized static CachedSession getInstance() {
		if (Instance != null) return Instance;
		return Instance = new CachedSession();
	}
	
	public static Boolean doHaveInfomation() {
		return getInstance().getID() != null;
	}
	
	SessionInfo bufferedinfo;
	private Boolean readed = false;
	
	public Map<String,String> getSessionCookie(){
		if (!readed) read();
		return bufferedinfo == null ? null : Collections.unmodifiableMap(bufferedinfo.cookies);
	}
	
	public Integer getPasswordLength() {
		if (!readed) read();
		return bufferedinfo == null ? null : bufferedinfo.passwordLength;
	}
	
	public StudentID getID() {
		if (!readed) read();
		return bufferedinfo == null ? null : bufferedinfo.ID;
	}
	
	public SessionInfo getSessionInfo() {
		if (!readed) read();
		return bufferedinfo;
	}
	
	private loginedUser lu;
	
	public loginedUser getLoginedUserByCookie() throws UnuseableLoginException, IOException {
		return bufferedinfo == null ? null : (lu==null? lu = login() : lu);
	}
	
	private loginedUser login() throws UnuseableLoginException,IOException{
		Map<String,String> cookies = getSessionCookie();
		Map<String,String> map = new HashMap<>(cookies);
		map.put(loginedUser.MapLoginDefinitier, getID().getValue());
		return new loginedUser(map);
	}
	
	public synchronized void clear() {
		RegistryHelper.Instance.clear();
		FileHelper.Instance.clear();
		Instance = null;
	}
	
	public synchronized Boolean write(SessionInfo user) {
		Objects.requireNonNull(user);
		Instance = null;
		Boolean result = RegistryHelper.Instance.write(user) | FileHelper.Instance.write(user);
		if (result)
			bufferedinfo = user;
		return result;
	}
	
	public Boolean write(loginedUser user, Integer passwdLength) {
		return write(new SessionInfo(user,passwdLength));
	}
	
	synchronized void read() {
		
		if (bufferedinfo != null) return;
		
		SessionInfo rh = RegistryHelper.Instance.read();
		SessionInfo fh = FileHelper.Instance.read();
		
		readed = true;
		
		if (rh != null && fh != null && rh.equals(fh)) {
			bufferedinfo = rh;
			return;
		}
		
		if (rh == fh && rh == null) {
			bufferedinfo = null;
			return;
		}
		
		if (rh == null && fh != null) {
			bufferedinfo = fh;
			return;
		}
		
		if (fh == null && rh != null) {
			bufferedinfo = rh;
			return;
		}
		
		if (!rh.equals(fh)) {
			clear();
			bufferedinfo = null;
			return;
		}
		
	}
	
	public static class SessionInfo implements java.io.Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1534939092001023514L;

		public SessionInfo(loginedUser user, Integer passwordLength) {
			Objects.requireNonNull(user);
			Objects.requireNonNull(passwordLength);
			this.cookies = user.getCookie();
			this.passwordLength = passwordLength;
			this.ID = user.getID();
		}
		
		private SessionInfo() {}
		
		private Map<String,String> cookies;
		
		private Integer passwordLength;
		
		private StudentID ID;

		public Map<String, String> getCookies() {
			return cookies;
		}

		public Integer getPasswordLength() {
			return passwordLength;
		}

		public StudentID getID() {
			return ID;
		}
		
		void CheckNonNull() throws NullPointerException{
			if (cookies==null || passwordLength==null || ID == null)
				throw new NullPointerException("Arguments can not be null");
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SessionInfo other = (SessionInfo) obj;
			if (ID == null) {
				if (other.ID != null)
					return false;
			} else if (!ID.equals(other.ID))
				return false;
			if (cookies == null) {
				if (other.cookies != null)
					return false;
			} else if (!cookies.equals(other.cookies))
				return false;
			if (passwordLength == null) {
				if (other.passwordLength != null)
					return false;
			} else if (!passwordLength.equals(other.passwordLength))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SessionInfo [cookies=" + cookies + ", PasswordLength=" + passwordLength + ", ID=" + ID.getValue() + "]";
		}
		
	}
	
	static class RegistryHelper extends SessionCacheUnit{
		
		static {
			Preferences value;
			try {
				value = Preferences.userNodeForPackage(CachedSession.class);
			} catch (Throwable e) {
				value = null;
			}
			WorkingPreference = value;
		}
		
		private static final Preferences WorkingPreference;
		
		private final static String registryIDKey="ID";
		private final static String registryPasswdLength="Password length";
		private final static String registryCookieKey="Cookie";
		
		private RegistryHelper() {}
		
		public static final RegistryHelper Instance = new RegistryHelper();
		
		@Override
		public SessionInfo read(){
			
			if (WorkingPreference == null) return null; //If system refused to provide preference services
			
			SessionInfo info = new SessionInfo();
			
			try {
				
				String ID = WorkingPreference.get(registryIDKey, null);
				if (ID==null) return null;
				
				try {
					info.ID = StudentID.Build(ID);
				} catch (IDFormatException | IllegalArgumentException e) {
					throw new IllegalStateException(e);
				}
				
				info.passwordLength = WorkingPreference.getInt(registryPasswdLength, -1);
				
				String CKeys = WorkingPreference.get(registryCookieKey, null);
				if (CKeys==null) throw new IllegalStateException();
				
				//Serialization
				Map<String,String> cookie = new HashMap<String,String>();
				int pointer;
				while ((pointer = CKeys.indexOf(","))!=-1) {
					String key = CKeys.substring(0, pointer);
					CKeys = CKeys.substring(pointer+1);
					String value = WorkingPreference.get(key, null);
					if (value == null) continue;
					cookie.put(key, value);
				}

				String value = WorkingPreference.get(CKeys, null);
				if (value != null) ;
					cookie.put(CKeys, value);
				
				
				info.cookies = cookie;
			
				return readed = info;
			}catch (IllegalStateException e) {
				clear();
				if (e.getCause()!=null) e.getCause().printStackTrace();
				else e.printStackTrace();
				return null;
			}
			
		}
		
		private SessionInfo readed;
		
		@Override
		public Boolean write(SessionInfo info) {
			
			if (info.equals(readed)) return false;
			
			if (WorkingPreference == null) return false;
			
			Objects.requireNonNull(info);
			info.CheckNonNull();
			
			//ID
			WorkingPreference.put(registryIDKey, info.getID().getValue());
			
			//passwordLength
			WorkingPreference.putInt(registryPasswdLength, info.getPasswordLength());
			
			//Cookie
			StringBuilder sb = new StringBuilder();
			if (info.getCookies().size()>0) {
				for (String key:info.getCookies().keySet()) {
					String value = info.getCookies().get(key);
					WorkingPreference.put(key, value);
					sb.append(key);
					sb.append(",");
				}
				sb.deleteCharAt(sb.length()-1);
			}
			WorkingPreference.put(registryCookieKey, sb.toString());
			
			try {
				WorkingPreference.flush();
			} catch (BackingStoreException e) {
				if (WorkingPreference.get(registryCookieKey, null)==null)
					return false;
			}
			
			return true;
		}
		
		@Override
		public Boolean clear() {
			try {
				readed = null;
				WorkingPreference.clear();
				return true;
			} catch (BackingStoreException e) {
				e.printStackTrace();
				return false;
			}
		}
		
	}

	static class FileHelper extends SessionCacheUnit{
		
		private static final File AppDataPoisition = appFileFinder("CRC",null);
		private static final File SessionFile = 
				AppDataPoisition != null ? new File(AppDataPoisition,"CRC_Session.cfg") : null;		
				
		private FileHelper() {}
		
		public static final FileHelper Instance = new FileHelper();
		
		private SessionInfo readed;
				
		@Override
		public synchronized SessionInfo read() {
			
			if (SessionFile == null || !SessionFile.exists()) return null;
			
			System.out.println(SessionFile.getPath());
			
			//Too lazy, just use object
			try(ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(SessionFile));) {
				
				try {
					return readed = (SessionInfo) objIn.readObject();
				} catch (ClassNotFoundException e) {
					throw new Error(e);
				} catch (RuntimeException e) {
					Thread.currentThread().getUncaughtExceptionHandler()
						.uncaughtException(Thread.currentThread(), e);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
			
		}
	
		@Override
		public Boolean write(SessionInfo info) {
			
			if (info.equals(readed)) return true;
			
			try {
			
				if (AppDataPoisition == null) return false;
				
				if (!SessionFile.exists()) {
					AppDataPoisition.mkdirs();
					SessionFile.createNewFile();
				}
				
				try (ObjectOutputStream objout = new ObjectOutputStream(new FileOutputStream(SessionFile))){
					objout.writeObject(info);
					objout.flush();
				}
				
				return true;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
	
		@Override
		public Boolean clear() {
			
			readed = null;
			
			if (AppDataPoisition == null || !SessionFile.exists()) return false;
			
			return SessionFile.delete();
		}
		
		//From https://gist.github.com/Cakemix1337/2427327
	    private static File appFileFinder(String folder, String Default) {
	        String OS = System.getProperty("os.name").toUpperCase();
	        if (OS.contains("WIN")) {
	            if (new File(System.getenv("APPDATA")).exists()) {
	                if (!new File(System.getenv("APPDATA") + "/"+folder).exists()) {
	                    new File(System.getenv("APPDATA") + "/"+folder).mkdirs();
	                }
	                return new File(System.getenv("APPDATA") + "/"+folder+"/" );
	            }
	        } else if (OS.contains("MAC")) {
	            if (new File(System.getProperty("user.home") + "/Library/Application Support").exists()) {
	                if (!new File(new File(System.getProperty("user.home") + "/Library/Application Support") + "/"+folder).exists()) {
	                    new File(new File(System.getProperty("user.home") + "/Library/Application Support") + "/"+folder).mkdirs();
	                }
	                return new File(new File(System.getProperty("user.home") + "/Library/Application Support") +"/"+folder+"/" );
	            }
	        }
	        return Default == null ? null : new File(Default);
	    }
		
	}

	private static abstract class SessionCacheUnit{
		
		public SessionCacheUnit() {}
		
		public abstract SessionInfo read();
		
		public abstract Boolean write(SessionInfo info);
		
		public abstract Boolean clear();
	}
	

	
}
