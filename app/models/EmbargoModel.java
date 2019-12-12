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
	
	public Calendar setEmbargoDate(String value) {
		String[] valueSplit = value.split("-");
		
		Calendar embargoCal = Calendar.getInstance();
		embargoCal.clear();
		embargoCal.add(Calendar.YEAR, Integer.parseInt(valueSplit[0]));
		embargoCal.add(Calendar.MONTH, Integer.parseInt(valueSplit[1]));
		embargoCal.add(Calendar.DAY_OF_MONTH, Integer.parseInt(valueSplit[2]));
		return embargoCal;
	}
	
	public boolean isActiveEmbargo(String embargoTime) {
		Calendar cal = setEmbargoDate(embargoTime);
		boolean test = cal.after(Calendar.getInstance());
		
		return test;
	}

	public String getAccessScheme(String embargoTime) {
		Calendar cal = setEmbargoDate(embargoTime);
		String schemeValue = "public";
		if(cal.after(Calendar.getInstance())) {
			schemeValue = "embargoed";
		};
		
		return schemeValue;
	}

}
