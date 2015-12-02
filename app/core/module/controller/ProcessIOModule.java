package core.module.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.BuildEngine;

import org.apache.commons.io.IOUtils;

import play.Logger;
import play.mvc.Scope.Session;
import core.common.controller.ModuleManager;
import core.common.type.Event;
import core.common.type.Module;
import core.module.type.CideProcess;
import core.net.controller.ConnectionManager;
import core.net.type.core.NetworkMessage;
import core.net.type.core.NetworkMessageBody;
import core.net.type.core.NetworkMessageHeader;

public class ProcessIOModule extends Module {

	Map<String, CideProcess> processes;

	/**
	 * Backend for the Execution widget
	 * 
	 * @param projectPath
	 *            rootPath of the project
	 * @throws IOException
	 *             Unable to launch the executable
	 */
	public ProcessIOModule() throws IOException {

		this.processes = new HashMap<>();
	}

	@Override
	public void onRegister(ModuleManager mm) {

		this.mm = mm;
	}

	@Override
	public void onUnregister() {
	}

	@Override
	public void onEvent(Event evt) {
		Logger.error("Unsupported event received %s", evt.message);
	}

	@Override
	public void onNetworkMessage(NetworkMessage msg) {

		// Invert source and destination
		NetworkMessage answer = msg.createAnswer();

		String username = Session.current().get("user");

		try {
			
			List<String> commands = new ArrayList<>();

			switch (msg.body.message) {

			case "bash":

				// Spawn a new instance of Bash
				commands.add("bash");
				answer = startProcess(msg, commands);

				break;

			case "stdin":

				answer.header.destination = "consoleStdout";
				answer.body.message = "consoleResume";
				
				CideProcess process = processes.get(username);
				IOUtils.write(msg.body.getObject(String.class) + "\n",
						process.stdin);
				process.stdin.flush();

				break;
				
			case "build":
				
				// Launch a build, clean or execute command
				commands.add("sh");
				commands.add(".cide/build.sh");
				commands.add(msg.body.getObject(String.class));
				answer = build(msg, commands);
				
				break;
				
			default:
				Logger.error("Unsupported NetworkMessage received %s",
						msg.body.message);
			}

		} catch (IOException e) {

			answer.body.object = msg.body.object + ": command not found.";
		}

		// If the message is set, we can send the NetworkMessage
		if (!answer.body.message.isEmpty()) {
			ConnectionManager.sendMessage(answer);
		}
	}
	
	/**
	 * Handles a new process
	 * @param msg
	 * @return
	 * @throws IOException 
	 */
	private NetworkMessage startProcess(NetworkMessage msg, List<String> commands) throws IOException {
		
		String username = msg.header.username;
		final NetworkMessage answer = msg.createAnswer();
		
		answer.header.destination = "consoleStdout";
		answer.body.message = "consoleStdout";
		answer.body.object = "";
		
		// Check if a process already exists for this user
		CideProcess existingProcess = processes.get(username);
		if(existingProcess != null) {
			existingProcess.p.destroy();
			/*
			existingProcess.stderr.close();
			existingProcess.stdin.close();
			existingProcess.stdout.close();
			*/
			processes.remove(existingProcess);
		}
		
		// Creating an execution environment per-user
		final CideProcess cideProcess = CideProcess.build(commands,
				mm.project);
		processes.put(username, cideProcess);

		final BufferedReader stderrReader = new BufferedReader(
				new InputStreamReader(cideProcess.stderr));
		final BufferedReader stdoutReader = new BufferedReader(
				new InputStreamReader(cideProcess.stdout));

		// We need a thread to read Stdout of Bash
		Thread stdoutThread = new Thread() {

			public void run() {

				try {
					boolean finished = false;
					do {
						String line = stdoutReader.readLine();

						// Creating a new message
						NetworkMessageHeader header = new NetworkMessageHeader(
								answer.header.username,
								answer.header.destination,
								answer.header.source);
						NetworkMessageBody body = new NetworkMessageBody(
								"consoleStdout", line);
						NetworkMessage stdoutMsg = new NetworkMessage(
								header, body);

						// stdoutMsg.body.object = line;
						// answer.body.object = line;
						ConnectionManager.sendMessage(stdoutMsg);

						if (line == null) {
							finished = true;
							break;
						}

					} while (!finished);
				} catch (IOException ex) {
					Logger.error(ex,
							"[ProcessIOModule] Error while piping 'stderr'");
				}
			}
		};

		stdoutThread.start();

		Thread stderrThread = new Thread() {

			public void run() {

				try {
					boolean finished = false;
					do {
						String line = stderrReader.readLine();

						// Creating a new message
						NetworkMessageHeader header = new NetworkMessageHeader(
								answer.header.username,
								answer.header.destination,
								answer.header.source);
						NetworkMessageBody body = new NetworkMessageBody(
								"consoleStderr", line);
						NetworkMessage stderrMsg = new NetworkMessage(
								header, body);

						// stdoutMsg.body.object = line;
						// answer.body.object = line;
						ConnectionManager.sendMessage(stderrMsg);

						if (line == null) {
							finished = true;
							break;
						}

					} while (!finished);
				} catch (IOException ex) {
					Logger.error(ex,
							"[ProcessIOModule] Error while piping 'stderr'");
				}
			}
		};

		stderrThread.start();

		Thread processThread = new Thread() {

			public void run() {

				// Wait for process-termination
				int returnValue;
				try {
					returnValue = cideProcess.p.waitFor();
					/* TODO: Find a way to fix that
					answer.header.destination = "sessionClosed";
					answer.body.message = "sessionClosed";
					answer.body.object = "session closed.";
					ConnectionManager.sendMessage(answer);
					*/
				} catch (InterruptedException e) {
					Logger.error(e,
							"[ProcessIOModule] Process interrupted, scheisse!");
				}
			}
		};

		processThread.start();
		
		return answer;
	}
	
	/**
	 * Builds the project
	 * @throws CideException
	 * @throws IOException 
	 */
	private NetworkMessage build(NetworkMessage msg, List<String> commands) throws IOException {
		
        // Getting the project BuildEngine
        BuildEngine buildEngine = mm.project.type.buildEngine;

        // Add the arguments, this is GCC specific
        /*
        for (BuildEngineOption buildEngineOption : buildEngine.options) {

                commands.add(buildEngineOption.value);
        }
        */
        
        return startProcess(msg, commands);
	}
}
