package com.redhat.victims.plugin.eclipse.handler;

import com.redhat.victims.VictimsException;

/**
 * Callback interface for option menu.
 * @author kurt
 */
public interface SettingsCallback {

	int callbackSettings() throws VictimsException;
}