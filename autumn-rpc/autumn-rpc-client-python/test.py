# coding=utf-8
from autumn_rpc import AutumnRPCClient

if __name__ == '__main__':
    client = AutumnRPCClient('127.0.0.1', 8801)
    service = client.get_service('com.example.test.TestService')
    print(service.echo('Hello Python!'))
    print(service.testEnum(123))
    print(service.testObject("Hello", {'myEnum': 'D'}))
    print(service.testArray([None, "test", 123]))
    print(service.testList([{'myEnum': 'A'}, {'myEnum': 'B'}, {'myEnum': 'C'}, {'myEnum': 'D'}]))
    try:
        print(service.testThrowException("test throw Exception"))
    except Exception, e:
        print(e)
    print(service.testReturnException("test return Exception"))
    pass
