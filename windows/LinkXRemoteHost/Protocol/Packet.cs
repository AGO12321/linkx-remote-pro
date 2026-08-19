using System;
using System.IO;

namespace LinkXRemoteHost.Protocol
{
    public enum PacketType : byte
    {
        AuthRequest = 0x01,
        AuthChallenge = 0x02,
        AuthResponse = 0x03,
        AuthResult = 0x04,
        Heartbeat = 0x05,
        Disconnect = 0x06,
        
        MouseMove = 0x10,
        MouseClick = 0x11,
        MouseScroll = 0x12,
        KeyboardEvent = 0x13,
        TouchEvent = 0x14,
        
        ScreenFrame = 0x20,
        AudioFrame = 0x21,
        WebcamFrame = 0x22,
        
        ClipboardData = 0x30,
        FileTransferStart = 0x31,
        FileTransferChunk = 0x32,
        FileTransferEnd = 0x33,
        
        TerminalCommand = 0x40,
        TaskManagerRequest = 0x41,
        PowerCommand = 0x42
    }

    [Flags]
    public enum PacketFlags : byte
    {
        None = 0,
        Encrypted = 1,
        Compressed = 2,
        AckRequired = 4,
        IsAck = 8,
        Fragmented = 16
    }

    public class BinaryPacket
    {
        public const ushort MAGIC = 0x4C58; // 'LX'
        public const byte VERSION = 1;

        public PacketType Type { get; set; }
        public PacketFlags Flags { get; set; }
        public uint SequenceId { get; set; }
        public byte[] Payload { get; set; }

        public BinaryPacket()
        {
            Payload = new byte[0];
        }

        public byte[] Serialize()
        {
            using (var ms = new MemoryStream())
            using (var writer = new BinaryWriter(ms))
            {
                writer.Write(MAGIC);
                writer.Write(VERSION);
                writer.Write((byte)Type);
                writer.Write((byte)Flags);
                writer.Write(SequenceId);
                writer.Write(Payload == null ? 0 : Payload.Length);
                
                if (Payload != null && Payload.Length > 0)
                {
                    writer.Write(Payload);
                }
                
                return ms.ToArray();
            }
        }

        public static BinaryPacket Deserialize(byte[] data)
        {
            if (data == null || data.Length < 12)
                return null;

            using (var ms = new MemoryStream(data))
            using (var reader = new BinaryReader(ms))
            {
                ushort magic = reader.ReadUInt16();
                if (magic != MAGIC) return null;

                byte version = reader.ReadByte();
                if (version != VERSION) return null;

                var packet = new BinaryPacket();
                packet.Type = (PacketType)reader.ReadByte();
                packet.Flags = (PacketFlags)reader.ReadByte();
                packet.SequenceId = reader.ReadUInt32();
                
                int payloadLength = reader.ReadInt32();
                if (payloadLength > 0 && ms.Position + payloadLength <= ms.Length)
                {
                    packet.Payload = reader.ReadBytes(payloadLength);
                }

                return packet;
            }
        }
    }
}
