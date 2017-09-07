package pers.wusatosi.CRC.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public final class GlobalConfig {
	
	private GlobalConfig() { throw new Error("NO!"); }
	
	static {
		
		final String SYSTEM_PROPERTY_NAME0 = "CRC.GlobalConfig";
		final String FILE_NAME0 = "Settings.cfg";
		
		
		try {
			
			Properties pro = new Properties();
			
			InputStream in;
			if ((in = GlobalConfig.class.getResourceAsStream("Config.cfg")) == null)
				throw new Error("Default Config file lost");
			pro.load(in);
			
			String provider = System.getProperty(SYSTEM_PROPERTY_NAME0);
			
			File file;
			
		
			if (provider == null) {
				file = appFileFinder("CRC",null);
				if (file == null) {
					file = new File(System.getProperty("user.home") + "\\CRC\\" + FILE_NAME0);
				}
			} else {
				file = new File(provider);
				if (!file.exists()) {
					if (!file.isDirectory())
						file = file.getParentFile();
					file.mkdirs();
					file = new File(provider);
					if (file.isDirectory()) {
						file = new File(file, FILE_NAME0);
						if (!file.exists()) file.createNewFile();
					} else {
						file.createNewFile();
					}
				}
			}
			
			ConfigProvider = file;
			
			pro.load(new FileInputStream(file));
			
			MainProperties = pro;
			
		}catch (IOException | SecurityException e) {
			
			Thread current;
			current = Thread.currentThread();
			current.getUncaughtExceptionHandler().uncaughtException(current, e);
			
		} finally {
		
			SYSTEM_PROPERTY_NAME = SYSTEM_PROPERTY_NAME0;
			FILE_NAME = FILE_NAME0;
		
		}
	}
	
	public static final String SYSTEM_PROPERTY_NAME; 
	
	public static final String 	FILE_NAME;
	
	private static File ConfigProvider;
	
	private static Properties MainProperties;
	
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
	
    public String getProperty(String Key) {
    	return MainProperties.getProperty(Key);
    }
    
    public void append(final Map<String,String> properties, Boolean doOverride) {
    	if (doOverride) {
    		MainProperties.putAll(properties);
    		return;
    	}
    	for (String key:properties.keySet()) {
    		if (MainProperties.getProperty(key) != null)
    			continue;
    		MainProperties.put(key, properties.get(key));
    	}
    }
    
    public void append(final Properties properties, Boolean doOverride) {
    	if (doOverride) {
    		MainProperties.putAll(properties);
    		return;
    	}
    	for (Object key:properties.keySet()) {
    		if (MainProperties.get(key) != null) 
    				continue;
    		MainProperties.put(key, properties.get(key));
    	}
    }
    
    void putProperty(String Key,String Value) {
    	MainProperties.setProperty(Key, Value);
    }
    
    public void updateConfigProvider(File Provider, Boolean Override) throws FileNotFoundException, IOException {
    	Properties pro = new Properties();
    	
    	pro.load(new FileInputStream(ConfigProvider));
    	
    	append(pro,Override);
    }
    
}
