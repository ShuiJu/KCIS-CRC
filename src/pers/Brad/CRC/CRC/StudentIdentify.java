package pers.Brad.CRC.CRC;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import pers.Brad.CRC.CRC.Exceptions.ErrorResponse;
import pers.Brad.CRC.CRC.Exceptions.IDFormatException;

public class StudentIdentify implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 769641239057630564L;
	
	protected final static List<StudentIdentify> buffer=new LinkedList<StudentIdentify>();
	
	public static StudentIdentify Build(String IDOrCardID) throws IDFormatException{
		Objects.requireNonNull(IDOrCardID);
		if (!loginedUser.StudentIDChecker(IDOrCardID)) throw new IDFormatException(IDOrCardID);
		Boolean isStudentID=(IDOrCardID.length()==5);
		for (StudentIdentify n:buffer)
			if (n.isStudentID()==isStudentID&&n.getValue().equals(IDOrCardID))
					return n;
		if (isStudentID) return new StudentID(IDOrCardID);
		return new StudentIdentify(IDOrCardID);
	}
	
	private StudentIdentify(String IDOrCardID){
		Value=IDOrCardID;
		if (!isStudentID()) buffer.add(this);
	}
	
	protected final String Value;
	
	private StudentID linkedStudentID=null;
	
	public String getValue(){
		return Value;
	}
	
	public Boolean isStudentID() {
		return false;
	}

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
			buffer.add(this);
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
			for (StudentIdentify holder:buffer){
				if (!(holder instanceof StudentID))
					continue;
				if (holder.getValue().equals(stu.getValue())) {
					stu.linkedStudentID=(StudentID) holder;
					return (StudentID) holder;
				}
			}
			return RollCallUtil.cardIDToID(stu);
		}
		
		@Override
		public String toString() {
			return getValue();
		}
		
		@Override
		public StudentID getStudentID() {
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
		public Boolean isStudentID() {
			return true;
		}
	}

}
