package networking.auth;

public class AuthenticationToken {
	
	public final String Username;
	private boolean Authenticated;
	private AuthenticationTokenType type;
	private IAuthenticationManager manager;
	
	public AuthenticationToken(IAuthenticationManager manager) {
		Username = "";
		Authenticated = false;
		this.manager = manager;
	}
	
	public AuthenticationToken(IAuthenticationManager manager, String uname) {
		Username = uname;
		this.manager = manager;
	}
	
	public void setAuthenticationType(AuthenticationTokenType type) {
		this.type = type;
	}
	public AuthenticationTokenType getAuthenticationTokenType() { return type; }

	public void login() {
		type = AuthenticationTokenType.GOOD;
		Authenticated = true;
	}
	
	public void logout() {
		Authenticated = false;
		manager.DeleteAuthentication(this);
	}
	
	public boolean authenticated() { return Authenticated; }

}
