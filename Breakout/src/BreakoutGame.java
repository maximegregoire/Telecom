// BreakoutGame.java

import ch.aplu.jgamegrid.*;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class BreakoutGame extends GameGrid implements GGMouseListener, GGActorCollisionListener
{
  private int points;
  private static final int TIMELIMIT = 120; //in seconds
  private static final int STARTSPEED = 40;
  private long startTime;
  private BreakerBar bar;
  private Breaker breaker;
  private boolean gameOver, startingStage;
  private static final Location startLocation = new Location(400, 463);
  private DatagramSocket socket;
  
  // Direction of the paddle, negative = left, positive = right
  private int directionIndicator = 0;
  
  // The UDP port (must be free)
  private static final int PORT_NUMBER = 7777;
  
  // Threshold that decides if the paddle should stay in place or more (between 1 and 126)
  private int directionThreshold = 30;
  
  // Time between thread ticks (in ms)
  private static final int TIME_BETWEEN_PADDLE_MOVE = 8;
  
  // Byte transmitted for the game to start (or restart)
  private static final byte STARTGAME_BYTE = -127;
  
  // Size of the collision paddle object (not linked to the displayed paddle, only used by the motor)
  // Should always be in sync with bar2.png width
  private static final int PADDLE_COLLISION_SIZE = 60;
  
  // The width of the level
  private static final int GAME_WIDTH = 800;
  
  
  public BreakoutGame()
  {
    super(GAME_WIDTH, 550, 1, false);
    
    try {
		socket = new DatagramSocket(PORT_NUMBER) ;
	} catch (SocketException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	Runnable direction = new Runnable() {
		public void run() {
			// Create a packet
		      DatagramPacket packet = new DatagramPacket( new byte[1], 1) ;

		      while(true)
		      {
		    	// Receive a packet (blocking)
			      try {
					socket.receive(packet) ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			      byte pkt = packet.getData()[0];
			      
			      if (pkt == STARTGAME_BYTE)
			      {
			    	  if (gameOver)
			          {
			            reset();
			          }
			          else if (startTime == 0)
			          { //start game
			            startTime = System.currentTimeMillis();
			            breaker.setActEnabled(true);
			            startingStage = false;
			          }
			      }
			      else if (startTime != 0)
			      {
				      if (pkt < -directionThreshold)
				      {
				    	  directionIndicator = -1;
				      }
				      else if (pkt > directionThreshold)
				      {
				    	  directionIndicator = 1;
				      }
				      else
				      {
				    	  directionIndicator = 0;
				      }
				      
				      System.out.println(packet.getData()[0] + "\n");
			      }
		      }
		      
		}
	};
	
	Runnable paddleMovement = new Runnable() {
		public void run() {
		      while(true)
		      {
		    	if (directionIndicator < 0)
		    	{		    	
		    		if (bar.getX() > PADDLE_COLLISION_SIZE/2)
		    		{
		    			bar.setX(bar.getX() - 1);
		    		}		    			
		    	}
		    	else if (directionIndicator > 0)
		    	{
		    		if (bar.getX() < GAME_WIDTH - PADDLE_COLLISION_SIZE/2)
		    		{
		    			bar.setX(bar.getX() + 1);
		    		}
		    	}	

					try 
					{
						Thread.sleep(TIME_BETWEEN_PADDLE_MOVE);
					} 
					catch (InterruptedException e) 
					{
						
					}
		      }		      
		}
	};
	
	
	
    getBg().setBgColor(Color.black);
    bar = new BreakerBar();
    
    
    Thread directionThread = new Thread(direction);
	directionThread.start();
	Thread movementThread = new Thread(paddleMovement);
	movementThread.start();
	
	
    bar.setCollisionRectangle(new Point(0, 0), PADDLE_COLLISION_SIZE, 15);
    breaker = new Breaker(this);
    breaker.setCollisionCircle(new Point(0, 0), 10);
    addActor(breaker, startLocation);
    breaker.addCollisionActor(bar);
    breaker.addActorCollisionListener(this);
    reset();
    setSimulationPeriod(STARTSPEED);
    for (int i = 1; i <= 3; i++)
    {
      LevelButton lvlBtn = new LevelButton(i);
      addActor(lvlBtn, new Location(i * 110 - 50, nbVertCells - 25));
      if (i == 1) //start with level 1
        lvlBtn.show(1);
    }
    addActor(bar, new Location(nbHorzCells / 2, nbVertCells - 70));
    addMouseListener(this, GGMouse.move | GGMouse.lClick);
    getBg().setPaintColor(Color.red);
    show();
    doRun();
  }

  public void act()
  {
    if (!(startingStage || gameOver))
    {
      getBg().clear();
      long timeLeft = (TIMELIMIT * 1000 + startTime - System.currentTimeMillis()) / 1000;
      if (timeLeft > 0)
        getBg().drawText(timeLeft + " seconds left", new Point(600, 540));
      else
        gameOver();
    }
  }

  public void reset()
  {
    setTitle("Network Breaker Game! Break as many bricks as possible in " + TIMELIMIT + " seconds");
    breaker.setActEnabled(false);
    getBg().clear();
    points = 0;
    startTime = 0;
    gameOver = false;
    startingStage = true;
    removeActors(Brick.class);
    breaker.setLocation(startLocation);
    bar.setLocation(new Location(nbHorzCells / 2, nbVertCells - 70));
    breaker.setDirection((Math.random() * 120 - 60) + 270); //upwards, but random
    for (int j = 0; j < 10; j++)
    {
      for (int i = 0; i < 13; i++)
      {
        if (Math.random() < 0.85)
        { 
          Brick brick = new Brick(j / 3);
          brick.setCollisionRectangle(new Point(0, 0), 50, 20);
          breaker.addCollisionActor(brick);
          addActor(brick, new Location(i * 51 + 90, 21 * j + 60));
        }
      }
    }
  }

  public boolean mouseEvent(GGMouse mouse)
  {
    switch (mouse.getEvent())
    {
      case GGMouse.lClick:
        if (gameOver)
        {
          reset();
        }
        else if (startTime == 0)
        { //start game
          startTime = System.currentTimeMillis();
          breaker.setActEnabled(true);
          startingStage = false;
        }
        break;

      case GGMouse.move:
        //bar.setX(mouse.getX());
        //if (startingStage)
        //  breaker.setX(mouse.getX());
        break;
    }
    return true;
  }
  

  public int collide(Actor actor1, Actor actor2)
  {
    double dir = actor1.getDirection();
    //how did they hit each other?
    double hitDirection = actor2.getLocation().getDirectionTo(actor1.getLocation());
    if (actor2.getClass().equals(Brick.class))
    { //hit brick
      actor1.setDirection(reflectPhysicallyCorrect(actor1.getLocation(), actor2.getLocation(), dir));
      points += 10;
      if (getActors(Brick.class).size() == 0)
        gameOver();
      else
        setTitle("Breaker-Game   Points: " + points);
      removeActor(actor2);
      return 0; //be immediately ready for next collision
    }
    else
    { //hit pad
      if (actor1.getY() <= actor2.getY()) // breaker has to be higher than bar
        actor1.setDirection(reflectWithHitZones(actor1.getLocation(), actor2.getLocation()));
      return 10; //don't hit for another 10 cycles
    }
  }

  private double reflectWithHitZones(Location loc1, Location loc2)
  {
    int distance = loc1.x - loc2.x;
    int dir = (int)(distance * 2.5);
    System.out.println(distance + " -> winkel: " + dir);
    return dir - 90;
    //TODO: what if exactly 90°
  }

  private double reflectPhysicallyCorrect(Location loc1, Location loc2, double dir)
  {
    int xMovement = loc2.x - loc1.x;
    int yMovement = loc2.y - loc1.y;
    if (Math.abs(xMovement) <= 25)
      dir = 360 - dir;
    else if (Math.abs(yMovement) <= 10)
      dir = 180 - dir;
    else
      System.out.println("omg, none of the cases!");
    return dir;
  }

  public void gameOver()
  {
    gameOver = true;
    breaker.setActEnabled(false);
    long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
    setTitle("Game Over! " + points + " points " + " in " + elapsedTime + " seconds. Reset by clicking.");
  }

  public static void main(String[] args)
  {
    new BreakoutGame();
  }
}
