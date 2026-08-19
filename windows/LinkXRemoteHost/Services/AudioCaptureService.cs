using System;
using System.Threading;
using System.Threading.Tasks;
using NAudio.Wave;
using LinkXRemoteHost.Protocol;

namespace LinkXRemoteHost.Services
{
    public class AudioCaptureService
    {
        private WasapiLoopbackCapture _capture;
        private CancellationTokenSource _cts;
        private Action<BinaryPacket> _onAudioCaptured;
        private bool _isRunning;

        public AudioCaptureService(Action<BinaryPacket> onAudioCaptured)
        {
            _onAudioCaptured = onAudioCaptured;
        }

        public void Start()
        {
            if (_isRunning) return;

            try
            {
                _cts = new CancellationTokenSource();
                _capture = new WasapiLoopbackCapture();
                _capture.DataAvailable += OnDataAvailable;
                _capture.RecordingStopped += OnRecordingStopped;

                _capture.StartRecording();
                _isRunning = true;
            }
            catch (Exception ex)
            {
                // Typically fails if no audio device or driver issue
                Console.WriteLine("Audio Capture Error: " + ex.Message);
            }
        }

        public void Stop()
        {
            if (!_isRunning) return;
            
            if (_cts != null) _cts.Cancel();
            if (_capture != null) _capture.StopRecording();
            _isRunning = false;
        }

        private void OnDataAvailable(object sender, WaveInEventArgs e)
        {
            if (e.BytesRecorded > 0 && _onAudioCaptured != null)
            {
                byte[] audioData = new byte[e.BytesRecorded];
                Buffer.BlockCopy(e.Buffer, 0, audioData, 0, e.BytesRecorded);

                var packet = new BinaryPacket
                {
                    Type = PacketType.AudioFrame,
                    Flags = PacketFlags.Encrypted, // or None if we want speed
                    Payload = audioData
                };

                _onAudioCaptured.Invoke(packet);
            }
        }

        private void OnRecordingStopped(object sender, StoppedEventArgs e)
        {
            if (_capture != null) _capture.Dispose();
            _capture = null;
        }
    }
}
