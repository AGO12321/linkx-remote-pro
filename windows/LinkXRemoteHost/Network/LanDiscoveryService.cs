using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace LinkXRemoteHost.Network
{
    public class LanDiscoveryService
    {
        private UdpClient _udpClient;
        private CancellationTokenSource _cts;
        private MainWindow _ui;

        public LanDiscoveryService(MainWindow ui)
        {
            _ui = ui;
        }

        public void Start(int discoveryPort = 8082)
        {
            _cts = new CancellationTokenSource();
            _udpClient = new UdpClient(discoveryPort);
            _udpClient.EnableBroadcast = true;
            
            _ui.LogMessage("LAN Discovery Service started on port " + discoveryPort);
            Task.Run(() => ListenForBroadcastsAsync(_cts.Token));
        }

        public void Stop()
        {
            if (_cts != null) _cts.Cancel();
            if (_udpClient != null) _udpClient.Close();
        }

        private async Task ListenForBroadcastsAsync(CancellationToken token)
        {
            try
            {
                while (!token.IsCancellationRequested)
                {
                    var result = await _udpClient.ReceiveAsync();
                    string message = Encoding.UTF8.GetString(result.Buffer);

                    if (message == "LINKX_DISCOVERY_PING")
                    {
                        _ui.LogMessage("Discovery ping from " + result.RemoteEndPoint.Address);

                        // Respond with host info (JSON format is fine for discovery since it's unencrypted LAN metadata)
                        string hostName = Dns.GetHostName();
                        string response = "{\"serverName\":\"" + hostName + "\",\"port\":8080,\"requiresPin\":true}";
                        byte[] responseBytes = Encoding.UTF8.GetBytes(response);

                        await _udpClient.SendAsync(responseBytes, responseBytes.Length, result.RemoteEndPoint);
                    }
                }
            }
            catch (Exception ex)
            {
                if (!token.IsCancellationRequested)
                    _ui.LogMessage("Discovery Service error: " + ex.Message);
            }
        }
    }
}
