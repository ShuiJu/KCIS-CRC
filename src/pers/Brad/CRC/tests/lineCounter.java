package pers.Brad.CRC.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class lineCounter{
	static final String[] path={new File(System.getProperty("user.dir")).getParent()};
	
	static int fileCount=0;
	static long sizeCount=0;
	static int LineCounter=0;
	
	public static void main(String[] args){
		for (int i=0;i<path.length;i++){
			System.out.println("-----Running On "+(i+1)+"th path-----");
			System.out.println("-----("+path[i]+")-----");
			getFiles(new File(path[i]));
			System.out.println();
		}
		System.out.println();
		System.out.println(fileCount+"\tfiles");
		System.out.println(LineCounter+"\tlines");
		System.out.print(sizeCount+"\tcharacters");
	}
	
	public static void getFiles(File inf){
		File[] fl=inf.listFiles();
		for (int i=0;i<fl.length;i++){
			String name=fl[i].getName();
			if (name.equals("bin")
					||name.equals(".classpath")			||name.equals(".project")				||name.indexOf(".fxml")!=-1
					||name.indexOf(".metada")!=-1		||name.indexOf(".recommenders")!=-1		||name.indexOf(".setting")!=-1
					||name.indexOf(".jpg")!=-1			||name.equals("listGenerater.java")		||name.indexOf(".html")!=-1
					||name.equals("build.fxbuild")	 	||name.indexOf(".JPG")!=-1				||name.indexOf("tests")!=-1
					||name.indexOf(".gif")!=-1
					)
				continue;
			if (fl[i].isDirectory()) getFiles(fl[i]);
			else signalFilelineCounter(fl[i]);
		}
	}
	
	public static void signalFilelineCounter(File f){
		try{
			System.out.print(f.getPath());
			fileCount++;
			String str;
			BufferedReader buf=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			int forSingle=0;
			while ((str=buf.readLine())!=null) {
				if (str.equals("")||str.indexOf("import")!=-1||str.indexOf("package")!=-1) continue;
				LineCounter++;
				forSingle++;
				int Pointer;
				while ((Pointer=str.indexOf(" "))!=-1){
					str=str.substring(0,Pointer)+str.substring(Pointer+1);
				}
				while ((Pointer=str.indexOf("\t"))!=-1){
					str=str.substring(0,Pointer)+str.substring(Pointer+1);
				}
				sizeCount=sizeCount+str.length();
			}
			buf.close();
			System.out.println("\t"+forSingle);
		}catch (Exception e){
			System.err.println(f.getPath());
			e.printStackTrace();
			System.exit(0);
		}
	}
}
