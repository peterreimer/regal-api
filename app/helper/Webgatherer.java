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
import com.sun.media.jfxmedia.logging.Logger;

import models.Gatherconf;
import models.Node;
import actions.Create;
import actions.Read;

/**
 * @author Jan Schnasse
 *
 */
public class Webgatherer implements Runnable {

    @Override
    public void run() {
	// get all webpages
	List<Node> webpages = new Read()
		.listRepo("webpage", "edoweb", 0, 50000);
	// get all configs
	for (Node n : webpages) {
	    try {
		Gatherconf conf = Gatherconf.create(n.getConf());
		// find open jobs
		if (isOutstanding(n, conf)) {
		    new Create().createWebpageVersion(n);
		}

	    } catch (Exception e) {
		play.Logger.error("", e);
	    }
	}
    }

    private boolean isOutstanding(Node n, Gatherconf conf) {
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

    private Date getSchedule(Calendar cal, Gatherconf conf) {
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
