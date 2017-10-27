package org.ericsson2017.yyes;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import org.capnproto.MessageBuilder;
import org.capnproto.Serialize;
import org.ericsson2017.protocol.test.BugfixClass;
import org.ericsson2017.protocol.test.RequestClass;
import org.ericsson2017.protocol.test.ResponseClass;

public class Main {
	public static final String HASH="r6mgylzcdd7vtrq5ewpw1zoopj04lgqrwor";
	public static final String HOST="ecovpn.dyndns.org";
	public static final int PORT=11223;
	public static final String TEAM="norbik";
	
	private final SocketChannel channel;
	
	public Main(SocketChannel channel) {
		this.channel=channel;
	}
	
	private void assertEnd(boolean end,
			ResponseClass.Response.Reader response) {
		if (end) {
			if (!response.isEnd()) {
				throw new IllegalArgumentException("response is not an end");
			}
			if (!response.getEnd()) {
				throw new IllegalArgumentException(
						"response is an end but end is false");
			}
		}
		else {
			if (response.isEnd()
					&& response.getEnd()) {
				throw new IllegalArgumentException("response is an end");
			}
		}
	}
	
	private void bugfix(int bugs, String message) throws Throwable {
		MessageBuilder messageBuilder=new MessageBuilder();
		RequestClass.Request.Builder request
				=messageBuilder.initRoot(RequestClass.Request.factory);
		BugfixClass.Bugfix.Builder bugfix=request.initBugfix();
		bugfix.setBugs((byte)bugs);
		bugfix.setMessage(message);
		Serialize.write(channel, messageBuilder);
		System.out.println("sent: bugfix "+bugs+" - "+message);
	}
	
	private void login() throws Throwable {
		MessageBuilder messageBuilder=new MessageBuilder();
		RequestClass.Request.Builder request
				=messageBuilder.initRoot(RequestClass.Request.factory);
		RequestClass.Request.Login.Builder login=request.initLogin();
		login.setHash(HASH);
		login.setTeam(TEAM);
		Serialize.write(channel, messageBuilder);
		System.out.println("sent: login");
	}
	
	public void main() throws Throwable { 
		login();
		assertEnd(true, print(response()));
		bugfix(9, "Fixed");
		assertEnd(true, print(response()));
		bugfix(8, "Fixed");
		assertEnd(true, print(response()));
		bugfix(19, "Fixed");
		assertEnd(true, print(response()));
		bugfix(18, "Fixed");
		assertEnd(true, print(response()));
		bugfix(17, "Fixed");
		assertEnd(true, print(response()));
		bugfix(16, "Fixed");
		assertEnd(true, print(response()));
		bugfix(2, "Fixed");
		assertEnd(true, print(response()));
		bugfix(2, "Fixed");
		assertEnd(true, print(response()));
		bugfix(2, "Fixed");
		assertEnd(true, print(response()));
		bugfix(1, "Fixed");
		assertEnd(true, print(response()));
		bugfix(0, "Fixed");
		assertEnd(true, print(response()));
		bugfix(-1, "Fixed");
		assertEnd(true, print(response()));
		bugfix(12, "I solved a huge amount of bug. I am proud of myself.");
		assertEnd(true, print(response()));
	}
	
    public static void main(String[] args) throws Throwable {
		try (SocketChannel channel=SocketChannel.open()) {
			channel.connect(new InetSocketAddress(HOST, PORT));
			new Main(channel).main();
		}
    }
	
	private ResponseClass.Response.Reader print(
			ResponseClass.Response.Reader response) {
		switch (response.which()) {
			case BUGFIX:
				System.out.println(String.format(
						"bugfix response: %1$s - %2$s - %3$s",
						response.getStatus(),
						response.getBugfix().getBugs(),
						response.getBugfix().getMessage()));
				break;
			case END:
				System.out.println(String.format(
						"end response: %1$s - %2$s",
						response.getStatus(),
						response.getEnd()));
				break;
			default:
				System.out.println(String.format(
						"unknown response: %1$s - %2$s",
						response.getStatus(),
						response.which()));
				break;
		}
		return response;
	}
	
	private ResponseClass.Response.Reader response() throws Throwable {
		return Serialize.read(channel).getRoot(ResponseClass.Response.factory);
	}
}
