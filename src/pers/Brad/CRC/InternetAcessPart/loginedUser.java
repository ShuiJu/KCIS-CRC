package pers.Brad.CRC.InternetAcessPart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pers.Brad.CRC.InternetAcessPart.Exceptions.ErrorResponse;
import pers.Brad.CRC.InternetAcessPart.Exceptions.IDFormatException;
import pers.Brad.CRC.InternetAcessPart.Exceptions.UnuseableLoginException;

/**
 * For user to login into the school roll call system and interact with the system
 * @author wusatosi/Brad.Wu
 *
 */
public class loginedUser implements Cloneable,java.io.Serializable{
	
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 4958538376480182518L;
	
	private static final String portalConnectionCheckURL	="http://portal.kcisec.com/rollcall/Account/ConnectionCheck";
	private static final String orderingConnectionCheckURL	="http://ordering.kcisec.com/chaxun.asp";
	private static final String baiduConnectionCheckURL		="http://www.baidu.com";
	private static final String googleConnectionCheckURL	="http://www.google.com";
	
	private static final String getStudentInfoURL			="http://portal.kcisec.com/rollcall/CarRollCall/GetStudentInfo";
	private static final String getCarTimeURL				="http://portal.kcisec.com/rollcall/CarRecordTime/GetCarTime";
	private static final String recordCarTimeURL			="http://portal.kcisec.com/rollcall/CarRecordTime/FillCarTime";
	private static final String sendRollCallDataURL			="http://portal.kcisec.com/rollcall/CarRollCall/RollCallFormUpdate";
	private static final String getListURL					="http://portal.kcisec.com/rollcall/CarRollCall/RollCallFormData";
	private static final String rootURL						="http://portal.kcisec.com";
	
	/**
	 * Creat a new login with ID and passwd in char[]
	 * @param ID - Student ID, Card ID is not useable
	 * @param passwd - Char[] only for securty reason
	 * @throws IOException - when there's problem connecting to server
	 * @throws UnuseableLoginException - when the ID_Passwd combine is not correct
	 * @throws IDFormatException - when ID is in wrong format
	 */
	public loginedUser(String ID,char[] passwd) throws IOException,UnuseableLoginException, IDFormatException{
		/*
		Normallogin=true;
		this.ID=ID;
		StringBuilder sb=new StringBuilder("http://portal.kcisec.com/rollcall/Account/LogInCheck?UserId=");
		sb.append(ID);
		sb.append("&Password=");
		for (int i=0;i<passwd.length;i++){
			sb.append(passwd[i]);
		}
		sb.append("&returnUrl=&_");
		sb.append(System.nanoTime());
		HttpURLConnection hcon=(HttpURLConnection) new URL(sb.toString()).openConnection();
		sb=null;
		BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8"));
		String backStr=buf.readLine();
		if (backStr.equals("/rollcall/CarRollCall/Index")){
			checked=true;
			cookie=new HashMap<String,String>();
			for (int i=0;hcon.getHeaderField(i)!=null;i++){
				if (hcon.getHeaderFieldKey(i)!=null&&hcon.getHeaderFieldKey(i).equals("Set-Cookie")){
					cookie.put(hcon.getHeaderField(i).substring(0,hcon.getHeaderField(i).indexOf("=")), 
							hcon.getHeaderField(i).substring(hcon.getHeaderField(i).indexOf("=")+1,
									hcon.getHeaderField(i).indexOf(";")));
				}
			}
		}else throw new UnuseableLoginException(this,backStr);
		//450-500ms 391
		String[] sets=getInitVaribles();
		//90-110ms 404
		this.UserName=sets[0];
		if (sets[1]==null) this.isTheDay=false;
		else this.isTheDay=true;
		this.lineID=sets[2];
		this.lineName=sets[3];
		*/
		this(ID,passwd,(info)->{});
	}
	
	/**
	 * Creat a new login with ID and passwd in char[], also having a listner to listen how's the state of the login process
	 * @param ID - Student ID, Card ID is not useable
	 * @param passwd - Char[] only for securty reason
	 * @param sl - state listener
	 * @throws IOException - when there's problem connecting to server
	 * @throws UnuseableLoginException - when the ID_Passwd combine is not correct
	 * @throws IDFormatException - when ID is in wrong format
	 */
	public loginedUser(String ID,char[] passwd,StateListener sl) throws IOException,UnuseableLoginException, IDFormatException{
		Objects.requireNonNull(ID);
		Objects.requireNonNull(passwd);
		Objects.requireNonNull(sl);
		if (!loginedUser.StudentIDChecker(ID)) throw new IDFormatException(ID);
		Normallogin=true;
		this.ID=ID;
		StringBuilder sb=new StringBuilder("http://portal.kcisec.com/rollcall/Account/LogInCheck?UserId=");
		sb.append(ID);
		sb.append("&Password=");
		for (int i=0;i<passwd.length;i++){
			sb.append(passwd[i]);
		}
		sb.append("&returnUrl=&_");
		sb.append(System.nanoTime());
		sl.call("logining to server...");
		HttpURLConnection hcon=(HttpURLConnection) new URL(sb.toString()).openConnection();
		sb=null;
		BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8"));
		String backStr=buf.readLine();
		if (backStr.equals("/rollcall/CarRollCall/Index")){
			checked=true;
			cookie=new HashMap<String,String>();
			for (int i=0;hcon.getHeaderField(i)!=null;i++){
				if (hcon.getHeaderFieldKey(i)!=null&&hcon.getHeaderFieldKey(i).equals("Set-Cookie")){
					cookie.put(hcon.getHeaderField(i).substring(0,hcon.getHeaderField(i).indexOf("=")), 
							hcon.getHeaderField(i).substring(hcon.getHeaderField(i).indexOf("=")+1,
									hcon.getHeaderField(i).indexOf(";")));
				}
			}
		}else throw new UnuseableLoginException(this,backStr);
		sl.call("login success! Getting basic infomation");;
		//450-500ms
		String[] sets=getInitVaribles();
		//90-110ms
		this.UserName=sets[0];
		if (sets[1]==null) this.isTheDay=false;
		else this.isTheDay=true;
		this.lineID=sets[2];
		this.lineName=sets[3];
		sl.call("Successed!");;
	}
	
