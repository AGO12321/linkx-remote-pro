using System;
using System.IO;
using System.Security.Cryptography;
using Org.BouncyCastle.Crypto.Engines;
using Org.BouncyCastle.Crypto.Modes;
using Org.BouncyCastle.Crypto.Parameters;

namespace LinkXRemoteHost.Security
{
    public class CryptoProvider
    {
        private RSACryptoServiceProvider _rsa;
        private byte[] _aesKey;

        public CryptoProvider()
        {
            _rsa = new RSACryptoServiceProvider(4096);
        }

        public byte[] GetPublicKey()
        {
            return _rsa.ExportCspBlob(false);
        }

        public void SetSessionKeyEncrypted(byte[] encryptedAesKey)
        {
            _aesKey = _rsa.Decrypt(encryptedAesKey, true); // OAEP
        }

        public void SetSessionKey(byte[] aesKey)
        {
            _aesKey = aesKey;
        }

        public byte[] GetSessionKey()
        {
            return _aesKey;
        }

        public byte[] Encrypt(byte[] plaintext)
        {
            if (_aesKey == null) throw new InvalidOperationException("Session key not established.");
            
            byte[] nonce = new byte[12];
            using (var rng = new RNGCryptoServiceProvider())
            {
                rng.GetBytes(nonce);
            }

            var gcmBlockCipher = new GcmBlockCipher(new AesEngine());
            var parameters = new AeadParameters(new KeyParameter(_aesKey), 128, nonce, null);
            gcmBlockCipher.Init(true, parameters);

            byte[] ciphertext = new byte[gcmBlockCipher.GetOutputSize(plaintext.Length)];
            int len = gcmBlockCipher.ProcessBytes(plaintext, 0, plaintext.Length, ciphertext, 0);
            gcmBlockCipher.DoFinal(ciphertext, len);

            byte[] result = new byte[nonce.Length + ciphertext.Length];
            Buffer.BlockCopy(nonce, 0, result, 0, nonce.Length);
            Buffer.BlockCopy(ciphertext, 0, result, nonce.Length, ciphertext.Length);
            
            return result;
        }

        public byte[] Decrypt(byte[] encrypted)
        {
            if (_aesKey == null) throw new InvalidOperationException("Session key not established.");
            
            byte[] nonce = new byte[12];
            Buffer.BlockCopy(encrypted, 0, nonce, 0, nonce.Length);
            
            byte[] ciphertext = new byte[encrypted.Length - nonce.Length];
            Buffer.BlockCopy(encrypted, nonce.Length, ciphertext, 0, ciphertext.Length);

            var gcmBlockCipher = new GcmBlockCipher(new AesEngine());
            var parameters = new AeadParameters(new KeyParameter(_aesKey), 128, nonce, null);
            gcmBlockCipher.Init(false, parameters);

            byte[] plaintext = new byte[gcmBlockCipher.GetOutputSize(ciphertext.Length)];
            int len = gcmBlockCipher.ProcessBytes(ciphertext, 0, ciphertext.Length, plaintext, 0);
            gcmBlockCipher.DoFinal(plaintext, len);

            return plaintext;
        }
    }
}
