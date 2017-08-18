package pers.Brad.CRC.CRC;

import java.io.IOException;
import java.util.Objects;

public class StudentIdentify extends BasicIdentify{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 769641239057630564L;
	
	protected final static IdentifyBufferMap buffer=IdentifyBufferMap.getInstance();
	
	public static StudentIdentify Build(String IDOrCardID) throws IDFormatException{
		Objects.requireNonNull(IDOrCardID);
		if (!loginedUser.StudentIDChecker(IDOrCardID)) throw new IDFormatException(IDOrCardID);
		Boolean isStudentID=(IDOrCardID.length()==5);
		StudentIdentify n;
		if ((n=buffer.get(IDOrCardID))!=null&&n.isStudentID().equals(isStudentID)) return n;
		if (isStudentID) return new StudentID(IDOrCardID);
		return new StudentIdentify(IDOrCardID);
	}
	
	public static StudentIdentify Build(BasicIdentify iden) throws IDFormatException {
		return Build(iden.getValue());
	}
	
	private StudentIdentify(String IDOrCardID){
		super(IDOrCardID);
		if (!isStudentID()) buffer.put(IDOrCardID, this);
	}

	private StudentID linkedStudentID=null;
	
	public StudentID getStudentID() throws NoSuchPersonOnServerException, NoSuchPersonInDataBaseException, IOException, ErrorResponse{
		if (linkedStudentID!=null) return linkedStudentID; 
		StudentID ID= StudentID.Build(this);
		linkedStudentID=ID;
		return ID;
	}
	
	@Override
	public String toString() {
		try {
			return isStudentID()?(linkedStudentID==null?linkedStudentID.getValue():getStudentID().getValue()):Value;
		} catch (NoSuchPersonOnServerException | NoSuchPersonInDataBaseException | IOException | ErrorResponse e) {
			throw new InternalError(e);
		}
	}

	@Override
	public int hashCode() {
		if (linkedStudentID!=null) return linkedStudentID.hashCode();
		final int prime = 31;
		int result = 1;
		result = prime * result + Value.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StudentIdentify other = (StudentIdentify) obj;
		if (!Value.equals(other.Value))
			return false;
		return true;
	}

	public static class StudentID extends StudentIdentify implements java.io.Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1204256651565854624L;
		
		private StudentID(String ID){
			super(ID);
			buffer.put(ID, this);
		}
		
		public static StudentID Build(String ID) throws IDFormatException{
			StudentIdentify stu=StudentIdentify.Build(ID);
			if (stu.isStudentID()) return (StudentID) stu;
			throw new IDFormatException("Require pure ID");
		}
		
		public static StudentID Build(StudentIdentify stu) throws NoSuchPersonOnServerException, NoSuchPersonInDataBaseException, IOException, ErrorResponse{
			Objects.requireNonNull(stu);
			if (stu.isStudentID()) return (StudentID) stu;
			if (stu.linkedStudentID!=null) return stu.linkedStudentID;
			StudentIdentify holder;
			if ((holder=buffer.get(stu.getValue()))!=null) return (StudentID) holder;
			return RollCallUtil.cardIDToID(stu);
		}
		
		@Override
		public String toString() {
			return getValue();
		}
		
		@Override
		public final StudentID getStudentID() {
			return this;
		}
		
		@Override
		public boolean equals(Object compare) {
			if (this==compare) return true;
			if (compare==null) return false;
			if (!(compare instanceof StudentIdentify)) return false;
			if (!((StudentIdentify)compare).isStudentID()) return false;
			return ((StudentID)compare).getValue()==this.getValue();
		}
	
		@Override
		public final Boolean isStudentID() {
			return true;
		}
	}

}
