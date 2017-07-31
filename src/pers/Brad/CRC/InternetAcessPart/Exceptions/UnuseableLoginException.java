package pers.Brad.CRC.InternetAcessPart.Exceptions;

import java.io.IOException;

import pers.Brad.CRC.InternetAcessPart.loginedUser;

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

	public final loginedUser ID;
	public String msg;
	
	public UnuseableLoginException(loginedUser ID,IOException e){
		super("ID:"+ID.ID,e);
		this.ID=ID;
		ID.setUseable(false);
	}
	
	public UnuseableLoginException(loginedUser ID,String msg){
		super("ID:"+ID.ID+"\t"+msg);
		this.ID=ID;
		this.msg=msg;
		ID.setUseable(false);
	}
	
	public UnuseableLoginException(loginedUser ID){
		super("ID:"+ID.ID+" "+"is NOT able to login");
		this.ID=ID;
		ID.setUseable(false);
	}
}
