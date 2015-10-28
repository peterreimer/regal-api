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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.ibm.icu.util.Calendar;

import models.Gatherconf;
import models.Globals;
import models.Link;
import models.Node;
import actions.Create;
import actions.Create.WebgathererTooBusyException;
import actions.Read;

/**
 * @author Jan Schnasse
 *
 */
public class Webgatherer implements Runnable {

    @Override
    public void run() {
	// get all webpages

	play.Logger.info("List 50000 resources of type webpage from namespace "
		+ Globals.namespaces[0] + ".");
	List<Node> webpages = new Read().listRepo("webpage",
		Globals.namespaces[0], 0, 50000);
	play.Logger.info("Found " + webpages.size() + " webpages.");
	int count = 0;
	int limit = 5;
	// get all configs
	for (Node n : webpages) {
	    try {
		Gatherconf conf = Gatherconf.create(n.getConf());
		play.Logger.info("Test if " + n.getPid() + " is scheduled.");
		// find open jobs
		if (isOutstanding(n, conf)) {
		    count++;
		    play.Logger.info("Create new version for: " + n.getPid()
			    + ".");
		    new Create().createWebpageVersion(n);
		}

	    } catch (WebgathererTooBusyException e) {
		play.Logger.error("Webgatherer stopped! Heritrix is too busy.");
	    } catch (Exception e) {
		play.Logger.error("", e);
	    }
	    if (count >= limit)
		break;
	}
    }

    /**
     * @param n
     *            a webpage
     * @param conf
     *            user definde config
     * @return nextLaunch
     * @throws Exception
     *             can be IOException or Json related Exceptions
     */
    public static Date nextLaunch(Node n) throws Exception {
	Date lastHarvest = new Read().getLastModifiedChild(n).getLastModified();
	Gatherconf conf = Gatherconf.create(n.getConf());
	Calendar cal = Calendar.getInstance();
	cal.setTime(lastHarvest);
	Date nextTimeHarvest = getSchedule(cal, conf);
	return nextTimeHarvest;
    }

    private boolean isOutstanding(Node n, Gatherconf conf) {
	if (new Date().before(conf.getStartDate()))
	    return false;
	List<Link> parts = n
		.getRelatives(archive.fedora.FedoraVocabulary.HAS_PART);
	if (parts == null || parts.isEmpty())
	    return true;
	Date lastHarvest = new Read().getLastModifiedChild(n).getLastModified();
	Calendar cal = Calendar.getInstance();
	Date now = cal.getTime();
	cal.setTime(lastHarvest);
	Date nextTimeHarvest = getSchedule(cal, conf);
	play.Logger.info(n.getPid()
		+ " "
		+ n.getConf()
		+ " will be launched next time at "
		+ new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss")
			.format(nextTimeHarvest));
	return now.after(nextTimeHarvest);
    }

    private static Date getSchedule(Calendar cal, Gatherconf conf) {
	switch (conf.getInterval()) {
	case daily:
	    cal.add(Calendar.DATE, 1);
	    break;
	case weekly:
	    cal.add(Calendar.DATE, 7);
	    break;
	case monthly:
	    cal.add(Calendar.MONTH, 1);
	    break;
	case quarterly:
	    cal.add(Calendar.MONTH, 3);
	    break;
	case halfYearly:
	    cal.add(Calendar.MONTH, 6);
	    break;
	case annually:
	    cal.add(Calendar.YEAR, 1);
	    break;
	}
	return cal.getTime();
    }
}
