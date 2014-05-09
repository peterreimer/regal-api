package models;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "message")
public class Message {
    String text;
    int code;

    public Message(String text) {
	this.text = text;
	this.code = 200;
    }

    public Message(String text, int code) {
	this.text = text;
	this.code = code;
    }

    public Message(Throwable t, int code) {
	text = getStackTrace(t);
	this.code = code;
    }

    String getStackTrace(Throwable t) {
	StringWriter sw = new StringWriter();
	t.printStackTrace(new PrintWriter(sw));
	return sw.toString();
    }

    public String getText() {
	return text;
    }

    public void setText(String text) {
	this.text = text;
    }

    public int getCode() {
	return code;
    }

    public void setCode(int code) {
	this.code = code;
    }

}
