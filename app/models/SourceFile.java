package models;

public class SourceFile {
	
	public enum FileType {
		CSource, CPPSource, HTMLSource, JavaSource, 
		Makefile, 
		PDFFile,
		Folder,
		Other, 
	}
	public String name;
	public FileType type;
}
