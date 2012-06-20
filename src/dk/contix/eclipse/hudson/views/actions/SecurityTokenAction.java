package dk.contix.eclipse.hudson.views.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import guitypes.checkers.quals.*;

public class SecurityTokenAction extends Action {

	private final Shell shell;

        @UIEffect
	public SecurityTokenAction(Shell shell) {
		this.shell = shell;
		setText("Set security token...");
		setToolTipText("Configure the security token used to schedule builds");
	}

	public void run() {

	}
}
