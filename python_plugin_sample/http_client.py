import requests
import json


class HttpClient:
    def __init__(self, host):
        self.host = host

    def post(self, path, data):
        response = requests.post(self.host + path, data=json.dumps(data), headers={})
        return response

