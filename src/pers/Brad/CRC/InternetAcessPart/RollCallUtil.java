package pers.Brad.CRC.InternetAcessPart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import pers.Brad.CRC.InternetAcessPart.Exceptions.ErrorResponse;
import pers.Brad.CRC.InternetAcessPart.Exceptions.IDFormatException;
import pers.Brad.CRC.InternetAcessPart.Exceptions.UnuseableLoginException;;

/**
 * RollCallUtil is a tool kit for rollcalling by Apis;
 * Where you can add users by Card ID, and it will 
 * return an Array with information in it.
 * 
 * @author wusatosi/Brad.Wu
 *
 */
public class RollCallUtil implements Cloneable,java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7797378644063552409L;
	
	/**
	 * WARMMING:
	 * This Constructor may have a really big delay,
	 * the time dpends on the network sitution  
	 * 
	 * @param user - The control user, all internet activility rely on it
	 * @throws IOException - when network isn't functioning
	 * @throws UnuseableLoginException - when the user's cookie is no longer useable	(cause by loginedUser.getStudentsInfoList() )
	 * @throws ErrorResponse - when the server send wrong infomation back		(cause by loginedUser.getStudentsInfoList() )
	 * @throws NotTheDayException - when it's not the day for rollcalling
	 */
	public RollCallUtil(loginedUser user) throws IOException,UnuseableLoginException,ErrorResponse{
		this.user=user;
		ArrayList<StanderStudent> list = (ArrayList<StanderStudent>) user.getStudentsInfoList();
		if (list.size()==0) {
			shouldBeList=new ArrayList<StanderStudent>();
			rollcalled=new ArrayList<StanderStudent>(10);
			bufferedStudentInfo=new ArrayList<StanderStudent>(5);
		}
		else {
			shouldBeList=list;
			rollcalled=new ArrayList<StanderStudent>(list.size()+10);
			bufferedStudentInfo=new ArrayList<StanderStudent>(list.size()+20);
			if (!(list.get(0).getProperty(loginedUser.onCarDefinitionKey).equals(loginedUser.NOT_CHECKED)))
				for (int i=0;i<list.size();i++) {
					if (list.get(i).getProperty(loginedUser.onCarDefinitionKey).equals(loginedUser.Checked_ON_CAR))
						rollcalled.add(list.get(i));
				}
		}
		bufferedStudentInfo.addAll(shouldBeList);
		LineID=user.lineID;
	}
	
	private final static String orderingURL="http://ordering.kcisec.com/chaxun.asp";
	
	public final loginedUser user;
	static final Map<Integer,String> mainMap=(Map<Integer, String>) MapGenerater.getHashedNameToIDMap();
	private List<StanderStudent> shouldBeList;
	public final String LineID;
	
	private ArrayList<StanderStudent> rollcalled;
	private ArrayList<StanderStudent> bufferedStudentInfo;
	
	/**
	 * Get the ArrayList with student should be in the roll call list
	 * 
	 * @return the ArrayList with students should be in the list filled
	 */
	public List<StanderStudent> getShouldBeStudentInfoList(){
		return this.shouldBeList;
	}
	
	/**
	 * Add a student into the list of rollcalled list* 
	 * 
	 * @param CardID - CardID or student ID both works
	 * @return StanderStudent - the infomation of the student inputed
	 * @throws IOException - Network Exception
	 * @throws NoSuchPersonInDataBaseException - when can't find the cardID in data base.
	 * @throws ErrorResponse - when server give an wrong rallback
	 * @throws IDFormatException - ID inputed is in wrong format
	 * @throws NoSuchPersonOnServerException 
	 */
	public StanderStudent add(String CardID) throws IOException, NoSuchPersonInDataBaseException, IDFormatException, NoSuchPersonOnServerException{
		if (CardID.length()==5){
			if (!loginedUser.StudentIDChecker(CardID)) throw new IDFormatException(CardID);
			for (StanderStudent checker:rollcalled)
				if (checker.getID().equals(CardID)) throw new PersonAlreadyExistsException(CardID);
			StanderStudent back=getStudentInfo(CardID);
			rollcalled.add(back);
			return back;
		}
		CardID=RollCallUtil.cardIDToID(CardID);
		return add(CardID);
	}
	
	public class PersonAlreadyExistsException extends java.lang.IllegalArgumentException{
		/**
		 * 
		 */
		private static final long serialVersionUID = 7182957073736958507L;

		public final String ID;
		
		private PersonAlreadyExistsException(String ID){
			super("ID:"+ID+" already exists");
			this.ID=ID;
		}
	}
	
	/**
	 * To remove a student in the list
	 * 
	 * @param StudentID0
	 * @return Boolean - do have the student
	 * @throws IDFormatException - Student ID in wrong format 
	 */
	public Boolean remove(String StudentID0) throws IDFormatException{
		if (!loginedUser.StudentIDChecker(StudentID0)) throw new IDFormatException(StudentID0);
		int rollcalledS=rollcalled.size();
		for (int i=0;i<rollcalledS;i++){
			if (rollcalled.get(i).getID().equals(StudentID0)){
				rollcalled.remove(i);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * To remove the index of the student in the list
	 * 
	 * @param index
	 * @return The sdutdent
	 */
	public StanderStudent remove(int index){
		return rollcalled.remove(index);
	}
	
	/**
	 * get the size of rollcalled list
	 * 
	 * @return int - the size of rollcalled list
	 */
	public int size(){
		return rollcalled.size();
	}
	
	/**
	 * get the rollcalled list
	 * 
	 * @return list<String> - a list with student ID in it
	 */
	public List<String> getRollCalledList(){
		List<String> sendBack=new ArrayList<String>(rollcalled.size());
		rollcalled.forEach((stu)->{
			sendBack.add(stu.getID());
		});
		return sendBack;
	}
	
	public List<StanderStudent> getRollCalledInfo(){
		return Collections.unmodifiableList(rollcalled);
	}
	
	/**
	 * Get student Info from the index of the list
	 * 
	 * @see getStudentInfo
	 * @param index - The index in the list
	 * @return StanderStudent - Student Info
	 */
	public StanderStudent getStudentInfo(int index){
		return rollcalled.get(index);
	}
	
	/**
	 * Get student info by student ID, or card ID
	 * 
	 * @param ID - both student ID and Card ID will work
	 * @return the Info of Student
	 * @throws NoSuchPersonInDataBaseException
	 * @throws IOException
	 * @throws IDFormatException - cause by cardIDToID
	 * @throws NoSuchPersonOnServerException 
	 */
	public StanderStudent getStudentInfo(String ID) throws NoSuchPersonInDataBaseException, IOException, IDFormatException, NoSuchPersonOnServerException{
		ID=RollCallUtil.cardIDToID(ID);
		int shouldBeSize=shouldBeList.size();
		for (int i=0;i<shouldBeSize;i++) {
			if (shouldBeList.get(i).getID().equals(ID))
				return shouldBeList.get(i);
		}
		StanderStudent stu=user.getStudentInfoByID(ID);
		if (stu==null) throw new NoSuchPersonOnServerException(ID);
		return stu;
	}
	
	/**
	 * Send the rollcalled data.
	 * 
	 * @see loginedUser.sendRollCallData
	 * @throws IOException - when an internet error had been thrown
	 * @throws ErrorResponse - when server returned the wrong infomation
	 */
	public void send() throws IOException, ErrorResponse, NotTheDayException{
		if (!user.isTheDay) throw new NotTheDayException();
		String handler;
		try {
			final List<String> sendIn=new ArrayList<String>(rollcalled.size());
			rollcalled.forEach((stu)->{
				sendIn.add(stu.getID());
			});
			if (!(handler=user.sendRollCallData(sendIn)).equals("确认成功!")) throw new ErrorResponse(handler);
		} catch (IDFormatException e) {
			throw new InternalError(e);
		}
	}
	
	public RollCallUtil clone(){
		RollCallUtil cloned=null;
		try {
			cloned = (RollCallUtil) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
		return cloned;
	}
	
	/**
	 * This is for transforming Card ID to student ID
	 * 
	 * @param CardID - 10 bits card ID
	 * @return Student Info
	 * @throws IOException - Exception hapened during network acesses, both during 
	 * @throws NoSuchPersonInDataBaseException - Just don't have that person in data base
	 * @throws IDFormatException - CardID format error
	 * @throws NoSuchPersonOnServerException 
	 * @throws ErrorResponse
	 */
	public static String cardIDToID(String CardID) 
			throws IOException,IDFormatException, NoSuchPersonOnServerException, NoSuchPersonInDataBaseException, ErrorResponse{
		if (CardID==null) throw new IDFormatException(null);
		if (CardID.length()==5){
			if (!loginedUser.StudentIDChecker(CardID)) throw new IDFormatException(CardID);
			return CardID;
		}
		if (CardID.length()!=10){
			throw new IDFormatException(CardID);
		}
		try{
			Long.parseLong(CardID);
		}catch (NumberFormatException e){
			throw new IDFormatException(CardID,e);
		}
		HttpURLConnection hcon=(HttpURLConnection) new URL(orderingURL+"?kahao="+CardID+"&OK=%C8%B7%B6%A8").openConnection();
		BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream(),"GBK"));
		buf.skip(1801);
		String str=buf.readLine();
		int bp=str.indexOf("年级以上]");
		if (bp==-1){
			if (str.indexOf("无此帐户，或帐户已被锁定！")!=-1) throw new NoSuchPersonOnServerException(CardID);
			char[] buffer=new char[hcon.getContentLength()];
			buf.read(buffer);
			str=new String(buffer);
			if ((bp=str.indexOf("年级以上]"))==-1)
				if (str.indexOf("无此帐户，或帐户已被锁定！")==-1)
					throw new ErrorResponse(str);
				else throw new NoSuchPersonOnServerException(CardID);
		}
		try{
			str=str.substring(bp+"年级以上]".length(), str.indexOf(" 20"));
		}catch(Exception e){
			throw new ErrorResponse("Error processing the name, failed to get the name from:"+str,e);
		}
		CardID=mainMap.get(str.hashCode());
		if (CardID==null) throw new NoSuchPersonInDataBaseException(str);
		return CardID;
	}

	public static Boolean isIDExistsInOrderingSystem(String ID) throws IOException, IDFormatException{
		if (ID.length()==10){
			try {
				RollCallUtil.cardIDToID(ID);
				return true;
			} catch (NoSuchPersonOnServerException e) {
				return false;
			} catch (NoSuchPersonInDataBaseException e) {
				return true;
			}catch (IDFormatException e) {
				throw new InternalError(e);
			}
		}
		if (loginedUser.StudentIDChecker(ID)) throw new IDFormatException(ID);
		HttpURLConnection hcon=(HttpURLConnection) new URL(orderingURL+"?kahao="+ID+"&OK=%C8%B7%B6%A8").openConnection();
		BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream()));
		buf.skip(1801);
		String str=buf.readLine();
		int bp=str.indexOf("年级以上]");
		if (bp==-1){
			if (str.indexOf("无此帐户，或帐户已被锁定！")!=-1) return false;
			char[] buffer=new char[hcon.getContentLength()];
			buf.read(buffer);
			str=new String(buffer);
			if ((bp=str.indexOf("年级以上]"))==-1)
				if (str.indexOf("无此帐户，或帐户已被锁定！")==-1)
					throw new ErrorResponse(str);
				else return false;
		}
		return true;
	}

	public static String cardIDToName(String CardID) throws IDFormatException, IOException, NoSuchPersonOnServerException,ErrorResponse{
		if (!loginedUser.StudentIDChecker(CardID)) throw new IDFormatException(CardID);
		HttpURLConnection hcon=(HttpURLConnection) new URL(orderingURL+"?kahao="+CardID+"&OK=%C8%B7%B6%A8").openConnection();
		BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream()));
		buf.skip(1801);
		String str=buf.readLine();
		int bp=str.indexOf("年级以上]");
		if (bp==-1){
			if (str.indexOf("无此帐户，或帐户已被锁定！")!=-1) throw new NoSuchPersonOnServerException(CardID);
			char[] buffer=new char[hcon.getContentLength()];
			buf.read(buffer);
			str=new String(buffer);
			if ((bp=str.indexOf("年级以上]"))==-1)
				if (str.indexOf("无此帐户，或帐户已被锁定！")==-1)
					throw new ErrorResponse(str);
				else throw new NoSuchPersonOnServerException(CardID);
		}
		try{
			return str.substring(bp+"年级以上]".length(), str.indexOf(" 20"));
		}catch(Exception e){
			throw new ErrorResponse("Error processing the name, failed to get the name from:"+str);
		}
	}
	
	public static StanderStudent getStanderStudentByCardIDFromOrdering(String CardID)
			throws NoSuchPersonOnServerException, IDFormatException, IOException, ErrorResponse, NoSuchPersonInDataBaseException{
		if (!loginedUser.StudentIDChecker(CardID)) throw new IDFormatException(CardID);
		String name=cardIDToName(CardID);
		if (CardID.length()==10){
			CardID=mainMap.get(name.hashCode());
			if (CardID==null) throw new NoSuchPersonInDataBaseException(CardID);
		}
		return new StudentInfo(CardID,name);
	}
	
	public class NotTheDayException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = -8384999746884724065L;
		
		public NotTheDayException(){
			super();
		}
	}

}