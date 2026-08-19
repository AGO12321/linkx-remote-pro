using System;
using System.Collections.Generic;

namespace LinkXRemoteHost.Media
{
    public interface IWebcamService : IDisposable
    {
        event Action<byte[]> OnFrameCaptured;
        
        List<string> GetAvailableCameras();
        void Start(string cameraName, int width, int height, int fps);
        void Stop();
        bool IsRunning { get; }
    }
}
