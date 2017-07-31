package pers.Brad.CRC.InternetAcessPart.Exceptions;

public class IDFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final String ERRID;
	
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