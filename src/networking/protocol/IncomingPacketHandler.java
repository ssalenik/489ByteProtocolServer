package networking.protocol;

import java.util.*;

import database.IResource;
import database.UserMessage;
import networking.UnformattedPacket;
import networking.auth.AuthenticationManager;
import networking.auth.AuthenticationToken;
import networking.protocol.types.MessageType;
import networking.protocol.types.responses.*;

public class IncomingPacketHandler {

	private AuthenticationToken auth;

	private IResource resource;
	private AuthenticationManager auth_manager;
	private IAsyncClientWriter asyncClientWriter;

	private static final UnformattedPacket BAD_MESSAGE_RESPONSE = new UnformattedPacket(
			MessageType.BADLY_FORMATTED_MESSAGE.getInt(),
			"You have input a badly formatted message, please check the available commands and try again");

	public static final String FIELD_TERMINATOR = ",";

	public IncomingPacketHandler(AuthenticationManager auth_manager, IResource resource,
			IAsyncClientWriter writer) {
		this.resource = resource;
		this.auth_manager = auth_manager;
		this.asyncClientWriter = writer;

		this.auth = this.auth_manager.EmptyToken();
	}

	// / Simply a wrapper to keep connection alive
	public UnformattedPacket keepAliveProcess(UnformattedPacket p) {
		UnformattedPacket np = process(p);
		return np;
	}

	private static String getRemaining(String s, int headerLength) {
		return s.substring(headerLength + 1);
	}


