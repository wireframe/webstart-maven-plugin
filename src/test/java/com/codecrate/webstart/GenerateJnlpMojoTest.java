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
import java.io.CharArrayWriter;
import java.io.File;

import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.project.MavenProject;
import org.custommonkey.xmlunit.XMLTestCase;

public class GenerateJnlpMojoTest extends XMLTestCase  {

	public void testDefaultBehaviorIsNotToSignJarFiles() throws Exception {
		ArtifactStub artifact = new ArtifactStub();
		artifact.setFile(new File(Thread.currentThread().getContextClassLoader().getResource("test.jar").toURI()));

		MavenProject project = new MavenProjectStub();
		project.setArtifact(artifact);

		GenerateJnlpMojo mojo = setupMojo(project);

		CharArrayWriter results = new CharArrayWriter();
		BufferedWriter writer = new BufferedWriter(results);
		mojo.createJnlpFile(writer);

		String expectedResult=
		"<jnlp spec=\"1.0+\" codebase=\"null\" href=\"launch.jnlp\">\n" +
		"  <information>\n" +
		"    <title>null</title>\n" +
		"    <vendor>null</vendor>\n" +
		"    <homepage href=\"null\" />\n" +
		"    <description>null</description>\n" +
		"  </information>\n" +
		"  <resources>\n" +
		"    <j2se version=\"1.5+\" />\n" +
		"    <jar href=\"test.jar\" />\n" +
		"  </resources>\n" +
		"  <application-desc  main-class=\"null\" />\n" +
		"</jnlp>\n";
		assertXMLEqual(expectedResult, results.toString());
	}
	
	public void testArtifactWithMainClassThatContainsPublicEntryPointIsFlaggedAsMainResource() throws Exception {
		ArtifactStub artifact = new ArtifactStub();
		artifact.setFile(new File(Thread.currentThread().getContextClassLoader().getResource("test.jar").toURI()));

		MavenProject project = new MavenProjectStub();
		project.setArtifact(artifact);

		String mainClass = "com.codecrate.shard.ui.ShardCyclops";
		GenerateJnlpMojo mojo = setupMojo(project);
		mojo.mainClass=mainClass;

		CharArrayWriter results = new CharArrayWriter();
		BufferedWriter writer = new BufferedWriter(results);
		mojo.createJnlpFile(writer);

		String expectedResult=
		"<jnlp spec=\"1.0+\" codebase=\"null\" href=\"launch.jnlp\">\n" +
		"  <information>\n" +
		"    <title>null</title>\n" +
		"    <vendor>null</vendor>\n" +
		"    <homepage href=\"null\" />\n" +
		"    <description>null</description>\n" +
		"  </information>\n" +
		"  <resources>\n" + 
		"    <j2se version=\"1.5+\" />\n" +
		"    <jar href=\"test.jar\" main=\"true\" />\n" +
		"  </resources>\n" +
		"  <application-desc  main-class=\"" + mainClass + "\" />\n" +
		"</jnlp>\n";
		assertXMLEqual(expectedResult, results.toString());
	}

	private GenerateJnlpMojo setupMojo(MavenProject project) {
		GenerateJnlpMojo mojo = new GenerateJnlpMojo();
		mojo.mavenProject = project;
		mojo.workDirectory = new File(System.getProperty("java.io.tmpdir"));
		return mojo;
	}
}
