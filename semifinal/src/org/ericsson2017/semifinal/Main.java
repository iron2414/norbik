package org.ericsson2017.semifinal;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.capnproto.MessageBuilder;
import org.capnproto.SerializePacked;
import org.ericsson2017.protocol.semifinal.CommandClass;
import org.ericsson2017.protocol.semifinal.CommonClass;
import org.ericsson2017.protocol.semifinal.ResponseClass;

public class Main {
	private static class Canvas extends JPanel {
		private static final long serialVersionUID=0l;
		
		private final AtomicReference<Prerendered> prerendered
                =new AtomicReference<>();
		
		@Override
		protected void paintComponent(Graphics graphics) {
			try {
                Prerendered prerendered2=prerendered.get();
				ResponseRenderer.render((Graphics2D)graphics, getWidth(),
						getHeight(),
                        (null==prerendered2)?null:prerendered2.ball,
                        (null==prerendered2)?null:prerendered2.response);
			}
			catch (Throwable throwable) {
				throwable.printStackTrace(System.err);
				for (Container container=this.getParent();
						null!=container;
						container=container.getParent()) {
					if (container instanceof Frame) {
						((Frame)container).dispose();
					}
				}
			}
		}
		
		public void render(Prerendered prerendered) {
			this.prerendered.set(prerendered);
			SwingUtilities.invokeLater(this::repaint);
		}
	}
    
    private class KeyboardListener implements KeyListener {
		private volatile CommonClass.Direction direction
				=CommonClass.Direction.RIGHT;
        
        @Override
        public void keyTyped(KeyEvent event) {
        }
        
