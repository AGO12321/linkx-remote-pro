using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using Serilog;

namespace LinkXRemoteHost.Media
{
    public class FFmpegWebcamService : IWebcamService
    {
        private Process _ffmpegProcess;
        private Stream _stdout;
        private bool _isRunning;
        public event Action<byte[]> OnFrameCaptured;

        public bool IsRunning { get { return _isRunning; } }

        public List<string> GetAvailableCameras()
        {
            var cameras = new List<string>();
            try
            {
                var startInfo = new ProcessStartInfo
                {
                    FileName = "ffmpeg.exe",
                    Arguments = "-list_devices true -f dshow -i dummy",
                    RedirectStandardError = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };

                using (var process = Process.Start(startInfo))
                {
                    string output = process.StandardError.ReadToEnd();
                    process.WaitForExit();

                    var regex = new Regex("\"([^\"]+)\" \\(video\\)");
                    var matches = regex.Matches(output);
                    foreach (Match match in matches)
                    {
                        if (match.Success)
                        {
                            string camName = match.Groups[1].Value;
                            if (!cameras.Contains(camName))
                                cameras.Add(camName);
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                Log.Error(ex, "Failed to get available cameras via FFmpeg");
            }
            return cameras;
        }

        public void Start(string cameraName, int width, int height, int fps)
        {
            try
            {
                var startInfo = new ProcessStartInfo
                {
                    FileName = "ffmpeg.exe",
                    Arguments = string.Format("-f dshow -video_size {0}x{1} -framerate {2} -i video=\"{3}\" -f rawvideo -vcodec rawvideo -pix_fmt bgr24 -", width, height, fps, cameraName),
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                };

                _ffmpegProcess = new Process { StartInfo = startInfo };
                _ffmpegProcess.ErrorDataReceived += (s, e) => { }; // Ignore stderr
                _ffmpegProcess.Start();
                _ffmpegProcess.BeginErrorReadLine();

                _stdout = _ffmpegProcess.StandardOutput.BaseStream;
                _isRunning = true;

                int frameSize = width * height * 3;
                Task.Run(async () => await ReadOutputLoop(frameSize));
                Log.Information("FFmpeg Webcam Capture started for: {CameraName}", cameraName);
            }
            catch (Exception ex)
            {
                Log.Error(ex, "Failed to start FFmpeg Webcam Capture");
            }
        }

        private async Task ReadOutputLoop(int frameSize)
        {
            byte[] buffer = new byte[frameSize];
            while (_isRunning && _stdout != null)
            {
                try
                {
                    int totalBytesRead = 0;
                    while (totalBytesRead < frameSize)
                    {
                        int bytesRead = await _stdout.ReadAsync(buffer, totalBytesRead, frameSize - totalBytesRead);
                        if (bytesRead == 0) break;
                        totalBytesRead += bytesRead;
                    }

                    if (totalBytesRead == frameSize)
                    {
                        if (OnFrameCaptured != null) OnFrameCaptured(buffer);
                    }
                    else
                    {
                        break;
                    }
                }
                catch (Exception ex)
                {
                    Log.Warning(ex, "Error reading webcam frames");
                    break;
                }
            }
        }

        public void Stop()
        {
            _isRunning = false;
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

        public void Dispose()
        {
            Stop();
        }
    }
}
