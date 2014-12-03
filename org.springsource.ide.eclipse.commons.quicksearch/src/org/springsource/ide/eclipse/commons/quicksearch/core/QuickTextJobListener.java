package org.springsource.ide.eclipse.commons.quicksearch.core;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;

/**
 * This class only meant for debugging purposes, 
 */
public class QuickTextJobListener {
	
	
	public static StringBuffer getStackDumps() {
		StringBuffer sb = new StringBuffer();
		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
		for (Map.Entry<Thread, StackTraceElement[]> entry : traces.entrySet()) {
			sb.append(entry.getKey().toString());
			sb.append("\n");
			for (StackTraceElement element : entry.getValue()) {
				sb.append("  ");
				sb.append(element.toString());
				sb.append("\n");
			}
			sb.append("\n");
		}
		return sb;
	}
	
	public static boolean DEBUG = true;
	private static long zeroTime = System.currentTimeMillis();
	
	
	public static void debug(String msg) {
		if (DEBUG) {
			System.out.println(showTime()+msg);
		}
	}

	private static String showTime() {
		return String.format("%9.3f  ", (System.currentTimeMillis()-zeroTime)/1000.0);
	}

	private static IJobChangeListener jobListener;
	
	public static synchronized IJobChangeListener jobListener() {
		if (jobListener==null) {
			jobListener = new IJobChangeListener() {
				@Override
				public void sleeping(IJobChangeEvent event) {
					debug("Job SLEEPING:     "+event.getJob());
				}
				
				@Override
				public void scheduled(IJobChangeEvent event) {
					debug("Job SCHEDULED:    "+event.getJob()+" delay: "+event.getDelay());
				}
				
				@Override
				public void running(IJobChangeEvent event) {
					debug("Job RUNNING:      "+event.getJob());
				}
				
				@Override
				public void done(IJobChangeEvent event) {
					debug("Job DONE:         "+event.getJob()+" result: "+event.getResult());
				}
				
				@Override
				public void awake(IJobChangeEvent event) {
					debug("Job AWAKE:        "+event.getJob());
				}
				
				@Override
				public void aboutToRun(IJobChangeEvent event) {
					debug("Job ABOUT TO RUN: "+event.getJob());
				}
			};
		}
		return jobListener;
	}

	/**
	 * Job listener that checks job transitions quickly to done state after it
	 * transitions to running state. 
	 */
	public static IJobChangeListener slowJobChecker(final long duration) {
		return new IJobChangeListener() {

			//Make a nice message listing all the jobs and their present state.
			private String jobInfos() {
				final IJobManager jm = Job.getJobManager();
				Job[] allJobs = jm.find(null);
				StringBuffer msg = new StringBuffer("JobManager contains: \n");
				for (Job job : allJobs) {
					msg.append("         "+jobType(job)+":"+job.getName() + " State: " + stateString(job) +"\n");
				}
				return msg.toString();
			}
			
			private String jobType(Job job) {
				if (job instanceof UIJob) {
					return "UIJob";
				}
				return "Job";
			}

			public String stateString(Job job) {
				int state = job.getState();
				switch (state) {
				case Job.RUNNING:
					return "RUNNING";
				case Job.SLEEPING:
					return "SLEEPING";
				case Job.WAITING:
					return "WAITING";
				case Job.NONE:
					return "NONE";
				default:
					return ""+state;
				}
			}
			
			private boolean done = false;

			@Override
			public void sleeping(IJobChangeEvent event) {
			}
			
			@Override
			public void scheduled(IJobChangeEvent event) {
			}
			
			@Override
			public void running(final IJobChangeEvent event) {
				new Job("Check slow job") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						if (!done) {
							debug("SLOW Job detected "+event.getJob());
							debug(jobInfos());
							debug("--- stack traces ---:\n"+getStackDumps());
							this.schedule(duration);
						}
						return Status.OK_STATUS;
					}
				}.schedule(duration);
			}
			
			@Override
			public void done(IJobChangeEvent event) {
				done = true;
			}
			
			@Override
			public void awake(IJobChangeEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void aboutToRun(IJobChangeEvent event) {
				// TODO Auto-generated method stub
				
			}
		};
	}

}
