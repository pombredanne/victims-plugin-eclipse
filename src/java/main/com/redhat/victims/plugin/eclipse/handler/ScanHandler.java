package com.redhat.victims.plugin.eclipse.handler;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.core.runtime.Status;

import com.redhat.victims.VictimsException;
import com.redhat.victims.plugin.eclipse.Activator;
import com.redhat.victims.plugin.eclipse.VictimScan;

/**
 * Eclipse command, initialises the Victims settings, gets relevant libraries
 * and passes them to VictimScan for vulnerability scanning.
 * 
 * @author kurt
 */
public class ScanHandler extends AbstractHandler implements SettingsCallback {

	//Array for all generated library paths
	private ArrayList<IPath> paths = new ArrayList<IPath>();  
	private OptionMenuRunnable optionMenu;
	/* Default log for this plugin */
	private ILog log = Platform.getLog(Activator.getDefault().getBundle());

	private String JAR_EXT = "jar";
	
	/**
	 * Begins the life-cycle of the plugin. Finds the absolute path names of
	 * all dependencies for a project in eclipse and creates the popup
	 * menu for settings.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		ISelection sel = HandlerUtil.getActiveMenuSelection(event);
		IStructuredSelection selection = (IStructuredSelection) sel;
		Object firstElement = selection.getFirstElement();

		if (firstElement instanceof IJavaProject) {
			IJavaProject jp = (IJavaProject) firstElement;
			optionMenu = new OptionMenuRunnable(this);
			EventQueue.invokeLater(optionMenu);

			try {
				IClasspathEntry[] cp = jp.getResolvedClasspath(true);
				/* Extracts all .jar files from the classpath and adds them to
				   a list to be passed to VictimScan during the callback*/
				for (IClasspathEntry entry : cp){
					IPath path = entry.getPath();
					String ext = path.getFileExtension();
					log.log(new Status(Status.INFO, Activator.PLUGIN_ID, path.getFileExtension()));
					if (JAR_EXT.equals(ext)){					
						paths.add(path);
					}
				}
			} catch (JavaModelException e) {
				log.log(new Status(Status.INFO, Activator.PLUGIN_ID, e
						.getLocalizedMessage()));
			}

		} else {
			MessageDialog.openInformation(shell, "Info",
					"Please select a Java Project");
		}
		return null;
	}

	/**
	 * Gets the settings entered from the option menu and begins execution of
	 * the VictimsScan.
	 * @throws VictimsException 
	 */
	public void callbackSettings() throws VictimsException {
		HashMap<String, String> settings = (HashMap<String, String>) optionMenu
				.getMenu().getSettings();
		/* logging */
		Collection<String> logSet = settings.values();
		for (String setting : logSet) {
			log.log(new Status(Status.INFO, Activator.PLUGIN_ID, setting));
		}
		
		/* Begin scan */
		VictimScan vs = new VictimScan(settings, paths);
		vs.execute();
	}

}