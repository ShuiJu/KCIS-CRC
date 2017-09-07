package pers.wusatosi.CRC.tests;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class SS {
	
	public static void main(String[] args) throws Throwable {
		Path  path = Paths.get("C:\\Users\\wusat\\Desktop\\SS_ubi.txt");
		List<String> lines = Files.readAllLines(path);
		lines.stream()
			.filter((a) ->  a.indexOf("connect to") != -1)
			.map((str) -> str.substring(str.indexOf("connect to") + "connect to ".length()))
			.map(SS::reducer)
			.distinct()
			.filter((a) -> a.indexOf("qq") == -1)
			.filter((a) -> a.indexOf("adobe") == -1)
			.filter((a) -> a.indexOf("eclipse") == -1)
			.filter((a) -> a.indexOf("google") == -1)
			.filter((a) -> a.indexOf("live") == -1)
			.filter((a) -> a.indexOf("grammarly") == -1)
			.filter((a) -> a.indexOf("cloudfront") == -1)
			.filter((a) -> a.indexOf("baidu") == -1)
			.filter((a) -> a.indexOf("bdstatic") == -1)
			.filter((a) -> a.indexOf("sogou") == -1)
			.filter((a) -> a.indexOf("oracle") == -1)
			.filter((a) -> a.indexOf("momentumdash") == -1)
			.filter((a) -> a.indexOf("akamaized") == -1 || a.indexOf("ubi") ==-1 || a.indexOf("ubisoft") == -1)
			.filter((a) -> a.indexOf("microsoft") == -1)
			.sorted()
			.forEach(System.out::println);
//			.forEach((a) -> {});
			;
	}
	
	private static String reducer(String str) {
		try{
//			System.out.println(str.replace(".", "").replace(":", ""));
			Long.parseLong(str.replace(".", "").replace(":", ""));
			return str.substring(0, str.indexOf(":"));
		}catch (NumberFormatException e) {}
		int l1 = str.toLowerCase().lastIndexOf(".com");
		if (l1 == -1) l1 = str.toLowerCase().indexOf(".net");
		if (l1 == -1) l1 = str.toLowerCase().indexOf(".org");
		if (l1 == -1) return str;
		int l2 = str.lastIndexOf(".", l1-1);
		if (l2 != -1) return "*"+str.substring(l2);
		return str.substring(0, str.indexOf(":"));
	}
	
}
