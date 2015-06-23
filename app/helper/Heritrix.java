/*
 * Copyright 2014 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPDigestAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

import play.Play;
import models.Gatherconf;

/**
 * @author Jan Schnasse
 *
 */
public class Heritrix {
    public static String openwaybackLink = Play.application().configuration()
	    .getString("regal-api.heritrix.openwaybackLink");

    class HeritrixWebclient {
	public Client createWebclient() {
	    String keystoreLoc = Play.application().configuration()
		    .getString("regal-api.heritrix.keystoreloc");
	    String keystorePass = Play.application().configuration()
		    .getString("regal-api.heritrix.keystorepwd");
	    String heritrixUser = Play.application().configuration()
		    .getString("regal-api.heritrix.user");
	    String heritrixPwd = Play.application().configuration()
		    .getString("regal-api.heritrix.pwd");

	    Client webclient = null;
	    ClientConfig cc = new DefaultClientConfig();
	    // cc.getProperties()
	    // .put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
	    cc.getFeatures().put(ClientConfig.FEATURE_DISABLE_XML_SECURITY,
		    true);
	    cc.getProperties().put(
		    HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
		    new HTTPSProperties(null, initSsl(cc, keystoreLoc,
			    keystorePass)));
	    webclient = Client.create(cc);
	    webclient.addFilter(new HTTPDigestAuthFilter(heritrixUser,
		    heritrixPwd));
	    return webclient;
	}

	private SSLContext initSsl(ClientConfig cc, String keystorelocation,
		String keystorepasswd) {
	    try (InputStream keystoreInput = new FileInputStream(
		    keystorelocation)) {
		SSLContext ctx = SSLContext.getInstance("SSL");
		KeyStore trustStore;
		trustStore = KeyStore.getInstance("JKS");
		trustStore.load(keystoreInput, keystorepasswd.toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory
			.getInstance("SunX509");
		tmf.init(trustStore);
		ctx.init(null, tmf.getTrustManagers(), null);
		return ctx;
	    } catch (Exception e) {
		throw new RuntimeException("Can not initiate SSL connection", e);
	    }
	}
    }

    final String restUrl = Play.application().configuration()
	    .getString("regal-api.heritrix.rest");

    final String jobDir = Play.application().configuration()
	    .getString("regal-api.heritrix.jobDir");

    final Client client = new HeritrixWebclient().createWebclient();

    /**
     * @param name
     * @return true if teardown did work
     */
    public boolean teardown(String name) {
	try {
	    teardownJobToHeritrix(name);
	    return true;
	} catch (RuntimeException e) {
	    return false;
	}
    }