	/**
	 * login by student ID only
	 * WARMING: this way is using the bug inside the system, it may be fix and no longer use anymore 
	 * @param ID - User's student ID
	 * @throws IOException - when there's trouble connecting to school server
	 * @throws IDFormatException - when the ID is unuseable to login
	 */
	public loginedUser(String ID) throws IOException, IDFormatException{
		/*
		if (!CardIDChecker(ID)) throw new IDUnuseableException(ID);
		this.ID=ID;
		Normallogin=false;
		HttpURLConnection hcon=(HttpURLConnection) new URL("http://portal.kcisec.com/rollcall/CarRollCall/Index").openConnection();
		hcon.addRequestProperty("Cookie", new StringBuilder(20).append("kcis_rollcall=").append(ID).append("; ").toString());
		cookie=new HashMap<String,String>();
		cookie.put("kcis_rollcall", ID);
		for (int i=0;hcon.getHeaderField(i)!=null;i++){
			if (hcon.getHeaderFieldKey(i)!=null&&hcon.getHeaderFieldKey(i).equals("Set-Cookie")){
				cookie.put(hcon.getHeaderField(i).substring(0,hcon.getHeaderField(i).indexOf("=")), 
						hcon.getHeaderField(i).substring(hcon.getHeaderField(i).indexOf("=")+1,
								hcon.getHeaderField(i).indexOf(";")));
			}
		}
		String[] sets=getInitVaribles();
		this.UserName=sets[0];
		if (sets[1]==null) this.isTheDay=false;
		else this.isTheDay=true;
		this.lineID=sets[2];
		this.lineName=sets[3];
		*/
		this(ID,(info)->{});
	}

	/**
	 * login by student ID only and having a listener to listen the state of login Process
	 * WARMING: this way is using the bug inside the system, it may be fix and no longer use anymore 
	 * @param ID - User's student ID
	 * @throws IOException - when there's trouble connecting to school server
	 * @throws IDFormatException - when the ID is unuseable to login
	 */
	public loginedUser(String ID,StateListener sl) throws IOException, IDFormatException{
		Objects.requireNonNull(ID);
		Objects.requireNonNull(sl);
		this.ID=ID;
		Normallogin=false;
		sl.call("logining to server...");
		HttpURLConnection hcon=(HttpURLConnection) new URL("http://portal.kcisec.com/rollcall/CarRollCall/Index").openConnection();
		hcon.addRequestProperty("Cookie", new StringBuilder(20).append("kcis_rollcall=").append(ID).append("; ").toString());
		cookie=new HashMap<String,String>();
		cookie.put("kcis_rollcall", ID);
		for (int i=0;hcon.getHeaderField(i)!=null;i++){
			if (hcon.getHeaderFieldKey(i)!=null&&hcon.getHeaderFieldKey(i).equals("Set-Cookie")){
				cookie.put(hcon.getHeaderField(i).substring(0,hcon.getHeaderField(i).indexOf("=")), 
						hcon.getHeaderField(i).substring(hcon.getHeaderField(i).indexOf("=")+1,
								hcon.getHeaderField(i).indexOf(";")));
			}
		}
		sl.call("login success! Getting basic infomation");;
		String[] sets=getInitVaribles();
		this.UserName=sets[0];
		if (sets[1]==null) this.isTheDay=false;
		else this.isTheDay=true;
		this.lineID=sets[2];
		this.lineName=sets[3];
		sl.call("Successed!");;
	}

	/**
	 * To identify the ID infomation in when creating the user
	 */
	public static final String MapLoginDefinitier="!!!ID!!!";
	
