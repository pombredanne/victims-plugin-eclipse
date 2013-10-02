package com.redhat.victims.plugin.eclipse;
 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.apache.commons.io.FileUtils;
import org.h2.Driver;

import com.redhat.victims.VictimsConfig;
import com.redhat.victims.VictimsException;
import com.redhat.victims.VictimsResultCache;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import com.redhat.victims.plugin.eclipse.Settings;
/**
 * Provides the functionality to scan java libraries for known vulnerabilities.
 * Very similar to the Ant library com.redhat.victims.plugin.ant
 * 
 * @author kurt
 */
public class VictimScan {

	public ExecutionContext ctx;
	public ILog log;
	private ArrayList<IPath> paths;
	
	/**
	 * 
	 * @param set
	 *            Victims settings map
	 * @param paths
	 *            Absolute paths to projects dependencies
	 * @throws VictimsException
	 */
	public VictimScan(Map<String, String> set, ArrayList<IPath> paths)
			throws VictimsException {
		this.paths = paths;

		/* Set up context */
		ctx = new ExecutionContext();
		ctx.setSettings(new Settings());
		log = ctx.getLog();
		Driver driver = new Driver();
		/* Apply user supplied/default settings from option menu */
		Settings settings = ctx.getSettings();
		settings.set(VictimsConfig.Key.URI, set.get(VictimsConfig.Key.URI));
		System.setProperty(VictimsConfig.Key.URI,
				set.get(VictimsConfig.Key.URI));
		settings.set(VictimsConfig.Key.DB_DRIVER,
				set.get(VictimsConfig.Key.DB_DRIVER));
		System.setProperty(VictimsConfig.Key.DB_DRIVER,
				set.get(VictimsConfig.Key.DB_DRIVER));
		settings.set(VictimsConfig.Key.DB_URL,
				set.get(VictimsConfig.Key.DB_URL));
		System.setProperty(VictimsConfig.Key.DB_URL,
				set.get(VictimsConfig.Key.DB_URL));
		settings.set(VictimsConfig.Key.ENTRY, set.get(VictimsConfig.Key.ENTRY));
		System.setProperty(VictimsConfig.Key.ENTRY,
				set.get(VictimsConfig.Key.ENTRY));
		settings.set(VictimsConfig.Key.DB_USER,
				set.get(VictimsConfig.Key.DB_USER));
		System.setProperty(VictimsConfig.Key.DB_USER,
				set.get(VictimsConfig.Key.DB_USER));
		settings.set(VictimsConfig.Key.DB_PASS, "(not shown)");
		System.setProperty(VictimsConfig.Key.DB_PASS,
				set.get(VictimsConfig.Key.DB_PASS));
		settings.set(Settings.METADATA, set.get(Settings.METADATA));
		settings.set(Settings.FINGERPRINT, set.get(Settings.FINGERPRINT));
		settings.set(Settings.UPDATE_DATABASE,
				set.get(Settings.UPDATE_DATABASE));

		// Only use 1 algorithm for comparisons
		System.setProperty(VictimsConfig.Key.ALGORITHMS, "SHA512");

		/* Create results cache & victims DB */
		VictimsResultCache cache = new VictimsResultCache();
		ctx.setCache(cache);
		driver.getClass();
		VictimsDBInterface db = VictimsDB.db();
		ctx.setDatabase(db);

		// validate
		ctx.getSettings().validate();
		ctx.getSettings().show(ctx.getLog());

	}

	public void execute() throws VictimsBuildException {
		VictimsResultCache cache = ctx.getCache();
		int cores = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = null;
		List<Future<FileStub>> jobs = null;

		try {
			// Sync database
			updateDatabase(ctx);
			// Concurrency, yay!
			executor = Executors.newFixedThreadPool(cores);
			jobs = new ArrayList<Future<FileStub>>();

			// Find all files under supplied path
			log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
					"Scanning Files:"));
			for (IPath path : paths) {

				// Grab the file
				FileStub fs = new FileStub(path.toFile());
				String fsid = fs.getId();
				// Check the cache
				if (cache.exists(fsid)) {
					HashSet<String> cves = cache.get(fsid);
					log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
							"Cached: " + fsid));

					/* Report vulnerabilities */
					if (!cves.isEmpty()) {
						VulnerableDependencyException err
								= new VulnerableDependencyException(
								fs, Settings.FINGERPRINT, cves);
						log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
								err.getLogMessage()));
						if (err.isFatal(ctx)) {
							throw new VictimsBuildException(
									err.getErrorMessage());
						}
					}
					continue;
				}

				// Process dependencies that haven't been cached
				Callable<FileStub> worker = new VictimsCommand(ctx, fs);
				jobs.add(executor.submit(worker));
			} 
			executor.shutdown();

			// Check the results
			for (Future<FileStub> future : jobs) {
				try {
					FileStub checked = future.get();
					if (checked != null) {
						log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
								"Finished: " + checked.getId()));
						cache.add(checked.getId(), null);
					}
				} catch (InterruptedException ie) {
					log.log(new Status(Status.ERROR, Activator.PLUGIN_ID, ie
							.getMessage()));
				} catch (ExecutionException e) {

					Throwable cause = e.getCause();
					if (cause instanceof VulnerableDependencyException) {
						VulnerableDependencyException vbe = (VulnerableDependencyException) cause;
						cache.add(vbe.getId(), vbe.getVulnerabilites());

						log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
								vbe.getLogMessage()));

						if (vbe.isFatal(ctx)) {
							throw new VictimsBuildException(
									vbe.getErrorMessage());
						}
					} else {
						throw new VictimsBuildException(e.getCause()
								.getMessage());
					}
				}
			}
		} catch (VictimsException ve) {
			log.log(new Status(Status.ERROR, Activator.PLUGIN_ID, ve
					.getMessage()));
			throw new VictimsBuildException(ve.getMessage());

		} finally {
			if (executor != null) {
				executor.shutdown();
			}
		}
	}

	/**
	 * Updates the database according to the given configuration
	 * 
	 * @param ctx
	 * @throws VictimsException
	 */
	public void updateDatabase(ExecutionContext ctx) throws VictimsException {

		VictimsDBInterface db = ctx.getDatabase();
		ILog log = ctx.getLog();

		Date updated = db.lastUpdated();

		// update automatically every time
		if (ctx.updateAlways()) {
			log.log(new Status(Status.INFO, Activator.PLUGIN_ID, (TextUI.fmt(
					Resources.INFO_UPDATES, updated.toString(),
					VictimsConfig.uri()))));
			db.synchronize();

			// update once per day
		} else if (ctx.updateDaily()) {

			Date today = new Date();
			SimpleDateFormat cmp = new SimpleDateFormat("yyyMMdd");
			boolean updatedToday = cmp.format(today)
					.equals(cmp.format(updated));

			if (!updatedToday) {
				log.log(new Status(Status.INFO, Activator.PLUGIN_ID, TextUI
						.fmt(Resources.INFO_UPDATES, updated.toString(),
								VictimsConfig.uri())));

				db.synchronize();

			} else {
				log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
						"Database last synchronized: " + updated.toString()));
			}

			// updates disabled
		} else {
			log.log(new Status(Status.INFO, Activator.PLUGIN_ID,
					"Database synchronization disabled."));
		}

	}
}