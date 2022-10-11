## HotCat

![image](https://user-images.githubusercontent.com/79463662/195034782-0ef26749-86d5-4973-8df5-23903c0af9bc.png)

HotCat is a plugin which provider a set of simple http interfaces. you can use it to get information from idea such as the file or text you selected, or you can send command to idea to control terminal or show dialog and so on.

## How It Works?
HotCat is a http api platform, and python plugin is suported. When HotCat is loaded, a http server is started too. The http server receive request then return infomation from IDEA or operate it directly.
</p>
python plugin will be call like this:

```
python3 $plugin_dir $local_http_port $idea_event_id
```

python script then can use `local_http_port` to send local http request


## Api Samples

All local http request are constructed like this:
```
requests.post("http://127.0.0.1:$port/api/idea/xxx", data={"eventId": $idea_event_id, "script": $script})
```
The `script` pramater is the most important one, so in the next part, we will make some samples to show how to build it.


### Sample 1: IDEA terminal(/api/idea/terminal)

- execute command
  ```
    script="terminal.setTab(\"this is tab name\")
                    .setScript(\"echo hello\")
                    .addCondition(\"neet input your password\", \"my password\")
                    .start()"
  ```
  
- get terminal output
  ```
    script="terminal.getOutPut()"
  ```

### Sample 2: Debugger(/api/idea/debugger)
- start a JVM debug session
  ```
  script="debugger.setHost(\"debug target host\")
                  .setPort(\"debug target port\")
                  .setDesc(\"this is debug tab name\")
                  .start()"
  ```
  
### Sample 3: Editor(/api/idea/editor)
editor is an instance of `com.intellij.openapi.editor.Editor`

- get selected text from editor
  ```
  script="editor.getSelectionModel()
                  .getSelectedText()"
  ```

### Sample 4: File(/api/idea/psi_file)
psi_file is an instance of `com.intellij.psi.PsiFile`

- get selected file path
  ```
  script="psiFile.getVirtualFile().getPath()"
  ```

</p>
</p>

This IDEA plugin is an orginal version, only few interfaces are complished, and maybe some bugs here. let me know if something troubled you. 


