// XXX: Remove this shit (once we use the WebSockets completely)
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import core.FileTraversal;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import play.mvc.Controller;

/**
 *
 * @author Gabriel
 */
public class BrowserWidget extends Controller {

	public static void index() {

		try {

			String rootPath = params.get("dir");
			if (rootPath == null) {
				rootPath = "/Users/Gabriel/Dropbox";
			}

			final List<File> files = new ArrayList<File>();
			final List<File> directories = new ArrayList<File>();

			new FileTraversal() {

				@Override
				public void onFile(final File f) {

					if (f.getName().charAt(0) != '.') {
						files.add(f);
					}
				}

				@Override
				public void onDirectory(final File d) {

					if (d.getName().charAt(0) != '.') {
						directories.add(d);
					}
				}
			}.traverse(new File(rootPath));

			renderTemplate("/widgets/browser/logic.html", rootPath, files, directories);

		} catch (IOException ex) {

			ex.printStackTrace();
		}
	}
}