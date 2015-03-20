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

import play.Play;
import archive.fedora.ApplicationProfile;
import archive.fedora.FedoraFacade;
import archive.fedora.FedoraFactory;
import archive.search.SearchFacade;

/**
 * Global Settings and accessors for Elasticsearch and Fedora
 * 
 * @author Jan Schnasse
 *
 */
public class Globals {

    /**
     * the server that hosts this app
     */
    public static String server = Play.application().configuration()
	    .getString("regal-api.serverName");

    /**
     * the server to where urns should point
     */
    public static String urnbase = Play.application().configuration()
	    .getString("regal-api.urnbase");

    /**
     * The Elasticsearch cluster
     */
    public static String escluster = Play.application().configuration()
	    .getString("regal-api.escluster");

    /**
     * An internal address available for requests from localhost
     */
    public static String fedoraIntern = Play.application().configuration()
	    .getString("regal-api.fedoraIntern");

    /**
     * A document to configure elasticsearch indexes used by this application
     */
    public static String contextDocument = "public-index-config.json";

    /**
     * a list of namespaces to create indexes for
     */
    public static String[] namespaces = Play.application().configuration()
	    .getString("regal-api.namespace").split("\\s*,[,\\s]*");

    /**
     * defines how objects will be referenced internally
     */
    public static boolean useHttpUris = false;

    /**
     * password to access fedora
     */
    public static String fedoraPassword = Play.application().configuration()
	    .getString("regal-api.fedoraUserPassword");
    /**
     * user to access fedora
     */
    public static String fedoraUser = Play.application().configuration()
	    .getString("regal-api.fedoraUser");

    /**
     * A config file for elasticsearch. Must be located at conf/ directory
     */
    public static String elasticsearchSettings = "public-index-config.json";

    /**
     * a globally available entry to elasticsearch
     */
    public static SearchFacade search = new SearchFacade(Globals.escluster,
	    Globals.namespaces);

    /**
     * a globally available entry to Fedora
     */
    public static FedoraFacade fedora = FedoraFactory.getFedoraImpl(
	    Globals.fedoraIntern, Globals.fedoraUser, Globals.fedoraPassword);

    /**
     * labels etc.
     */
    public static ApplicationProfile profile = new ApplicationProfile();

    /**
     * defines a protocol used by this app
     */
    public static String protocol = "http://";

    public static final String PDFBOX_OCR_TYPE = "pdfbox-ocr";

    public static final String PDFBOX_OCR_INDEX_PREF = "fulltext_";

    public static final String PUBLIC_INDEX_PREF = "public_";

}
