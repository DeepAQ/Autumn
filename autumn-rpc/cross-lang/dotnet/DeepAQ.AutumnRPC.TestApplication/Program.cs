using System;
using System.Collections.Generic;
using DeepAQ.AutumnRPC.Client;
using DeepAQ.AutumnRPC.TestApplication.TestNamespace;

namespace DeepAQ.AutumnRPC.TestApplication
{
    internal class Program
    {
        private static void Main(string[] args)
        {
            var client = new AutumnRPCClient("127.0.0.1", 8801);
            var testService = client.GetService<TestService>("com.example.test.TestService");
            // ===== TESTS =====
            Console.WriteLine(testService.echo("Hello C#!"));
            Console.WriteLine(testService.testEnum(3));
            Console.WriteLine(testService.testObject("Hello", new MyObject(MyEnum.C)));
            Console.WriteLine(testService.testArray(new object[] {null, null, null}));
            Console.WriteLine(testService.testList(
                new[] {new MyObject(MyEnum.A), new MyObject(MyEnum.B), new MyObject(MyEnum.C), new MyObject(MyEnum.D)}
            ));
            try
            {
                testService.testThrowException("Test message");
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
            Console.WriteLine(testService.testReturnException("Test message"));
        }
    }
}