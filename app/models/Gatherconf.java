package models;

import java.io.StringWriter;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.util.JsonUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Gatherconf {
    public enum Interval {
	annually, halfYearly, quarterly, monthly, weekly, daily
    };

    public enum RobotsPolicy {
	classic, ignore
    }

    String url;
    String deepness;
    RobotsPolicy robotsPolicy;
    Interval interval;
    Date startDate;

    public Gatherconf() {
	url = null;
	deepness = null;
	robotsPolicy = null;
	interval = null;
	startDate = null;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public String getDeepness() {
	return deepness;
    }

    public void setDeepness(String deepness) {
	this.deepness = deepness;
    }

    public RobotsPolicy getRobotsPolicy() {
	return robotsPolicy;
    }

    public void setRobotsPolicy(RobotsPolicy robotsPolicy) {
	this.robotsPolicy = robotsPolicy;
    }

    public Interval getInterval() {
	return interval;
    }

    public void setInterval(Interval interval) {
	this.interval = interval;
    }

    public Date getStartDate() {
	return startDate;
    }

    public void setStartDate(Date startDate) {
	this.startDate = startDate;
    }

    @Override
    public String toString() {
	ObjectMapper mapper = JsonUtil.mapper();
	StringWriter w = new StringWriter();
	try {
	    mapper.writeValue(w, this);
	} catch (Exception e) {
	    return super.toString();
	}
	return w.toString();
    }
}
