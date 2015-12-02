/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Gabriel
 */
public class FileTraversal {

	public final void traverse(final File f) throws IOException {

		final File[] childs = f.listFiles();
		for (File child : childs) {
			//traverse(child);
			if (child.isDirectory()) {
				onDirectory(child);
			} else {
				onFile(child);
			}
			//return;
		}
	}

	public void onDirectory(final File d) {

		System.out.println(d);
	}

	public void onFile(final File f) {

		System.out.println(f);
	}
}
