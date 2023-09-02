import requests
import json


class Client:

    def __init__(self, path=None):
        self.host = "http://localhost:17122"
        self.event_id = "999999999"
        self.path = path

    def post(self, script, path=None):
        if self.path is None and path is None:
            raise Exception("path is None")
        req_path = path
        if path is None:
            req_path = self.path
        data = {"eventId": self.event_id, "script": script}
        d = json.dumps(data, ensure_ascii=False).encode('utf-8')
        response = requests.post(self.host + req_path, data=d,
                                 headers={"Content-Type": "text/json;charset=utf-8"})
        return response
