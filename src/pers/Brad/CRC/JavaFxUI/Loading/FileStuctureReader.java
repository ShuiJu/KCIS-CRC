package pers.Brad.CRC.JavaFxUI.Loading;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FileStuctureReader {
	
	public static void main(String[] args) throws Throwable {
		BufferedReader buf=new BufferedReader(new InputStreamReader(FileStuctureReader.class.getResourceAsStream("Images")));
		String str;
		while ((str=buf.readLine())!=null) {
			System.out.println(str);
		}
	}
	
}
