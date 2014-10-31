// Brick.java

import ch.aplu.jgamegrid.Actor;

public class Brick extends Actor
{
  public Brick(int sprite)
  {
    super("sprites/brickS.png", 4);
    show(sprite);
  }
}
