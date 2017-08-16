# coding=utf-8
import requests

from .exception import InvocationTargetException, AutumnInvokeException


class AutumnRPCClient(object):
    def __init__(self, server='localhost', port=8801):
        self.server = server
        self.port = port

    def get_service(self, service):
        class Proxy(object):
            def __init__(_self):
                _self.__url = 'http://%s:%d/%s' % (self.server, self.port, service)

            def __getattr__(_self, item):
                def invoke(*args):
                    request = (item, None, args)
                    try:
                        response = requests.post(_self.__url, json=request).json()
                    except Exception as e:
                        raise AutumnInvokeException(e.message)
                    if response[0] == 0:
                        return response[1]
                    else:
                        raise InvocationTargetException(response[1]['message'])

                return invoke

        return Proxy()


if __name__ == '__main__':
    pass