	/**
	 * login by cookie
	 * @param cookie - Map<String,String> type of cookie, and need to have ID including
	 * @throws IOException - when trouble connecting to the server
	 * @throws IDFormatException - when there's no ID infomation inside the map
	 */
	public loginedUser(Map<String,String> cookie) throws IOException, IDFormatException{
		/*
		this.Normallogin=true;
		this.ID=cookie.get(MapLoginDefinitier);
		cookie.remove(MapLoginDefinitier);
		if (ID==null) throw new IDFormatException("There is no ID number in cached table");
		this.cookie=cookie;
		if (!checkUserServiceability()) throw new IDFormatException("ID:"+ID+" in cached table is not in right format");
		System.out.println(new StringBuilder(50).append("login By cookie: ").append(cookie).toString());
		String[] sets=getInitVaribles();
		this.UserName=sets[0];
		if (sets[1]==null) this.isTheDay=false;
		else this.isTheDay=true;
		this.lineID=sets[2];
		this.lineName=sets[3];
		*/
		this(cookie,(info)->{});
	}
	
	/**
	 * login by cookie with a listener listening state
	 * @param cookieS - Map<String,String> type of cookie, and need to have ID including
	 * @param sl - state listener
	 * @throws IOException - when trouble connecting to the server
	 * @throws IDFormatException - when there's no ID infomation inside the map
	 * @throws IllegalArgumentException - when the map's infomation is not in right format
	 */
	public loginedUser(Map<String,String> cookie,StateListener sl) throws IOException, IDFormatException, IllegalArgumentException{
		this.Normallogin=true;
		this.ID=cookie.get(MapLoginDefinitier);
		if (ID==null) throw new IllegalArgumentException();
		this.cookie=new HashMap<String,String>(cookie.size()-1);
		this.cookie.putAll(cookie);
		this.cookie.remove(MapLoginDefinitier);
		sl.call("logining to server...");
		if (!checkUserServiceability()) throw new IDFormatException(ID);
		System.out.println(new StringBuilder(50).append("login By cookie: ").append(cookie).toString());
		sl.call("login success! Getting basic infomation");;
		String[] sets=getInitVaribles();
		this.UserName=sets[0];
		if (sets[1]==null) this.isTheDay=false;
		else this.isTheDay=true;
		this.lineID=sets[2];
		this.lineName=sets[3];
		sl.call("Successed!");;
	}
	
	
	private String[] getInitVaribles() throws IOException{
		HttpURLConnection hcon=(HttpURLConnection) new URL(
				"http://portal.kcisec.com/rollcall/CarRecordDriver/CarRecordDriver_MainPage").openConnection();
		if (Normallogin) cookieWriter(hcon);
		else cookieWriterNoCheck(hcon);
		BufferedReader website=new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8"));
		String str;
		String[] set=new String[4];
		for (int i=0;i<67;i++) website.readLine();
		str=website.readLine();
		try{
			set[0]=str.substring(str.indexOf("title='")+"title='".length(), str.indexOf("' style='"));
		}catch (Exception e){
			throw new ErrorResponse();
		}
		for (int i=0;i<144-68-1;i++) website.readLine();
		str=website.readLine();
		try{
			if (str.indexOf("\"0\"")==-1) throw new ErrorResponse();
			if (str.indexOf("\"1\"")!=-1) set[1]=true+"";
		}catch (Exception e){
			throw new ErrorResponse();
		}
		for (int i=0;i<10;i++) website.readLine();
		str=website.readLine();
		try{
			set[2]=str.substring(str.indexOf("\"<option value='")+"\"<option value='".length(),str.indexOf("'>"));
			set[3]=str.substring(str.indexOf("'>")+"'>".length(),str.indexOf("</option>"));
		}catch (Exception e){
			hcon=(HttpURLConnection) new URL("http://portal.kcisec.com/rollcall/CarRecordReport/CarRecordReport001_MainPage").openConnection();
			if (Normallogin) cookieWriter(hcon);
			else cookieWriterNoCheck(hcon);
			website=new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8"));
			for (int i=0;i<143;i++) website.readLine();
			str=website.readLine();
			try{
				set[2]=str.substring(str.indexOf("append(\"<option value='")+"append(\"<option value='".length(),str.indexOf("'>"));
				set[3]=str.substring(str.indexOf("'>")+2,str.indexOf("</option>"));
			}catch (Exception e1){
				throw new ErrorResponse(e1);
			}
		}
		return set;
	}

	/**The ID of the user*/
	public final String ID;
	/**The line's Name of the user*/
	public final String lineID;
	/**The line's ID of the user*/
	public final String lineName;
	/**The User's name*/
	public final String UserName;
	/**Whether the user in logined in a normal way*/
	public final Boolean Normallogin;
	/**Whether this is the day for roll calling*/
	public final Boolean isTheDay;
	
	private Boolean checked=false;
	private Map<String,String> cookie;
	
	private Map<String,StanderStudent> StudentInfoBuffer=new HashMap<String,StanderStudent>();
	
	/**To clear the buffer for caching the student's info*/
	public void clearBuffer(){
		StudentInfoBuffer=new HashMap<String,StanderStudent>();
	}
	
	/**
	 * Clear the buffer of a certain student
	 * @param ID - the student's ID
	 */
	public void clearBuffer(String ID){
		StudentInfoBuffer.remove(ID);
	}
	
