package pers.Brad.CRC.CRC.Exceptions;

public class ErrorResponse extends Error{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public ErrorResponse(){
		super();
	}
	
	public ErrorResponse(String msg){
		super(msg);
	}
	
	public ErrorResponse(Exception e){
		super(e);
	}
	
	public ErrorResponse(String msg,Exception e){
		super(msg,e);
	}
}
