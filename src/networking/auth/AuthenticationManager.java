package networking.auth;

import java.util.*;

public class AuthenticationManager implements IAuthenticationManager {

	private Hashtable<String,AuthenticationToken> sessions;
	private IAuthenticator authenticator;
	
	public AuthenticationManager(IAuthenticator authenticator) {
		sessions = new Hashtable<String,AuthenticationToken>();
		this.authenticator = authenticator;
	}
	
	public synchronized void DeleteAuthentication(AuthenticationToken token) {
		if (sessions.containsKey(token.Username)) {
			sessions.remove(token.Username);
			token.logout();
		}
	}
	
	public AuthenticationToken Authenticate(String u, String pass) {
		if (sessions.containsKey(u)) {
			AuthenticationToken token = new AuthenticationToken(this);
			token.setAuthenticationType(AuthenticationTokenType.ALREADY_LOGGED_IN);
			return token; // bad token, already logged in
		}
		if (authenticator.Authenticate(u, pass)) {
			AuthenticationToken token = new AuthenticationToken(this,u);
			token.login();
			sessions.put(u, token);
			return token;
		} else {
			AuthenticationToken token = new AuthenticationToken(this);
			token.setAuthenticationType(AuthenticationTokenType.LOGIN_FAILED);
			return token; // bad auth
		}
	}
	
	public AuthenticationToken EmptyToken() {
		AuthenticationToken token = new AuthenticationToken(this);
		token.setAuthenticationType(AuthenticationTokenType.EMPTY_TOKEN);
		return token;
	}
	
}
