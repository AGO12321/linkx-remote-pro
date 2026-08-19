using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Threading.Tasks;
using System.IO;
using LinkXRemoteHost.Protocol;
using LinkXRemoteHost.Security;

namespace LinkXRemoteHost.Network
{
    public class TcpServer
    {
        private TcpListener _listener;
        private CancellationTokenSource _cts;
        private MainWindow _ui;
        private CryptoProvider _crypto;
        private LinkXRemoteHost.Services.InputSimulationService _inputSim;
        
        public TcpServer(MainWindow ui, CryptoProvider crypto)
        {
            _ui = ui;
            _crypto = crypto;
            _inputSim = new LinkXRemoteHost.Services.InputSimulationService();
        }
        
        public void Start(int port = 8080)
        {
            _cts = new CancellationTokenSource();
            _listener = new TcpListener(IPAddress.Any, port);
            _listener.Start();
            
            _ui.LogMessage("TCP Server listening on port " + port);
            Task.Run(() => ListenForClientsAsync(_cts.Token));
        }
        
        public void Stop()
        {
            if (_cts != null) _cts.Cancel();
            if (_listener != null) _listener.Stop();
        }
        
        private async Task ListenForClientsAsync(CancellationToken token)
        {
            try
            {
                while (!token.IsCancellationRequested)
                {
                    var client = await _listener.AcceptTcpClientAsync();
                    _ui.LogMessage("Client connected: " + client.Client.RemoteEndPoint.ToString());
                    Task.Run(() => HandleClientAsync(client, token));
                }
            }
            catch (Exception ex)
            {
                if (!token.IsCancellationRequested)
                    _ui.LogMessage("Server error: " + ex.Message);
            }
        }
        
        private async Task HandleClientAsync(TcpClient client, CancellationToken token)
        {
            using (client)
            using (var stream = client.GetStream())
            {
                try
                {
                    while (!token.IsCancellationRequested && client.Connected)
                    {
                        var packet = await ReadPacketAsync(stream, token);
                        if (packet == null) break;

                        ProcessPacket(packet, stream, token);
                    }
                }
                catch (Exception ex)
                {
                    _ui.LogMessage("Client error: " + ex.Message);
                }
                finally
                {
                    _ui.LogMessage("Client disconnected.");
                }
            }
        }

        private async Task<BinaryPacket> ReadPacketAsync(NetworkStream stream, CancellationToken token)
        {
            byte[] header = new byte[12];
            int read = await ReadExactAsync(stream, header, 0, 12, token);
            if (read < 12) return null;

            var packet = BinaryPacket.Deserialize(header);
            if (packet == null) return null; // Invalid magic or version
            
            // Re-read payload length from header to know how much payload to read
            // Because Deserialize() of just 12 bytes will have set Payload to empty.
            using (var ms = new MemoryStream(header))
            using (var br = new BinaryReader(ms))
            {
                br.ReadBytes(8); // skip magic, version, type, flags, seq
                int payloadLength = br.ReadInt32();
                
                if (payloadLength > 0)
                {
                    byte[] payload = new byte[payloadLength];
                    int payloadRead = await ReadExactAsync(stream, payload, 0, payloadLength, token);
                    if (payloadRead < payloadLength) return null;
                    packet.Payload = payload;
                }
            }
            
            return packet;
        }

        private async Task<int> ReadExactAsync(NetworkStream stream, byte[] buffer, int offset, int count, CancellationToken token)
        {
            int totalRead = 0;
            while (totalRead < count)
            {
                int read = await stream.ReadAsync(buffer, offset + totalRead, count - totalRead, token);
                if (read == 0) break;
                totalRead += read;
            }
            return totalRead;
        }

