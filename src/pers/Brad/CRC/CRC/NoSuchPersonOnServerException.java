package pers.Brad.CRC.CRC;

import pers.Brad.CRC.CRC.Exceptions.IDFormatException;

public class NoSuchPersonOnServerException extends PersonNotFoundException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8162358078468116419L;

	public NoSuchPersonOnServerException(StudentIdentify ID) {
		super(ID);
	}

	public NoSuchPersonOnServerException(StudentIdentify cardID, Throwable e) {
		super(cardID,e);
	}
	
	public NoSuchPersonOnServerException(String ID) throws IDFormatException {
		super(StudentIdentify.Build(ID));
	}
}
