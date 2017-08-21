using System;
using System.Net.Http;
using System.Reflection;
using Castle.DynamicProxy;
using Newtonsoft.Json.Linq;

namespace DeepAQ.AutumnRPC.Client
{
    public class AutumnRPCClient
    {
        private static readonly ProxyGenerator ProxyGenerator = new ProxyGenerator();

        private readonly HttpClient _httpClient;
        private readonly string _baseUrl;

        public AutumnRPCClient(string host, int port) : this(host, port, 3000)
        {
        }

        public AutumnRPCClient(string host, int port, int timeout)
        {
            this._baseUrl = $"http://{host}:{port}";
            this._httpClient = new HttpClient {Timeout = TimeSpan.FromMilliseconds(timeout)};
        }

        public T GetService<T>() where T : class
        {
            return GetService<T>(typeof(T).FullName);
        }

        public T GetService<T>(string remoteServiceName) where T : class
        {
            return ProxyGenerator.CreateInterfaceProxyWithoutTarget<T>(
                new Interceptor(remoteServiceName, this.InvokeInternal)
            );
        }

        public T Invoke<T>(string serviceName, string methodName, params object[] paramArray)
        {
            return (T) InvokeInternal(serviceName, methodName, typeof(T), paramArray);
        }

        private object InvokeInternal(string serviceName, string methodName, Type returnType,
            params object[] paramArray)
        {
            var request = new AutumnRPCRequest
            {
                MethodName = methodName,
                Params = paramArray
            };
            var httpResponse = _httpClient.PostAsync($"{_baseUrl}/{serviceName}", new StringContent(request.ToJson()))
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
            internal delegate object InvokeDelegate(string serviceName, string methodName, Type returnType,
                params object[] paramArray);

            private readonly string _serviceName;
            private readonly InvokeDelegate _delegate;

            public Interceptor(string serviceName, InvokeDelegate invokeDelegate)
            {
                _serviceName = serviceName;
                _delegate = invokeDelegate;
            }

            public void Intercept(IInvocation invocation)
            {
                try
                {
                    invocation.ReturnValue = _delegate(_serviceName, invocation.Method.Name,
                        invocation.Method.ReturnType, invocation.Arguments);
                }
                catch (Exception e)
                {
                    if (e is TargetInvocationException)
                    {
                        throw e.InnerException;
                    }
                    throw;
                }
            }
        }
    }
}