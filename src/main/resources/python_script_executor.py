import http.server
import base64
import subprocess
from subprocess import STDOUT


class ScriptHandler(http.server.BaseHTTPRequestHandler):

    def do_shut_down(self):

        httpd.shutdown()

    def do_GET(self):
        try:
            path = self.path
            cmd = path[1:]
            cmd = base64.b64decode(cmd).decode()
            if cmd == "shutdown":
                self.protocol_version = "HTTP/1.1"
                self.send_response(200)
                self.send_header("Content-type", "text/plain")
                self.end_headers()
                self.wfile.write(b"ok")
                import threading
                threading.Thread(target=self.do_shut_down()).start()
                return
            cmd = "cd ~ \n" + cmd
            output = subprocess.check_output(cmd, shell=True, stderr=STDOUT).decode("UTF-8")
            self.protocol_version = "HTTP/1.1"
            self.send_response(200)
            self.send_header("Content-type", "text/plain")
            self.end_headers()
            self.wfile.write(output.encode())
        except subprocess.CalledProcessError as e:
            error_info = repr(e)+" \n\n"+"".join(str(e.output))
            self.send_response(400)
            self.send_header("Content-type", "text/plain")
            self.end_headers()
            self.wfile.write(error_info.encode())


server_address = ('127.0.0.1', 17022)
httpd = http.server.ThreadingHTTPServer(server_address, ScriptHandler)


if __name__ == "__main__":
    httpd.serve_forever()

