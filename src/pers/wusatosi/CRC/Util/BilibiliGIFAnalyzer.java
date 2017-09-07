package pers.wusatosi.CRC.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BilibiliGIFAnalyzer{
	
	private static final String JSON_URL_PATH="http://www.bilibili.com/index/index-icon.json";
	
	public static List<SorceGIFInfo> getURLs() throws IOException{
		String json=getJson();
		String[] objects=json.split("\"id\"");
		List<SorceGIFInfo> returnList=new ArrayList<SorceGIFInfo>(objects.length-1); 
		Boolean first=true;
		for (String thisObj:objects){
			if (first){
				first=false;
				continue;
			}
			thisObj=thisObj.split("title\":\"")[1];
			String title=thisObj.substring(0, thisObj.indexOf("\""));
			thisObj=thisObj.split("icon\":\"")[1];
			String surl=thisObj.substring(0, thisObj.indexOf("\""));
			URL url=surl.indexOf("http")==-1?(new URL("http:"+surl)):new URL(surl);
			returnList.add(new SorceGIFInfo(title, url));
		}
		return returnList;
	}

	public static class SorceGIFInfo{
		private SorceGIFInfo(String title,URL url){
			this.title=title;
			this.url=url;
		}
		
		private final String title;
		private final URL url;
		
		public String getTitle(){
			return this.title;
		}
		
		public URL getURL(){
			return this.url;
		}
	}
	
	private static String getJson() throws IOException{
		InputStream input=null;
		ByteArrayOutputStream out=null;
		try {
			HttpURLConnection hcon=(HttpURLConnection) new URL(JSON_URL_PATH).openConnection();
			input=hcon.getInputStream();
			out=new ByteArrayOutputStream(1024*50);
			byte[] buffer=new byte[1024];
			int len=-1;
			while ((len=input.read(buffer))!=-1){
				out.write(buffer, 0, len);
			}
		} catch (MalformedURLException e) {
			throw new InternalError(e);
		} finally {
			if (input!=null) input.close();
			if (out!=null) out.close();
		}
		return decode(out.toString());
	}
	
	private static String decode(String unicodeStr) {  
	    if (unicodeStr == null) {  
	        return null;  
	    }  
	    StringBuffer retBuf = new StringBuffer(unicodeStr.length());  
	    int maxLoop = unicodeStr.length();  
	    for (int i = 0; i < maxLoop; i++) {  
	        if (unicodeStr.charAt(i) == '\\') {  
	            if ((i < maxLoop - 5)  
	                    && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr  
	                            .charAt(i + 1) == 'U')))  
	                try {  
	                    retBuf.append((char) Integer.parseInt(  
	                            unicodeStr.substring(i + 2, i + 6), 16));  
	                    i += 5;  
	                } catch (NumberFormatException localNumberFormatException) {  
	                    retBuf.append(unicodeStr.charAt(i));  
	                }  
	            else  
	                retBuf.append(unicodeStr.charAt(i));  
	        } else {  
	            retBuf.append(unicodeStr.charAt(i));  
	        }  
	    }
	    String lol=retBuf.toString().replace("\\/", "/");
	    return lol.indexOf("////")==-1?lol:lol.replaceAll("////", "//");  
	}
	
}
