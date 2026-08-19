using System;
using System.Diagnostics;
using System.IO;
using System.Threading.Tasks;
using Serilog;

namespace LinkXRemoteHost.Media
{
    public class FFmpegVideoEncoder : IDisposable
    {
        private Process _ffmpegProcess;
        private Stream _stdin;
        private Stream _stdout;
        private bool _isRunning;
        private readonly int _width;
        private readonly int _height;
        private readonly int _fps;

        public event Action<byte[]> OnEncodedFrame;

        public FFmpegVideoEncoder(int width, int height, int fps)
        {
            _width = width;
            _height = height;
            _fps = fps;
        }

        public void Start()
        {
            try
            {
                var startInfo = new ProcessStartInfo
                {
                    FileName = "ffmpeg.exe",
                    Arguments = string.Format("-y -f rawvideo -vcodec rawvideo -s {0}x{1} -pix_fmt bgr24 -r {2} -i - -c:v libx264 -preset ultrafast -tune zerolatency -b:v 2M -f h264 -", _width, _height, _fps),
                    RedirectStandardInput = true,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };

                _ffmpegProcess = new Process { StartInfo = startInfo };
                _ffmpegProcess.ErrorDataReceived += (s, e) => 
                {
                    if (!string.IsNullOrEmpty(e.Data))
                    {
                        // Log.Verbose("FFmpeg: {Data}", e.Data);
                    }
                };
                
                _ffmpegProcess.Start();
                _ffmpegProcess.BeginErrorReadLine();

                _stdin = _ffmpegProcess.StandardInput.BaseStream;
                _stdout = _ffmpegProcess.StandardOutput.BaseStream;
                _isRunning = true;

                Task.Run(async () => await ReadOutputLoop());
                Log.Information("FFmpeg Video Encoder started");
            }
            catch (Exception ex)
            {
                Log.Error(ex, "Failed to start FFmpeg. Ensure ffmpeg.exe is in the application directory or PATH.");
            }
        }

        private async Task ReadOutputLoop()
        {
            byte[] buffer = new byte[65536];
            while (_isRunning && _stdout != null)
            {
                try
                {
                    int bytesRead = await _stdout.ReadAsync(buffer, 0, buffer.Length);
                    if (bytesRead > 0)
                    {
                        byte[] frameData = new byte[bytesRead];
                        Buffer.BlockCopy(buffer, 0, frameData, 0, bytesRead);
                        if (OnEncodedFrame != null) OnEncodedFrame(frameData);
                    }
                }
                catch (Exception ex)
                {
                    Log.Warning(ex, "Error reading from FFmpeg stdout");
                    break;
                }
            }
        }

        public void EncodeFrame(byte[] rgbData)
        {
            if (!_isRunning || _stdin == null) return;
            try
            {
                _stdin.Write(rgbData, 0, rgbData.Length);
                _stdin.Flush();
            }
            catch (Exception ex)
            {
                Log.Warning(ex, "Failed to write frame to FFmpeg stdin");
            }
        }

        public void Dispose()
        {
            _isRunning = false;
            try { if (_stdin != null) _stdin.Close(); } catch { }
            try { if (_stdout != null) _stdout.Close(); } catch { }
            try
            {
                if (_ffmpegProcess != null && !_ffmpegProcess.HasExited)
                {
                    _ffmpegProcess.Kill();
                }
            }
            catch { }
        }
    }
}
