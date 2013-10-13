package com.redhat.victims.plugin.eclipse.handler;

import static org.junit.Assert.*;
import org.eclipse.core.runtime.Path;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.junit.Test;

import com.redhat.victims.VictimsException;
import com.redhat.victims.plugin.eclipse.VictimScan;
import com.redhat.victims.plugin.eclipse.mockCallbackHandler;

public class ScanHandlerTest {

	@Test
	public void testCallbackSettings() throws VictimsException {
		ScanHandler handler = new ScanHandler();

		ArrayList<IPath> paths = handler.getPaths();
		File spring = new File("testdata", "spring-2.5.6.jar");
		Path path = new Path(spring.getAbsolutePath());
		paths.add(path);
		handler.setPaths(paths);
		OptionMenuRunnable optionMenu = new OptionMenuRunnable(
				new mockCallbackHandler());
		optionMenu.run();
		optionMenu.getMenu().setVisible(false);
		optionMenu.getMenu().applySettings();
		handler.setOptionMenu(optionMenu);
		int vuln = handler.callbackSettings();
		assertTrue(vuln == VictimScan.VULN_DETECTED);
	}

}
