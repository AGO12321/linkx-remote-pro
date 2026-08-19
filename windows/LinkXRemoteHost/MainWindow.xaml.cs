using System;
using System.Net;
using System.Net.Sockets;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Interop;
using System.Windows.Media.Imaging;
using QRCoder;
using System.Drawing;
using Newtonsoft.Json;

namespace LinkXRemoteHost
{
    public partial class MainWindow : Window
    {
        [System.Runtime.InteropServices.DllImport("gdi32.dll")]
        private static extern bool DeleteObject(IntPtr hObject);

        private bool isServerRunning = false;
        
        private LinkXRemoteHost.Network.TcpServer _tcpServer;
        private LinkXRemoteHost.Network.UdpServer _udpServer;
        private LinkXRemoteHost.Network.LanDiscoveryService _discoveryService;
        private LinkXRemoteHost.Security.CryptoProvider _crypto;
        private LinkXRemoteHost.Services.ScreenCaptureService _screenCapture;
        private LinkXRemoteHost.Services.AudioCaptureService _audioCapture;

        public MainWindow()
        {
            InitializeComponent();
            _crypto = new LinkXRemoteHost.Security.CryptoProvider();
            
            _tcpServer = new LinkXRemoteHost.Network.TcpServer(this, _crypto);
            _udpServer = new LinkXRemoteHost.Network.UdpServer(this, _crypto);
            _discoveryService = new LinkXRemoteHost.Network.LanDiscoveryService(this);
            _screenCapture = new LinkXRemoteHost.Services.ScreenCaptureService();
            _audioCapture = new LinkXRemoteHost.Services.AudioCaptureService(async (audioData) =>
            {
                var packet = new LinkXRemoteHost.Protocol.BinaryPacket
                {
                    Type = LinkXRemoteHost.Protocol.PacketType.AudioFrame,
                    Flags = LinkXRemoteHost.Protocol.PacketFlags.Encrypted,
                    Payload = audioData.Payload
                };
                await _udpServer.SendPacketAsync(packet);
            });

            _screenCapture.OnFrameCaptured += async (frameData) =>
            {
                // Send frame over UDP
                var packet = new LinkXRemoteHost.Protocol.BinaryPacket
                {
                    Type = LinkXRemoteHost.Protocol.PacketType.ScreenFrame,
                    Flags = LinkXRemoteHost.Protocol.PacketFlags.Encrypted,
                    Payload = frameData
                };
                await _udpServer.SendPacketAsync(packet);
            };

            // _audioCapture event logic moved to constructor assignment.
            
            LoadLocalIp();
            GeneratePin();
            GenerateQrCode();
        }

        private void GenerateQrCode()
        {
            try
            {
                var payload = new
                {
                    serverName = Environment.MachineName,
                    ip = IpAddressText.Text,
                    port = 8080,
                    pin = PinText.Text
                };
                string json = JsonConvert.SerializeObject(payload);

                using (var qrGenerator = new QRCodeGenerator())
                using (var qrCodeData = qrGenerator.CreateQrCode(json, QRCodeGenerator.ECCLevel.Q))
                using (var qrCode = new QRCode(qrCodeData))
                using (Bitmap qrBitmap = qrCode.GetGraphic(20, System.Drawing.Color.White, System.Drawing.Color.Transparent, true))
                {
                    var hBitmap = qrBitmap.GetHbitmap();
                    var source = Imaging.CreateBitmapSourceFromHBitmap(
                        hBitmap,
                        IntPtr.Zero,
                        Int32Rect.Empty,
                        BitmapSizeOptions.FromEmptyOptions());
                    
                    QrCodeImage.Source = source;
                    DeleteObject(hBitmap);
                }
            }
            catch (Exception ex)
            {
                LogMessage("QR Code Error: " + ex.Message);
            }
        }

        private void TitleBar_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            if (e.ClickCount == 2)
            {
                if (ResizeMode != ResizeMode.NoResize)
                {
                    WindowState = WindowState == WindowState.Maximized ? WindowState.Normal : WindowState.Maximized;
                }
            }
            else
            {
                DragMove();
            }
        }
        
        private void Minimize_Click(object sender, RoutedEventArgs e)
        {
            WindowState = WindowState.Minimized;
        }
        
        private void Close_Click(object sender, RoutedEventArgs e)
        {
            _tcpServer.Stop();
            _udpServer.Stop();
            _discoveryService.Stop();
            Application.Current.Shutdown();
        }
        
        private void LoadLocalIp()
        {
            try
            {
                var host = Dns.GetHostEntry(Dns.GetHostName());
                foreach (var ip in host.AddressList)
                {
                    if (ip.AddressFamily == AddressFamily.InterNetwork)
                    {
                        IpAddressText.Text = ip.ToString();
                        return;
                    }
                }
            }
            catch (Exception ex)
            {
                LogMessage("Error getting IP: " + ex.Message);
            }
        }
        
        private void GeneratePin()
        {
            Random r = new Random();
            PinText.Text = r.Next(1000, 9999).ToString();
        }

        private void ToggleServerBtn_Click(object sender, RoutedEventArgs e)
        {
            isServerRunning = !isServerRunning;
            if (isServerRunning)
            {
                StatusText.Text = "Listening";
                StatusIndicator.Fill = new SolidColorBrush(Colors.LimeGreen);
                ToggleServerBtn.Content = "STOP SERVER";
                _tcpServer.Start(8080);
                _udpServer.Start(8081);
                _discoveryService.Start(8082);
                _screenCapture.Start(30); // Target 30 FPS for now
                _audioCapture.Start();
            }
            else
            {
                StatusText.Text = "Offline";
                StatusIndicator.Fill = new SolidColorBrush(Colors.Red);
                ToggleServerBtn.Content = "START SERVER";
                _screenCapture.Stop();
                _audioCapture.Stop();
                _tcpServer.Stop();
                _udpServer.Stop();
                _discoveryService.Stop();
                LogMessage("Servers stopped.");
            }
        }
        
        public void LogMessage(string message)
        {
            Dispatcher.Invoke(() =>
            {
                string time = DateTime.Now.ToString("HH:mm:ss");
                LogTextBox.Text += "[" + time + "] " + message + "\n";
            });
        }
    }
}
