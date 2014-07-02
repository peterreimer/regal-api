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
package actions;

import models.User;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

/**
 * Implemented as shown at:
 * 
 * @see <a
 *      href="http://digitalsanctum.com/2012/06/07/basic-authentication-in-the-play-framework-using-custom-action-annotation/http://digitalsanctum.com/2012/06/07/basic-authentication-in-the-play-framework-using-custom-action-annotation">digitalsanctum</a>
 */
public class BasicAuthAction extends Action<BasicAuth> {

    private static final String AUTHORIZATION = "authorization";
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String REALM = "Basic realm=\"Your Realm Here\"";

    @Override
    public F.Promise<SimpleResult> call(Http.Context context) throws Throwable {

	String authHeader = context.request().getHeader(AUTHORIZATION);
	if (authHeader == null) {
	    return unauthorized(context);
	}

	String auth = authHeader.substring(6);
	byte[] decodedAuth = new sun.misc.BASE64Decoder().decodeBuffer(auth);
	String[] credString = new String(decodedAuth, "UTF-8").split(":");

	if (credString == null || credString.length != 2) {
	    return unauthorized(context);
	}

	String username = credString[0];
	String password = credString[1];
	User authUser = new User().authenticate(username, password);

	return (authUser == null) ? unauthorized(context) : delegate
		.call(context);
    }

    private play.libs.F.Promise<SimpleResult> unauthorized(Http.Context context) {
	context.response().setHeader(WWW_AUTHENTICATE, REALM);
	return F.Promise.promise(new F.Function0<SimpleResult>() {
	    @Override
	    public SimpleResult apply() throws Throwable {
		return unauthorized();
	    }
	});
    }
}