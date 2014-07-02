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
package models;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 */
@XmlRootElement(name = "errorMessage")
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
