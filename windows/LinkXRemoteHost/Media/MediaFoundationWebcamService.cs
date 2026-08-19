using System;
using System.Collections.Generic;

namespace LinkXRemoteHost.Media
{
    public class MediaFoundationWebcamService : IWebcamService
    {
        public event Action<byte[]> OnFrameCaptured;

        public bool IsRunning { get; private set; }

        public List<string> GetAvailableCameras()
        {
            throw new NotSupportedException("MediaFoundation API not fully installed. Falling back to FFmpeg.");
        }

        public void Start(string cameraName, int width, int height, int fps)
        {
            throw new NotSupportedException("MediaFoundation API not fully installed. Falling back to FFmpeg.");
        }

        public void Stop()
        {
            IsRunning = false;
        }

        public void Dispose()
        {
            Stop();
        }
    }
}
