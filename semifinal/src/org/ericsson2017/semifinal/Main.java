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
    
    private void move(CommonClass.Direction dir) {
        try {
            MessageBuilder messageBuilder=new MessageBuilder();
            CommandClass.Command.Builder request
                            =messageBuilder.initRoot(CommandClass.Command.factory);
            org.capnproto.StructList.Builder<CommandClass.Move.Builder> command = request.initMoves(1);

            command.get(0).setUnit(0);
            command.get(0).setDirection(dir);

            Serialize.write(channel, messageBuilder);
            System.out.println("sent: move(" + dir.name() + ")");
            
            print(response());
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void main() throws Throwable { 
        login();
        System.out.println("Logined");
        print(response());
        
        move(CommonClass.Direction.RIGHT);
        move(CommonClass.Direction.RIGHT);
        move(CommonClass.Direction.RIGHT);
        move(CommonClass.Direction.DOWN);
        move(CommonClass.Direction.DOWN);
        move(CommonClass.Direction.LEFT);
        move(CommonClass.Direction.LEFT);
        
    }
	
    public static void main(String[] args) throws Throwable {
        try (SocketChannel channel=SocketChannel.open()) {
            channel.connect(new InetSocketAddress(HOST, PORT));
            new Main(channel).main();
        }
    }
    
    private void printStatus(ResponseClass.Response.Reader response) {
        System.out.println("");
        System.out.println("*************");
        System.out.println("STATUS REPORT");
        System.out.println("*************");
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
        
        System.out.println("Unit owner: " + response.getUnits().get(0).getOwner());
        System.out.println("Unit health: " + response.getUnits().get(0).getHealth());
        System.out.println("Unit position: " + response.getUnits().get(0).getPosition().getX() + ":" + response.getUnits().get(0).getPosition().getY());

    }
	
    private ResponseClass.Response.Reader print(
                    ResponseClass.Response.Reader response) {
        
        printStatus(response);
        
        return response;
    }
	
    private ResponseClass.Response.Reader response() throws Throwable {
            return Serialize.read(channel).getRoot(ResponseClass.Response.factory);
    }
}