	/**
	 * For getting the buffered student's info
	 * @return the map with buffered student inside
	 */
	public Map<String,StanderStudent> getBufferedStudentInfo(){
		return this.StudentInfoBuffer;
	}
	
	/**
	 * Get the Image URL of the student
	 * @param ID - the student's ID
	 * @return the URL
	 * @throws IOException - when exception throws while connecting to server
	 */
	public String getImageURLByID(String ID) throws IOException{
		StanderStudent a=getStudentInfoByID(ID);
		if (a==null) return null;
		String str=(a.getProperty("PIC"));
		if (str.toString().equals("")){
			String g=a.getProperty("gender");
			if (g==null) throw new ErrorResponse();
			if (g.equals("F")){
				StringBuilder sb=new StringBuilder(50).append(rootURL);
				return sb.append("/rollcall/images/female.gif").toString();
			}
			else{
				StringBuilder sb=new StringBuilder(50).append(rootURL);
				return sb.append("/rollcall/images/male.gif").toString();
			}
		};
		int breakpoint=str.indexOf("src='");
		if (breakpoint==-1) throw new ErrorResponse(str);
		try{
			str=(str.substring(breakpoint+"src='".length(), str.lastIndexOf("' /")));
			return new StringBuilder(str.length()+rootURL.length()).insert(0, rootURL).toString();
		}catch (Exception e){
			throw new ErrorResponse(str,e);
		}
	}
	
	/**
	 * Get the Name of the certain student
	 * @param ID - the student's ID
	 * @return the Name of the student
	 * @throws IOException - Exception while connecting to server
	 */
	public String getNameByID(String ID) throws IOException{
		StanderStudent a=getStudentInfoByID(ID);
		if (a==null) return null;
		String aPro=a.getProperty("fullname");
		if (aPro==null){
			StudentInfoBuffer.remove(ID);
			return getStudentInfoByID(ID).getProperty("fullname");
		}
		return a.getProperty("fullname");
	}
	
	/**
	 * Get the Class ID of the student
	 * @param ID - Student's ID
	 * @return The Class's ID
	 * @throws IOException - Exception while connecting to server
	 */
	public String getClassIDByID(String ID) throws IOException{
		StanderStudent a=getStudentInfoByID(ID);
		if (a==null) return null;
		String aPro;
		if ((aPro=a.getProperty("deptid"))==null){
			StudentInfoBuffer.remove(ID);
			a=getStudentInfoByID(ID);
		}
		return aPro;
	}
	
	/**
	 * get a StanderStudent by ID
	 * @param ID - the student's ID
	 * @return A StanderStudent with info map from the server
	 * @throws IOException - Exception while connecting to server
	 */
	public StanderStudent getStudentInfoByID(String ID) throws IOException{
		StanderStudent cache=StudentInfoBuffer.get(ID);
		if (cache!=null) return cache;
		Map<String,String> back=new HashMap<String,String>();
		String str=null;
		try{
			HttpURLConnection hcon=(HttpURLConnection) new URL(new StringBuilder(100).
					append(getStudentInfoURL).append("?strStudentID=")
					.append(ID).append("&t1=").append(System.nanoTime()).toString()).openConnection();
			cookieWriter(hcon);
			BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8"));
			str=buf.readLine();
			buf.close();
		}catch (IOException e){
			checked=false;
			throw e;
		}
		try{
			if (str==null||str.indexOf("\"")==-1) return null;
			int breakpoint=str.indexOf("{");
			if (breakpoint!=-1) str=str.substring(breakpoint+2, str.lastIndexOf("}"));
			/*
			while (str.indexOf("\"")!=-1){
				if (str.indexOf(",")!=-1) {
					String[] a=getInfoArrayByID_(str.substring(0,str.indexOf(",")));
					back.put(a[0], a[1]);
					str=str.substring(str.indexOf(",")+1);
				}
				else{
					String[] a=getInfoArrayByID_(str);
					back.put(a[0], a[1]);
					break;
				}
			}
			*/
			String[] cellholder=str.split("\",");
			for (String holder:cellholder){
				String[] infoH=holder.split(":",2);
				if (infoH.length!=2){
					throw new java.lang.StringIndexOutOfBoundsException("String splited here shouldn't have two returned result, "
							+ "(now have "+infoH.length+" one) String is:"+holder);
				}
				for (int i=0;i<2;i++){
					infoH[i]=infoH[i].replaceAll("\"", "");
				}
				back.put(infoH[0], infoH[1]);
			}
			StanderStudent stu=new StudentInfo(back);
			StudentInfoBuffer.put(ID, stu);
			return stu;
		}catch (java.lang.StringIndexOutOfBoundsException e){
			throw new ErrorResponse("Exception while analyzing:"+str,e);
		}
	}
	
