namespace DeepAQ.AutumnRPC.TestApplication.TestNamespace
{
    public class MyObject
    {
        public MyEnum myEnum { get; }

        public MyObject(MyEnum myEnum)
        {
            this.myEnum = myEnum;
        }

        public override string ToString()
        {
            return $"MyObject[{myEnum}]";
        }
    }
}