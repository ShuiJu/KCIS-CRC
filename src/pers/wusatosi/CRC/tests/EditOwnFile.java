package pers.wusatosi.CRC.tests;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EditOwnFile {
	
	public static void main(String[] args) throws URISyntaxException, IOException {
//		String JarPath = EditOwnFile.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().substring(1);
		Path JarPath = Paths.get("C:\\Users\\wusat\\Desktop\\TestBeta.jar");
		Path toPath = Paths.get("C:\\Users\\wusat\\Desktop\\Config.cfg");
		System.out.println(JarPath);
		try (FileSystem fs = FileSystems.newFileSystem(JarPath, null)){
			Path path = fs.getPath("/pers/wusatosi/CRC/Util/Config.cfg");
			System.out.println(path);
			Files.delete(path);
			Files.copy(toPath, path);
		}

		File thisJ = new File(EditOwnFile.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().substring(1));
		System.out.println(thisJ.getPath());
		thisJ.deleteOnExit();
	}
	
}
