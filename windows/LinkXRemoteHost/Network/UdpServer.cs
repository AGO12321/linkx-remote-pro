using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Threading.Tasks;
using LinkXRemoteHost.Protocol;
using LinkXRemoteHost.Security;

namespace LinkXRemoteHost.Network
{
    public class UdpServer
    {
        private UdpClient _listener;
        private CancellationTokenSource _cts;
        private MainWindow _ui;
        private CryptoProvider _crypto;
        private LinkXRemoteHost.Services.InputSimulationService _inputSim;

        public UdpServer(MainWindow ui, CryptoProvider crypto)
        {
            _ui = ui;
            _crypto = crypto;
            _inputSim = new LinkXRemoteHost.Services.InputSimulationService();
        }

        public void Start(int port = 8081)
        {
            _cts = new CancellationTokenSource();
            _listener = new UdpClient(port);
            
            _ui.LogMessage("UDP Server listening on port " + port);
            Task.Run(() => ListenAsync(_cts.Token));
        }

        public void Stop()
        {
            if (_cts != null) _cts.Cancel();
            if (_listener != null) _listener.Close();
        }

        private IPEndPoint _activeClient;

        private async Task ListenAsync(CancellationToken token)
        {
            try
            {
                while (!token.IsCancellationRequested)
                {
                    var result = await _listener.ReceiveAsync();
                    _activeClient = result.RemoteEndPoint;
                    
                    byte[] data = result.Buffer;

                    // Deserialize binary packet
                    var packet = BinaryPacket.Deserialize(data);
                    if (packet == null) continue;

                    // Process Packet
                    if (packet.Flags.HasFlag(PacketFlags.Encrypted))
                    {
                        try
                        {
                            packet.Payload = _crypto.Decrypt(packet.Payload);
                        }
                        catch (Exception ex)
                        {
                            _ui.LogMessage("UDP Decryption failed: " + ex.Message);
                            continue;
                        }
                    }

                    HandlePacket(packet);
                }
            }
            catch (Exception ex)
            {
                if (!token.IsCancellationRequested)
                    _ui.LogMessage("UDP Server error: " + ex.Message);
            }
        }

        public async Task SendPacketAsync(BinaryPacket packet)
        {
            if (_activeClient == null || _listener == null) return;
            
            try
            {
                if (packet.Flags.HasFlag(PacketFlags.Encrypted))
                {
                    packet.Payload = _crypto.Encrypt(packet.Payload);
                }
                
                byte[] data = packet.Serialize();
                await _listener.SendAsync(data, data.Length, _activeClient);
            }
            catch (Exception ex)
            {
                _ui.LogMessage("UDP Send error: " + ex.Message);
            }
        }

        private void HandlePacket(BinaryPacket packet)
        {
            switch (packet.Type)
            {
                case PacketType.Heartbeat:
                    _ui.LogMessage("[UDP] Heartbeat.");
                    break;
                case PacketType.MouseMove:
                    if (packet.Payload != null && packet.Payload.Length >= 8)
                    {
                        float dx = BitConverter.ToSingle(packet.Payload, 0);
                        float dy = BitConverter.ToSingle(packet.Payload, 4);
                        _inputSim.MoveMouseDelta((int)dx, (int)dy);
                    }
                    break;
                case PacketType.MouseClick:
                    if (packet.Payload != null && packet.Payload.Length >= 2)
                    {
                        int button = packet.Payload[0];
                        bool isDown = packet.Payload[1] == 1;
                        _inputSim.MouseClick(button, isDown);
                    }
                    break;
                case PacketType.MouseScroll:
                    if (packet.Payload != null && packet.Payload.Length >= 4)
                    {
                        int delta = BitConverter.ToInt32(packet.Payload, 0);
                        _inputSim.MouseScroll(delta);
                    }
                    break;
                case PacketType.KeyboardEvent:
                    if (packet.Payload != null && packet.Payload.Length >= 3)
                    {
                        ushort vk = BitConverter.ToUInt16(packet.Payload, 0);
                        bool isDown = packet.Payload[2] == 1;
                        _inputSim.KeyEvent(vk, isDown);
                    }
                    break;
                case PacketType.ScreenFrame:
                case PacketType.AudioFrame:
                default:
                    break;
            }
        }
    }
}
