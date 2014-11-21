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
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import org.junit.Test;

import play.libs.WS;

/**
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 *         add your integration test here in this example we just check if the
 *         welcome page is being shown
 */
@SuppressWarnings("javadoc")
public class IntegrationTest {

    @Test
    public void serverStarts() {
	running(testServer(3333), new Runnable() {
	    @SuppressWarnings("deprecation")
	    public void run() {
		assertThat(
			WS.url("http://localhost:3333").get().get().getStatus())
			.isEqualTo(200);
	    }
	});
    }

    @Test
    public void asksForAuthorization() {

    }

    @Test
    public void createObject() {

    }

    @Test
    public void moveObject() {

    }

    @Test
    public void listObject() {

    }

    @Test
    public void deleteObject() {

    }

    @Test
    public void listObjects() {

    }

    @Test
    public void accessObject_fail() {

    }

    @Test
    public void accessObject_succeed() {

    }

    @Test
    public void addUrn() {

    }

    @Test
    public void createTransformer() {

    }

    @Test
    public void addAndRemoveTransformer() {

    }

    @Test
    public void checksum_fail() {

    }

    @Test
    public void checksum_succeed() {

    }

}
