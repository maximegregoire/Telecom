// Breaker.java

import ch.aplu.jgamegrid.*;

public class Breaker extends Actor
{
  private double speed;
  private BreakoutGame gg;

  public Breaker(BreakoutGame gg)
  {
    super("sprites/breaker.png");
    this.gg = gg;
  }

  public void act()
  {
    double dir = getDirection();
    if (getY() > this.gameGrid.nbVertCells - 60)
    { //breaker has dropped
      gg.gameOver();
    }
    //collision with wall:
    if (getX() < 10)
    {
      dir = 180 - dir;
    }
    if (getX() > 790)
    {
      dir = 180 - dir;
    }
    if (getY() < 10)
    {
      dir = 360 - dir;
    }
    setDirection(dir);
    move();
  }
}