	/*
	private String[] getInfoArrayByID_(String str){
		/*
		String[] back=new String[2];
		int breakpoint=str.indexOf(":");
		back[0]=str.substring(0,breakpoint);
		back[1]=str.substring(breakpoint+1);
		for (int i=0;i<2;i++){
			breakpoint=back[i].indexOf("\"");
			if (breakpoint!=-1){
				back[i]=back[i].substring(breakpoint+1,back[i].length()-1);
			}
		}
		return back;
		String[] cache=str.split(":");
		for (int i=0;i<cache.length;i++){
			cache[i]=cache[i].replaceAll("\"", "");
		}
		return cache;
	}
*/
	
	/**
	 * Get called stop calls, this method will check from the server everytime so if you just recorded stop calls, it will still give you the lastest info
	 * @return A String array of the time of stop calls
	 * @throws IOException - Exception while connecting to server
	 */
	public String[] getStopCalls() throws IOException{
		HttpURLConnection hcon=(HttpURLConnection) new URL(
				new StringBuilder(90).append(getCarTimeURL)
				.append("?strLineID=").append(lineID)
				.append("&t1=").append(System.nanoTime())
				.toString()).openConnection();
		cookieWriter(hcon);
		BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8"));
		String str=buf.readLine();
		if (str==null) return new String[2];
		try{
			if (str.indexOf(",")!=-1){
				String[] out=new String[2];
				if (str.indexOf(",")!=0) out[0]=str.substring(0, str.indexOf(","));
				out[1]=str.substring(str.indexOf(",")+1);
				if (out[1].equals("")) out[1]=null;
				return out;
			}
		}catch (Exception e){
			e.printStackTrace();
			return new String[2];
		}
		return new String[2];
	}
	
	/**
	 * Set a stop call in the server
	 * @param StartFinash - true=record start time, false=record end time
	 * @throws IOException - Exception while connecting to server
	 */
	public void recordCarStopCall(Boolean StartFinash) throws IOException{
		String strType;
		strType=StartFinash?"STime":"ETime";
		HttpURLConnection hcon=(HttpURLConnection) new URL(
				new StringBuilder(105).append(recordCarTimeURL).append("?strLineID=")
				.append(lineID).append("&strType=").append(strType).append("&t1=")
				.append(System.nanoTime()).toString()).openConnection();
		cookieWriter(hcon);
		hcon.setRequestProperty("Referer", "http://portal.kcisec.com/rollcall/CarRecordTime/Index");
		BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8"));
		String str=buf.readLine();
		if ((!str.equals("到站时间已记录成功！")&&!str.equals("发车时间已记录成功！"))){
			ErrorResponse exce=new ErrorResponse(str);
			System.err.println(exce);
			throw exce;
		}
	}
	
	/**
	 * Send Roll Call Data to the server
	 * @param Data - the list of data which contains 
	 * @return info back from server
	 * @throws IOException - Exception while connecting to server
	 * @throws IDFormatException - When ID inside is not right format
	 */
	public String sendRollCallData(List<String> Data) throws IOException, IDFormatException{
		int DataS=Data.size();
		StringBuilder sb=new StringBuilder(6*DataS+1);
		for (int i=0;i<DataS;i++){
			String str=Data.get(i);
			if (!loginedUser.StudentIDChecker(str)) throw new IDFormatException(str);
			sb.append(Data.get(i)+",");
		}
		return sendRollCallData(sb.toString());
	}
	
	/**
	 * Send Roll Call Data to the server
	 * @param Data - String Array fill with ID 
	 * @return info back from server
	 * @throws IOException - Exception while connecting to server
	 * @throws IDFormatException 
	 */
	public String sendRollCallData(String[] Data) throws IOException, IDFormatException{
		StringBuilder sb=new StringBuilder(6*Data.length+1);
		for (int i=0;i<sb.length();i++){
			String str=Data[i];
			if (!loginedUser.StudentIDChecker(str)) throw new IDFormatException(str);
			sb.append(str+",");
		}
		return sendRollCallData(sb.toString());
	}
	
	/**
	 * Send Roll Call Data to the server
	 * @param Data - already formed String
	 * @return info back from server
	 * @throws IOException
	 */
	public String sendRollCallData(String Data) throws IOException{
		HttpURLConnection hcon=(HttpURLConnection) new URL
				(new StringBuilder(107+Data.length()).append(sendRollCallDataURL)
						.append("?isAnswerRetValue=").append(Data)
						.append("&lineID=").append(lineID).append("&t1=")
						.append(System.nanoTime()).toString()).openConnection();
		cookieWriter(hcon);
		BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8"));
		return buf.readLine();
	}
	
	public static final String onCarDefinitionKey="isOnCarInSystem";
	public static final String Checked_NOT_ON_CAR="Checked_NOT_ON_CAR";
	public static final String Checked_ON_CAR="Checked_ON_CAR";
	public static final String NOT_CHECKED="NOT_CHECKED";
	
