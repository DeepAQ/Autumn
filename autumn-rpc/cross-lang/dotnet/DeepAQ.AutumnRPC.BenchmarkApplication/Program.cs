using System;
using System.Threading;
using DeepAQ.AutumnRPC.Client;

namespace DeepAQ.AutumnRPC.BenchmarkApplication
{
    internal class Program
    {
        private const int ThreadNum = 8;
        private static readonly AutumnRPCClient Client = new AutumnRPCClient("127.0.0.1", 8801);
        private static readonly BenchmarkThread[] Threads = new BenchmarkThread[ThreadNum];

        private static void Main(string[] args)
        {
            for (var i = 0; i < ThreadNum; i++)
            {
                Threads[i] = new BenchmarkThread();
                new Thread(Threads[i].Run).Start();
            }
            var lastTime = DateTime.Now.Ticks;
            var lastCount = 0;
            while (true)
            {
                Thread.Sleep(1000);
                var current = DateTime.Now.Ticks;
                var count = 0;
                for (var i = 0; i < ThreadNum; i++)
                {
                    count += Threads[i].Count;
                }
                var qps = (count - lastCount) * 1e7 / (current - lastTime);
                Console.WriteLine(qps + " " + (current - lastTime));
                lastCount = count;
                lastTime = current;
            }
        }

        private class BenchmarkThread
        {
            internal int Count = 0;

            internal void Run()
            {
                while (true)
                {
                    Client.Invoke<string>("com.example.test.TestService", "echo", "Hello C#!");
                    Count++;
                }
            }
        }
    }
}