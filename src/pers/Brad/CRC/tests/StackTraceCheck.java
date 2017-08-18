package pers.Brad.CRC.tests;

import pers.Brad.CRC.CRC.IDFormatException;
import pers.Brad.CRC.CRC.UnuseableLoginException;

public class StackTraceCheck {
	
	public static void main(String[] args) throws Throwable, Throwable, Throwable, Throwable {
		entry1();
		new UnuseableLoginException(new IDFormatException("23333"));
	}
	
	static void entry1() {
		entry2();
	}
	
	static void entry2() {
		entry3();
	}
	
	static void entry3() {
		entry4();
	}

	static void entry4() {
		System.out.println((Thread.currentThread().getStackTrace()[2]));
	}
	
}
