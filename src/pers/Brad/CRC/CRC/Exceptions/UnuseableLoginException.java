package pers.Brad.CRC.CRC.Exceptions;

import java.io.IOException;

import pers.Brad.CRC.CRC.loginedUser;
import pers.Brad.CRC.CRC.StudentIdentify.StudentID;

/**
 * When server don't accept the user anymore, which means every request they sent will give the page of relogin
 * @author wusatosi/Brad.Wu
 *
 */
public class UnuseableLoginException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final StudentID ID;
	public String msg;
	
	public UnuseableLoginException(loginedUser ID,IOException e){
		super("ID:"+ID.ID.getValue(),e);
		this.ID=ID.ID;
		ID.setUseable();
	}
	
	public UnuseableLoginException(loginedUser ID,String msg){
		super("ID:"+ID.ID.getValue()+"\t"+msg);
		this.ID=ID.ID;
		this.msg=msg;
		ID.setUseable();
	}
	
	public UnuseableLoginException(loginedUser ID){
		super("ID:"+ID.ID.getValue()+" "+"is NOT able to login");
		this.ID=ID.ID;
		ID.setUseable();
	}
	
	public UnuseableLoginException(StudentID ID) {
		super("ID:"+ID.getValue()+" "+"is NOT able to login");
		this.ID=ID;
	}
	
	public UnuseableLoginException(IDFormatException e) {
		super(e);
		this.ID=null;
	}
}
