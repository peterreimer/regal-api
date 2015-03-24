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

import com.ibm.icu.util.Calendar;

import controllers.MyUtils;

/**
 * @author Jan Schnasse
 *
 */
public class UrnAllocator implements Runnable {

    @Override
    public void run() {
	Calendar cal = Calendar.getInstance();
	cal.add(Calendar.DATE, -7);
	Date date = cal.getTime();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
	MyUtils.addUrnToAll("edoweb", "hbz:929:02", dateFormat.format(date));
    }

}
