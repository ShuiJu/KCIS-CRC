package pers.Brad.CRC.InternetAcessPart;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A basic class store the information of the student, it can process as a single student.
 * @author wusatosi/Brad.Wu
 *
 */
public class StudentInfo implements StanderStudent{
	
	/**
	 * creat a deafult student, all infomation is null
	 */
	public StudentInfo(){
		fillBlank();
	}
	
	/**
	 * creat a student with ID filled
	 * @param ID - Student ID, not a card ID
	 */
	public StudentInfo(String ID) throws NumberFormatException{
		setID(ID);
		fillBlank();
	}
	
	/**
	 * Creat a student with ID and name filled
	 * 
	 * @param ID - Student, not a Card ID
	 * @param name - Student's name
	 */
	public StudentInfo(String ID,String name){
		setID(ID);
		this.name=name;
		fillBlank();
	}
	
	/**
	 * Create a student with ID, name, ImageURL filled
	 * 
	 * @param ID
	 * @param name
	 * @param ImageURL
	 */
	public StudentInfo(String ID,String name,URL ImageURL){
		setID(ID);
		this.name=name;
		this.ImageURL=ImageURL;
		fillBlank();
	}
	
	/**
	 * Create a student with Advanced Infomation, this system will automaticly analyze some infomation out
	 * 
	 * @param info - Infomation  Map
	 */
	public StudentInfo(Map<String,String> info){
		MapInputReader(info);
	}
	
	private void MapInputReader(final Map<String,String> info){
		String str;
		if ((str=info.get("AccountID"))!=null){
			this.ID=str;
		}
		if ((str=info.get("fullname"))!=null){
			this.name=str;
		}
		if ((str=info.get("PIC"))!=null){
			PICReader(str);
		}else{
			String g=info.get("gender");
			if (g!=null){
				StringBuilder sb=new StringBuilder(60);
				sb.append("http://portal.kcisec.com");
				try{
					if (g.equals("F")) setImageURL(new URL(sb.append("/rollcall/images/female.gif").toString()));
					else setImageURL(new URL(sb.append("/rollcall/images/male.gif").toString()));
				}catch (MalformedURLException e){
				}
			}
		}
		if ((str=info.get("remark"))!=null){
			int pointer=str.indexOf("同時在[");
			if (pointer!=-1){
				try{
					this.LineID=str.substring(pointer+4, str.indexOf("]"));
				}catch (Exception e){}
			}
		}
		fillBlank();
	}
	
	private void PICReader(final String in){
		if (in==null) return;
		int bp=in.indexOf("/rollcall");
		if (bp==-1) return;
		int ep=in.indexOf(".gif",bp)+4;
		if ((ep-4)==-1){
			ep=in.indexOf(".jpeg",bp)+5;
			if ((ep-5)==-1) return;
		}
		final StringBuilder sb=new StringBuilder(60);
		sb.append("http://portal.kcisec.com");
		try{
			sb.append(in.substring(bp, ep));
		}catch(Exception e){
			return;
		}
		try {
			this.ImageURL=new URL(sb.toString());
		} catch (MalformedURLException e) {
			return;
		}
	}
	
	private void fillBlank(){
		if (ID==null)
			ID=NA;
		if (name==null)
			name=NA;
		if (LineID==null)
			LineID=NA;
	}
	
	private String ID;
	private String name;
	private URL ImageURL;
	private String LineID;

	/**
	 * To set this ID of student to another one
	 * 
	 * @param ID - the new ID, Student ID only
	 * @throws NumberFormatException while the ID in come are having some problem
	 */
	@Override
	public void setID(final String ID){
		this.ID=ID;
	}
	
	@Override
	public String getID() {
		return ID;
	}

	@Override
	public void setName(final String name) {
		// TODO Auto-generated method stub
		this.name=name;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}
	
	@Override
	public void setLineID(final String LineID) {
		// TODO Auto-generated method stub
		this.LineID=LineID;
	}

	@Override
	public String getLineID() {
		// TODO Auto-generated method stub
		return this.LineID;
	}
	
	@Override
	public void setImageURL(final URL ImageURL) {
		// TODO Auto-generated method stub
		this.ImageURL=ImageURL;
	}

	@Override
	public URL getImageURL() {
		// TODO Auto-generated method stub
		return this.ImageURL;
	}
	
	private HashMap<String,String> properties=new HashMap<String,String>();

	public void setAdvanceProperties(Map<String, String> Properties) {
		// TODO Auto-generated method stub
		this.properties=(HashMap<String, String>) Properties;
	}

	public Map<String, String> getAdvanceProperties() {
		// TODO Auto-generated method stub
		return this.properties;
	}

	public String getProperty(String arg0) {
		// TODO Auto-generated method stub
		return this.properties.get(arg0);
	}

	public void putProperty(String arg0, String arg1) {
		// TODO Auto-generated method stub
		if ((arg0.equals("AccountID"))){
			this.ID=arg0;
			this.properties.put(arg0, arg1);
			return;
		}
		if ((arg0.equals("fullname"))){
			this.name=arg0;
			this.properties.put(arg0, arg1);
			return;
		}
		if ((arg0.equals("PIC"))){
			PICReader(arg0);
			this.properties.put(arg0, arg1);
			return;
		}
		this.properties.put(arg0, arg1);
	}

	public void putAll(Map<String, String> arg) {
		// TODO Auto-generated method stub
		MapInputReader(arg);
		this.properties.putAll(arg);
	}
	
	@Override
	public StudentInfo clone(){
		StudentInfo cloned = null;
		try {
			cloned = (StudentInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			// impossible
			e.printStackTrace();
		}
		return cloned;
	}
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder(10);
		sb.append(this.ID==null?"":"ID:"+this.ID+":");
		sb.append(this.name==null?"":"Name:"+this.name);
		if (sb.length()==0) return "NoIDOrNameInfoStudent";
		return sb.toString();
	}
	
}
