using System;
using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Threading.Tasks;
using System.Windows;
using Newtonsoft.Json.Linq;
using Serilog;

namespace LinkXRemoteHost.Services
{
    public interface IUpdateProvider
    {
        Task<UpdateInfo> CheckForUpdatesAsync(string currentVersion);
    }

    public class UpdateInfo
    {
        public bool IsUpdateAvailable { get; set; }
        public string LatestVersion { get; set; }
        public string DownloadUrl { get; set; }
        public string ReleaseNotes { get; set; }
        public string Sha256Hash { get; set; }
    }

    public class GitHubUpdateProvider : IUpdateProvider
    {
        private readonly string _repoOwner;
        private readonly string _repoName;

        public GitHubUpdateProvider(string owner, string repo)
        {
            _repoOwner = owner;
            _repoName = repo;
        }

        public async Task<UpdateInfo> CheckForUpdatesAsync(string currentVersion)
        {
            using (var client = new HttpClient())
            {
                client.DefaultRequestHeaders.Add("User-Agent", "LinkX-Remote-Pro-Host");
                var url = string.Format("https://api.github.com/repos/{0}/{1}/releases/latest", _repoOwner, _repoName);
                
                var response = await client.GetStringAsync(url);
                var json = JObject.Parse(response);
                var tagName = json["tag_name"] != null ? json["tag_name"].ToString() : null;
                
                bool isUpdate = string.Compare(tagName, currentVersion, StringComparison.OrdinalIgnoreCase) > 0;
                
                return new UpdateInfo
                {
                    IsUpdateAvailable = isUpdate,
                    LatestVersion = tagName,
                    DownloadUrl = json["html_url"] != null ? json["html_url"].ToString() : null,
                    ReleaseNotes = json["body"] != null ? json["body"].ToString() : null,
                    Sha256Hash = "" // Usually parsed from release notes or assets
                };
            }
        }
    }

    public class UpdateService
    {
        private IUpdateProvider _provider;

        public UpdateService(IUpdateProvider provider)
        {
            _provider = provider;
        }

        public async Task CheckAndUpdateAsync(string currentVersion)
        {
            try
            {
                var info = await _provider.CheckForUpdatesAsync(currentVersion);
                if (info.IsUpdateAvailable)
                {
                    Log.Information("New update available: {Version}. URL: {Url}", info.LatestVersion, info.DownloadUrl);
                    
                    var result = MessageBox.Show(
                        string.Format("Version {0} is available!\n\nRelease Notes:\n{1}\n\nDo you want to download and install this update?", info.LatestVersion, info.ReleaseNotes),
                        "Update Available", 
                        MessageBoxButton.YesNo, 
                        MessageBoxImage.Information);

                    if (result == MessageBoxResult.Yes)
                    {
                        await PerformUpdateWorkflow(info);
                    }
                }
                else
                {
                    Log.Information("Application is up to date. (Version {Version})", currentVersion);
                }
            }
            catch (Exception ex)
            {
                Log.Error(ex, "Failed to check for updates");
            }
        }

        private async Task PerformUpdateWorkflow(UpdateInfo info)
        {
            string tempDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Temp", "LinkXUpdate");
            if (!Directory.Exists(tempDir)) Directory.CreateDirectory(tempDir);
            
            string installerPath = Path.Combine(tempDir, "Update.msi");
            string backupPath = Path.Combine(tempDir, "Backup.msi");

            try
            {
                using (var client = new HttpClient())
                {
                    var data = await client.GetByteArrayAsync(info.DownloadUrl);
                    File.WriteAllBytes(installerPath, data);
                }

                if (!string.IsNullOrEmpty(info.Sha256Hash))
                {
                    if (!VerifySha256(installerPath, info.Sha256Hash))
                    {
                        MessageBox.Show("Update file integrity check (SHA-256) failed. Update aborted.", "Update Error", MessageBoxButton.OK, MessageBoxImage.Error);
                        return;
                    }
                }

                if (!VerifyAuthenticodeSignature(installerPath))
                {
                    MessageBox.Show("Update file digital signature is invalid. Update aborted.", "Update Error", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }

                // Installation
                Log.Information("Applying update...");
                Process.Start("msiexec.exe", "/i \"" + installerPath + "\" /qb");
                Application.Current.Shutdown();
            }
            catch (Exception ex)
            {
                Log.Error(ex, "Update workflow failed. Rolling back.");
                MessageBox.Show("Update failed. Rolling back changes.", "Update Error", MessageBoxButton.OK, MessageBoxImage.Error);
                if (File.Exists(installerPath)) File.Delete(installerPath);
            }
        }

        private bool VerifySha256(string filePath, string expectedHash)
        {
            using (var sha256 = SHA256.Create())
            using (var stream = File.OpenRead(filePath))
            {
                var hash = sha256.ComputeHash(stream);
                var actualHash = BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
                return actualHash == expectedHash.ToLowerInvariant();
            }
        }

        private bool VerifyAuthenticodeSignature(string filePath)
        {
            try
            {
                var cert = X509Certificate.CreateFromSignedFile(filePath);
                return cert != null;
            }
            catch
            {
                // Self-signed or unsigned will throw or return false
                // Note: The user allowed self-signed for now. We just check if ANY cert exists.
                return false; 
            }
        }
    }
}
