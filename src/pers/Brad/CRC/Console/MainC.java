package pers.Brad.CRC.Console;

import java.io.IOException;

import pers.Brad.CRC.InternetAcessPart.*;
import pers.Brad.CRC.InternetAcessPart.RollCallUtil.NotTheDayException;
import pers.Brad.CRC.InternetAcessPart.Exceptions.ErrorResponse;
import pers.Brad.CRC.InternetAcessPart.Exceptions.IDFormatException;
import pers.Brad.CRC.InternetAcessPart.Exceptions.UnuseableLoginException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainC {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("		....Init....");
		System.out.print("Init Map Generater		......");
		MapGenerater.getHashedNameToIDMap();
		System.out.println("	OK");
		System.out.println("Checking network		......");
		try {
			System.out.println("\tTo portal.kcisec.com:		"+ConnectionCheck.portalCheck()+"ms");
			System.out.println("\tTo ordering.kcisec.com: 	"+ConnectionCheck.orderingCheck()+"ms");
			System.out.println("					OK");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR!");
			e.printStackTrace();
		}
		loginedUser lu = null;
		try {
			lu=new loginedUser("08426");
		} catch (IDFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(lu==null){
			System.out.print("Please enter your ID:\t\t");
			String ID=new BufferedReader(new InputStreamReader(System.in)).readLine();
			System.out.print("Please enter your passwd:\t");
			char[] passwd=new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray();
			try{
				lu=new loginedUser(ID,passwd,(msg)->{
					System.out.println(msg);
				});
			}catch (Exception e){
				System.out.print("login state failed..:  ");
				System.out.println(e);
				System.out.println();
				System.out.println("--------------");
			}
		}
		System.out.println("------------------------------------");
		System.out.println("User Infomation:");
		System.out.println("ID				....."+lu.ID);
		System.out.println("Name				....."+lu.UserName);
		System.out.println("line ID				....."+lu.lineID);
		System.out.println("------------------------------------");
		System.out.println("loading roll call tool kit	.....");
		RollCallUtil rcu = null;
		try {
			rcu=new RollCallUtil(lu);
		} catch (ErrorResponse|UnuseableLoginException e) {
			System.err.println("Server error");
			System.out.println("Clict Enter to exit");
			new BufferedReader(new InputStreamReader(System.in)).readLine();
			System.exit(20);
		}
		System.out.println("				      Success");
		System.out.println("/////////welcome////////////");
		while (true){
			System.out.println();
			System.out.println();
			System.out.println("Input(0) to roll call, input(1) to record car time, input(2) to get car times, input(3) to get student lists");
			try{
				int in=Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
				switch (in){
				case 0:
					File cache=new File(System.getProperty("user.home")+"RCRCache");
					cache.exists();
					System.out.println("Just input Card ID or student ID");
					System.out.println("Input \"R\" to reinput the prevoid one,\"S\" to see the how many people roll called,\"F\"to finish the process"
							+ " and upload info+\"C\"");
					while (true){
						String str=new BufferedReader(new InputStreamReader(System.in)).readLine();
						if (str.toLowerCase().equals("r")){
							rcu.remove(rcu.size());
							continue;
						}
						if (str.toLowerCase().equals("s")){
							System.out.println(rcu.size()+" has been rollcalled");
							continue;
						}
						if (str.toLowerCase().equals("c")){
							break;
						}
						if (str.toLowerCase().equals("f")){
							try{
								try {
									rcu.send();
								} catch (ErrorResponse e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (NotTheDayException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}catch (IOException e){
								System.err.println("Exception while uploading info");
								e.printStackTrace();
							}
						}
					}
					break;
				case 1:{
					System.out.println("This is for recording car times, enter true or false to record start time or end time, enter \"0\" to quit");
					String lol=new BufferedReader(new InputStreamReader(System.in)).readLine();
					if (lol.equals("0")) break;
					try{
						Boolean b=new Boolean(lol);
						lu.recordCarStopCall(b);
						System.out.println("successed! uploaded as	"+(b?lu.getStopCalls()[0]:lu.getStopCalls()[1]));
						break;
					}catch (Exception e){
						if (e.getClass().equals(IOException.class)) e.printStackTrace();
						break;
					}
				}
				case 2:
					String[] stopC=lu.getStopCalls();
					System.out.println(new StringBuilder(50).append("ST:").append(stopC[0]).append("\tET:").append(stopC[1]));
					break;
				case 3:
					ArrayList<StanderStudent> studentL=(ArrayList<StanderStudent>) rcu.getShouldBeStudentInfoList();
					int LS=studentL.size();
					for (int i=0;i<LS;i++){
						StanderStudent stu=studentL.get(i);
						StringBuilder sb=new StringBuilder(100).append(stu.getID()).append(",").append(stu.getName());
						if (!(sb.length()>23)){
							sb.append("\t");
						}
						sb.append("\t").append(stu.getLineID());
						System.out.println(sb.toString());
					}
					break;
				}
			}catch (NumberFormatException e){}
		}
	}

}
