package actions;

import models.User;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

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