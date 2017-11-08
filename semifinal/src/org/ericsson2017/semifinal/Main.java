package org.ericsson2017.semifinal;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import org.capnproto.MessageBuilder;
import org.capnproto.Serialize;
import org.ericsson2017.protocol.semifinal.CommandClass;
import org.ericsson2017.protocol.semifinal.CommonClass;
import org.ericsson2017.protocol.semifinal.ResponseClass;

public class Main {
    public static final String HASH="r6mgylzcdd7vtrq5ewpw1zoopj04lgqrwor";
    public static final String HOST="ecovpn.dyndns.org";
    public static final int PORT=11224;
    public static final String TEAM="norbik";

    private final SocketChannel channel;

    public Main(SocketChannel channel) {
        this.channel=channel;
    }

    private void login() throws Throwable {
            MessageBuilder messageBuilder=new MessageBuilder();
            CommandClass.Command.Builder request
                            =messageBuilder.initRoot(CommandClass.Command.factory);
            CommandClass.Command.Login.Builder login=request.initLogin();
            login.setHash(HASH);
            login.setTeam(TEAM);
            Serialize.write(channel, messageBuilder);
            System.out.println("sent: login");
    }

    public void main() throws Throwable { 
            login();
            System.out.println("Logined");
            print(response());
    }
	
    public static void main(String[] args) throws Throwable {
        try (SocketChannel channel=SocketChannel.open()) {
            channel.connect(new InetSocketAddress(HOST, PORT));
            new Main(channel).main();
        }
    }
	
    private ResponseClass.Response.Reader print(
                    ResponseClass.Response.Reader response) {
        System.out.println("Response which");
        System.out.println(response.getStatus().toString());
        
        for(int sl=0; sl<response.getCells().size(); sl++) {
            for(int i=0; i<response.getCells().get(sl).size(); i++) {
                System.out.print(response.getCells().get(sl).get(i).getOwner());
            }
            System.out.println();
        }
        
        for(int e = 0; e<response.getEnemies().size(); e++) {
            System.out.println(response.getEnemies().get(e).getPosition().getX() 
                    + ":" 
                    + response.getEnemies().get(e).getPosition().getY() 
                    + " -> "
                    + response.getEnemies().get(e).getDirection().getHorizontal()
                    + " - "
                    + response.getEnemies().get(e).getDirection().getVertical());
        }
        
        /*
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
*/
        return response;
    }
	
    private ResponseClass.Response.Reader response() throws Throwable {
            return Serialize.read(channel).getRoot(ResponseClass.Response.factory);
    }
}
