using System;
using Newtonsoft.Json;

namespace DeepAQ.AutumnRPC.Client
{
    public class AutumnRPCRequest
    {
        public string MethodName { get; set; }

        public object[] Params { get; set; }

        public string ToJson()
        {
            return JsonConvert.SerializeObject(new object[] {MethodName, null, Params});
        }
    }
}