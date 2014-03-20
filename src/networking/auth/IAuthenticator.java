package networking.auth;

public interface IAuthenticator {

	public boolean Authenticate(String username, String password);
	
}
