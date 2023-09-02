package io.github.binarybeing.hotcat.plugin.server.grpc;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.binarybeing.hotcat.plugin.EventContext;
import io.github.binarybeing.hotcat.plugin.server.service.ProjectService;
import com.github.binarybeing.hotcat.proto.BaseRequest;
import com.github.binarybeing.hotcat.proto.project.IdeaProjectGrpcService;
import com.github.binarybeing.hotcat.proto.project.ProjectGrpcServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.junit.Assert;

public class ProjectRpcService extends ProjectGrpcServiceGrpc.ProjectGrpcServiceImplBase {


    @Override
    public void current(BaseRequest request, StreamObserver<IdeaProjectGrpcService.CurrentResponse> responseObserver) {
        IdeaProjectGrpcService.CurrentResponse.Builder resp = IdeaProjectGrpcService.CurrentResponse.newBuilder();
        try {
            long eventId = request.getEventId();
            AnActionEvent event = EventContext.getEvent(eventId);
            Assert.assertNotNull(event);
            ProjectService service = new ProjectService(event);
            IdeaProjectGrpcService.Project.Builder builder = IdeaProjectGrpcService.Project.newBuilder();
            builder.setName(service.getProjectName());
            builder.setPath(service.getProjectPath());
            builder.addAllModules(service.getModules());
            builder.setCurrentModule(service.getCurrentModule());

            resp.setCode(20000);
            resp.setData(builder.build());
        } catch (Exception e) {
            resp.setCode(50000);
            resp.setMsg(e.getMessage());
        } finally {
            responseObserver.onNext(resp.build());
            responseObserver.onCompleted();
        }

    }
}