	private UnformattedPacket process(UnformattedPacket p) {
		switch (MessageType.ofInt(p.getHeader())) {
		case ECHO: // return same packet (echo, duh!)
			return UnformattedPacket.CreateResponsePacket(
					MessageType.ECHO.getInt(), Echo.ECHO_OK.getInt(),
					p.getPayload());

		case EXIT: // exit the system
			auth.logout();
			return UnformattedPacket.CreateResponsePacket(
					MessageType.EXIT.getInt(), Exit.EXIT_OK.getInt(), "");

		case BADLY_FORMATTED_MESSAGE:
			return BAD_MESSAGE_RESPONSE;

		case CREATE_USER: {
			int code = UserCreate.BADLY_FORMATTED.getInt();
			String msg = "";

			if (auth.authenticated()) {
				code = UserCreate.ALREADY_LOGGED_IN.getInt();
				msg = "You are already logged in as another user";
			} else {
				String user_and_pass = p.payloadAsString();
				StringTokenizer st = new StringTokenizer(user_and_pass,
						FIELD_TERMINATOR);
				if (st.countTokens() >= 2) {
					String user = st.nextToken();
					String pass = getRemaining(user_and_pass, user.length());
					boolean created = resource.createUser(user, pass);
					if (created) {
						code = UserCreate.CREATE_SUCCESS.getInt();
						msg = "Created new user " + user;
					} else {
						code = UserCreate.USER_ALREADY_EXISTS.getInt();
						msg = "Failed to create user " + user
								+ ". User probably already exists";
					}
				} else {
					code = UserCreate.BADLY_FORMATTED.getInt();
					msg = "Badly formatted user create message";
				}
			}

			return UnformattedPacket.CreateResponsePacket(
					MessageType.CREATE_USER.getInt(), code, msg);
		}

		case CREATE_STORE: {
			int code = CreateStore.STORE_ALREADY_EXISTS.getInt();
			String msg = "";

			if (auth.authenticated()) {
				boolean created = resource.createUserDataTable(auth.Username);
				if (created) {
					code = CreateStore.STORE_CREATED.getInt();
					msg = "User's data store created";
				} else {
					code = CreateStore.STORE_ALREADY_EXISTS.getInt();
					msg = "The user's data store already exists";
				}
			} else {
				code = CreateStore.NOT_LOGGED_IN.getInt();
				msg = "Must login first to create a user store";
			}

			return UnformattedPacket.CreateResponsePacket(
					MessageType.CREATE_STORE.getInt(), code, msg);
		}

		case DELETE_USER: {
			int code = UserDelete.ERROR.getInt();
			String msg = "";

			if (auth.authenticated()) {
				auth.logout();
				if (resource.deleteUser(auth.Username)) {
					code = UserDelete.DELETE_SUCCESS.getInt();
					msg = "User deletion successful. You are now logged out and the account is removed";
				} else { // shouldn't ever happen
					code = UserDelete.ERROR.getInt();
					msg = "User deletion failed due to an underlying error";
				}
			} else {
				code = UserDelete.NOT_LOGGED_IN.getInt();
				msg = "User deletion failed, you must login first to delete yourself";
			}

			return UnformattedPacket.CreateResponsePacket(
					MessageType.DELETE_USER.getInt(), code, msg);
		}

		case SEND_TO_USER: {
			int code = SendToUser.BADLY_FORMATTED.getInt();
			String msg = "";

			if (auth.authenticated()) {
				String payload = p.payloadAsString();
				StringTokenizer st = new StringTokenizer(payload,
						FIELD_TERMINATOR);
				if (st.countTokens() >= 2) {
					String u = st.nextToken();
					String user_message = getRemaining(payload, u.length());

					boolean user_exists = resource.userDataTableExists(u);
					if (user_exists) {
						boolean sent = resource.sendMessageToUser(u,
								auth.Username, user_message);
						if (sent) {
							code = SendToUser.MESSAGE_SENT.getInt();
							msg = "Message sent";
						} else {
							code = SendToUser.FAILED_TO_WRITE_TO_USER_STORE
									.getInt();
							msg = "Failed to write the message to the user store";
						}
					} else {
						code = SendToUser.USER_DOESNT_EXIST.getInt();
						msg = "The specified user (or at least their data store) doesn't exist";
					}
				} else {
					code = SendToUser.BADLY_FORMATTED.getInt();
					msg = "Must have the format <username>,<message> where message may contain commas";
				}
			} else {
				code = SendToUser.NOT_LOGGED_IN.getInt();
				msg = "Musg login in order to send a message to a user";
			}

			return UnformattedPacket.CreateResponsePacket(
					MessageType.SEND_TO_USER.getInt(), code, msg);
		}

		case QUERY_MESSAGES: {
			int code = QueryMessages.NO_MESSAGES.getInt();
			String msg = "";

			if (auth.authenticated()) {
				UserMessage[] msgs = resource.getNewUserMessages(auth.Username);
				if (msgs.length > 0) {
					int i = 0;
					// This loop sends the first (n-1) messages over the wire before returning the
					// last message in the queue to the processing thread 
					for (; i < msgs.length - 1; i++) {
						String msgtxt = msgs[i].format();
						UnformattedPacket up = UnformattedPacket
								.CreateResponsePacket(
										MessageType.QUERY_MESSAGES.getInt(),
										QueryMessages.MESSAGES.getInt(), msgtxt);
						asyncClientWriter.writePacket(up);
					}
					UserMessage lastMessage = msgs[i];

					code = QueryMessages.MESSAGES.getInt();
					msg = lastMessage.format();
				} else {
					code = QueryMessages.NO_MESSAGES.getInt();
					msg = "No messages available";
				}
			} else {
				code = QueryMessages.NOT_LOGGED_IN.getInt();
				msg = "Must login first";
			}
			return UnformattedPacket.CreateResponsePacket(
					MessageType.QUERY_MESSAGES.getInt(), code, msg);
		}

		case LOGIN: {
			int code = Login.BADLY_FORMATTED.getInt();
			String msg = "";

			if (auth.authenticated()) {
				code = Login.ALREADY_LOGGED_IN.getInt();
				msg = "Cannot login as another user while already logged in";
			} else {
				String payload = p.payloadAsString();
				StringTokenizer st = new StringTokenizer(payload,
						FIELD_TERMINATOR);
				if (st.countTokens() >= 2) {
					String user = st.nextToken();
					String pass = getRemaining(payload, user.length());

					auth = auth_manager.Authenticate(user, pass);
					switch (auth.getAuthenticationTokenType()) {
					case GOOD:
						code = Login.LOGIN_OK.getInt();
						msg = "Login successful";
						break;
					case ALREADY_LOGGED_IN:
						code = Login.ALREADY_LOGGED_IN.getInt();
						msg = "Already logged in";
						break;
					case LOGIN_FAILED:
						code = Login.BAD_CREDENTIALS.getInt();
						msg = "Failed to login, bad credentials";
						break;
					default:
						code = Login.BAD_CREDENTIALS.getInt();
						msg = "Failed to login, bad credentials";
						break;
					}
				} else {
					msg = "Failed to login, badly formatted message";
				}
			}

			return UnformattedPacket.CreateResponsePacket(
					MessageType.LOGIN.getInt(), code, msg);
		}

		case LOGOFF: {
			int code = Logoff.NOT_LOGGED_IN.getInt();
			String msg = "";

			if (auth.authenticated()) {
				auth.logout();
				code = Logoff.LOGOFF_OK.getInt();
				msg = "Logoff successsful";
			} else {
				msg = "You are not logged in";
			}

			return UnformattedPacket.CreateResponsePacket(
					MessageType.LOGOFF.getInt(), code, msg);
		}
		}
		return BAD_MESSAGE_RESPONSE;
	}

	public void logoffFired() {
		UnformattedPacket p = UnformattedPacket.CreateResponsePacket(
				MessageType.LOGOFF.getInt(), Logoff.SESSION_EXPIRED.getInt(),
				"You've been logged out due to inactivity");
		asyncClientWriter.writePacket(p);
	}
}
