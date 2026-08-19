using System;
using System.IO;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;

namespace LinkXRemoteHost.UI
{
    public partial class WebcamWindow : Window
    {
        private bool _isFullScreen = false;
        private WindowState _previousState;
        private WindowStyle _previousStyle;
        private Rect _previousBounds;

        public WebcamWindow()
        {
            InitializeComponent();
        }

        public void RenderFrame(byte[] jpegData)
        {
            try
            {
                Dispatcher.Invoke(() =>
                {
                    using (var ms = new MemoryStream(jpegData))
                    {
                        var bitmap = new BitmapImage();
                        bitmap.BeginInit();
                        bitmap.CacheOption = BitmapCacheOption.OnLoad;
                        bitmap.StreamSource = ms;
                        bitmap.EndInit();
                        bitmap.Freeze(); // Make it thread-safe
                        
                        WebcamImage.Source = bitmap;
                    }
                });
            }
            catch (Exception)
            {
                // Ignore rendering exceptions (e.g. invalid partial frame)
            }
        }

        private void Window_MouseDown(object sender, MouseButtonEventArgs e)
        {
            if (e.ChangedButton == MouseButton.Left)
                DragMove();
        }

        private void CloseButton_Click(object sender, RoutedEventArgs e)
        {
            this.Hide();
        }

        private void Window_MouseDoubleClick(object sender, MouseButtonEventArgs e)
        {
            ToggleFullScreen();
        }

        private void ToggleFullScreen()
        {
            if (!_isFullScreen)
            {
                _previousState = WindowState;
                _previousStyle = WindowStyle;
                _previousBounds = RestoreBounds;

                WindowStyle = WindowStyle.None;
                WindowState = WindowState.Maximized;
                _isFullScreen = true;
            }
            else
            {
                WindowStyle = _previousStyle;
                WindowState = _previousState;
                _isFullScreen = false;
            }
        }
    }
}
