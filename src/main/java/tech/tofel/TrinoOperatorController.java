package tech.tofel;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.*;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;

@Controller
public class TrinoOperatorController implements ResourceController<TrinoOperator> {

    private final KubernetesClient client;

    public TrinoOperatorController(KubernetesClient client) {
        this.client = client;
    }

    // TODO Fill in the rest of the controller

    @Override
    public void init(EventSourceManager eventSourceManager) {
        // TODO: fill in init
    }

    @Override
    public UpdateControl<TrinoOperator> createOrUpdateResource(
        TrinoOperator resource, Context<TrinoOperator> context) {
        // TODO: fill in logic

        return UpdateControl.noUpdate();
    }
}

