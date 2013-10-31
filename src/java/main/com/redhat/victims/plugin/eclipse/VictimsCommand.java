package com.redhat.victims.plugin.eclipse;

/*
 * #%L
 * This file is part of victims-plugin-ant.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.Status;

import com.redhat.victims.VictimsRecord;
import com.redhat.victims.VictimsScanner;
import com.redhat.victims.database.VictimsDBInterface;
/**
 * callable class to implement scanning of jar files
 * against a database of known vulnerabilities.
 * 
 * @author kurt, gmurphy
 */
public class VictimsCommand implements Callable<FileStub> {
	private FileStub jar;
	private ExecutionContext ctx;

	/**
	 * Initialises the victimscommand!
	 * @param ctx Context for execution
	 * @param jar Dependency to scan against database
	 */
	VictimsCommand(ExecutionContext ctx, FileStub jar) {
		this.jar = jar;
		this.ctx = ctx;
	}

	/**
	 * Processes a FileStub (jar) and compares it to a database of
	 * known vulnerabilities
	 */
	public FileStub call() throws Exception {
		assert(ctx != null);
		ctx.getLog().log(new Status(Status.INFO, Activator.PLUGIN_ID,
				"Scanning: " + jar.getFileName()));
		VictimsDBInterface db = ctx.getDatabase();
		String dependency = jar.getFile().getAbsolutePath();

		// compare fingerprint to database
		if (ctx.isEnabled(Settings.FINGERPRINT)) {
			
			for (VictimsRecord vr : VictimsScanner.getRecords(dependency)) {
				HashSet<String> cves = db.getVulnerabilities(vr);
				if (! cves.isEmpty()) {
					throw new VulnerableDependencyException(jar,
							Settings.FINGERPRINT, cves);
				}
			}
		}

		// compare metadata to database
		if (ctx.isEnabled(Settings.METADATA)){
            HashMap<String,String> gav = new HashMap<String,String>();
            gav.put("title", jar.getTitle());
            gav.put("artifactId", jar.getArtifactId());
            gav.put("version", jar.getVersion());
            HashSet<String> cves = db.getVulnerabilities(gav);
            if (! cves.isEmpty()){
              throw new VulnerableDependencyException(jar, Settings.METADATA, cves);
			}
		}
		return jar;
	}

}