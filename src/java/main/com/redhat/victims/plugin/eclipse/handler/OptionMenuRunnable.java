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