        @Override
        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_DOWN:
					direction=CommonClass.Direction.DOWN;
                    break;
                case KeyEvent.VK_LEFT:
					direction=CommonClass.Direction.LEFT;
                    break;
                case KeyEvent.VK_RIGHT:
					direction=CommonClass.Direction.RIGHT;
                    break;
                case KeyEvent.VK_UP:
					direction=CommonClass.Direction.UP;
                    break;
            }
        }
        
        @Override
        public void keyReleased(KeyEvent event) {
		}
    }
    
    private class Prerender implements Runnable {
		private final Object lock=new Object();
        private ServerResponseParser response;
        
        public void prerender(ServerResponseParser response) {
            synchronized (lock) {
                this.response=response;
                lock.notify();
            }
        }
        
        @Override
        public void run() {
            try {
                while (true) {
                    ServerResponseParser response2;
                    synchronized (lock) {
                        while (null==response) {
                            lock.wait();
                        }
                        response2=response;
                        response=null;
                    }
                    CrystalBall ball=new CrystalBall();
                    ball.reset(response2, 20);
                    ball.addEnemies(response2.enemies);
                    canvas.render(new Prerendered(ball, response2));
                }
            }
            catch (InterruptedException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }
    
    private class Prerendered {
        public final CrystalBall ball;
        public final ServerResponseParser response;
        
        public Prerendered(CrystalBall ball, ServerResponseParser response) {
            this.ball=ball;
            this.response=response;
        }
    }
	
    public static final String HASH="r6mgylzcdd7vtrq5ewpw1zoopj04lgqrwor";
    //public static final String HOST="ecovpn.dyndns.org";
    public static final String HOST="epb2017.dyndns.org";
    public static final int PORT=11224;
    public static final String TEAM="norbik";

    private final Canvas canvas;
    private final SocketChannel channel;
	private final JFrame frame;
    private final KeyboardListener keyboardListener=new KeyboardListener();
	private final Prerender prerender=new Prerender();

    public Main(SocketChannel channel) {
        this.channel=channel;
		frame=new JFrame("Semifinal");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
        frame.addKeyListener(keyboardListener);
		canvas=new Canvas();
        canvas.addKeyListener(keyboardListener);
		frame.getContentPane().add(canvas, BorderLayout.CENTER);
		frame.setBounds(0, 0, 800, 600);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    }

    private void login() throws Throwable {
        MessageBuilder messageBuilder=new MessageBuilder();
        CommandClass.Command.Builder request
                        =messageBuilder.initRoot(CommandClass.Command.factory);
        CommandClass.Command.Commands.Login.Builder login=request.initCommands().initLogin();
        login.setHash(HASH);
        login.setTeam(TEAM);
        org.capnproto.SerializePacked.writeToUnbuffered(channel, messageBuilder);
        //Serialize.write(channel, messageBuilder);
        System.out.println("sent: login");
    }
    
    private void move(CommonClass.Direction dir) throws Throwable {
		MessageBuilder messageBuilder=new MessageBuilder();
		CommandClass.Command.Builder request
						=messageBuilder.initRoot(CommandClass.Command.factory);
		org.capnproto.StructList.Builder<CommandClass.Move.Builder> command = request.initCommands().initMoves(1);

		command.get(0).setUnit(0);
		command.get(0).setDirection(dir);

                org.capnproto.SerializePacked.writeToUnbuffered(channel, messageBuilder);
		//Serialize.write(channel, messageBuilder);
		System.out.println("sent: move(" + dir.name() + ")");
    }

    public void main() throws Throwable 
    {     
        
        boolean isKeyboard = true;
        SimManager simManager;
        Tuple<List<CommonClass.Direction>, Double> stepListWithProb;
        int health;
        
        Thread prerenderer=new Thread(prerender);
        prerenderer.setDaemon(true);
        prerenderer.start();
        
        frame.setVisible(true);
        try {
            login();
            System.out.println("Logged in");

            ResponseClass.Response.Reader response=response();
            simManager = new SimManager(response);
            
            if(!isKeyboard)
            {
                print(response, simManager.serverResponseParser.copy());
                boolean winArea = true;

                while ((health = response.getUnits().get(0).getHealth())>0) {
                    // keress egy viszonylag nagy területnyereséggel kecsegtető
                    // viszonylag nagy valószínűséggel bejárható útvonalat!
                    stepListWithProb = simManager.findPath(winArea);
                    if (stepListWithProb == null) {
                        System.out.println("*** CANNOT FIND PATH!!! ***\nUsing DOWN step - what else?");
                        List<CommonClass.Direction> dirList = new ArrayList<>();
                        move(CommonClass.Direction.DOWN);   // menj egyet le és utána is majd egyet le
                        dirList.add(CommonClass.Direction.DOWN);

                        stepListWithProb = new Tuple<>(dirList, 100.0);
                    }
                    List<CommonClass.Direction> stepList = stepListWithProb.first;
                    System.out.println("Try steps: "+stepList.toString()+"\nProbability: "+stepListWithProb.second+"\n----------------------\n");

                    // kezdjük el végigjárni az ajánlott utat
                    for(int i=0; i<stepList.size(); ++i) {
                        move(stepList.get(i));
                        response=response();
                        if (response.getUnits().get(0).getHealth() < health) {
                            System.out.println("***** DIE *****");
                            printCells(response);
                            System.out.println("***** DIE *****");
                            break;
                        }

                        // futtassuk újra a szimulációt, ellenőrizzük az ütközés valószínűségét
                        // és ha szükséges, keressünk menekülő útvonalat!
                        simManager.setResponse(response);
                                            print(response, simManager.serverResponseParser.copy());
                        stepList = simManager.checkPath(stepList, i);
                    }

                    simManager.setResponse(response);

                    // elvileg most bejártuk a tervezett vagy a menekülő  útvonalat
                    // ellenőrizzük, hogy kaptunk-e területet
                    if (stepList.size()>0) {
                        winArea = simManager.hasWinArea(stepList.get(stepList.size()-1));
                    } else {
                        winArea = false;
                    }
                    System.out.println("*** Planned stepList successfully stepped ***");
                    System.out.println(winArea ? ":-) WIN new area " : ":-( not win new area ");
                }
            } else {
                List<String> kbDirList = new ArrayList<>();
                Path file;
                
                CommonClass.Direction keyboardDirection = keyboardListener.direction;
                int keyboardHealth = response.getUnits().get(0).getHealth();
                int keyboardLevel = response.getInfo().getLevel();
                
                // Try to load levels from file
                while (true) {
                    try {
                        file = Paths.get("xonix_"+keyboardLevel+".kbd");
                        kbDirList = Files.readAllLines(file);
                        for(String dirStr : kbDirList) {
                            CommonClass.Direction dir = strToDir(dirStr);
                            if (dir != null) {
                                move(dir);
                                response=response();
                                simManager.setResponse(response);
                                print(response, simManager.serverResponseParser.copy());
                                //Thread.sleep(150);
                            }
                        }
                        keyboardLevel++;
                    } catch (NoSuchFileException e) {
                        break;
                    }
                }
                kbDirList.clear();
                
                /* 
                
                // Az összes ellenég mozgását fájlokba írja miközben a unit a pálya szélén zizeg
                List<List<Coord>> enemiesPosList = new ArrayList<>();
                for(Enemy enemy : simManager.serverResponseParser.enemies) {
                    List<Coord> posList = new ArrayList<>();
                    enemiesPosList.add(posList);
                }
                
                keyboardLevel++;
                CommonClass.Direction dir;
                int i;
                try {
                    while(true) {
                        dir = CommonClass.Direction.DOWN;
                        dir = simManager.checkMove(dir);
                        move(dir);
                        response=response();

                        if (response.getUnits().get(0).getHealth() == 0) {
                            i = 0;
                            for(List<Coord> posList : enemiesPosList) {
                                file = Paths.get("xonix_"+keyboardLevel+"_enemy_"+i+".pos");
                                Files.write(file, posList.toString().getBytes());
                                i++;
                            }
                            break;
                        }

                        simManager.setResponse(response);
                        print(response, simManager.serverResponseParser.copy());

                        i = 0;
                        for(Enemy enemy : simManager.serverResponseParser.enemies) {
                            List<Coord> posList = enemiesPosList.get(i);
                            posList.add(enemy.coord);
                            i++;
                        }
                    }
                } catch (Throwable e) {
                    i = 0;
                    for(List<Coord> posList : enemiesPosList) {
                        file = Paths.get("xonix_"+keyboardLevel+"_enemy_"+i+".pos");
                        Files.write(file, posList.toString().getBytes());
                        i++;
                    }
                }
                */    

                // no more level files - use your hand!
                while(true)
                {
                    Thread.sleep(150);
                    simManager.setResponse(response);
                    print(response, simManager.serverResponseParser.copy());
					keyboardDirection=keyboardListener.direction;
                    int newLevel = response.getInfo().getLevel();
                    if (keyboardLevel != newLevel) {
                        file = Paths.get("xonix_"+keyboardLevel+".kbd");
                        Files.write(file, kbDirList);
                        kbDirList.clear();

                        keyboardLevel = newLevel;
                        keyboardHealth = response.getUnits().get(0).getHealth();
                        keyboardDirection = CommonClass.Direction.RIGHT;
                    } else if(keyboardHealth < response.getUnits().get(0).getHealth() ) {
                        //kbDirList.add("-");
                        keyboardDirection = CommonClass.Direction.RIGHT;
                        keyboardHealth--;
                        if (keyboardHealth == 0) {
                            break;
                        }
                    }
                    keyboardDirection = simManager.checkMove(keyboardDirection);
                    kbDirList.add(dirToStr(keyboardDirection));
                    move(keyboardDirection);
                    response=response();
                }
            }
        } finally {
            frame.dispose();
        }
    }
	
    public static void main(String[] args) throws Throwable {
        try (SocketChannel channel=SocketChannel.open()) {
            channel.connect(new InetSocketAddress(HOST, PORT));
            new Main(channel).main();
        }
    }
    
    private void printCells(ResponseClass.Response.Reader response) {
        for(int sl=0; sl<response.getCells().size(); sl++) {
            for(int i=0; i<response.getCells().get(sl).size(); i++) {
                System.out.print(response.getCells().get(sl).get(i).getOwner());
            }
            System.out.println();
        }
        
        System.out.println();
        System.out.println(response.getStatus().toString());
        System.out.println("Tick: " + response.getInfo().getTick());
    }
    
    private void printStatus(ResponseClass.Response.Reader response) {
        if (response.getInfo().getLevel()<2) return;
        System.out.println("");
        System.out.println("*************");
        System.out.println("STATUS REPORT");
        System.out.println("*************");
        System.out.println(response.getStatus().toString());
        
        for(int e = 0; e<response.getEnemies().size(); e++) {
            System.out.println("ENEMY "+e+": " + response.getEnemies().get(e).getPosition().getX() 
                    + ":" 
                    + response.getEnemies().get(e).getPosition().getY() 
                    + " -> "
                    + response.getEnemies().get(e).getDirection().getHorizontal()
                    + "-"
                    + response.getEnemies().get(e).getDirection().getVertical());
        }
        
        System.out.println("Unit owner: " + response.getUnits().get(0).getOwner());
        System.out.println("Unit health: " + response.getUnits().get(0).getHealth());
        System.out.println("Unit position: " + response.getUnits().get(0).getPosition().getX() + ":" + response.getUnits().get(0).getPosition().getY());
        
    }
	
    private ResponseClass.Response.Reader print(
			ResponseClass.Response.Reader response,
			ServerResponseParser response2) {
		if (!frame.isDisplayable()) {
			throw new RuntimeException("frame closed");
		}
        prerender.prerender(response2.copy());
        printStatus(response);
        return response;
    }
	
    private ResponseClass.Response.Reader response() throws Throwable {
        return SerializePacked.readFromUnbuffered(channel).getRoot(ResponseClass.Response.factory);
		//return Serialize.read(channel).getRoot(ResponseClass.Response.factory);
    }
    
    private String dirToStr(CommonClass.Direction dir) {
        String result = "";
        switch (dir) {
            case UP:
                result = "U";
                break;
            case DOWN:
                result = "D";
                break;
            case RIGHT:
                result = "R";
                break;
            case LEFT:
                result = "L";
                break;
        }
        
        return result;
    }
    
    private CommonClass.Direction strToDir(String dirStr) {
        CommonClass.Direction result = null;
        switch (dirStr) {
            case "U":
                result = CommonClass.Direction.UP;
                break;
            case "D":
                result = CommonClass.Direction.DOWN;
                break;
            case "R":
                result = CommonClass.Direction.RIGHT;
                break;
            case "L":
                result = CommonClass.Direction.LEFT;
                break;
        }
        
        return result;
    }
}
