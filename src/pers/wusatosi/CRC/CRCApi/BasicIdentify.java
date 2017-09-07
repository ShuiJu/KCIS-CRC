package pers.wusatosi.CRC.CRCApi;

public abstract class BasicIdentify implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9097893048621613661L;

	protected BasicIdentify(String Value) {
		this.Value=Value;
	}
	
	protected final String Value;
	
	public String getValue() {
		return Value;
	}

	public Boolean isStudentID() {
		return false;
	}

}