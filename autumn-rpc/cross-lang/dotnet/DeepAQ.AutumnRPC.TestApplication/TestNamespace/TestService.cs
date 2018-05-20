using System;
using System.Collections.Generic;

namespace DeepAQ.AutumnRPC.TestApplication.TestNamespace
{
    public interface TestService
    {
        string echo(string input);

        MyObject testEnum(int num);

        string testObject(string str, MyObject o);

        string testArray(object[] arr);

        IList<MyObject> testList(IList<MyObject> list);

        void testThrowException(string msg);

        Exception testReturnException(string msg);
    }
}