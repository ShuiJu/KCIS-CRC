package pers.Brad.CRC.InternetAcessPart;

public class NoSuchPersonOnServerException extends PersonNotFoundException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8162358078468116419L;

	public NoSuchPersonOnServerException(String ID) {
		super(ID);
	}
	
	public NoSuchPersonOnServerException(String ID,Throwable e){
		super(ID,e);
	}

	public NoSuchPersonOnServerException(StudentIdentify cardID, Throwable e) {
		this(cardID.getValue(),e);
	}
}
