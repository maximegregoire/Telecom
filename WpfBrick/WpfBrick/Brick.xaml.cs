using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace WpfBrick
{
    /// <summary>
    /// Brick class which knows when a ball collides or not
    /// </summary>
    public partial class Brick : UserControl
    {
        public Brick()
        {
            InitializeComponent();
        }

        /// <summary>
        /// X position of the top left corner
        /// </summary>
        public double X { get { return (double)GetValue(Canvas.LeftProperty); } set { SetValue(Canvas.LeftProperty, value); } }

        /// <summary>
        /// Y position of the top left corner
        /// </summary>
        public double Y { get { return (double)GetValue(Canvas.TopProperty); } set { SetValue(Canvas.TopProperty, value); } }

        /// <summary>
        /// Checks if the ball collide with the brick (the ball will be handled as a square for simplicity)
        /// </summary>
        /// <param name="ball"></param>
        /// <returns></returns>
        public bool Collide(Ball ball)
        {
            if (!(ball.X + ball.Radius < X || ball.X - ball.Radius > X + this.Width || ball.Y + ball.Radius < Y || ball.Y - ball.Radius > Y + this.Height))
                return true;
            return false;
        }

        /// <summary>
        /// Remove the brick from the game
        /// </summary>
        public void Destroy()
        {
            ((Canvas)this.Parent).Children.Remove(this);
            MainWindow.RunningWindow.Bricks.Remove(this);
        }
    }
}
