# coding=utf-8
import requests


class AutumnRPCClient(object):
    def __init__(self, server='localhost', port=8801):
        self.server = server
        self.port = port

    def get_service(self, service):
        class Proxy(object):
            def __getattribute__(s_self, item):
                def invoke(*args):
                    url = 'http://%s:%d/%s' % (self.server, self.port, service)
                    request = (item, None, args)
                    response = requests.post(url, json=request).json()
                    if response[0] == 0:
                        return response[1]
                    else:
                        raise Exception(response[1]['message'])

                return invoke

        return Proxy()


if __name__ == '__main__':
    pass
