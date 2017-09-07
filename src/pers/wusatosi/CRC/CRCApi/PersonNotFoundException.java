package pers.wusatosi.CRC.CRCApi;

public class PersonNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final StudentIdentify ID;
	
	public StudentIdentify getID(){
		return ID;
	}
	
	public PersonNotFoundException(StudentIdentify ID){
		super(ID!=null?"ID:"+ID.getValue()+" doesn't exists in server":"ID info unavailable");
		this.ID=ID;
	}
	
	public PersonNotFoundException(StudentIdentify ID,String msg){
		super(ID!=null?(msg.indexOf(ID.getValue())==-1?"ID:"+ID.getValue()+" "+msg:msg):msg);
		this.ID=ID;
	}
	
	public PersonNotFoundException(StudentIdentify ID,Throwable e){
		super("ID:"+ID.getValue()+" doesn't exists in server",e);
		this.ID=ID;
	}
}
