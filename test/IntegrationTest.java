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
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.HTMLUNIT;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import org.junit.Test;

import play.libs.F.Callback;
import play.test.TestBrowser;

public class IntegrationTest {

    /**
     * add your integration test here in this example we just check if the
     * welcome page is being shown
     */
    @Test
    public void test() {
	running(testServer(3333, fakeApplication(inMemoryDatabase())),
		HTMLUNIT, new Callback<TestBrowser>() {
		    public void invoke(TestBrowser browser) {
			browser.goTo("http://localhost:3333");
			assertThat(browser.pageSource()).contains(
				"Your new application is ready.");
		    }
		});
    }

}
