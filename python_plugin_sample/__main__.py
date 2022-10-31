import sys
import http_client


if __name__ == '__main__':
    # get input params
    if len(sys.argv) < 4:
        print("Error: missing port or event id")
        exit(1)
    port = sys.argv[1]
    eventId = sys.argv[2]
    base_path = sys.argv[3]
    print("port: " + port + " eventId: " + eventId + " base_path: " + base_path)
    # parse eventId to int
    eventId = int(eventId)

    httpClient = http_client.HttpClient("http://127.0.0.1:" + port)

    script = "panel.setTitle(\"Hello World\")" \
                  ".showInput(\"terminal command you want to execute\",\"shell_script\",\"echo hello\")" \
                  ".showSelect(\"Your Gender\",\"gender\", [\"default\", \"male\", \"female\"],\"default\")" \
                  ".showAndGet()"
    resp = httpClient.post("/api/idea/panel", {"eventId": eventId,
                                               "script": script})

    import json
    resp = json.loads(resp.text)
    if resp["code"] == 200:
        print(resp["data"]["shell_script"])

        script = "terminal.setTab(\"my terminal\")" \
                 ".setScript(\""+resp["data"]["shell_script"]+"\")" \
                 ".start()"
        httpClient.post("/api/idea/terminal", {"eventId": eventId,
                                                "script": script})
