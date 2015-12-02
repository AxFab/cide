package core.module.controller;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;

import play.Logger;
import core.common.controller.ModuleManager;
import core.common.type.Event;
import core.common.type.Module;
import core.net.controller.ConnectionManager;
import core.net.type.core.NetworkMessage;

public class BuildModule extends Module {

	public BlockingQueue<ProcessBuilder> execQueue;

	@Override
	public void onRegister(final ModuleManager mm) {
		
		this.mm = mm;

		this.execQueue = new SynchronousQueue<>();

		// Consumer thread
		Thread consumer = new Thread() {

			public void run() {

				while (true) {
					try {
						while (true) {

							ProcessBuilder processBuilder = execQueue.take();
							Logger.debug(
									"[BuildModule] Executing a command for project '%s'",
									mm.project.name);

							doJob(processBuilder);

						}
					} catch (InterruptedException e) {
						Logger.warn("Execution queue interrupted");
					} catch (Exception e) {
						Logger.error("[BuildModule] Error while building!");
					}
				}
			}
		};

		consumer.start();
	}

	@Override
	public void onUnregister() {
	}

	@Override
	public void onNetworkMessage(NetworkMessage msg) {

		// Invert source and destination
		NetworkMessage answer = msg.createAnswer();

		try {

			switch (msg.body.message) {

			case "buildProject":

				answer.body.message = "buildStarted";
				queueBuild();
				break;

			case "cleanProject":

				throw new NotImplementedException();

			default:
				Logger.error(
						"[ProjectModule] Unsupported NetworkMessage received '%s'",
						msg.body.message);
			}

			// If the message is set, we can send the NetworkMessage
			if (answer.body.message != null) {
				ConnectionManager.sendMessage(answer);
			}

		} catch (Exception ex) {

			//answer.body.message = "buildFailed";
			//ConnectionManager.sendMessage(answer);
		}
	}

	/**
	 * Queue the build in the command synchronous queue
	 * 
	 * @throws Exception
	 */
	private void queueBuild() throws Exception {

		/* TODO: FIX
		ProjectType projectType = mm.project.type;
		BuildEngine buildEngine = projectType.buildEngine;

		// Sending the list of files in the project folder to the BuildEngine
		List<SourceFile> sourceFiles = new ArrayList<>();
		for (File file : FileUtils.listFiles(mm.project.root, null, true)) {

			sourceFiles.add(new SourceFile(mm.project, 
					file.toURI().relativize(mm.project.root.toURI()).toString()));
		}

		// Get all the building commands from the BuildEngine
		List<ProcessBuilder> processBuilders = buildEngine.build(mm.project,
				sourceFiles);
		this.execQueue.addAll(processBuilders);
		*/
	}

	@Override
	public void onEvent(Event evt) {

		/*
		 * try {
		 * 
		 * switch (evt.type) {
		 * 
		 * default: Logger.error("Unsupported event received %s", evt.type); }
		 * 
		 * } catch (InvalidTargetObjectTypeException e) {
		 * Logger.error("Unsupported message type %d received for event %s",
		 * evt.messageType.toString(), evt.type); }
		 */
	}

	/**
	 * Update the build project's build files
	 */
	private void updateProjectTree() {

		throw new NotImplementedException();
	}

	/**
	 * Add a command to the build queue
	 * 
	 * @param projectId
	 *            Project Id
	 * @param command
	 * @throws IOException
	 */
	private void doJob(ProcessBuilder processBuilder) throws Exception {

		final Process p = processBuilder.start();
		for (String command : processBuilder.command()) {
			Logger.debug("[BuildModule] Command is composed of: '%s'", command);
		}

		// Wait for the process to end
		p.waitFor();
		
		String stdout = IOUtils.toString(p.getInputStream());
		String stderr = IOUtils.toString(p.getErrorStream());
		
		Logger.debug("[BuildModule] Stdout: %s", stdout);
		Logger.debug("[BuildModule] Stderr: %s", stderr);
		
		//ConnectionManager.sendMessage();

		Logger.debug("[BuildModule] Job is finished, return value is %s!",
				p.exitValue());
	}
}
