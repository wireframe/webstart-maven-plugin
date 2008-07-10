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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.taskdefs.Zip;
import org.codehaus.mojo.keytool.GenkeyMojo;

/**
 * @goal generate-jnlp
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class GenerateJnlpMojo extends AbstractAntMojo {

	/**
	 * The project helper used to attach the artifact produced by this plugin to the project.
	 * @parameter expression="${component.org.apache.maven.project.MavenProjectHelper}
	 */
	private MavenProjectHelper projectHelper;

    /**
     * The directory in which files will be stored prior to processing.
     *
     * @parameter expression="${project.build.directory}/webstart"
     * @required
     */
    File workDirectory;
    
    /**
     * @parameter
     * @required
     */
    String mainClass;
    
    /**
     * @parameter expression="${project.artifacts}"
     */
    private Collection projectArtifacts = Collections.EMPTY_LIST;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject mavenProject;
    
	/**
	 * @parameter expression="${project.name}"
     * @required
	 */
	private String title;
	
	/**
	 * @parameter expression="${project.organization.name}"
     * @required
	 */
	private String vendor;

	/**
	 * @parameter expression="${project.url}"
     * @required
	 */
	private String homepage;

	/**
	 * @parameter
	 */
	private boolean offlineAllowed = false;

	/**
	 * @parameter
	 */
	boolean allPermissions = false;
	
	/**
	 * @parameter expression="${project.url}"
	 * @required
	 */
	private String codebase;

	/**
	 * @parameter expression="${project.description}"
     * @required
	 */
	private String description;
	
	/**
	 * @parameter 
	 */
	private String spec = "1.0+";

	/**
	 * @parameter 
	 */
	private String jnlpFile = "launch.jnlp";

	/**
	 * @parameter expression="${project.build.directory}"
	 */
	private File outputDirectory;
	
	/**
	 * @parameter expression="${project.build.finalName}.zip"
	 */
	private String finalName;

	/**
	 * @parameter
	 */
	private String j2seVersion = "1.5+";
	
	private JarProcessor jarProcessor = JarProcessor.NO_OP;

	public void execute() throws MojoExecutionException {
		if (!workDirectory.exists() && !workDirectory.mkdirs()) {
			throw new MojoExecutionException("Unable to setup JNLP work directory: " + workDirectory);
		}
		
		if (allPermissions) {
			jarProcessor = new SigningJarProcessor();
		} else {
			jarProcessor = new SimpleFileCopyJarProcessor();
		}
		createJnlpFile(createWriter());
		createDistribution();
	}

	private BufferedWriter createWriter() throws MojoExecutionException {
		File file = new File(workDirectory, jnlpFile);
		getLog().info("Creating JNLP File: " + file);
		try {
			return new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to write to: " + file);
		}
	}

	void createJnlpFile(BufferedWriter writer) throws MojoExecutionException {
		try {
			addLine(writer, "<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			addLine(writer, "<jnlp");
			addLine(writer, " spec=\"" + spec + "\" ");  
			addLine(writer, " codebase=\"" + codebase + "\" "); 
			addLine(writer, " href=\"" + jnlpFile + "\">");
			addLine(writer, "  <information>");
			addLine(writer, "    <title>" + title + "</title>");
			addLine(writer, "    <vendor>" + vendor + "</vendor>");
			addLine(writer, "    <homepage href=\"" + homepage + "\" />");
			addLine(writer, "    <description>" + description + "</description>");
			if (offlineAllowed) {
				addLine(writer, "    <offline-allowed/>");
			}
			addLine(writer, "  </information>");
			if (allPermissions) {
				addLine(writer, "  <security>");
				addLine(writer, "    <all-permissions/>");
				addLine(writer, "  </security>");
			}
			addLine(writer, "  <resources>");
			addLine(writer, "    <j2se version=\""+ j2seVersion + "\" />");

			processArtifact(mavenProject.getArtifact(), writer);
	        for (Iterator iterator = projectArtifacts.iterator(); iterator.hasNext();) {
	            Artifact artifact = (Artifact) iterator.next();
	            processArtifact(artifact, writer);
	        }
			addLine(writer, "  </resources>");
			addLine(writer, "  <application-desc  main-class=\"" + mainClass + "\" />");
			addLine(writer, "</jnlp>");
			writer.close();
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to write JNLP file", e);
		}
	}

	private void addLine(BufferedWriter writer, String string) throws IOException {
		writer.append(string);
		writer.newLine();
	}

	private void createDistribution() throws MojoExecutionException {
		File toFile = new File(outputDirectory, finalName);
		getLog().info("Creating archive distribution: " + toFile);
		try {
			if (toFile.exists()) {
				toFile.delete();
			}
			Zip zip = new Zip();
			zip.setProject(getProject());
			zip.setBasedir(workDirectory);
			zip.setDestFile(toFile);
			zip.execute();

			projectHelper.attachArtifact(mavenProject, "zip", toFile);
		} catch (Exception e) {
			throw new MojoExecutionException("Unable to create archive distribution of webstart app", e);
		}
	}

	private void processArtifact(Artifact artifact, BufferedWriter writer) throws IOException {
		getLog().debug("Processing artifact: " + artifact);
		File source = artifact.getFile();
		File destination = new File(workDirectory, source.getName());
		
        addLine(writer, "    <jar href=\"" + source.getName() + "\"" + (artifactContainsMainClass(artifact) ? " main=\"true\"" : "" ) + " />");
        jarProcessor.processJar(source, destination);
	}

	private boolean artifactContainsMainClass(Artifact artifact) {
		try {
			ClassLoader cl = new URLClassLoader(new URL[] { artifact.getFile().toURI().toURL() });
			Class.forName(mainClass, false, cl);
			return true;
		} catch (Exception e) {
			getLog().debug("Unable to find mainclass " + mainClass + " in artifact " + artifact);
		}

		return false;
	}

	protected MavenProject getMavenProject() {
		return mavenProject;
	}
	
	
	static interface JarProcessor {
		public static JarProcessor NO_OP = new JarProcessor() {
			public void processJar(File source, File destination) throws IOException {
			}
		};
		void processJar(File source, File destination) throws IOException;
	}
	
	static class SimpleFileCopyJarProcessor implements JarProcessor {
		public void processJar(File source, File destination) throws IOException {
			FileUtils.copyFile(source, destination);
		}
	}
	
	class SigningJarProcessor implements JarProcessor {

		public void processJar(File source, File destination) throws IOException {
			getLog().info("Signing jar " + destination);
			SignJar sign = new SignJar();
			sign.setProject(getProject());
			sign.setKeystore(getKeystore().getAbsolutePath());
			sign.setAlias(getKeystoreAlias());
			sign.setStorepass(getStorePassword());
			sign.setJar(source);
			sign.setSignedjar(destination);
			sign.setVerbose(getLog().isDebugEnabled());
			
			sign.execute();
		}

		private String getStorePassword() {
			return "123123";
		}

		private String getKeystoreAlias() {
			return mavenProject.getArtifactId();
		}
		
		private File getKeystore() throws IOException {
			File keystore = new File(outputDirectory, ".keystore");
			if (!keystore.exists()) {
		        GenkeyMojo genKeystore = new GenkeyMojo();
		        genKeystore.setAlias(getKeystoreAlias());
		        genKeystore.setStorepass(getStorePassword());
				genKeystore.setDname("CN=" + escapeCommas(title) + ", O=" + escapeCommas(vendor));
				genKeystore.setKeystore(keystore.getAbsolutePath());
				genKeystore.setVerbose(getLog().isDebugEnabled());
				genKeystore.setWorkingDir(workDirectory);
				genKeystore.setLog(getLog());
		    
		        try {
					genKeystore.execute();
				} catch (MojoExecutionException e) {
					throw new IOException("Unable to create keystore at: " + keystore, e);
				}
			}
			return keystore;
		}

	    private String escapeCommas(String string) {
	    	return string.replaceAll(",", "\\,");
		}
	}
}