        private void ProcessPacket(BinaryPacket packet, NetworkStream stream, CancellationToken token)
        {
            // Decrypt payload if flagged
            if (packet.Flags.HasFlag(PacketFlags.Encrypted))
            {
                try
                {
                    packet.Payload = _crypto.Decrypt(packet.Payload);
                }
                catch (Exception ex)
                {
                    _ui.LogMessage("Decryption failed: " + ex.Message);
                    return;
                }
            }

            _ui.LogMessage("Received Packet: " + packet.Type);

            switch (packet.Type)
            {
                case PacketType.AuthRequest:
                    HandleAuthRequest(stream, token);
                    break;
                case PacketType.AuthResponse:
                    HandleAuthResponse(packet.Payload, stream, token);
                    break;
                case PacketType.Heartbeat:
                    SendAck(packet.SequenceId, stream, token);
                    break;
                case PacketType.MouseMove:
                    // Payload format: X (int32), Y (int32)
                    if (packet.Payload.Length >= 8)
                    {
                        int x = BitConverter.ToInt32(packet.Payload, 0);
                        int y = BitConverter.ToInt32(packet.Payload, 4);
                        _inputSim.MoveMouseDelta(x, y);
                    }
                    break;
                case PacketType.MouseClick:
                    // Payload: button (byte)
                    if (packet.Payload.Length >= 1) {
                        _inputSim.MouseClick(packet.Payload[0], true);
                        _inputSim.MouseClick(packet.Payload[0], false);
                    }
                    break;
                case PacketType.KeyboardEvent:
                    // Payload: keycode (int32)
                    if (packet.Payload.Length >= 4) {
                        ushort key = (ushort)BitConverter.ToInt32(packet.Payload, 0);
                        _inputSim.KeyEvent(key, true);
                        _inputSim.KeyEvent(key, false);
                    }
                    break;
                case PacketType.WebcamFrame:
                    // Dispatch to UI
                    App.Current.Dispatcher.Invoke(() => 
                    {
                        if (App.WebcamWindow == null || !App.WebcamWindow.IsVisible)
                        {
                            App.WebcamWindow = new LinkXRemoteHost.UI.WebcamWindow();
                            App.WebcamWindow.Show();
                        }
                        App.WebcamWindow.RenderFrame(packet.Payload);
                    });
                    break;
                case PacketType.ClipboardData:
                    // Dispatch to ClipboardService
                    App.Current.Dispatcher.Invoke(() => 
                    {
                        var text = System.Text.Encoding.UTF8.GetString(packet.Payload);
                        System.Windows.Clipboard.SetText(text);
                    });
                    break;
            }
        }

        private async void HandleAuthRequest(NetworkStream stream, CancellationToken token)
        {
            // Send AuthChallenge with public key
            var pubKey = _crypto.GetPublicKey();
            var response = new BinaryPacket
            {
                Type = PacketType.AuthChallenge,
                Payload = pubKey
            };
            await SendPacketAsync(response, stream, token);
        }

        private async void HandleAuthResponse(byte[] payload, NetworkStream stream, CancellationToken token)
        {
            // Payload should be the AES session key encrypted with our RSA public key
            try
            {
                _crypto.SetSessionKeyEncrypted(payload);
                _ui.LogMessage("Session key established via RSA.");

                var success = new BinaryPacket
                {
                    Type = PacketType.AuthResult,
                    Payload = new byte[] { 1 } // 1 = OK
                };
                await SendPacketAsync(success, stream, token);
            }
            catch (Exception ex)
            {
                _ui.LogMessage("AuthResponse failed: " + ex.Message);
            }
        }

        private async void SendAck(uint sequenceId, NetworkStream stream, CancellationToken token)
        {
            var ack = new BinaryPacket
            {
                Type = PacketType.Heartbeat,
                Flags = PacketFlags.IsAck,
                SequenceId = sequenceId
            };
            await SendPacketAsync(ack, stream, token);
        }

        private async Task SendPacketAsync(BinaryPacket packet, NetworkStream stream, CancellationToken token)
        {
            byte[] data = packet.Serialize();
            await stream.WriteAsync(data, 0, data.Length, token);
        }
    }
}
