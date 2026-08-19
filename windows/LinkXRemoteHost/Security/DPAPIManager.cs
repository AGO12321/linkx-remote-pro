using System;
using System.Security.Cryptography;
using System.Text;
using Serilog;

namespace LinkXRemoteHost.Security
{
    public class DPAPIManager
    {
        private static readonly byte[] Entropy = Encoding.UTF8.GetBytes("LinkXRemoteProEntropyV1");

        public static byte[] Protect(byte[] userData)
        {
            try
            {
                return ProtectedData.Protect(userData, Entropy, DataProtectionScope.CurrentUser);
            }
            catch (Exception ex)
            {
                Log.Error(ex, "Failed to encrypt data using DPAPI");
                return null;
            }
        }

        public static byte[] Unprotect(byte[] encryptedData)
        {
            try
            {
                return ProtectedData.Unprotect(encryptedData, Entropy, DataProtectionScope.CurrentUser);
            }
            catch (Exception ex)
            {
                Log.Error(ex, "Failed to decrypt data using DPAPI");
                return null;
            }
        }
    }
}
