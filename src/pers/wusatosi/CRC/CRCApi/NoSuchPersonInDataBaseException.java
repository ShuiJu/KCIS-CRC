package pers.wusatosi.CRC.CRCApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.List;

import pers.wusatosi.CRC.CRCApi.StudentIdentify.StudentID;


public class NoSuchPersonInDataBaseException extends PersonNotFoundException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8446601063728498776L;
	
	private volatile  IDAnalyzation Analyzation;
	private volatile ReportState reportState=ReportState.No_Need;
	private static MissingNumberReporting action=new DefualtReportingHandlerImpl();
	
	public NoSuchPersonInDataBaseException(StudentIdentify ID){
		super(ID);
		confirmAndReport();
	}

	public NoSuchPersonInDataBaseException(StudentIdentify ID,String msg){
		super(ID);
		confirmAndReport();
	}
	
	public NoSuchPersonInDataBaseException(StudentIdentify ID,Throwable e){
		super(ID,e);
		confirmAndReport();
	}
	
	public NoSuchPersonInDataBaseException(String ID) throws IDFormatException {
		super(StudentIdentify.Build(ID));
	}
	
	private void confirmAndReport(){
		StudentIdentify ID=super.getID();
		if (ID==null){
			Analyzation=IDAnalyzation.illegal_ID_Format.setResponse("ID can't be null");
			return;
		}
		if (Analyzation==IDAnalyzation.illegal_ID_Format) return;
		HttpURLConnection hcon;
		String name;
		try {
			hcon = (HttpURLConnection) new URL("http://ordering.kcisec.com/chaxun.asp"+"?kahao="+ID.getValue()+"&OK=%C8%B7%B6%A8").openConnection();
			BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream()));
			buf.skip(1801);
			name=buf.readLine();
			int bp=name.indexOf("年级以上]");
			if (bp==-1){
				Analyzation=IDAnalyzation.No_Such_ID_In_Server.setResponse(name);
				return;
			}
			try{
				name=name.substring(bp+"年级以上]".length(), name.indexOf(" 20"));
			}catch(Exception e){
				Analyzation=IDAnalyzation.Error_While_Defining.setResponse("Error while processing the name");
				return;
			}
		} catch (IOException e1) {
			Analyzation=IDAnalyzation.Error_While_Defining.setResponse("IOException");
			return;
		}
		if (MapGenerater.needUpdate)
			try {
				MapGenerater.init();
			} catch (IOException e) {}
		List<StudentID> list=MapGenerater.getIDList();
		for (StudentID str:list){
			if (str.equals(ID)){
				Analyzation=IDAnalyzation.ID_Exists;
				return;
			}
		}
		Analyzation=IDAnalyzation.No_Such_ID_In_DataBase;
		synchronized (reportState){
			reportState=ReportState.Reporting;
			final String innerID=name;
			new Thread(()->{reportState=action.takeAction(super.getID().getValue(),innerID);},"ID Missing Reporting").start();;
		}
	}
	
	public static enum IDAnalyzation{	
		illegal_ID_Format,
		Error_While_Defining,
		No_Such_ID_In_Server,
		No_Such_ID_In_DataBase,
		ID_Exists{
			@Override
			public String getMsg(){
				return "ID exists, it must be cause by InternalError";
			}
		};
		
		private String msg=null;
		
		private IDAnalyzation setResponse(String msg){
			this.msg=msg;
			return this;
		}
		
		public String getMsg(){
			return msg;
		}
	}
	
	public static enum ReportState{
		No_Need,
		Reporting,
		Report_Success,
		Report_Failed;
	}
	
	public static void setReportAction(MissingNumberReporting action){
		if (action==null) return;
		NoSuchPersonInDataBaseException.action=action;
	}
	
	public interface MissingNumberReporting{
		
		default public ReportState takeAction(String ID,String name){
			try{
				final String MainURL="http://crl-feedback.oss-cn-shanghai.aliyuncs.com";
				final String fileName=java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
				final URL URL=new URL(MainURL+"/Missing_ID_Reporting/"+fileName);
				HttpURLConnection hcon=(HttpURLConnection) URL.openConnection();
				hcon.setRequestMethod("PUT");
				hcon.setDoOutput(true);
				hcon.setDoInput(true);
				String upload=new StringBuilder(35).append("ID:").append(ID).append(" - name on server: ").append(name).toString();
				String MD5=java.util.Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(upload.getBytes()));
				hcon.addRequestProperty("Cache-Control", "no-cache");
				hcon.addRequestProperty("Content-Disposition", fileName);
				hcon.addRequestProperty("Content-Encoding", "utf-8");
				hcon.addRequestProperty("Content-Type", "text/html");
				hcon.addRequestProperty("Content-MD5", MD5);
				hcon.addRequestProperty("x-oss-server-side-encryption", "AES256");
				hcon.addRequestProperty("x-oss-object-acl", "public-read");
				hcon.addRequestProperty("Content-Length", upload.length()+"");
				OutputStream out=hcon.getOutputStream();
				out.write(upload.getBytes());
				out.flush();
				out.close();
				try{
					BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream()));
					String str;
					while ((str=buf.readLine())!=null){
						System.out.println(str);
					}
					buf.close();
					return ReportState.Report_Success;
				}catch (IOException e){
					BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getErrorStream()));
					String str;
					while ((str=buf.readLine())!=null){
						System.out.println(str);
					}
					buf.close();
					return ReportState.Report_Failed;
				}
			} catch (Exception e){
				e.printStackTrace();
				return ReportState.Report_Failed;
			}
		}
		
	}
	
	static class DefualtReportingHandlerImpl implements MissingNumberReporting{}
}
