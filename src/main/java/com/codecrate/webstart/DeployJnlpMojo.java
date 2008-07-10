/*
 * Copyright 2007 Codecrate
 *
 * Licensed under the Apache License, Version 2.0 (the "License" );
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codecrate.webstart;

import java.io.File;

import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.observers.Debug;
import org.apache.maven.wagon.repository.Repository;

/**
 * @goal deploy-jnlp
 * @phase deploy
 * @execute goal=generate-jnlp
 */
public class DeployJnlpMojo extends AbstractMojo {
	/**
	 * @parameter
	 * @required
	 */
	private String id;

	/**
	 * @parameter
	 * @required
	 */
	private String url;

	/**
	 * The directory in which files will be stored prior to processing.
	 *
	 * @parameter expression="${project.build.directory}/webstart"
	 * @required
	 */
	private File workDirectory;

	/**
	 * @component
	 */
	private WagonManager wagonManager;

	public void execute() throws MojoExecutionException {
		if (!workDirectory.exists() && !workDirectory.mkdirs()) {
			throw new MojoExecutionException("Unable to setup JNLP work directory: " + workDirectory);
		}

		getLog().info("Deploying JNLP files from " + workDirectory + " to " + url);
		Repository repo = new Repository(id, url);

		Wagon wagon = null;
		try {
			wagon = getWagon(repo);
			wagon.connect(repo, wagonManager.getAuthenticationInfo(id));

			if (!wagon.supportsDirectoryCopy()) {
				getLog().warn("Unable to copy directories using the specified protocol: " + url);
				getLog().info("Attempting to transfer individual files to remote server.");
				File[] files = workDirectory.listFiles();
				for (int x = 0; x < files.length; x++) {
					File file = files[x];
					getLog().info("Transfering file: " + file.getName());
					wagon.put(file, file.getName());
				}
			} else {
				wagon.putDirectory(workDirectory, ".");
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to deploy JNLP files.", e);
		} finally {
			if (wagon != null) {
				try {
					wagon.disconnect();
				} catch (ConnectionException e) {
					getLog().warn("Error disconnecting from repo: " + repo, e);
				}
			}
		}
	}

	private Wagon getWagon(Repository repo)
			throws UnsupportedProtocolException, WagonConfigurationException {
		Wagon wagon;
		wagon = wagonManager.getWagon(repo);
		if (getLog().isDebugEnabled()) {
			Debug debug = new Debug();
			wagon.addSessionListener(debug);
			wagon.addTransferListener(debug);
		}
		return wagon;
	}
}
