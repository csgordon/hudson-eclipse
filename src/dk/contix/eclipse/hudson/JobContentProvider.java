package dk.contix.eclipse.hudson;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import dk.contix.eclipse.hudson.views.actions.BuildStatusAction;

import org.checkerframework.checker.guieffect.qual.*;

/**
 * Content Provider for job listing.
 * 
 * @author Joakim Recht
 * 
 */
public class JobContentProvider implements IStructuredContentProvider {
	private static final Logger log = Logger.getLogger(JobContentProvider.class);

	private org.eclipse.core.runtime.jobs.Job updateJob;

	private final HudsonClient client = new HudsonClient();

	private Preferences prefs;

	private final TableViewer viewer;

	private Job[] jobs = new Job[0];

	private boolean updating = false;

	private boolean error = false;

	private final BuildStatusAction action;

	private String viewUrl;

	public JobContentProvider(final TableViewer viewer, BuildStatusAction action) {
		this.viewer = viewer;
		this.action = action;
		prefs = Activator.getDefault().getPluginPreferences();
		prefs.addPropertyChangeListener(new Preferences.IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				error = false;
				reloadUpdateJob();
			}
		});
		reloadUpdateJob();
	}

	private void reloadUpdateJob() {
		if (updateJob != null) {
			updateJob.cancel();
		}

		if (prefs.getBoolean(Activator.PREF_AUTO_UPDATE)) {
			updateJob = new org.eclipse.core.runtime.jobs.Job("Fetch Hudson status") {
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("Refreshing Hudson status", 1);
					refresh();
					try {
						Display.getDefault().asyncExec(new @UI Runnable() {
							public void run() {
								viewer.refresh();
								updateJob.schedule(prefs.getInt(Activator.PREF_UPDATE_INTERVAL) * 1000);
							}
						});
						return Status.OK_STATUS;
					} finally {
						monitor.done();
					}
				}
			};
			updateJob.setPriority(org.eclipse.core.runtime.jobs.Job.DECORATE);
			updateJob.schedule(1500);
		}

	}
	
	public void refresh() {
		if (updating) return;
		
		try {
			updating = true;
			final Job[] newJobs;
			if (viewUrl == null) {
				newJobs = client.getJobs();
			} else {
				newJobs = client.getJobs(viewUrl);
			}

			if (jobs != null && prefs.getBoolean(Activator.PREF_POPUP_ON_ERROR)) {
				for (int i = 0; i < newJobs.length; i++) {
					if (hasFailed(newJobs[i])) {
						final Job job = newJobs[i];
						// Bug note by Sai Zhang, the following code should be wrapped in
						// Display.getDefault().syncExec(new Runnable() {  public void run() {} { .... }} // Colin Gordon: BUG
						/* BUG -- Colin Gordon */ MessageDialog.openWarning(viewer.getControl().getShell(), "Hudson build failed", "Hudson build failed for " + job.getName());
						break;
					}
				}
			}
			check: {
				for (int i = 0; i < newJobs.length; i++) {
					if (newJobs[i].getStatus().getStatus() == dk.contix.eclipse.hudson.BuildStatus.Status.FAIL || newJobs[i].getStatus().getStatus() == dk.contix.eclipse.hudson.BuildStatus.Status.TEST_FAIL) {
						if (!ignore(newJobs[i])) {
							final Job j = newJobs[i];
							Display.getDefault().syncExec(new @UI Runnable() {
								public void run() {
									action.setError(j);
								}
							});
							break check;
						}
					}
				}
				Display.getDefault().syncExec(new @UI Runnable() {
					public void run() {
						action.setOk();
					}
				});
			}

			jobs = newJobs;
			error = false;
		} catch (final RuntimeException e) {
			log.error("Unable to get jobs", e);
			// Bug note by Sai Zhang, the following code should be wrapped in
			// Display.getDefault().syncExec(new Runnable() {  public void run() {} { .... }}
			/* BUG -- Colin Gordon */ action.setUnknown();
			/* BUG -- Colin Gordon */ ErrorDialog.openError(viewer.getControl().getShell(), "Unable to get Hudson status",
					"Unable to get status from Hudson.", new Status(Status.ERROR, Activator.PLUGIN_ID, 0,
							"Unable to communicate with Hudson", e));
			
			error = true;
			jobs = new Job[0];
		} catch (final Exception e) {
			log.error("Unable to get jobs", e);
			// Bug note by Sai Zhang, the following code should be wrapped in
			// Display.getDefault().syncExec(new Runnable() {  public void run() {} { .... }}
			/* BUG -- Colin Gordon */ action.setUnknown();
			if (!error && prefs.getBoolean(Activator.PREF_POPUP_ON_CONNECTION_ERROR)) {
				/* BUG -- Colin Gordon */ ErrorDialog.openError(viewer.getControl().getShell(), "Unable to get Hudson status",
						"Unable to get status from Hudson. Check that the base url is configured correctly under preferences.", new Status(Status.ERROR, Activator.PLUGIN_ID, 0,
								"Unable to communicate with Hudson", e));
			}
			error = true;
			jobs = new Job[0];
		} finally {
			updating = false;
		}

	}

	public Object[] getElements(Object inputElement) {
		return jobs;
	}

	private boolean hasFailed(Job job) {
		if (ignore(job)) {
			return false;
		}
		for (int i = 0; i < jobs.length; i++) {
			if (jobs[i].getName().equals(job.getName())) {
				return job.getStatus().getStatus() == dk.contix.eclipse.hudson.BuildStatus.Status.FAIL && jobs[i].getStatus().getStatus() == dk.contix.eclipse.hudson.BuildStatus.Status.SUCCESS;
			}
		}
		return false;
	}

	private boolean ignore(Job job) {
		return prefs.getBoolean(Activator.PREF_FILTER_IGNORE_PROJECT + "_" + job.getName());
	}

	public void dispose() {
		if (updateJob != null) {
			updateJob.cancel();
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void setView(String url) {
		this.viewUrl = url;
	}

}
