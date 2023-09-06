package io.github.binarybeing.hotcat.plugin.server.grpc;

import com.google.protobuf.Any;
import com.google.protobuf.ProtocolStringList;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.server.controller.IdeaPanelController;
import com.github.binarybeing.hotcat.porto.panel.PanelGrpcService;
import com.github.binarybeing.hotcat.porto.panel.PanelServiceGrpc;
import com.github.binarybeing.hotcat.proto.DataResponse;
import com.github.binarybeing.hotcat.proto.StringRequest;
import com.github.binarybeing.hotcat.proto.StringResponse;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;

import java.util.Map;

public class PanelRpcService extends PanelServiceGrpc.PanelServiceImplBase {

    @Override
    public void showAndGet(PanelGrpcService.FormRequest request, StreamObserver<DataResponse> responseObserver) {
        DataResponse.Builder builder = DataResponse.newBuilder();
        try {
            long eventId = request.getEventId();
            String title = request.getTitle();
            int width = request.getWidth();
            AnActionEvent event = EventContext.getEvent(eventId);
            Assert.assertNotNull("event id error, no event found", event);
            IdeaPanelController.IdeaPanel panel = new IdeaPanelController.IdeaPanel(event);
            panel.setTitle(title);

            for (Any any : request.getElementsList()) {
                if (any.is(PanelGrpcService.InputElement.class)) {
                    PanelGrpcService.InputElement element = any.unpack(PanelGrpcService.InputElement.class);
                    panel.showInput(element.getLabel(), element.getField(), element.getDefaultValue());
                }
                if (any.is(PanelGrpcService.SelectElement.class)) {
                    PanelGrpcService.SelectElement element = any.unpack(PanelGrpcService.SelectElement.class);
                    String[] names = element.getNameList().toArray(String[]::new);
                    String[] values = element.getValueList().toArray(String[]::new);
                    if (ArrayUtils.isEmpty(names)) {
                        panel.showSelect(element.getLabel(), element.getField(), values, element.getDefaultValue());
                    }else{
                        panel.showSelect(element.getLabel(), element.getField(), names, values, element.getDefaultValue());
                    }
                }

                if (any.is(PanelGrpcService.CheckElement.class)) {
                    PanelGrpcService.CheckElement element = any.unpack(PanelGrpcService.CheckElement.class);
                    String label = element.getLabel();
                    String defaultValue = element.getDefaultValue();
                    String field = element.getField();
                    boolean checked = element.getChecked();
                    panel.showCheck(label, defaultValue, field, checked);
                }
            }
            PanelGrpcService.FormInfo.Builder form = PanelGrpcService.FormInfo.newBuilder();
            Map<String, String> resp = panel.showAndGet(width > 0 ? width : 300);
            resp.forEach((key, value) ->
                    form.addKvPairs(PanelGrpcService.KvPair.newBuilder().setKey(key).setValue(value).build()));
            builder.setCode(20000);
            builder.setData(Any.pack(form.build()));
        } catch (Exception e) {
            builder.setCode(50000);
            builder.setMsg(e.getMessage());
        }finally {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }

    }

    @Override
    public void showError(PanelGrpcService.BasicPanelRequest request, StreamObserver<DataResponse> responseObserver) {
        showInfoMsg(request, responseObserver, true);
    }

    @Override
    public void showMsg(PanelGrpcService.BasicPanelRequest request, StreamObserver<DataResponse> responseObserver) {
        showInfoMsg(request, responseObserver, false);
    }
    @Override
    public void showConfirm(PanelGrpcService.BasicPanelRequest request, StreamObserver<DataResponse> responseObserver) {
        DataResponse.Builder builder = DataResponse.newBuilder();
        try {
            long eventId = request.getEventId();
            String title = "提示";
            String msg = request.getMsg();
            AnActionEvent event = EventContext.getEvent(eventId);
            Assert.assertNotNull("event id error, no event found", event);
            IdeaPanelController.IdeaPanel panel = new IdeaPanelController.IdeaPanel(event);
            boolean resp = panel.showConfirmDialog(title, msg);
            if (resp) {
                builder.setCode(20000);
            }else{
                builder.setCode(20400);
            }
        } catch (Exception e) {
            builder.setCode(50000);
            builder.setMsg(e.getMessage());
        }finally {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }
    }

    private void showInfoMsg(PanelGrpcService.BasicPanelRequest request, StreamObserver<DataResponse> responseObserver, boolean error){
        DataResponse.Builder resp = DataResponse.newBuilder();
        try {
            long eventId = request.getEventId();
            AnActionEvent event = EventContext.getEvent(eventId);
            Assert.assertNotNull(event);
            String title = "提示";
            String msg = request.getMsg();
            new IdeaPanelController.IdeaPanel(event).showMsg(title, msg, error ? "error" : "info");
            resp.setCode(20000);
        } catch (Exception e) {
            resp.setCode(50000);
            resp.setMsg(e.getMessage());
        } finally {
            responseObserver.onNext(resp.build());
            responseObserver.onCompleted();
        }
    }