    private String teardownJobToHeritrix(String name) {
	try {
	    WebResource resource = client.resource(restUrl + "/engine/job/"
		    + name);
	    String response = resource.accept("application/xml").post(
		    String.class, "action=teardown");
	    play.Logger.info(response);
	    return response;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Create job directory with config. Add Job to heritrix
     * "https://webarchive.jira.com/wiki/display/Heritrix/Heritrix+3.x+API+Guide#Heritrix3.xAPIGuide-AddJobDirectory"
     * 
     * "curl -v -d \"action=add&addpath=/Users/hstern/job\" -k -u admin:admin --anyauth --location -H \"Accept:application/xml\" https://localhost:8443/engine"
     * 
     * @param conf
     */
    public void createJob(Gatherconf conf) {
	play.Logger.debug("Create new job " + conf.getName());
	File dir = createJobDir(conf);
	try {
	    teardown(conf.getName());
	} catch (RuntimeException e) {
	    play.Logger.debug("", e);
	}
	try {
	    addJobDirToHeritrix(dir);
	} catch (Exception e) {
	    if (dir.exists())
		dir.delete();
	    teardown(conf.getName());
	}

    }

    private File createJobDir(Gatherconf conf) {
	try {
	    // Create Job Directory
	    play.Logger.debug("Create job Directory " + jobDir + "/"
		    + conf.getName());
	    File dir = new File(jobDir + "/" + conf.getName());
	    dir.mkdirs();
	    // Copy Job-Config to JobDirectory
	    File crawlerConf = Play.application().getFile("crawler-beans.cxml");

	    /*
	     * metadata.operatorContactUrl=${OPERATOR_URL}
	     * metadata.jobName=${JOBNAME} metadata.description=${DESCRIPTION}
	     * metadata.robotsPolicyName=${ROBOTSPOLICY} ${URL}
	     */

	    Path path = Paths.get(crawlerConf.getAbsolutePath());
	    Charset charset = StandardCharsets.UTF_8;
	    String content = new String(Files.readAllBytes(path), charset);
	    content = content.replaceAll("\\$\\{OPERATOR_URL\\}",
		    "https://www.edoweb-rlp.de");
	    content = content.replaceAll("\\$\\{JOBNAME\\}", conf.getName());
	    content = content.replaceAll("\\$\\{ROBOTSPOLICY\\}", conf
		    .getRobotsPolicy().toString());
	    content = content.replaceAll("\\$\\{DESCRIPTION\\}",
		    "Edoweb crawl of" + conf.getUrl());
	    content = content.replaceAll("\\$\\{URL\\}", conf.getUrl());

	    play.Logger.debug("Print-----\n" + content + "\n to \n"
		    + dir.getAbsolutePath() + "/crawler-beans.cxml");

	    Files.write(
		    Paths.get(dir.getAbsolutePath() + "/crawler-beans.cxml"),
		    content.getBytes(charset));
	    return dir;
	} catch (Exception e) {

	    throw new RuntimeException(e);
	}
    }

    private void addJobDirToHeritrix(File dir) {
	try {
	    WebResource resource = client.resource(restUrl + "/engine");
	    String response = resource.accept("application/xml")
		    .post(String.class,
			    "action=add&addpath=" + dir.getAbsolutePath());
	    play.Logger.info(response);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * "curl -v -d \"action=launch\" -k -u admin:admin --anyauth --location -H \"Accept: application/xml\" https://localhost:8443/engine/job/myjob"
     * 
     * @param name
     * @return the url to the warc file
     */
    public boolean launch(String name) {
	// Launch Job
	try {
	    launchJobToHeritrix(name);
	    return true;
	} catch (Throwable e) {
	    return false;
	}

    }

    private void launchJobToHeritrix(String name) {
	try {
	    WebResource resource = client.resource(restUrl + "/engine/job/"
		    + name);
	    play.Logger.debug(resource.accept("application/xml").post(
		    String.class, "action=launch"));
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * @param name
     * @return
     */
    public boolean unpause(String name) {
	try {
	    unpauseJobToHeritrix(name);
	    return true;

	} catch (RuntimeException e) {
	    return false;
	}

    }

    /**
     * @param name
     *            the jobs name e.g. the pid
     * @return the servers directory where to store the data
     */
    public File getCurrentCrawlDir(String name) {
	File dir = new File(this.jobDir + "/" + name);
	File[] files = dir.listFiles(file -> {
	    String now = new SimpleDateFormat("yyyyMMdd")
		    .format(new java.util.Date());
	    play.Logger.debug(now);
	    return file.isDirectory() && file.getName().startsWith(now);
	});
	Arrays.sort(
		files,
		(f1, f2) -> {
		    return Long.valueOf(f1.lastModified()).compareTo(
			    f2.lastModified());
		});

	File latest = files[files.length - 1];
	return latest;
    }

    /**
     * @param latest
     *            dir of the latest (most recent) job
     * @return local path of the latest harvested warc
     */
    public String findLatestWarc(File latest) {
	play.Logger.debug(latest.getAbsolutePath() + "/warcs");
	File warcDir = new File(latest.getAbsolutePath() + "/warcs");
	return warcDir.listFiles()[0].getAbsolutePath();
    }

    /**
     * @param str
     * @return
     */
    public String getUriPath(String str) {
	return str.replace(jobDir, "").replace(".open", "");
    }

    private String unpauseJobToHeritrix(String name) {
	try {
	    WebResource resource = client.resource(restUrl + "/engine/job/"
		    + name);
	    String response = resource.accept("application/xml").post(
		    String.class, "action=unpause");
	    play.Logger.info(response);
	    return response;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * @param name
     *            of the job
     * @return true if a jobdirectory is present
     */
    public boolean jobExists(String name) {
	play.Logger.debug("Test if jobDir exists " + jobDir + "/" + name);
	return new File(jobDir + "/" + name).exists();
    }

    /**
     * @param name
     * @return pass the job status file
     */
    public String getJobStatus(String name) {
	try {
	    WebResource resource = client.resource(restUrl + "/engine/job/"
		    + name);
	    String response = resource.accept("application/xml").get(
		    String.class);
	    return response;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * @param name
     * @param crawlDir
     * @return
     */
    public String getJobStatus(String name, String crawlDir) {
	try {
	    WebResource resource = client.resource(restUrl + "/engine/job/"
		    + name + "/jobdir/" + crawlDir
		    + "/reports/crawl-report.txt");
	    String response = resource.get(String.class);
	    return response;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

}
