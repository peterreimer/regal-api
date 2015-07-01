package helper;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.ibm.icu.util.Calendar;

import models.Gatherconf;

public class GatherconfImporter {

    private static final CsvPreference PIPE_DELIMITED = new CsvPreference.Builder(
	    '"', '|', "\n").build();

    public static List<Gatherconf> read(String csv) {
	try (ICsvMapReader mapReader = new CsvMapReader(new StringReader(csv),
		PIPE_DELIMITED)) {
	    Calendar cal = Calendar.getInstance();
	    List<Gatherconf> result = new ArrayList<Gatherconf>();
	    final String[] header = mapReader.getHeader(true);
	    final CellProcessor[] processors = getProcessors();
	    Map<String, Object> row;
	    while ((row = mapReader.read(header, processors)) != null) {
		cal.add(Calendar.HOUR, 2);
		Date startDate = cal.getTime();
		Gatherconf conf = new Gatherconf();
		conf.setStartDate(startDate);
		conf.setDeepness(-1);
		String intervallString = (String) row.get("INTERVALL_H");
		if ("null".equals(intervallString)) {
		    conf.setInterval(Gatherconf.Interval.annually);
		} else {
		    int intervall = Integer.parseInt(intervallString);
		    if (intervall <= 4320) {
			conf.setInterval(Gatherconf.Interval.halfYearly);
		    } else {
			conf.setInterval(Gatherconf.Interval.annually);
		    }
		}
		String robots = (String) row.get("IGNORE_ROBOTS");
		if (robots != null) {
		    play.Logger.warn("Robots for " + conf.getUrl() + " is "
			    + robots);
		}
		conf.setRobotsPolicy(Gatherconf.RobotsPolicy.classic);
		conf.setUrl((String) row.get("URL"));
		conf.setName((String) row.get("ALEPHIDN"));
		result.add(conf);
	    }
	    return result;
	} catch (IOException e) {
	    throw new HttpArchiveException(500, e);
	}
    }

    /**
     * ID|URL|ALEPHIDN|WEBSCHNITT|INTERVALL_H|EBENEN|GATHER_DIR|LAST_ENTRY|
     * LAST_GATHER|LAST_INGEST|TOLOAD_DIR|STATUS|LAST_PID|USR_STAT|ENTRY_DATE|
     * EXTENDED_TEXT|IGNORE_ROBOTS|DEACT_MSG
     * 
     * @return
     */
    private static CellProcessor[] getProcessors() {
	final CellProcessor[] processors = new CellProcessor[] {
		new UniqueHashCode(), // ID
		new Optional(), // URL
		new Optional(), // ALEPHIDN
		new Optional(), // WEBSCHNITT
		new Optional(), // INTERVALL_H
		new Optional(), // EBENEN
		new Optional(), // GATHER_DIR
		new Optional(), // LAST_ENTRY
		new Optional(), // LAST_GATHER
		new Optional(), // LAST_INGEST
		new Optional(), // TOLOAD_DIR
		new Optional(), // STATUS
		new Optional(), // LAST_PID
		new Optional(), // USR_STAT
		new Optional(), // ENTRY_DATE
		new Optional(), // EXTENDED_TEXT
		new Optional(), // IGNORE_ROBOTS
		new Optional() // DEACT_MSG
	}; // EMPTY
	return processors;
    }
}