	public List<StanderStudent> getStudentsInfoList() throws IOException{
		HttpURLConnection hcon=(HttpURLConnection) new URL(
				new StringBuilder(90).append(getListURL).append("?lineID=")
				.append(lineID).append("&_=").append(System.nanoTime()).toString()).openConnection();
		cookieWriter(hcon);
		BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8"));
		String line=buf.readLine();
		line="<tr id='07376' class='tr_Roll' style='color:#d0cfcf' ><td><h2>1</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/07376.jpeg' /></td><td name='fullname' align='left' title='张涵越(CHERYL_ZHANG)'><h2>张涵越(CHERYL_ZHANG)</h2></td><td><h2>07376</td><td align='left'><h2>张涵越</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='07376' checked value='07376_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='07376' value='07376_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='07376'  value='07376_UN'>未点名</span></h2></td></tr><tr id='07439' class='tr_Roll' style='color:#d0cfcf' ><td><h2>2</h2></td><td><img  src='/rollcall/images/female.gif' /></td><td name='fullname' align='left' title='蔡新蕊(Rita_Cai)'><h2>蔡新蕊(Rita_Cai)</h2></td><td><h2>07439</td><td align='left'><h2>蔡新蕊</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='07439' value='07439_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='07439' checked value='07439_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='07439'  value='07439_UN'>未点名</span></h2></td></tr><tr id='07588' class='tr_Roll' style='color:#d0cfcf' ><td><h2>3</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/07588.jpeg' /></td><td name='fullname' align='left' title='雷淇(Edith_Lei)'><h2>雷淇(Edith_Lei)</h2></td><td><h2>07588</td><td align='left'><h2>雷淇</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='07588' checked value='07588_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='07588' value='07588_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='07588'  value='07588_UN'>未点名</span></h2></td></tr><tr id='06072' class='tr_Roll' style='color:#d0cfcf' ><td><h2>4</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/06072.jpeg' /></td><td name='fullname' align='left' title='朱博艺(BETTY_ZHU)'><h2>朱博艺(BETTY_ZHU)</h2></td><td><h2>06072</td><td align='left'><h2>朱博艺</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='06072' checked value='06072_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='06072' value='06072_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='06072'  value='06072_UN'>未点名</span></h2></td></tr><tr id='06031' class='tr_Roll' style='color:#d0cfcf' ><td><h2>5</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/06031.jpeg' /></td><td name='fullname' align='left' title='章天一(ELING_ZHANG)'><h2>章天一(ELING_ZHANG)</h2></td><td><h2>06031</td><td align='left'><h2>章天一</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='06031' checked value='06031_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='06031' value='06031_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='06031'  value='06031_UN'>未点名</span></h2></td></tr><tr id='06032' class='tr_Roll' style='color:#d0cfcf' ><td><h2>6</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/06032.jpeg' /></td><td name='fullname' align='left' title='齐浠希(STEPHANIE_QI)'><h2>齐浠希(STEPHANIE_QI)</h2></td><td><h2>06032</td><td align='left'><h2>齐浠希</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='06032' value='06032_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='06032' checked value='06032_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='06032'  value='06032_UN'>未点名</span></h2></td></tr><tr id='06265' class='tr_Roll' style='color:#d0cfcf' ><td><h2>7</h2></td><td><img  src='/rollcall/images/female.gif' /></td><td name='fullname' align='left' title='郁嘉筠(Annie_Yu)'><h2>郁嘉筠(Annie_Yu)</h2></td><td><h2>06265</td><td align='left'><h2>郁嘉筠</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='06265' value='06265_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='06265' checked value='06265_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='06265'  value='06265_UN'>未点名</span></h2></td></tr><tr id='12156' class='tr_Roll' style='color:#d0cfcf' ><td><h2>8</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/12156.jpeg' /></td><td name='fullname' align='left' title='孙绛(June_Sun)'><h2>孙绛(June_Sun)</h2></td><td><h2>12156</td><td align='left'><h2>孙绛</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='12156' checked value='12156_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='12156' value='12156_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='12156'  value='12156_UN'>未点名</span></h2></td></tr><tr id='11063' class='tr_Roll' style='color:#d0cfcf' ><td><h2>9</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/11063.jpeg' /></td><td name='fullname' align='left' title='施苏苡(Susie_Shi)'><h2>施苏苡(Susie_Shi)</h2></td><td><h2>11063</td><td align='left'><h2>施苏苡</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='11063' value='11063_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='11063' checked value='11063_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='11063'  value='11063_UN'>未点名</span></h2></td></tr><tr id='11079' class='tr_Roll' style='color:#d0cfcf' ><td><h2>10</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/11079.jpeg' /></td><td name='fullname' align='left' title='王思懿(Susan_Wang)'><h2>王思懿(Susan_Wang)</h2></td><td><h2>11079</td><td align='left'><h2>王思懿</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='11079' value='11079_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='11079' checked value='11079_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='11079'  value='11079_UN'>未点名</span></h2></td></tr><tr id='11170' class='tr_Roll' style='color:#d0cfcf' ><td><h2>11</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/11170.jpeg' /></td><td name='fullname' align='left' title='夏雯珺(Amy_Xia)'><h2>夏雯珺(Amy_Xia)</h2></td><td><h2>11170</td><td align='left'><h2>夏雯珺</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='11170' value='11170_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='11170' checked value='11170_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='11170'  value='11170_UN'>未点名</span></h2></td></tr><tr id='10003' class='tr_Roll' style='color:#d0cfcf' ><td><h2>12</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/10003.jpeg' /></td><td name='fullname' align='left' title='陈妤(Angela_Chen)'><h2>陈妤(Angela_Chen)</h2></td><td><h2>10003</td><td align='left'><h2>陈妤</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='10003' value='10003_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='10003' checked value='10003_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='10003'  value='10003_UN'>未点名</span></h2></td></tr><tr id='08395' class='tr_Roll' style='color:#d0cfcf' ><td><h2>13</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/08395.jpeg' /></td><td name='fullname' align='left' title='林诗炀(CAROLINE_LIN)'><h2>林诗炀(CAROLINE_LIN)</h2></td><td><h2>08395</td><td align='left'><h2>此臨時新增的上車人員同時在[SH4]線別也存在名單</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='08395' value='08395_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='08395' checked value='08395_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='08395'  value='08395_UN'>未点名</span></h2></td></tr><tr id='08398' class='tr_Roll' style='color:#d0cfcf' ><td><h2>14</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/08398.jpeg' /></td><td name='fullname' align='left' title='林玥星(Linda_Lin)'><h2>林玥星(Linda_Lin)</h2></td><td><h2>08398</td><td align='left'><h2>林玥星</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='08398' checked value='08398_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='08398' value='08398_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='08398'  value='08398_UN'>未点名</span></h2></td></tr><tr id='09292' class='tr_Roll' style='color:#d0cfcf' ><td><h2>15</h2></td><td><img style='width:80px;height:100px' src='/rollcall/images/student/09292.jpeg' /></td><td name='fullname' align='left' title='陈慧妍(ADA_CHEN)'><h2>陈慧妍(ADA_CHEN)</h2></td><td><h2>09292</td><td align='left'><h2>陈慧妍</h2></td><td align='center'><h2><span class='span_Link'><input type='radio' class='isAnswer' name='09292' checked value='09292_OK'>已到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='09292' value='09292_LA'>未到</span>&nbsp;&nbsp;<span class='span_Link'><input type='radio' class='isAnswer' name='09292'  value='09292_UN'>未点名...";
		buf.close();
		List<StanderStudent> data=new ArrayList<StanderStudent>();
		for(;;){
			String str = null;
			try{
				int to=line.indexOf("未点名</span></h2></td></tr>")+"未点名</span></h2></td></tr>".length();
				if ((to-"未点名</span></h2></td></tr>".length())==-1){
					break;
				}
				str=line.substring(0, to);
				line=line.substring(to);
				StanderStudent student=new StudentInfo();
				//picture determiner
				student.setID(str.substring(str.indexOf("id='")+"id='".length(),str.indexOf("'",str.indexOf("'")+1)));
				student.setImageURL(new URL("http://portal.kcisec.com"+str.substring(str.indexOf("/rollcall/images"),str.indexOf("' /></td>"))));
				str=str.substring(str.indexOf("fullname")-"<td name='".length());
				//full name
				student.setName(str.substring(str.indexOf("'left' title='")+"'left' title='".length(),str.indexOf("<h2>")-2));
				//Line
				if (str.indexOf("不存在")==-1){
					int pointer=str.indexOf("同時在[");
					if (pointer!=-1) student.setLineID(str.substring(str.indexOf("同時在[")+"同時在[".length(),str.indexOf("]線別也存在名")));
					else student.setLineID(lineID);
				};
				//is checked
				int checked=str.indexOf("checked");
				if (checked!=-1) {
					str=str.substring(str.indexOf(">",checked)+1,str.indexOf("</span>",checked));
					switch (str) {
					case "未点名":
						student.putProperty(onCarDefinitionKey, NOT_CHECKED);
						break;
					case "未到":
						student.putProperty(onCarDefinitionKey, Checked_NOT_ON_CAR);
						break;
					case "已到":
						student.putProperty(onCarDefinitionKey, Checked_ON_CAR);
						break;
					default:
						throw new ErrorResponse("can't define check state by using"+str);
					}
				}
				data.add(student);
			}catch (Exception e){
				if (str!=null){
					StringBuilder sb=new StringBuilder(70+str.length());
					sb.append("Exception while analyzing shouldbelist rollback: ");
					sb.append(str);
					throw new ErrorResponse(sb.toString(),e);
				}
				throw new ErrorResponse(e);
			}
		}
		return data;
	}
	
