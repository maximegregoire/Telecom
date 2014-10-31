// LevelButton.java

import java.awt.Point;
import ch.aplu.jgamegrid.*;

public class LevelButton extends Actor implements GGMouseTouchListener
{
  private int level;

  public LevelButton(int level)
  {
    super("sprites/Level" + level + ".png", 2);
    this.level = level;
    setMouseTouchImage();
    this.addMouseTouchListener(this, GGMouse.lClick);
  }

  public void mouseTouched(Actor actor1, GGMouse arg1, Point arg2)
  {
    for (Actor act : gameGrid.getActors(LevelButton.class)) //set all buttons to gray
      act.show(0);
    this.show(1);
    switch (level)  //change speed
    {
      case 1:
        gameGrid.setSimulationPeriod(35);
        break;
      case 2:
        gameGrid.setSimulationPeriod(25);
        break;
      case 3:
        gameGrid.setSimulationPeriod(15);
        break;
    }
  }
}
