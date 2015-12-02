package core.module.type;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import models.Project;


public class CideProcess {

	public Process p;
	public OutputStream stdin;
	public InputStream stdout;
	public InputStream stderr;
	
	private CideProcess() {}
	
	public static CideProcess build(List<String> commands, Project project) throws IOException {
		
		ProcessBuilder builder = new ProcessBuilder(commands);
		builder.directory(project.root);

		CideProcess process = new CideProcess();
		builder.environment().put("PS1", "\\s-\\v\\$ ");
		builder.environment().put("TERM", "xterm-256color");
		process.p = builder.start();
		process.stdin = process.p.getOutputStream();
		
		process.stdout = process.p.getInputStream();
		process.stderr = process.p.getErrorStream();
		
		return process;
	}
	
	public static CideProcess build(String command, Project project) throws IOException {
		
		List<String> commands = new ArrayList<>();
		commands.add(command);
		return build(commands, project);
	}
}
