package pers.wusatosi.CRC.CRCApi;

import java.io.IOException;

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

	public final BasicIdentify ID;
	
	public UnuseableLoginException(loginedUser ID,IOException e){
		super("ID:"+ID.ID.getValue(),e);
		this.ID=ID.ID;
		ID.setUnUseable();
	}
	
	public UnuseableLoginException(loginedUser ID,String msg){
		super("ID:"+ID.ID.getValue()+"\t"+msg);
		this.ID=ID.ID;
		ID.setUnUseable();
	}
	
	public UnuseableLoginException(loginedUser ID){
		super("ID:"+ID.ID.getValue()+" "+"is NOT able to login");
		this.ID=ID.ID;
		ID.setUnUseable();
	}
	
	public UnuseableLoginException(BasicIdentify ID) {
		super("ID:"+ID.getValue()+" "+"is NOT able to login");
		this.ID=ID;
	}
	
	public UnuseableLoginException(IDFormatException e) {
		super(e);
		this.ID=e.ERRID;
	}
	
	public UnuseableLoginException(String ID) {
		this(new ErrorIdentify(ID));
	}
	
	public static class RefusedLoginException extends UnuseableLoginException{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5986261891428002659L;

		public RefusedLoginException(loginedUser ID, String ReturnMsg) {
			this(ID,ErrorType.getErrorTypeByMSG(ReturnMsg));
		}
		
		RefusedLoginException(loginedUser ID, ErrorType et){
			super(ID);
			type = et;
		}
		
		private final ErrorType type;
		
		public ErrorType getErrorType() {
			return type;
		}
		
		@Override
		public String toString() {
			return super.toString()+" because of "+type.toString();
		}
		
		public static enum ErrorType{
			UnverifiedUser("還未進行程式授權"),
			WrongUserNameOrPassword("密碼錯誤!"),
			Undefined("");
			
			private ErrorType(String arg) {
				msg = arg;
			}
			
			private String msg;
			
			private ErrorType setMSG(String msg) {
				this.msg = msg;
				return this;
			}
			
			public String getMsg() {
				return msg;
			}
			
			public static ErrorType getErrorTypeByMSG(String arg) {
				switch (arg) {
					case "還未進行程式授權":
						return UnverifiedUser;
					case "密碼錯誤!":
						return WrongUserNameOrPassword;
					default:
						return Undefined.setMSG(arg);
				}
			}
			
		}
		
		
		
	}
	
}
