using System;
using Concentus.Structs;
using Concentus.Enums;
using Serilog;

namespace LinkXRemoteHost.Media
{
    public class OpusAudioService
    {
        private readonly OpusEncoder _encoder;
        private readonly OpusDecoder _decoder;
        
        private const int SampleRate = 48000;
        private const int Channels = 2;
        private const int FrameSize = 960; // 20ms frame at 48kHz

        public OpusAudioService()
        {
            try
            {
                _encoder = OpusEncoder.Create(SampleRate, Channels, OpusApplication.OPUS_APPLICATION_AUDIO);
                _encoder.Bitrate = 128000; // 128 kbps
                
                _decoder = OpusDecoder.Create(SampleRate, Channels);
                Log.Information("Opus Audio Codec initialized.");
            }
            catch (Exception ex)
            {
                Log.Error(ex, "Failed to initialize Opus Codec.");
            }
        }

        public byte[] Encode(short[] pcmSamples)
        {
            if (_encoder == null) return null;
            try
            {
                byte[] outputBuffer = new byte[4000];
                int bytesWritten = _encoder.Encode(pcmSamples, 0, FrameSize, outputBuffer, 0, outputBuffer.Length);
                
                byte[] finalPacket = new byte[bytesWritten];
                Buffer.BlockCopy(outputBuffer, 0, finalPacket, 0, bytesWritten);
                return finalPacket;
            }
            catch (Exception ex)
            {
                Log.Error(ex, "Error encoding Opus frame");
                return null;
            }
        }

        public short[] Decode(byte[] opusData)
        {
            if (_decoder == null) return null;
            try
            {
                short[] outputSamples = new short[FrameSize * Channels];
                int samplesDecoded = _decoder.Decode(opusData, 0, opusData.Length, outputSamples, 0, FrameSize, false);
                return outputSamples;
            }
            catch (Exception ex)
            {
                Log.Error(ex, "Error decoding Opus frame");
                return null;
            }
        }
    }
}
