package tech.tofel;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.*;
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
        log.info("instantiating client in namespace: {}", client.getNamespace());
        this.client = client;
    }

    @Override
    public List<EventSource> prepareEventSources(
            EventSourceContext<TrinoOperator> context) {
        return List.of();
    }

    @Override
    public UpdateControl<TrinoOperator> reconcile(TrinoOperator trinoDBApp, Context context) {
        log.info("Reconciling update: {}", trinoDBApp.getMetadata().getName());
        final var name = trinoDBApp.getMetadata().getName();
        final var labels = Map.of(APP_LABEL, trinoDBApp.getMetadata().getName());
        final var spec = trinoDBApp.getSpec();
        final var imageRef = spec.getImageRef();
        final var metadata = createMetadata(trinoDBApp, labels);

        log.info("Create deployment {}", metadata.getName());
        var deployment = new DeploymentBuilder()
                .withMetadata(metadata)
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

        log.info("Create service {}", metadata.getName());
        client.services().createOrReplace(new ServiceBuilder()
                .withMetadata(createMetadata(trinoDBApp, labels))
                .withNewSpec()
                .addNewPort()
                .withName("http")
                .withPort(8080)
                .withNewTargetPort().withIntVal(8080).endTargetPort()
                .endPort()
                .withSelector(labels)
                .withType("ClusterIP")
                .endSpec()
                .build());

        log.info("Create ingress {}. Note: Must add `ingress-nginx-controller` to your cluster. " +
                "Must port-forward to the ingress controller see README", metadata.getName());
        metadata.setAnnotations(Map.of(
                "nginx.ingress.kubernetes.io/rewrite-target", "/",
                "kubernetes.io/ingress.class", "nginx"
        ));

        client.network().v1().ingresses().createOrReplace(new IngressBuilder()
                .withMetadata(metadata)
                .withNewSpec()
                .withNewDefaultBackend()
                .withNewService()
                .withName(name)
                .withNewPort()
                .withNumber(8080)
                .endPort()
                .endService()
                .endDefaultBackend()
                .endSpec()
                .build());

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

