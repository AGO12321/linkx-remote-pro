package com.example.desktop_code

object WindowsDesktopAppGenerator {

    val pythonServerScript = """
# ==============================================================================
#                 LinkX Remote Pro - Windows Desktop Host Server
#                      Protocol Version 1.0 (JSON/WebSocket)
# ==============================================================================
import asyncio
import json
import socket
import ctypes
import os
import subprocess
import sys
import time

# Win32 API Constants for Mouse & Keyboard Simulation
USER32 = ctypes.windll.user32
SW_HIDE = 0
SW_SHOW = 5

# Input Event Constants
MOUSEEVENTF_LEFTDOWN   = 0x0002
MOUSEEVENTF_LEFTUP     = 0x0004
MOUSEEVENTF_RIGHTDOWN  = 0x0008
MOUSEEVENTF_RIGHTUP    = 0x0010
MOUSEEVENTF_MIDDLEDOWN = 0x0020
MOUSEEVENTF_MIDDLEUP   = 0x0040
MOUSEEVENTF_WHEEL      = 0x0800

VK_VOLUME_MUTE = 0xAD
VK_VOLUME_DOWN = 0xAE
VK_VOLUME_UP   = 0xAF
VK_MEDIA_NEXT  = 0xB0
VK_MEDIA_PREV  = 0xB1
VK_MEDIA_STOP  = 0xB2
VK_MEDIA_PLAY_PAUSE = 0xB3

MAPPINGS_FILE = "mappings.json"

DEFAULT_MAPPINGS = [
    {"trigger": "LAUNCH_CHROME", "actionType": "Launch App", "targetValue": "chrome.exe", "description": "Launch Google Chrome Browser"},
    {"trigger": "MUTE_MIC", "actionType": "Keyboard Shortcut", "targetValue": "CTRL+SHIFT+M", "description": "Mute Microphone in Discord/Teams"},
    {"trigger": "VOL_BOOST", "actionType": "Media Control", "targetValue": "VOLUME_UP", "description": "Volume Boost"},
    {"trigger": "LOCK_WORKSTATION", "actionType": "System Power", "targetValue": "LOCK", "description": "Lock Workstation"}
]

class LinkXWindowsHost:
    def __init__(self, port=8080, pin="1234"):
        self.port = port
        self.pin = pin
        self.host_ip = self.get_local_ip()
        self.mappings = self.load_mappings()
        print(f"[LinkX Server] Initializing on {self.host_ip}:{self.port}")

    def load_mappings(self):
        if os.path.exists(MAPPINGS_FILE):
            try:
                with open(MAPPINGS_FILE, "r") as f:
                    return json.load(f)
            except Exception as e:
                print(f"[LinkX Server] Error loading mappings file: {e}")
        return DEFAULT_MAPPINGS

    def save_mappings(self):
        try:
            with open(MAPPINGS_FILE, "w") as f:
                json.dump(self.mappings, f, indent=2)
        except Exception as e:
            print(f"[LinkX Server] Error saving mappings file: {e}")

    def get_local_ip(self):
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            s.connect(("8.8.8.8", 80))
            ip = s.getsockname()[0]
        except Exception:
            ip = "127.0.0.1"
        finally:
            s.close()
        return ip

    def handle_mouse_move(self, dx, dy):
        class POINT(ctypes.Structure):
            _fields_ = [("x", ctypes.c_long), ("y", ctypes.c_long)]
        pt = POINT()
        USER32.GetCursorPos(ctypes.byref(pt))
        USER32.SetCursorPos(pt.x + int(dx), pt.y + int(dy))

    def handle_mouse_click(self, click_type):
        if click_type == "LEFT":
            USER32.mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0)
            USER32.mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0)
        elif click_type == "RIGHT":
            USER32.mouse_event(MOUSEEVENTF_RIGHTDOWN, 0, 0, 0, 0)
            USER32.mouse_event(MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0)
        elif click_type == "MIDDLE":
            USER32.mouse_event(MOUSEEVENTF_MIDDLEDOWN, 0, 0, 0, 0)
            USER32.mouse_event(MOUSEEVENTF_MIDDLEUP, 0, 0, 0, 0)

    def handle_media_control(self, cmd):
        key_map = {
            "PLAY_PAUSE": VK_MEDIA_PLAY_PAUSE,
            "NEXT": VK_MEDIA_NEXT,
            "PREVIOUS": VK_MEDIA_PREV,
            "STOP": VK_MEDIA_STOP,
            "VOLUME_UP": VK_VOLUME_UP,
            "VOLUME_DOWN": VK_VOLUME_DOWN,
            "MUTE": VK_VOLUME_MUTE
        }
        if cmd in key_map:
            vk = key_map[cmd]
            USER32.keybd_event(vk, 0, 0, 0)
            USER32.keybd_event(vk, 0, 2, 0)

    def handle_power_action(self, action):
        print(f"[LinkX System] Power command received: {action}")
        if action == "LOCK_WORKSTATION" or action == "LOCK":
            ctypes.windll.user32.LockWorkStation()
        elif action == "RESTART":
            os.system("shutdown /r /t 5")
        elif action == "SHUTDOWN":
            os.system("shutdown /s /t 10")

    def execute_mapped_command(self, trigger_key):
        print(f"[LinkX Mapper] Received mapped trigger: {trigger_key}")
        for m in self.mappings:
            if m.get("trigger") == trigger_key:
                atype = m.get("actionType")
                target = m.get("targetValue")
                if atype == "Launch App":
                    subprocess.Popen(target, shell=True)
                elif atype == "Media Control":
                    self.handle_media_control(target)
                elif atype == "System Power":
                    self.handle_power_action(target)
                elif atype == "Shell Script":
                    subprocess.Popen(target, shell=True)
                return True, f"Executed mapped action: {atype} -> {target}"
        return False, f"Trigger '{trigger_key}' not found in mappings"

    def execute_cmd(self, command):
        try:
            res = subprocess.run(command, shell=True, capture_output=True, text=True, timeout=10)
            return res.stdout if res.stdout else res.stderr
        except Exception as e:
            return f"Error executing command: {str(e)}"

    def process_packet(self, data_str):
        try:
            packet = json.loads(data_str)
            msg_type = packet.get("type")
            payload = packet.get("payload", {})

            if msg_type == "PING":
                return json.dumps({"type": "PONG", "payload": {"status": "ALIVE", "latencyMs": 10}})
            elif msg_type == "MOUSE_MOVE":
                self.handle_mouse_move(payload.get("dx", 0), payload.get("dy", 0))
            elif msg_type == "MOUSE_CLICK":
                self.handle_mouse_click(payload.get("click", "LEFT"))
            elif msg_type == "MEDIA_CONTROL":
                self.handle_media_control(payload.get("command"))
            elif msg_type == "POWER_ACTION":
                self.handle_power_action(payload.get("action"))
            elif msg_type == "MAPPED_CMD":
                trigger = payload.get("trigger", "")
                success, details = self.execute_mapped_command(trigger)
                return json.dumps({"type": "MAPPED_CMD_RESP", "payload": {"success": success, "details": details}})
            elif msg_type == "CMD_REQ":
                output = self.execute_cmd(payload.get("command", ""))
                return json.dumps({"type": "CMD_RESP", "payload": {"output": output}})
            
            return json.dumps({"type": "CONNECT_ACK", "payload": {"status": "OK"}})
        except Exception as e:
            return json.dumps({"type": "ERROR", "payload": {"msg": str(e)}})

# Standard Socket Server Launcher
async def main():
    try:
        import websockets
    except ImportError:
        print("[LinkX Installer] Installing required dependency: websockets...")
        subprocess.check_call([sys.executable, "-m", "pip", "install", "websockets"])
        import websockets

    host = LinkXWindowsHost()

    async def ws_handler(websocket):
        print(f"[LinkX Server] Remote Controller Connected: {websocket.remote_address}")
        try:
            async for message in websocket:
                response = host.process_packet(message)
                if response:
                    await websocket.send(response)
        except websockets.exceptions.ConnectionClosed:
            print("[LinkX Server] Controller Disconnected. Listening for auto-reconnect...")

    print(f"\n=======================================================")
    print(f"   LinkX Remote Pro - Windows Desktop Server Active    ")
    print(f"   Listening on: ws://{host.host_ip}:{host.port}")
    print(f"   Mappings Loaded: {len(host.mappings)} Custom Shortcuts")
    print(f"=======================================================\n")

    async with websockets.serve(ws_handler, "0.0.0.0", host.port):
        await asyncio.Future() # Run forever

if __name__ == "__main__":
    asyncio.run(main())
""".trimIndent()

