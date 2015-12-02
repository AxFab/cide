package core.metaplugins;

import core.interfaces.IPlugin;
import java.util.List;
import data.SourceFile;

public abstract class IFileManager implements IPlugin {

	public abstract List<SourceFile> getFilesFromDirectory(SourceFile directory);

	public abstract SourceFile getTreeFromDirectory(String directory);

	public abstract SourceFile getTreeFromDirectory(SourceFile directory);

	public abstract boolean openFile(SourceFile file);

	public abstract boolean saveFile(SourceFile file);
}
