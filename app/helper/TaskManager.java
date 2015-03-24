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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author Jan Schnasse
 *
 */
public class TaskManager {

    private List<ScheduledTask> scheduledTasks = new ArrayList<ScheduledTask>();

    /**
     * register all jobs
     */
    public void init() {
	// play.Logger
	// .info("Register Job: heartbeat. Will run every day at 0:23h");
	// addTask("heartbeat",
	// () -> play.Logger.debug("Heartbeat every 5 sec: "
	// + new Date().toString()), "*/5 * * * * ?");
	play.Logger
		.info("Register Job: urn allocator. Will run every day at 0:23h");
	addTask("urn allocator", new UrnAllocator(), "0 23 0 * * ?");
    }

    private void addTask(String name, Runnable r, String cronExpression) {
	try {
	    scheduledTasks.add(new ScheduledTask(name, cronExpression, r));
	} catch (Exception e) {
	    play.Logger.error("Not able to schedule \"" + name + "\" task", e);
	}
    }

    /**
     * activate all jobs
     */
    public void execute() {
	for (ScheduledTask c : scheduledTasks) {
	    c.schedule();
	}
    }

    /**
     * cancel execution of all jobs
     */
    public void shutdown() {
	for (ScheduledTask c : scheduledTasks) {
	    c.cancel();
	}
    }
}
