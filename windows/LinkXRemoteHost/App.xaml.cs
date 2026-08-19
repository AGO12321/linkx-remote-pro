using System;
using System.IO;
using System.Windows;
using Serilog;

namespace LinkXRemoteHost
{
    public partial class App : Application
    {
        public static LinkXRemoteHost.UI.WebcamWindow WebcamWindow { get; set; }

        protected override void OnStartup(StartupEventArgs e)
        {
            base.OnStartup(e);
            
            var logPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "LinkXRemotePro", "logs", "host-.log");
            
            Log.Logger = new LoggerConfiguration()
                .MinimumLevel.Debug()
                .WriteTo.Console()
                .WriteTo.File(logPath, rollingInterval: RollingInterval.Day, retainedFileCountLimit: 7)
                .CreateLogger();

            Log.Information("LinkX Remote Pro Host starting up...");

            AppDomain.CurrentDomain.UnhandledException += (s, args) =>
            {
                Exception ex = (Exception)args.ExceptionObject;
                Log.Fatal(ex, "A fatal unhandled exception occurred.");
                Log.CloseAndFlush();
            };
            
            DispatcherUnhandledException += (s, args) =>
            {
                Log.Fatal(args.Exception, "A fatal dispatcher unhandled exception occurred.");
                args.Handled = true;
                Log.CloseAndFlush();
                Environment.Exit(1);
            };
        }
        
        protected override void OnExit(ExitEventArgs e)
        {
            Log.Information("LinkX Remote Pro Host shutting down...");
            Log.CloseAndFlush();
            base.OnExit(e);
        }
    }
}
