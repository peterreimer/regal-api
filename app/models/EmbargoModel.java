/**
 * 
 */
package models;

import java.util.Calendar;

/**
 * @author aquast
 *
 */
public class EmbargoModel {

	/**
	 * @param value
	 * @return
	 */
	public Calendar setEmbargoDate(String value) {
		String[] valueSplit = value.split("-");

		Calendar embargoCal = Calendar.getInstance();
		embargoCal.clear();
		embargoCal.set(Calendar.YEAR, Integer.parseInt(valueSplit[0]));
		embargoCal.set(Calendar.MONTH, Integer.parseInt(valueSplit[1]) - 1);
		embargoCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(valueSplit[2]));
		return embargoCal;
	}

	/**
	 * @param embargoTime
	 * @return
	 */
	public boolean isActiveEmbargo(String embargoTime) {
		Calendar cal = setEmbargoDate(embargoTime);
		boolean test = cal.getTime().after(Calendar.getInstance().getTime());

		return test;
	}

	/**
	 * @param embargoTime
	 * @return
	 */
	public String getAccessScheme(String embargoTime) {
		Calendar cal = setEmbargoDate(embargoTime);
		String schemeValue = "public";
		if (cal.getTime().after(Calendar.getInstance().getTime())) {
			schemeValue = "embargoed";
		}

		return schemeValue;
	}

}
