package pers.Brad.CRC.InternetAcessPart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import pers.Brad.CRC.InternetAcessPart.Exceptions.ErrorResponse;
import pers.Brad.CRC.InternetAcessPart.Exceptions.IDFormatException;

public class StudentIdentify implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 769641239057630564L;
	
	private final static String orderingURL="http://ordering.kcisec.com/chaxun.asp";
	
	private static ArrayList<StudentIdentify> buffer=new ArrayList<StudentIdentify>(100);
	
	public static StudentIdentify Build(String IDOrCardID) throws IDFormatException{
		if (!loginedUser.StudentIDChecker(IDOrCardID)) throw new IDFormatException(IDOrCardID);
		Boolean isCardID=IDOrCardID.length()==10;
		for (int i=0;i<buffer.size();i++){
			StudentIdentify holder=buffer.get(i);
			if (isCardID&&holder.CardID!=null){
				if (IDOrCardID.equals(holder.CardID))
					return holder;
			}
		}
		return new StudentIdentify(IDOrCardID,!isCardID);
	}
	
	private StudentIdentify(String IDOrCardID,Boolean isID){
		this.isID=isID;
		if (!isID){
			CardID=IDOrCardID;
		}else{
			CardID=null;
			ID=IDOrCardID;
		}
		buffer.add(this);
	}
	
	private final String CardID;
	private String ID;
	
	private Boolean isID;
	
	public static class StudentID extends StudentIdentify implements java.io.Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1204256651565854624L;
		
		private static ArrayList<StudentID> buffer=new ArrayList<StudentID>(100);
		
		private StudentID(StudentIdentify stu) throws NoSuchPersonOnServerException, NoSuchPersonInDataBaseException, IOException, ErrorResponse{
			super(forInit(stu),true);
			buffer.add(this);
		}
		
		private static String forInit(StudentIdentify stu) throws NoSuchPersonOnServerException, NoSuchPersonInDataBaseException, IOException, ErrorResponse{
			if (stu.isID)return stu.ID;
			try {
				return RollCallUtil.cardIDToID(stu.CardID);
			} catch (IDFormatException e) {
				throw new InternalError("Error while init studentID, this exception shouldn't been throw ",e);
			}
			
		}
		
		public static StudentID builder(StudentIdentify stu) throws NoSuchPersonOnServerException, NoSuchPersonInDataBaseException, IOException, ErrorResponse{
			if (!stu.isID)
				return new StudentID(stu);
			for (int i=0;i<buffer.size();i++){
				StudentID holder=buffer.get(i);
				if (holder.getValue().equals(stu.ID))
					return holder;
			}
			return new StudentID(stu);
		}
		
		static StudentID builder_NoSearch(StudentIdentify stu){
			if (!stu.isID) throw new IllegalArgumentException("CardID tranform is not support here");
			for (int i=0;i<buffer.size();i++){
				StudentID holder=buffer.get(i);
				if (holder.getValue().equals(stu.ID))
					return holder;
			}
			try {
				return new StudentID(stu);
			} catch (NoSuchPersonOnServerException | NoSuchPersonInDataBaseException | IOException | ErrorResponse e) {
				throw new InternalError("shouldn't reach",e);
			}
		}
	}
	
	public String getValue(){
		return isID?ID:CardID;
	}

	public StudentID getStudentID() throws NoSuchPersonOnServerException, NoSuchPersonInDataBaseException, IOException, ErrorResponse{
		return StudentID.builder(this);
	}
	
	public static StudentID cardIDToID(StudentIdentify CardID) 
			throws IOException,NoSuchPersonOnServerException, NoSuchPersonInDataBaseException, ErrorResponse{
		if (CardID.isID) return CardID.getStudentID();
		HttpURLConnection hcon=(HttpURLConnection) new URL(orderingURL+"?kahao="+CardID.ID+"&OK=%C8%B7%B6%A8").openConnection();
		BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream()));
		buf.skip(1801);
		String str=buf.readLine();
		int bp=str.indexOf("年级以上]");
		if (bp==-1) throw new NoSuchPersonOnServerException(CardID,new ErrorResponse(str));
		try{
			str=str.substring(bp+"年级以上]".length(), str.indexOf(" 20"));
		}catch(Exception e){
			throw new ErrorResponse("Error processing the name, failed to get the name from:"+str,e);
		}
		String holder=RollCallUtil.mainMap.get(str.hashCode());
		if (holder==null) throw new NoSuchPersonInDataBaseException(str);
		try {
			return StudentID.builder_NoSearch(Build(holder));
		} catch (IDFormatException e) {
			throw new InternalError(e);
		}
	}
}
