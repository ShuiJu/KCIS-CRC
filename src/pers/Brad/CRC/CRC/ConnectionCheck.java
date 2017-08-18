package pers.Brad.CRC.CRC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Connection Check to portal.kcisec.com, www.baidu.com, www.google.com, ordering.kcisec.com
 * @author wusatosi/Brad.Wu
 *
 */
public class ConnectionCheck {
	public static int portalCheck() throws IOException{
		long time=System.currentTimeMillis();
		HttpURLConnection hcon=(HttpURLConnection) new URL(new StringBuilder(75).append("http://portal.kcisec.com/rollcall/Account/ConnectionCheck?_=")
					.append(System.nanoTime()).toString()).openConnection();
		if (!new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8")).readLine().equals("正常")) throw new ErrorResponse("Wrong Response while checking connection");
		return (int) (System.currentTimeMillis()-time);
	}

	public static int baiduCheck() throws IOException{
		long time=System.currentTimeMillis();
		HttpURLConnection hcon=(HttpURLConnection) new URL("http://www.baidu.com").openConnection();
		hcon.connect();
		hcon.disconnect();
		return (int) (System.currentTimeMillis()-time);
	}

	public static int googleCheck() throws IOException{
		long time=System.currentTimeMillis();
		HttpURLConnection hcon=(HttpURLConnection) new URL("http://www.google.com").openConnection();
		hcon.connect();
		hcon.disconnect();
		return (int) (System.currentTimeMillis()-time);
	}
	
	public static int orderingCheck() throws IOException{
		long time=System.currentTimeMillis();
		HttpURLConnection hcon=(HttpURLConnection) new URL("http://ordering.kcisec.com/chaxun.asp").openConnection();
		hcon.connect();
		hcon.disconnect();
		return (int) (System.currentTimeMillis()-time);
	}
}
