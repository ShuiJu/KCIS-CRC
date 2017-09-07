package pers.wusatosi.CRC.tests;

import java.io.IOException;

import pers.wusatosi.CRC.CRCApi.IDFormatException;
import pers.wusatosi.CRC.CRCApi.StudentIdentify.StudentID;
import pers.wusatosi.CRC.CRCApi.loginedUser;
import pers.wusatosi.CRC.Util.CachedSession;

public class CachedSessionTest {
	
	public static void main(String[] args) throws IOException, IDFormatException {
//		CachedSession.getInstance().write(new loginedUser(StudentID.Build("08426")),4);
		System.out.println(CachedSession.doHaveInfomation());
		System.out.println(CachedSession.getInstance().getID());
	}
	
}
