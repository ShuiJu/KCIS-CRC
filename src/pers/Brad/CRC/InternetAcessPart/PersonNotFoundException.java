package pers.Brad.CRC.InternetAcessPart;

public class PersonNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String ID;
	
	public PersonNotFoundException(String ID){
		super(ID!=null?"ID:"+ID+" doesn't exists in server":"ID info unavailable");
		this.ID=ID;
	}
	
	public PersonNotFoundException(String ID,String msg){
		super(ID!=null?(msg.indexOf(ID)==-1?"ID:"+ID+" "+msg:msg):msg);
		this.ID=ID;
	}
	
	public PersonNotFoundException(String ID,Throwable e){
		super("ID:"+ID+" doesn't exists in server",e);
		this.ID=ID;
	}
	
	public String getID(){
		return ID;
	}
}
