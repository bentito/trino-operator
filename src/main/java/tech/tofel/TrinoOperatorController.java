package tech.tofel;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ControllerConfiguration(finalizerName = Constants.NO_FINALIZER, namespaces = Constants.WATCH_CURRENT_NAMESPACE,
        name = "trinooperator")
public class TrinoOperatorController implements Reconciler<TrinoOperator>, ErrorStatusHandler<TrinoOperator>,
        EventSourceInitializer<TrinoOperator> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    static final String APP_LABEL = "app.kubernetes.io/name";
    private final KubernetesClient client;

    public TrinoOperatorController(KubernetesClient client) {
        this.client = client;

    }

    @Override
    public List<EventSource> prepareEventSources(
            EventSourceContext<TrinoOperator> context) {
        return List.of();
    }

    @Override
    public UpdateControl<TrinoOperator> reconcile(TrinoOperator schema, Context context) {
        log.info("Reconciling update: {}", schema.getMetadata().getName());
        final var name = schema.getMetadata().getName();
        final var labels = Map.of(APP_LABEL, schema.getMetadata().getName());
        final var spec = schema.getSpec();
        final var imageRef = spec.getImageRef();

        var deployment = new DeploymentBuilder()
                .withMetadata(createMetadata(schema, labels))
                .withNewSpec()
                .withNewSelector().withMatchLabels(labels).endSelector()
                .withNewTemplate()
                .withNewMetadata().withLabels(labels).endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName(name).withImage(imageRef)
                .addNewPort()
                .withName("http").withProtocol("TCP").withContainerPort(8080)
                .endPort()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        client.apps().deployments().createOrReplace(deployment);
        return UpdateControl.noUpdate();
    }

    @Override
    public DeleteControl cleanup(TrinoOperator schema, Context context) {
        log.info("Cleaning up for: {}", schema.getMetadata().getName());
        return DeleteControl.noFinalizerRemoval();
    }

    @Override
    public Optional<TrinoOperator> updateErrorStatus(TrinoOperator schema, RetryInfo retryInfo,
                                                     RuntimeException e) {
        return Optional.empty();
    }

    private ObjectMeta createMetadata(TrinoOperator resource, Map<String, String> labels) {
        final var metadata = resource.getMetadata();
        return new ObjectMetaBuilder()
                .withName(metadata.getName())
                .addNewOwnerReference()
                .withUid(metadata.getUid())
                .withApiVersion(resource.getApiVersion())
                .withName(metadata.getName())
                .withKind(resource.getKind())
                .endOwnerReference()
                .withLabels(labels)
                .build();
    }
}