    val batchLauncherScript = """
@echo off
title LinkX Remote Pro - Windows Host Server
color 0A
echo =======================================================
echo          LinkX Remote Pro Windows Server Launcher       
echo =======================================================
echo.
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python is not installed or not in PATH!
    echo Please install Python 3.9+ from https://www.python.org/
    pause
    exit /b
)

echo [1/2] Checking dependencies...
python -c "import websockets" >nul 2>&1
if %errorlevel% neq 0 (
    echo Installing websockets...
    pip install websockets
)

echo [2/2] Starting LinkX Remote Pro Server...
echo.
python linkx_remote_server.py
pause
""".trimIndent()

    val cSharpWpfSourceCode = """
// ==============================================================================
//                LinkX Remote Pro - C# .NET Windows Desktop Host
// ==============================================================================
using System;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Runtime.InteropServices;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace LinkXRemotePro.WindowsServer
{
    public class Program
    {
        [DllImport("user32.dll")]
        public static extern bool SetCursorPos(int X, int Y);

        [DllImport("user32.dll")]
        public static extern bool GetCursorPos(out POINT lpPoint);

        [DllImport("user32.dll")]
        public static extern void mouse_event(uint dwFlags, uint dx, uint dy, uint dwData, int dwExtraInfo);

        [DllImport("user32.dll")]
        public static extern bool LockWorkStation();

        public struct POINT { public int X; public int Y; }

        private const uint MOUSEEVENTF_LEFTDOWN = 0x02;
        private const uint MOUSEEVENTF_LEFTUP = 0x04;

        public static async Task Main(string[] args)
        {
            Console.Title = "LinkX Remote Pro - Windows Host Engine";
            Console.ForegroundColor = ConsoleColor.Cyan;
            Console.WriteLine("====================================================");
            Console.WriteLine("   LinkX Remote Pro Windows Desktop Engine v1.0    ");
            Console.WriteLine("====================================================");

            TcpListener server = new TcpListener(IPAddress.Any, 8080);
            server.Start();
            Console.WriteLine("[LinkX Host] Listening for Android Controller on port 8080...");

            while (true)
            {
                TcpClient client = await server.AcceptTcpClientAsync();
                Console.WriteLine("[LinkX Host] Android Client Connected!");
                _ = Task.Run(() => HandleClientAsync(client));
            }
        }

        private static async Task HandleClientAsync(TcpClient client)
        {
            using NetworkStream stream = client.GetStream();
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = await stream.ReadAsync(buffer, 0, buffer.Length)) > 0)
            {
                string jsonMessage = Encoding.UTF8.GetString(buffer, 0, bytesRead);
                ProcessProtocolPacket(jsonMessage);
            }
        }

        private static void ProcessProtocolPacket(string jsonStr)
        {
            try
            {
                using JsonDocument doc = JsonDocument.Parse(jsonStr);
                JsonElement root = doc.RootElement;
                string type = root.GetProperty("type").GetString();
                JsonElement payload = root.GetProperty("payload");

                switch (type)
                {
                    case "MOUSE_MOVE":
                        int dx = (int)payload.GetProperty("dx").GetDouble();
                        int dy = (int)payload.GetProperty("dy").GetDouble();
                        GetCursorPos(out POINT pt);
                        SetCursorPos(pt.X + dx, pt.Y + dy);
                        break;

                    case "MOUSE_CLICK":
                        mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0);
                        mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
                        break;

                    case "POWER_ACTION":
                        string action = payload.GetProperty("action").GetString();
                        if (action == "LOCK_WORKSTATION") LockWorkStation();
                        break;
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[LinkX Error] {ex.Message}");
            }
        }
    }
}
""".trimIndent()
}