	public Boolean checkUserServiceability() throws IOException{
		HttpURLConnection hcon=(HttpURLConnection) new URL("http://portal.kcisec.com/rollcall/CarRollCall/Index").openConnection();
		cookieWriterNoCheck(hcon);
		if (hcon.getResponseCode()==302){
			checked=false;
			return false;
		}
		BufferedReader buf=new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8"));
		buf.skip(4229+24);
		String str=buf.readLine();
		if (str.indexOf("提示：帐号是学号，密码与点餐系统相同。")!=-1){
			checked=false;
			return false;
		}
		checked=true;
		return true;
	}
	
	public Map<String,String> getCookie(){
		return (Map<String, String>) cookie;
	}
	
	public HttpURLConnection cookieWriter(HttpURLConnection hcon){
		try{
			if (checked==false){
				System.out.println("TRY UPDATE COOKIE");
				tryUpdateCookie();
				checkUserServiceability();
			}
		}catch (IOException e){
			throw new UnuseableLoginException(this,e);
		}
		if (checked==false) throw new UnuseableLoginException(this);
		StringBuilder sb=new StringBuilder(50);
		for (String key:cookie.keySet()){
			sb.append(key);
			sb.append("=");
			sb.append(cookie.get(key));
			sb.append("; ");
		}
		hcon.addRequestProperty("Cookie", sb.toString());
		return hcon;
	}
	
