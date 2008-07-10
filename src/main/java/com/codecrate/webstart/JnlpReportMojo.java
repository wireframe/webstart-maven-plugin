package com.codecrate.webstart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

/**
 *
 * @goal report
 * @phase site
 */
public class JnlpReportMojo extends AbstractMavenReport {
	/**
	 * The Maven project.
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject mavenProject;

	/**
	 * Directory where reports will go.
	 *
	 * @parameter expression="${project.reporting.outputDirectory}"
	 * @required
	 * @readonly
	 */
	private String outputDirectory;

	/**
	 * @parameter expression="${component.org.apache.maven.doxia.siterenderer.Renderer}"
	 * @required
	 * @readonly
	 */
	private Renderer siteRenderer;
	
	/**
	 * @parameter expression="${project.url}"
	 * @required
	 */
	private String codebase;

	/**
	 * @parameter 
	 */
	private String jnlpFile = "launch.jnlp";

	protected void executeReport(Locale locale) throws MavenReportException {
		getLog().debug("jnlp report output is set to: " + getOutputDirectory());

		String url = ensureTrailingSlash(codebase) + jnlpFile;
		Sink sink = getSink();
		sink.head();
		sink.title();
		sink.text(getName(locale));
		sink.title_();
		insertBrowserDetectScripts(sink);
		sink.head_();
		sink.body();

		sink.rawText("<div id=\"java_required\" style=\"display: none;\">");
		sink.rawText("<div class=\"section\">");
		sink.rawText("<h2>Install Java</h2>");
		sink.rawText("<b>");
		sink.rawText("<a href=\"http://java.com\">Java</a> is required to be installed before launching this application.  ");
		sink.rawText("Click <a href=\"http://java.sun.com/PluginBrowserCheck?pass=" + url + "&fail=http://java.com/download\">here</a> to automatically install and launch the application.");
		sink.rawText("</b></div></div>");
		showTextIfNeeded(sink);

		sink.section1();
		sink.sectionTitle1();
		sink.text("Launch the Application");
		sink.sectionTitle1_();
		sink.link(url);
		sink.text("Launch " + mavenProject.getName());
		sink.link_();
		sink.section1_();

		sink.body_();
		sink.flush();
		sink.close();
	}

	private void showTextIfNeeded(Sink sink) {
		sink.rawText("<script language=\"JavaScript\">\n");
		sink.rawText("if (!javawsInstalled) {\n");
		sink.rawText("document.getElementById(\"java_required\").style.display = \"block\";\n");
		sink.rawText("}\n");
		sink.rawText("</script>\n");
	}

	private String ensureTrailingSlash(String string) {
		if (!string.endsWith("/")) {
			return string + "/";
		}
		return string;
	}

	/**
	 * @see http://java.sun.com/javase/6/docs/technotes/guides/javaws/developersguide/launch.html
	 */
	private void insertBrowserDetectScripts(Sink sink) {
		InputStream stream = getClass().getClassLoader().getResourceAsStream("browserDetect.js");
		try {
			sink.rawText(IOUtils.toString(stream));
		} catch (IOException e) {
			getLog().error("Error writing file", e);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				getLog().warn("Error closing stream", e);
			}
		}
	}

	public String getDescription(Locale locale) {
		return "Launch Application using Java Web Start";
	}

	public String getName(Locale locale) {
		return "Launch Application";
	}

	protected String getOutputDirectory() {
		return outputDirectory;
	}

	protected MavenProject getProject() {
		return mavenProject;
	}

	protected Renderer getSiteRenderer() {
		return siteRenderer;
	}

	public String getOutputName() {
		return "launch";
	}
}