    @Override
    public void showFileChooserAndGet(PanelGrpcService.FileChooseRequest request, StreamObserver<StringResponse> responseObserver) {
        StringResponse.Builder resp = StringResponse.newBuilder();
        try {
            long eventId = request.getEventId();
            AnActionEvent event = EventContext.getEvent(eventId);
            Assert.assertNotNull(event);
            String path = request.getPath();
            boolean typeFile = request.getTypeFile();
            boolean typeDir = request.getTypeDir();
            int type = 0;
            if(typeFile) {
                type = type | 1;
            }
            if(typeDir) {
                type = type | (1<<1);
            }
            if(type == 0) {
                type = 3;
            }
            ProtocolStringList suffixesList = request.getSuffixesList();
            String selected = new IdeaPanelController.IdeaPanel(event).showFileChooserAndGet(path, type, suffixesList.toArray(String[]::new));
            resp.setCode(20000);
            resp.setData(selected);
        } catch (Exception e) {
            resp.setCode(50000);
            resp.setMsg(e.getMessage());
        } finally {
            responseObserver.onNext(resp.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void showProcessing(PanelGrpcService.BasicPanelRequest request, StreamObserver<StringResponse> responseObserver) {
        StringResponse.Builder resp = StringResponse.newBuilder();
        try {
            long eventId = request.getEventId();
            AnActionEvent event = EventContext.getEvent(eventId);
            Assert.assertNotNull(event);
            String msg = request.getMsg();
            String s = new IdeaPanelController.IdeaPanel(event).setTitle(msg).showProcessing();
            resp.setCode(20000);
            resp.setData(s);
        } catch (Exception e) {
            resp.setCode(50000);
            resp.setMsg(e.getMessage());
        } finally {
            responseObserver.onNext(resp.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void closeProcessing(StringRequest request, StreamObserver<StringResponse> responseObserver) {
        StringResponse.Builder resp = StringResponse.newBuilder();
        try {
            long eventId = request.getEventId();
            AnActionEvent event = EventContext.getEvent(eventId);
            Assert.assertNotNull(event);
            String s = new IdeaPanelController.IdeaPanel(event).closeProcessing(request.getParam());
            resp.setCode(20000);
            resp.setData(s);
        } catch (Exception e) {
            resp.setCode(50000);
            resp.setMsg(e.getMessage());
        } finally {
            responseObserver.onNext(resp.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void showSidePanelEditor(PanelGrpcService.SideEditorRequest request, StreamObserver<StringResponse> responseObserver) {
        StringResponse.Builder resp = StringResponse.newBuilder();
        try {
            long eventId = request.getEventId();
            AnActionEvent event = EventContext.getEvent(eventId);
            Assert.assertNotNull(event);
            String panelName = request.getPanelName();
            String path = request.getFileAbPath();
            String s = new IdeaPanelController.IdeaPanel(event).showSidePanelEditor(panelName, path);
            resp.setCode(20000);
            resp.setData(s);
        } catch (Exception e) {
            resp.setCode(50000);
            resp.setMsg(e.getMessage());
        } finally {
            responseObserver.onNext(resp.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void showSidePanelWebBrowser(StringRequest request, StreamObserver<StringResponse> responseObserver) {
        StringResponse.Builder resp = StringResponse.newBuilder();
        try {
            long eventId = request.getEventId();
            AnActionEvent event = EventContext.getEvent(eventId);
            Assert.assertNotNull(event);
            String url = request.getParam();
            String s = new IdeaPanelController.IdeaPanel(event).showSidePanelWebBrowser("browser", url);
            resp.setCode(20000);
            resp.setData(s);
        } catch (Exception e) {
            resp.setCode(50000);
            resp.setMsg(e.getMessage());
        } finally {
            responseObserver.onNext(resp.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void showFloatMiniEditor(PanelGrpcService.FloatMiniEditorRequest request, StreamObserver<StringResponse> responseObserver) {
        StringResponse.Builder resp = StringResponse.newBuilder();
        try {
            long eventId = request.getEventId();
            AnActionEvent event = EventContext.getEvent(eventId);
            Assert.assertNotNull(event);
            String title = request.getTitle();
            String fromFile = request.getFromFile();
            int line = request.getLine();
            String content = request.getFloatDefaultContent();
            boolean callback = request.getCallback();
            boolean multiLine = request.getMultiLine();
            String s = new IdeaPanelController.IdeaPanel(event).showFloatMiniEditor(title, fromFile, line, content, multiLine, null);
            resp.setCode(20000);
            resp.setData(s);
        } catch (Exception e) {
            resp.setCode(50000);
            resp.setMsg(e.getMessage());
        } finally {
            responseObserver.onNext(resp.build());
            responseObserver.onCompleted();
        }
    }
}
