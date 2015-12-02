package core.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import core.interfaces.IPlugin;
import core.type.NetworkMessage;
import data.Event;
import data.SourceFile;

public class LocalFileManager implements IPlugin {

	public LocalFileManager() {
	}

	@Override
	public void start() {
	}

	@Override
	public String id() {
		return "Cide.LocalFileManager";
	}

	@Override
	public void onNetworkMessage(NetworkMessage msg) {
		throw new NotImplementedException();
	}

	@Override
	public void onEvent(Event evt, Object data) {
		throw new NotImplementedException();
	}

	public List<SourceFile> getFilesFromDirectory(SourceFile directory) {

		if (directory.type != SourceFile.FileType.Folder)
			return null;

		List<SourceFile> sourceFileList = new ArrayList<SourceFile>();

		File[] children = directory.file.listFiles();

		for (File child : children) {
			SourceFile sourceFile = new SourceFile(child);
			sourceFileList.add(sourceFile);
		}
		return sourceFileList;
	}

	public SourceFile getTreeFromDirectory(String directory) {

		File rootFile = new File(directory);
		SourceFile root = new SourceFile(rootFile);
		getTreeFromDirectory(root);
		
		return root;
	}

	public SourceFile getTreeFromDirectory(SourceFile directory) {

		if (directory.type == SourceFile.FileType.Folder) {
			List<SourceFile> children = getFilesFromDirectory(directory);
			directory.children = children;
			for (SourceFile child : children) {
				getTreeFromDirectory(child);
			}
		}
		return directory;
	}

	public boolean openFile(SourceFile file) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean saveFile(SourceFile file) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
