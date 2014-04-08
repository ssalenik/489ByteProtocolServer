package networking.protocol;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import database.FileUploader;
import database.IResource;
import database.UserFile;
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
	private FileUploader fileUploader;
	private DecimalFormat dformat = new DecimalFormat("#.##");
	
	// used to save the newest file id (wrt the db)
	// of which the currently logged in user has been already notified
	private int newestFileId = 0; 

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
		
		// one FileUploader per packet handler
		this.fileUploader = new FileUploader();
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
				boolean created_data_store = resource.createUserDataTable(auth.Username);
				boolean created_file_store = resource.createUserFilesTable(auth.Username);
				if (created_data_store && created_file_store) {
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
				UserFile[] files = resource.getNewUserFiles(auth.Username, newestFileId);
				
				if (files.length > 0) {
					// if there are files for the user, first send him all of the new messages
					// then send all the new files
					
					// update up to which files the user has been notified of
					int last = files.length - 1;
					this.newestFileId = files[last].id;
					
					// This loop sends all the messages over the wire then returns to the processing thread
					if(msgs.length > 0) {
						for (int i = 0; i < msgs.length; i++) {
							String msgtxt = msgs[i].format();
							UnformattedPacket up = UnformattedPacket
									.CreateResponsePacket(
											MessageType.QUERY_MESSAGES.getInt(),
											QueryMessages.MESSAGES.getInt(), msgtxt);
							asyncClientWriter.writePacket(up);
						}
					}
					
					// this loop sends the first n - 1 files before returning to the processing thread
					int i = 0;
					for (; i < files.length - 1; i++) {
						String filetxt = files[i].format();
						UnformattedPacket up = UnformattedPacket
								.CreateResponsePacket(
										MessageType.QUERY_MESSAGES.getInt(),
										QueryMessages.FILES.getInt(), filetxt);
						asyncClientWriter.writePacket(up);
					}
					UserFile lastfile = files[i];
					
					code = QueryMessages.FILES.getInt();
					msg = lastfile.format();
					
				} else if (msgs.length > 0) {
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
						// reset stuff, just in case
						fileUploader.cancelUpload();
						newestFileId = 0;
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
				// reset stuff
				fileUploader.cancelUpload();
				newestFileId = 0;
			} else {
				msg = "You are not logged in";
			}

			return UnformattedPacket.CreateResponsePacket(
					MessageType.LOGOFF.getInt(), code, msg);
		}
		
		case REQUEST_SEND_FILE: {
			int code = RequestSendFile.BADLY_FORMATTED.getInt();
			String msg = "";

			if (auth.authenticated()) {
				// check message formatting and stuff
				String payload = p.payloadAsString();
				StringTokenizer st = new StringTokenizer(payload,
						FIELD_TERMINATOR);
				if (st.countTokens() >= 3) {
					String u = st.nextToken();
					String filesize_str = st.nextToken();
					long filesize = -1;
					String filename = getRemaining(payload, u.length() + filesize_str.length());

					boolean user_exists = resource.userFilesTableExists(u);
					if (user_exists) {
						// can send to user
						
						//try converting filesize to int and check that its valid
						try {
							filesize = Integer.parseInt(filesize_str);
						} catch (java.lang.NumberFormatException e) {
							// not an int
						}
						
						// basic check on filesize
						if(filesize >= 0) {
							// filesize OK
							// check if another file send is in progress
							if(!fileUploader.isUploadInProgress()) {
								// try starting file upload
								if(fileUploader.startFileUpload(u, filename, filesize)) {
									// file upload OK!
									code = RequestSendFile.SEND_APPROVED.getInt();
									msg = "File transfer started.";
								} else {
									// something went wrong trying to write to the disk
									code = RequestSendFile.FAILED_TO_START_SEND.getInt();
									msg = "Could not start file send (likely an error creating the file on the server).";
								}
							} else {
								code = RequestSendFile.ANOTHER_SEND_IN_PROGRESS.getInt();
								msg = "Another file send is currently in progress." +
									"Client must finish or cancel the current file send before starting another one.";
							}
						} else {
							code = RequestSendFile.BADLY_FORMATTED.getInt();
							msg = "The filesize argument is either not an int or is less than 0.";
						}
					} else {
						code = RequestSendFile.USER_DOESNT_EXIST.getInt();
						msg = "The specified user (or at least their file store) doesn't exist";
					}
				} else {
					code = RequestSendFile.BADLY_FORMATTED.getInt();
					msg = "Must have the format <username>,<file size>,<file name>";
				}
			} else {
				code = RequestSendFile.NOT_LOGGED_IN.getInt();
				msg = "Must login first";
			}
			return UnformattedPacket.CreateResponsePacket(
					MessageType.REQUEST_SEND_FILE.getInt(), code, msg);
			
		}
		case SEND_FILE_CHUNK: {
			int code = SendFileChunk.SEND_NOT_APPROVED.getInt();
			String msg = "";

			if (auth.authenticated()) {
				// check if upload is in progress
				if(fileUploader.isUploadInProgress()) {
					// get bytes
					byte[] payload = p.getPayload();
					
					try {
						fileUploader.uploadFileChunk(payload);
						// assume everything went OK
						code = SendFileChunk.RECEIVED_CHUNK.getInt();
						msg = "Transfered " + dformat.format(fileUploader.getUploadPercent()) + "%.";
						System.out.println(msg);
					} catch (IllegalArgumentException e) {
						// bytes send make file exceed size
						code = SendFileChunk.CHUNK_EXCEEDS_EXPECTED_SIZE.getInt();
						msg = "Received file chunk makes file exceed expected file size. File transfer cancelled.";
						// stop file transfer
						fileUploader.cancelUpload();
					} catch (IOException e) {
						// error writing to file
						code = SendFileChunk.IO_ERROR.getInt();
						msg = "Error writing data to file on server.";
					}
					
					// check if upload was complete
					if(fileUploader.isUploadComplete()) {
						//complete, so add file to db
						resource.sendFileToUser(
								fileUploader.getCurrentDestUsername(),
								auth.Username,
								fileUploader.getCurrentFilename(),
								fileUploader.getCurrentDBFilename(),
								fileUploader.getCurrentFileSize());
					}
				} else {
					// upload is not in progress
					code = SendFileChunk.SEND_NOT_APPROVED.getInt();
					msg = "File transfer has not been approved.";
				}
			} else {
				code = SendFileChunk.NOT_LOGGED_IN.getInt();
				msg = "Must login first";
			}
			return UnformattedPacket.CreateResponsePacket(
					MessageType.SEND_FILE_CHUNK.getInt(), code, msg);
		}
		case CANCEL_SEND_FILE: {
			
			break;
		}
		case REQUEST_RECEIVE_FILE: {
			
			break;
		}
		case RECEIVE_FILE_CHUNK: {
			
			break;
		}
		case CANCEL_RECEIVE_FILE: {
			
			break;
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
