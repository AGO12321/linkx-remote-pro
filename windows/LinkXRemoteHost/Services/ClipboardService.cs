using System;
using System.Runtime.InteropServices;
using System.Windows;
using System.Windows.Interop;
using Serilog;

namespace LinkXRemoteHost.Services
{
    public class ClipboardService : IDisposable
    {
        [DllImport("user32.dll", SetLastError = true)]
        private static extern bool AddClipboardFormatListener(IntPtr hwnd);

        [DllImport("user32.dll", SetLastError = true)]
        private static extern bool RemoveClipboardFormatListener(IntPtr hwnd);

        private const int WM_CLIPBOARDUPDATE = 0x031D;

        private IntPtr _windowHandle;
        private HwndSource _hwndSource;
        private string _lastClipboardText = string.Empty;

        public event Action<string> OnClipboardTextChanged;

        public void Start(Window mainWindow)
        {
            _windowHandle = new WindowInteropHelper(mainWindow).EnsureHandle();
            _hwndSource = HwndSource.FromHwnd(_windowHandle);
            _hwndSource.AddHook(HwndHook);
            
            if (!AddClipboardFormatListener(_windowHandle))
            {
                Log.Error("Failed to add clipboard format listener. Error: {Error}", Marshal.GetLastWin32Error());
            }
            else
            {
                Log.Information("Clipboard synchronization service started.");
            }
        }

        private IntPtr HwndHook(IntPtr hwnd, int msg, IntPtr wParam, IntPtr lParam, ref bool handled)
        {
            if (msg == WM_CLIPBOARDUPDATE)
            {
                ProcessClipboardUpdate();
            }
            return IntPtr.Zero;
        }

        private void ProcessClipboardUpdate()
        {
            try
            {
                if (Clipboard.ContainsText())
                {
                    string newText = Clipboard.GetText();
                    if (!string.IsNullOrEmpty(newText) && newText != _lastClipboardText)
                    {
                        _lastClipboardText = newText;
                        Log.Debug("Clipboard text changed, length: {Length}", newText.Length);
                        if (OnClipboardTextChanged != null) OnClipboardTextChanged(newText);
                    }
                }
            }
            catch (Exception ex)
            {
                Log.Warning(ex, "Failed to read clipboard contents.");
            }
        }

        public void SetClipboardText(string text)
        {
            if (_lastClipboardText == text) return;
            
            try
            {
                _lastClipboardText = text;
                Clipboard.SetText(text);
                Log.Debug("Updated local clipboard from remote. Length: {Length}", text.Length);
            }
            catch (Exception ex)
            {
                Log.Warning(ex, "Failed to set local clipboard contents.");
            }
        }

        public void Dispose()
        {
            if (_windowHandle != IntPtr.Zero)
            {
                RemoveClipboardFormatListener(_windowHandle);
                if (_hwndSource != null) _hwndSource.RemoveHook(HwndHook);
                _windowHandle = IntPtr.Zero;
            }
        }
    }
}