	private HttpURLConnection cookieWriterNoCheck(HttpURLConnection hcon){
		StringBuilder sb=new StringBuilder(50);
		for (String key:cookie.keySet()){
			sb.append(key);
			sb.append("=");
			sb.append(cookie.get(key));
			sb.append("; ");
		}
		hcon.addRequestProperty("Cookie", sb.toString());
		return hcon;
	}
	
	public void tryUpdateCookie() throws IOException{
		try {
			loginedUser li=new loginedUser(ID);
			this.cookie=li.getCookie();
		} catch (IDFormatException e) {}
	}
	
	/*private void checkToUse(){
		if (checked==false){
			try{
				checkUserServiceability();
			}catch (IOException e){
				throw new UnuseableLoginException(ID,e);
			}
			if (!checked) throw new UnuseableLoginException(ID);
		}
	}*/
	
	public void setUseable(Boolean newUsable){
		this.checked=newUsable;
	}
	
	public Boolean getUseable(){
		return this.checked;
	}
	
	public String getID(){
		return this.ID;
	}
	
	public String getlineID(){
		return this.lineID;
	}
	
	public String getlineName(){
		return this.lineName;
	}
	
	public String getUserName(){
		return this.UserName;
	}
	
	public Boolean isNormallogin(){
		return this.Normallogin;
	}
	
	public Boolean isTheDay(){
		return this.isTheDay;
	}
	
	public String toString(){
		StringBuilder sb=new StringBuilder(60);
		sb.append("login in with:");
		sb.append(ID);
		sb.append("\t");
		sb.append("state:");
		if (checked) sb.append("Checked");
		else sb.append("Unchecked");
		sb.append(" and ");
		try {
			loginedUser.portalCheck();
			sb.append("Connected");
		} catch (IOException e) {
			sb.append("Disconnected");
		}
		return sb.toString();
	}

	public loginedUser clone(){
		loginedUser cloned;
		try {
			cloned = (loginedUser) super.clone();
			return cloned;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}
	
	public static Boolean StudentIDChecker(String ID){
		if (ID.length()!=5) return false;
		try{
			Integer.parseInt(ID);
		}catch (NumberFormatException e){
			return false;
		}
		SimpleDateFormat format=new SimpleDateFormat("yyyy");
		String year=format.format(new Date());
		year=year.substring(2,4);
		ID=ID.substring(0, 2);
		try{
			int v=Integer.parseInt(year)-Integer.parseInt(ID);
			if (v<5||v>20) return false;
		}catch (NumberFormatException e){
			return false;
		}
		return true;
	}

	public static int portalCheck() throws IOException{
		long time=System.currentTimeMillis();
		HttpURLConnection hcon=(HttpURLConnection) new URL(
				new StringBuilder(60).append(portalConnectionCheckURL).append("?_=").append(System.nanoTime())
				.toString()).openConnection();
		if (!new BufferedReader(new InputStreamReader(hcon.getInputStream(),"utf-8")).readLine().equals("正常")) 
			throw new ErrorResponse();
		return (int) (System.currentTimeMillis()-time);
	}
	
	public static int orderingCheck() throws IOException{
		long time=System.currentTimeMillis();
		HttpURLConnection hcon=(HttpURLConnection) new URL(orderingConnectionCheckURL).openConnection();
		hcon.connect();
		hcon.disconnect();
		return (int) (System.currentTimeMillis()-time);
	}

	public static int baiduCheck() throws IOException{
		long time=System.currentTimeMillis();
		HttpURLConnection hcon=(HttpURLConnection) new URL(baiduConnectionCheckURL).openConnection();
		hcon.connect();
		hcon.disconnect();
		return (int) (System.currentTimeMillis()-time);
	}

	public static int googleCheck() throws IOException{
		long time=System.currentTimeMillis();
		HttpURLConnection hcon=(HttpURLConnection) new URL(googleConnectionCheckURL).openConnection();
		hcon.connect();
		hcon.disconnect();
		return (int) (System.currentTimeMillis()-time);
	}
	
	public static Boolean doStudentExistsInList(String ID){
		if (!loginedUser.StudentIDChecker(ID)) return false;
		List<String> list=MapGenerater.getIDList();
		for (String onName:list){
			if (onName.equals(ID)) return true;
		}
		return false;
	}
}