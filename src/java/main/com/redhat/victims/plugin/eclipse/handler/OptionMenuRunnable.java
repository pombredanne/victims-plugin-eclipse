package com.redhat.victims.plugin.eclipse.handler;

import com.redhat.victims.plugin.eclipse.VictimsOptionMenu;

/**
 * Interface for handling creation of the option menu.
 * Should be invoked with a callback parameter on the EDT.
 * @author kurt
 */
public class OptionMenuRunnable implements Runnable {

	/* Callback interface */
	private SettingsCallback settingsHandler;
	/* Popup menu for options */
	private VictimsOptionMenu menu;
	
	public OptionMenuRunnable(SettingsCallback handler){
		settingsHandler = handler;
	}
	public OptionMenuRunnable(ScanHandler handler) {
		settingsHandler = handler;
	}
	
	/**
	 * Retrieve menu
	 * @return The Victims settings menu.
	 */
	public VictimsOptionMenu getMenu(){
		return menu;
	}
	
	/**
	 * Create and show the option menu
	 */
	public void run() {
		try {
			menu = new VictimsOptionMenu(settingsHandler);
			menu.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}