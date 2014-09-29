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
    /// Ball class handling collisions and movements
    /// </summary>
    public partial class Ball : UserControl
    {
        public Ball()
        {
            InitializeComponent();
        }

        double radius = 8;
        /// <summary>
        /// Radius of the ball
        /// </summary>
        public double Radius { get { return radius; } set { radius = value; this.Width = radius; this.Height = radius; } }

        /// <summary>
        /// X position of the ball
        /// </summary>
        public double X { get { return (double)GetValue(Canvas.LeftProperty) + radius; } set { SetValue(Canvas.LeftProperty, value - radius); } }

        /// <summary>
        /// Y position of the ball
        /// </summary>
        public double Y { get { return (double)GetValue(Canvas.TopProperty) + radius; } set { SetValue(Canvas.TopProperty, value - radius); } }

        double direction = 0;
        /// <summary>
        /// Direction of the ball in radiant (0 -> 2*PI)
        /// </summary>
        public double Direction
        {
            get
            {
                return direction;
            }
            set
            {
                direction = value;
                direction = direction % (Math.PI * 2.0);
                while (direction < 0)
                    direction += Math.PI * 2;
            }
        }

        /// <summary>
        /// Ball speed
        /// </summary>
        public double Speed { get; set; }

        /// <summary>
        /// Ball horizontal velocity
        /// </summary>
        public double VX { get { return Math.Cos(Direction) * Speed; } }

        /// <summary>
        /// Ball vertical velocity
        /// </summary>
        public double VY { get { return Math.Sin(Direction) * Speed; } }

        /// <summary>
        /// Handle the ball movement and collisions
        /// </summary>
        /// <returns></returns>
        public bool Handle()
        {
            X += VX;
            Y += VY;

            bool rb = false;
            bool rt = false;
            bool lb = false;
            bool lt = false;

            // Check all the bricks for collision
            for (int i = 0; i < MainWindow.RunningWindow.Bricks.Count; i++)
            {
                Brick b = MainWindow.RunningWindow.Bricks[i];
                // It collides!
                if (b.Collide(this))
                {
                    if (this.X + Radius > b.X && this.Y + Radius > b.Y) // Right bottom
                    {
                        rb = true;
                    }
                    else if (this.X + Radius > b.X && this.Y - Radius < b.Y + b.Height) // Right top
                    {
                        rt = true;
                    }
                    else if (this.X - Radius < b.X + b.Width && this.Y + Radius > b.Y) // Left bottom
                    {
                        lb = true;
                    }
                    else if (this.X - Radius < b.X + b.Width && this.Y - Radius < b.Y + b.Height) // Left top
                    {
                        lt = true;
                    }
                    else if (this.X + Radius > b.X) // Right
                    {
                        rb = true;
                        rt = true;
                    }
                    else if (this.X - Radius < b.X + b.Width) // Left
                    {
                        lb = true;
                        lt = true;
                    }
                    else if (this.Y + Radius > b.Y) // Bottom
                    {
                        lb = true;
                        rb = true;
                    }
                    else if (this.Y - Radius < b.Y + b.Height) // Top
                    {
                        rt = true;
                        lt = true;
                    }
                    b.Destroy();
                }
            }

            // Vertical only collision
            if ((rt == true && lt == true && rb == false && rb == false) || (rt == false && lt == false && rb == true && rt == true))
            {
                Direction = 2 * Math.PI - Direction;
            }
            // Horizontal collision
            else if ((rt == true && rb == true && lt == false && lb == false) || (rt == false && rb == false && lt == true && lb == true))
            {
                Direction = Math.PI - Direction;
            }
            // Any other kind of collision (certainly not correct but it's ok for the moment)
            else if (rt == true || rb == true || lt == true || lb == true)
            {
                Direction = -Direction;
            }

            // Check the 4 walls
            if (X < radius) // Left
            {
                X = radius;
                Direction = Math.PI - Direction;
            }
            if (X > 300 - radius) // Right
            {
                X = 300 - radius;
                Direction = Math.PI - Direction;
            }
            if (Y < radius) // Top
            {
                Y = radius;
                Direction = 2 * Math.PI - Direction;
            }
            if (Y > 400 - radius) // Bottom => dead ball
            {
                return false;
            }

            // Check paddle collision
            if (Y > 345 - radius && Y < 355 - radius && X >= MainWindow.RunningWindow.PaddleX && X < MainWindow.RunningWindow.PaddleX + MainWindow.RunningWindow.PaddleWidth)
            {
                Y = 345 - radius;
                // For flat shaped paddle use this formula
                Direction = 2 * Math.PI - Direction;
            }

            return true;
        }
    }
}
