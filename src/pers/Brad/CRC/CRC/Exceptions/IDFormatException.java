package pers.Brad.CRC.CRC.Exceptions;

import pers.Brad.CRC.CRC.StudentIdentify;

public class IDFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final String ERRID;
	
	public IDFormatException(StudentIdentify ERRID){
		super("ID: "+ERRID.getValue()+" is in the wrong format");
		this.ERRID=ERRID.getValue();
	}
	
	public IDFormatException(StudentIdentify ID,Exception e){
		super (ID.getValue()+" is in wrong format",e);
		this.ERRID=ID.getValue();
	}
	
	public IDFormatException(StudentIdentify ERRID,String msg){
		super(msg);
		this.ERRID=ERRID.getValue();
	}
	
	public IDFormatException(String ERRID){
		super("ID: "+ERRID+" is in the wrong format");
		this.ERRID=ERRID;
	}
	
	public IDFormatException(String ID,Exception e){
		super (ID+" is in wrong format",e);
		this.ERRID=ID;
	}
	
	public IDFormatException(String ERRID,String msg){
		super(msg);
		this.ERRID=ERRID;
	}
}
