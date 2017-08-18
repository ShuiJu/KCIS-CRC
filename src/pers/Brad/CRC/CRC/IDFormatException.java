package pers.Brad.CRC.CRC;

public class IDFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final BasicIdentify ERRID;
	
	public IDFormatException(BasicIdentify ERRID){
		super("ID: "+ERRID.getValue()+" is in the wrong format");
		this.ERRID=ERRID;
	}
	
	public IDFormatException(BasicIdentify ID,Exception e){
		super (ID.getValue()+" is in wrong format",e);
		this.ERRID=ID;
	}
	
	public IDFormatException(BasicIdentify ERRID,String msg){
		super(msg);
		this.ERRID=ERRID;
	}
	
	public IDFormatException(String ERRID){
		super("ID: "+ERRID+" is in the wrong format");
		this.ERRID=new ErrorIdentify(ERRID);
	}
	
	public IDFormatException(String ID,Exception e){
		super (ID+" is in wrong format",e);
		this.ERRID=new ErrorIdentify(ID);
	}
	
	public IDFormatException(String ERRID,String msg){
		super(msg);
		this.ERRID=new ErrorIdentify(ERRID);
	}
}
