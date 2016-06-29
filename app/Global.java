
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
import static play.mvc.Results.notFound;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import helper.oai.OaiDispatcher;
import models.Globals;
import play.Application;
import play.GlobalSettings;
import play.Play;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Request;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;

/**
 * @author Jan Schnasse
 *
 */
public class Global extends GlobalSettings {
	public void onStart(Application app) {
		try {
			if (play.api.Play.isProd(play.api.Play.current())) {
				play.Logger.info("Regal-API started!");
				for (int i = 0; i < Globals.namespaces.length; i++) {

					play.Logger
							.info("Init fedora content models for " + Globals.namespaces[i]);
					OaiDispatcher.initContentModels(Globals.namespaces[i]);
				}
				play.Logger.info("Init fedora content models for default namespace");
				OaiDispatcher.initContentModels("");

				Globals.search.init(Globals.namespaces);
			} else {
				// play Framework läuft im Test- oder Entwicklungsmodus
				// Logger-Konfiguration für Entwicklung laden
				// Quelle hier:
				// http://stackoverflow.com/questions/14288623/logback-externalization

				String logbackConfFilename = "conf/logback.developer.xml";
				File logbackConfigurationFile = null;
				try {
					logbackConfigurationFile =
							Play.application().getFile(logbackConfFilename);
				} catch (Exception exception) {
					play.Logger.error("Can't read resource {} !", logbackConfFilename,
							exception);
				}

				if (logbackConfigurationFile.exists()) {

					play.Logger.info(
							"Found logback configuration {} - Overriding default configuration.",
							logbackConfFilename);
					JoranConfigurator configurator = new JoranConfigurator();
					LoggerContext loggerContext =
							(LoggerContext) LoggerFactory.getILoggerFactory();
					loggerContext.reset();
					configurator.setContext(loggerContext);
					try {
						configurator.doConfigure(logbackConfigurationFile);
						play.Logger.info(
								"Default configuration overridden by logback configuration {}.",
								logbackConfFilename);
					} catch (Exception exception) {
						try {
							new ContextInitializer(loggerContext).autoConfig();
						} catch (JoranException e) {
							BasicConfigurator.configureDefaultContext();
							play.Logger.error("Can't configure default configuration",
									exception);
						}
						play.Logger.error(
								"Can't configure logback with specified file {} - Keep default configuration",
								logbackConfFilename, exception);
					}

				} else {
					play.Logger.warn(
							"Can't read logback configuration conf/logback.developer.xml - Keeping default configuration.");
				}

			}
			Globals.taskManager.init();
			Globals.taskManager.execute();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void onStop(Application app) {
		// Globals.profile.saveMap();
		play.Logger.info("Application shutdown...");
		Globals.taskManager.shutdown();
	}

	public Promise<Result> onHandlerNotFound(RequestHeader request) {
		return Promise.<Result> pure(notFound("Action not found " + request.uri()));
	}

	@SuppressWarnings("rawtypes")
	public Action onRequest(Request request, Method actionMethod) {
		play.Logger.debug("\n" + request.toString() + "\n\t"
				+ mapToString(request.headers()) + "\n\t" + request.body().toString());
		return super.onRequest(request, actionMethod);
	}

	private String mapToString(Map<String, String[]> map) {
		StringBuilder sb = new StringBuilder();
		Iterator<Entry<String, String[]>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String[]> entry = iter.next();
			sb.append(entry.getKey());
			sb.append('=').append('"');
			sb.append(Arrays.toString(entry.getValue()));
			sb.append('"');
			if (iter.hasNext()) {
				sb.append("\n\t'");
			}
		}
		return sb.toString();

	}
}
