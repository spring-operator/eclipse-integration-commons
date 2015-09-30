package org.springsource.ide.eclipse.commons.frameworks.core.async;

import java.util.concurrent.Executor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class JobBasedExecutor implements Executor {

	private static Executor instance;

	@Override
	public void execute(final Runnable command) {
		Job job = new Job("No name") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				command.run();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	public synchronized static Executor getInstance() {
		if (instance == null) {
			instance = new JobBasedExecutor();
		}
		return instance;
	}

}
