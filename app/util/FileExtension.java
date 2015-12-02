package util;

import java.io.File;

public class FileExtension extends play.templates.JavaExtensions {

	public static String getExt(File file) {

		int dotPos = file.getName().lastIndexOf(".");

		if (dotPos == -1) {
			return "";
		}

		return file.getName().substring(dotPos + 1);
	}
}
