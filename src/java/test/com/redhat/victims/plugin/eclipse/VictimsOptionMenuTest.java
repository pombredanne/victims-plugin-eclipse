package com.redhat.victims.plugin.eclipse;

import static org.junit.Assert.*;

import org.junit.Test;

import com.redhat.victims.VictimsConfig;
import com.redhat.victims.database.VictimsDB;

public class VictimsOptionMenuTest {

	/**
	 * Test applySettings correctly applied defaults.
	 */
	@Test
	public void testApplySettings() {
		mockCallbackHandler mock = new mockCallbackHandler();
		VictimsOptionMenu vm = new VictimsOptionMenu(mock);
		String baseUrl, entryPoint, jdbcDriver, jdbcUrl, jdbcUser;
		String jdbcPass, metadata, fingerprint, updates;
		vm.applySettings();
		baseUrl = vm.getSettings().get(VictimsConfig.Key.URI);
		entryPoint = vm.getSettings().get(VictimsConfig.Key.ENTRY);
		jdbcDriver = vm.getSettings().get(VictimsConfig.Key.DB_DRIVER);
		jdbcUrl = vm.getSettings().get(VictimsConfig.Key.DB_URL);
		jdbcUser = vm.getSettings().get(VictimsConfig.Key.DB_USER);
		jdbcPass =vm.getSettings().get(VictimsConfig.Key.DB_PASS);
		metadata = vm.getSettings().get(Settings.METADATA);
		fingerprint = vm.getSettings().get(Settings.FINGERPRINT);
		updates = vm.getSettings().get(Settings.UPDATE_DATABASE);
		
		assertTrue(baseUrl.equals("http://www.victi.ms/"));
		assertTrue(entryPoint.equals("service/"));
		assertTrue(jdbcDriver.equals(VictimsDB.defaultDriver()));
		assertTrue(jdbcUrl.equals(VictimsDB.defaultURL()));
		assertTrue(jdbcUser.equals("victims"));
		assertTrue(jdbcPass.equals("victims"));
		assertTrue(metadata.equals("warning"));
		assertTrue(fingerprint.equals("fatal"));
		assertTrue(updates.equals("auto"));
	}

}
