using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using LinkXRemoteHost.Protocol;
using Serilog;

namespace LinkXRemoteHost.Services
{
    public class FileTransferService
    {
        private const int ChunkSize = 64 * 1024; // 64 KB chunks
        public event Action<string, long, long> OnTransferProgress;
        public event Action<string> OnTransferComplete;
        public event Action<string, Exception> OnTransferFailed;

        public async Task SendFileAsync(string filePath, Action<BinaryPacket> sendPacketFunc, CancellationToken cancellationToken)
        {
            if (!File.Exists(filePath))
            {
                if (OnTransferFailed != null) OnTransferFailed(filePath, new FileNotFoundException());
                return;
            }

            try
            {
                var fileInfo = new FileInfo(filePath);
                string fileName = fileInfo.Name;
                long fileSize = fileInfo.Length;
                string sha256 = ComputeSha256(filePath);

                string startPayload = string.Format("{0}|{1}|{2}", fileName, fileSize, sha256);
                var startPacket = new BinaryPacket
                {
                    Type = PacketType.FileTransferStart,
                    Payload = Encoding.UTF8.GetBytes(startPayload)
                };
                sendPacketFunc(startPacket);

                using (var fs = new FileStream(filePath, FileMode.Open, FileAccess.Read, FileShare.Read))
                {
                    byte[] buffer = new byte[ChunkSize];
                    int bytesRead;
                    long totalBytesRead = 0;
                    int sequence = 0;

                    while ((bytesRead = await fs.ReadAsync(buffer, 0, buffer.Length, cancellationToken)) > 0)
                    {
                        cancellationToken.ThrowIfCancellationRequested();

                        byte[] chunkPayload = new byte[8 + bytesRead];
                        BitConverter.GetBytes(totalBytesRead).CopyTo(chunkPayload, 0);
                        Buffer.BlockCopy(buffer, 0, chunkPayload, 8, bytesRead);

                        var chunkPacket = new BinaryPacket
                        {
                            Type = PacketType.FileTransferChunk,
                            SequenceId = (uint)sequence++,
                            Payload = chunkPayload
                        };
                        sendPacketFunc(chunkPacket);

                        totalBytesRead += bytesRead;
                        if (OnTransferProgress != null) OnTransferProgress(fileName, totalBytesRead, fileSize);
                        
                        await Task.Delay(1); // Yield to prevent blocking thread
                    }
                }

                var endPacket = new BinaryPacket
                {
                    Type = PacketType.FileTransferEnd,
                    Payload = Encoding.UTF8.GetBytes(fileName)
                };
                sendPacketFunc(endPacket);
                
                if (OnTransferComplete != null) OnTransferComplete(fileName);
                Log.Information("File sent successfully: {FileName}", fileName);
            }
            catch (Exception ex)
            {
                Log.Error(ex, "File transfer failed for {FileName}", filePath);
                if (OnTransferFailed != null) OnTransferFailed(filePath, ex);
            }
        }

        private string ComputeSha256(string filePath)
        {
            using (var sha256 = SHA256.Create())
            using (var stream = File.OpenRead(filePath))
            {
                var hash = sha256.ComputeHash(stream);
                return BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
            }
        }

        private FileStream _receivingStream;
        private string _receivingFilePath;
        private string _expectedSha256;

        public void HandleTransferStart(byte[] payload)
        {
            string startData = Encoding.UTF8.GetString(payload);
            var parts = startData.Split('|');
            if (parts.Length >= 3)
            {
                string fileName = parts[0];
                long fileSize = long.Parse(parts[1]);
                _expectedSha256 = parts[2];

                string downloadsPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), "Downloads");
                _receivingFilePath = Path.Combine(downloadsPath, fileName);
                
                _receivingStream = new FileStream(_receivingFilePath, FileMode.Create, FileAccess.Write, FileShare.None);
                Log.Information("Started receiving file: {FileName} ({Size} bytes)", fileName, fileSize);
            }
        }

        public void HandleTransferChunk(byte[] payload)
        {
            if (_receivingStream != null && payload.Length > 8)
            {
                long offset = BitConverter.ToInt64(payload, 0);
                int dataLength = payload.Length - 8;
                _receivingStream.Position = offset;
                _receivingStream.Write(payload, 8, dataLength);
            }
        }

        public void HandleTransferEnd(byte[] payload)
        {
            if (_receivingStream != null)
            {
                _receivingStream.Flush();
                _receivingStream.Dispose();
                _receivingStream = null;

                string actualSha256 = ComputeSha256(_receivingFilePath);
                if (actualSha256 == _expectedSha256)
                {
                    Log.Information("File received and verified successfully: {FilePath}", _receivingFilePath);
                }
                else
                {
                    Log.Warning("File integrity verification failed for {FilePath}", _receivingFilePath);
                }
            }
        }
    }
}
