package pers.Brad.CRC.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import pers.Brad.CRC.InternetAcessPart.RollCallUtil;
import pers.Brad.CRC.InternetAcessPart.StanderStudent;
import pers.Brad.CRC.InternetAcessPart.loginedUser;

public class T16_6 {
	public static void main(String[] args) throws Throwable{
		File f=new File("C:\\Users\\wusat\\Desktop\\New Text Document.txt");
		new loginedUser("08426","54sb".toCharArray()).getStudentsInfoList();
		System.out.print("Finish");
		/*
		RollCallUtil rcu=new RollCallUtil(new loginedUser("08426","54sb".toCharArray()));
		BufferedReader buf=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String str;
		System.out.println("OK");
		while ((str=buf.readLine())!=null){
			try{
				System.out.println("adding "+str);
				StanderStudent stu=rcu.add(str);
				System.out.println(stu.getName()+" added");
			}catch (Throwable e){
				System.out.println("Exception while adding "+str);
				e.printStackTrace();
			}
		}
		System.out.println("Finish");
		rcu.send();
		buf.close();
		*/
	}
}
