using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms; // Needed for SystemInformation (requires adding System.Windows.Forms reference)

namespace LinkXRemoteHost.Services
{
    public class ScreenCaptureService
    {
        private CancellationTokenSource _cts;
        private int _targetFps = 30;
        private long _quality = 50L;
        
        public event Action<byte[]> OnFrameCaptured;
        
        public void Start(int fps = 30)
        {
            _targetFps = fps;
            _cts = new CancellationTokenSource();
            Task.Run(() => CaptureLoopAsync(_cts.Token));
        }

        public void Stop()
        {
            if (_cts != null) _cts.Cancel();
        }

        private async Task CaptureLoopAsync(CancellationToken token)
        {
            int delayMs = 1000 / _targetFps;
            ImageCodecInfo jpegCodec = GetEncoder(ImageFormat.Jpeg);
            EncoderParameters encoderParams = new EncoderParameters(1);
            encoderParams.Param[0] = new EncoderParameter(System.Drawing.Imaging.Encoder.Quality, _quality);

            Rectangle bounds = Screen.PrimaryScreen.Bounds;
            
            while (!token.IsCancellationRequested)
            {
                try
                {
                    using (Bitmap bitmap = new Bitmap(bounds.Width, bounds.Height))
                    {
                        using (Graphics g = Graphics.FromImage(bitmap))
                        {
                            g.CopyFromScreen(Point.Empty, Point.Empty, bounds.Size);
                        }

                        using (MemoryStream ms = new MemoryStream())
                        {
                            bitmap.Save(ms, jpegCodec, encoderParams);
                            byte[] frameData = ms.ToArray();
                            if (OnFrameCaptured != null)
                            {
                                OnFrameCaptured(frameData);
                            }
                        }
                    }
                }
                catch (Exception)
                {
                    // Ignore capture errors and retry
                }

                await Task.Delay(delayMs, token);
            }
        }

        private ImageCodecInfo GetEncoder(ImageFormat format)
        {
            ImageCodecInfo[] codecs = ImageCodecInfo.GetImageDecoders();
            foreach (ImageCodecInfo codec in codecs)
            {
                if (codec.FormatID == format.Guid)
                {
                    return codec;
                }
            }
            return null;
        }
    }
}
