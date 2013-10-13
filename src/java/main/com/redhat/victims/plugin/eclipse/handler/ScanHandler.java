package com.redhat.victims.plugin.eclipse.handler;

/*
 * #%L
 * This file is part of victims-plugin-eclipse.
 * %%
 * Copyright (C) 2013 The Victims Project
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

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

	// Array for all generated library paths
	private ArrayList<IPath> paths = new ArrayList<IPath>();
	private OptionMenuRunnable optionMenu;
	/* Default log for this plugin */
	private ILog log = Platform.getLog(Activator.getDefault().getBundle());

	private String JAR_EXT = "jar";

	/**
	 * Begins the life-cycle of the plugin. Finds the absolute path names of all
	 * dependencies for a project in eclipse and creates the popup menu for
	 * settings.
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
				/*
				 * Extracts all .jar files from the classpath and adds them to a
				 * list to be passed to VictimScan during the callback
				 */
				for (IClasspathEntry entry : cp) {
					IPath path = entry.getPath();
					String ext = path.getFileExtension();
					// This condition seems weird but if it's the other way
					// around you open yourself up to null pointer exceptions!
					if (JAR_EXT.equals(ext)) {
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
	 * @return 0 if no vulnerabilities detected 9996667 otherwise
	 * @throws VictimsException
	 */
	public int callbackSettings() throws VictimsException {
		HashMap<String, String> settings = (HashMap<String, String>) optionMenu
				.getMenu().getSettings();

		/* Run the scan */
		VictimScan vs = new VictimScan(settings, paths);
		if (vs.execute() == VictimScan.VULN_DETECTED) {
			JOptionPane.showMessageDialog(null,
					"A vulnerability was detected in your dependencies,"
							+ "Please see the log for details.", "ALERT",

					JOptionPane.ERROR_MESSAGE);
			return VictimScan.VULN_DETECTED;
		}
		return 0;
	}

	protected ArrayList<IPath> getPaths() {
		return paths;
	}

	protected void setPaths(ArrayList<IPath> pathset) {
		paths = pathset;
	}

	protected OptionMenuRunnable getOptionMenu() {
		return optionMenu;
	}

	protected void setOptionMenu(OptionMenuRunnable options) {
		optionMenu = options;
	}

}