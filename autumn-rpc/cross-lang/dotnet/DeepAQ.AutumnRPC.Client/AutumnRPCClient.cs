using System;
using System.Net.Http;
using System.Reflection;
using Castle.DynamicProxy;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace DeepAQ.AutumnRPC.Client
{
    public class AutumnRPCClient
    {
        private static readonly HttpClient HttpClient = new HttpClient();
        private static readonly ProxyGenerator ProxyGenerator = new ProxyGenerator();

        private readonly string _baseUrl;

        public AutumnRPCClient(string host, int port)
        {
            this._baseUrl = $"http://{host}:{port}";
        }

        public T GetService<T>() where T : class
        {
            return GetService<T>(typeof(T).FullName);
        }

        public T GetService<T>(string remoteServiceName) where T : class
        {
            return GetService<T>(remoteServiceName, 3000);
        }

        public T GetService<T>(string remoteServiceName, int timeout) where T : class
        {
            return ProxyGenerator.CreateInterfaceProxyWithoutTarget<T>(
                new Interceptor(remoteServiceName, timeout, this.Invoke)
            );
        }

        public T Invoke<T>(string serviceName, string methodName, int timeout, params object[] paramArray)
        {
            return (T) Invoke(new HttpClient {Timeout = TimeSpan.FromMilliseconds(timeout)},
                serviceName, methodName, typeof(T), paramArray);
        }

        private object Invoke(HttpClient httpClient, string serviceName, string methodName, Type returnType,
            params object[] paramArray)
        {
            var request = new AutumnRPCRequest
            {
                MethodName = methodName,
                Params = paramArray
            };
            var httpResponse = httpClient.PostAsync($"{_baseUrl}/{serviceName}", new StringContent(request.ToJson()))
                .Result.Content.ReadAsStringAsync().Result;
            var respArray = JArray.Parse(httpResponse);
            if (respArray[0].Value<int>() == 0)
            {
                return respArray[1].ToObject(returnType);
            }
            else
            {
                throw new TargetInvocationException(new Exception(respArray[1]["message"].ToString()));
            }
        }

        private class Interceptor : IInterceptor
        {
            internal delegate object InvokeDelegate(HttpClient httpClient, string serviceName, string methodName,
                Type returnType, params object[] paramArray);

            private readonly string _serviceName;
            private readonly HttpClient _httpClient;
            private readonly InvokeDelegate _delegate;

            public Interceptor(string serviceName, int timeout, InvokeDelegate invokeDelegate)
            {
                _serviceName = serviceName;
                _httpClient = new HttpClient {Timeout = TimeSpan.FromMilliseconds(timeout)};
                _delegate = invokeDelegate;
            }

            public void Intercept(IInvocation invocation)
            {
                try
                {
                    invocation.ReturnValue = _delegate(_httpClient, _serviceName, invocation.Method.Name,
                        invocation.Method.ReturnType, invocation.Arguments);
                }
                catch (Exception e)
                {
                    Console.WriteLine(e);
                    if (e is TargetInvocationException)
                    {
                        throw e.InnerException;
                    }
                }
            }
        }
    }
}