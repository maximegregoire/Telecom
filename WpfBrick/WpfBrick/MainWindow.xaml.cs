using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
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
using System.Windows.Media.Animation;
using System.Windows.Threading;
using System.Threading;

namespace WpfBrick
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        /// <summary>
        /// Currently running window
        /// </summary>
        public static MainWindow RunningWindow;

        public UdpClient receiver;
        public IPEndPoint groupEP;

        public const bool MaxIsAwesome = true;
        public const float SensorAngle = 40;
        public const int PaddleSpeed = 20;

        /// <summary>
        /// The main game look timer (will invoke gameLoop_Tick every 20 mili sec.)
        /// </summary>
        DispatcherTimer gameLoop = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(20) };

        /// <summary>
        /// Contains all the balls of the game (with this system we can handle multiple balls at the same time)
        /// </summary>
        List<Ball> balls = new List<Ball>();

        /// <summary>
        /// Points to a newly created ball linked to the paddle
        /// </summary>
        Ball newBall = null;

        /// <summary>
        /// List of all bricks of the game
        /// </summary>
        public List<Brick> Bricks = new List<Brick>();

        /// <summary>
        /// Contructor and initialize the game
        /// </summary>
        public MainWindow()
        {
            RunningWindow = this;
            InitializeComponent();

            int port = 7777;
            receiver = new UdpClient(port);
            groupEP = new IPEndPoint(IPAddress.Any, port);

            this.Loaded += new RoutedEventHandler(MainWindow_Loaded);
            gameLoop.Tick += new EventHandler(gameLoop_Tick);
            gameLoop.Start();

            var thread = new Thread(ReceiveData);
            thread.IsBackground = true;
            thread.Start();
        }

        /// <summary>
        /// Resize the game window once the window is loaded to accomodate the window borders.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        void MainWindow_Loaded(object sender, RoutedEventArgs e)
        {
            this.Width = 300 + (300 - container.ActualWidth);
            this.Height = 400 + (400 - container.ActualHeight);
        }

        /// <summary>
        /// Add bricks and clear all balls.
        /// </summary>
        void InitGame()
        {
            balls.Clear();
            for (int i = 0; i < 10; i++)
            {
                for (int j = 0; j < 5; j++)
                {
                    Brick b = new Brick { X = i * 30, Y = j * 15 };
                    layoutRoot.Children.Add(b);
                    Bricks.Add(b);
                }
            }
            PaddleX = 100;
        }

        /// <summary>
        /// Handle the balls. If the last ball is dead then create a new one attached to the paddle
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        void gameLoop_Tick(object sender, EventArgs e)
        {
            if (Bricks.Count == 0)
                InitGame();

            for (int i = 0; i < balls.Count; )
            {
                if (balls[i].Handle())
                    i++;
                else
                {
                    layoutRoot.Children.Remove(balls[i]);
                    balls.RemoveAt(i);
                }
            }

            if (balls.Count == 0)
            {
                if (newBall == null)
                {
                    newBall = new Ball { Direction = Math.PI * 2 - Math.PI / 4, Speed = 2 };
                    layoutRoot.Children.Add(newBall);
                }
                newBall.X = (double)paddle.GetValue(Canvas.LeftProperty) + paddle.Width / 2;
                newBall.Y = 337;
            }            
        }

        /// <summary>
        /// The paddle X position
        /// </summary>
        public double PaddleX
        {
            get
            {
                return (double)paddle.GetValue(Canvas.LeftProperty);
            }
            set
            {
                paddle.SetValue(Canvas.LeftProperty, value);
            }
        }

        public void ReceiveData()
        {
            while (MaxIsAwesome)
            {
                Byte[] receiveBytes = receiver.Receive(ref groupEP);
                sbyte orientation = (sbyte)receiveBytes[0];
                Console.Write(orientation + "\n");
                //string orientationString = returnData.Split()[4];
                //orientationString = orientationString.Substring(0, 6);
                //double orientation = double.Parse(orientationString, new System.Globalization.CultureInfo("en-US"));
                this.Dispatcher.Invoke((Action)(() =>
                {
                    if (orientation < -SensorAngle)
                    {
                        if (PaddleX <= 3)
                        {
                            PaddleX = 0;
                        }
                        else
                        {
                            PaddleX += (orientation + SensorAngle)/PaddleSpeed;
                        }
                    }
                    else if (orientation > SensorAngle)
                    {
                        if (PaddleX >= 247)
                        {
                            PaddleX = 250;
                        }
                        else
                        {
                            PaddleX += (orientation - SensorAngle) / PaddleSpeed;
                        }
                    }
                }));
                
            }
        }

        /// <summary>
        /// The padle Width (we can make it grow or shrink)
        /// </summary>
        public double PaddleWidth
        {
            get
            {
                return paddle.Width;
            }
            set
            {
                paddle.Width = value;
            }
        }

        /// <summary>
        /// Handle the paddle movement
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Window_MouseMove(object sender, MouseEventArgs e)
        {
            //Point p = e.GetPosition(this);
            //if (p.X < 0)
            //    PaddleX = 0;
            //else if (p.X > 300 - PaddleWidth)
            //    PaddleX = 300 - PaddleWidth;
            //else
            //    PaddleX = p.X;
        }
        
        //private void DataReceived(IAsyncResult ar)
        //{
        //    UdpClient c = (UdpClient)ar.AsyncState;
        //    IPEndPoint receivedIpEndPoint = new IPEndPoint(IPAddress.Any, 0);
        //    Byte[] receivedBytes = c.EndReceive(ar, ref receivedIpEndPoint);

        //    // Convert data to ASCII and print in console
        //    string receivedText = ASCIIEncoding.ASCII.GetString(receivedBytes);
            
        //    //Console.Write(receivedIpEndPoint + ": " + receivedText + Environment.NewLine);
            
        //}

        /// <summary>
        /// Handle the left click to fire the ball if a new ball is glued to the paddle
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void Window_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            if (newBall != null)
            {
                balls.Add(newBall);
                newBall = null;
            }
        }
    }
}
