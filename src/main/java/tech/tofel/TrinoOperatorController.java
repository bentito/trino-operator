package tech.tofel;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ControllerConfiguration
public class TrinoOperatorController implements Reconciler<TrinoOperator>{

    private final Logger log = LoggerFactory.getLogger(getClass());
    static final String APP_LABEL = "app.kubernetes.io/name";
    private final KubernetesClient client;

    public TrinoOperatorController(KubernetesClient client) {
        log.info("instantiating client in namespace: {}", client.getNamespace());
        this.client = client;
    }

    @Override
    public UpdateControl<TrinoOperator> reconcile(TrinoOperator trinoDBApp, Context context) {
        var appName = trinoDBApp.getMetadata().getName();
        log.info("Reconciling update: {}", trinoDBApp.getMetadata().getName());
        final var name = trinoDBApp.getMetadata().getName();
        final var labels = Map.of(APP_LABEL, trinoDBApp.getMetadata().getName());
        final var spec = trinoDBApp.getSpec();
        final var imageRef = spec.getImageRef();
        final var metadata = createMetadata(trinoDBApp, labels);

        Map<String, String> data = new HashMap<String, String>();
        data.put("coordinator", String.valueOf(spec.isCoordinator()));
        data.put("node-scheduler.include-coordinator", String.valueOf(spec.isNodeSchedulerIncludeCoordinator()));
        data.put("http-server.http.port", String.valueOf(spec.getHttpServerHttpPort()));
        data.put("discovery.uri", spec.getDiscoveryURI());

//        var configMapMetadata = trinoDBApp.getMetadata();
//        configMapMetadata.setName("config-properties");
//        var configmap = new ConfigMapBuilder()
//                .withMetadata(configMapMetadata)
//                .withApiVersion("v1")
//                .withData(data)
//                .build();
//        log.info("Create ConfigMap {}", metadata.getName());
//        client.configMaps().createOrReplace(configmap);
//        trinoDBApp.getMetadata().setName(appName);
//        log.info("Create ConfigMapVolumeSource {}", metadata.getName());
//        ConfigMapVolumeSource configMapVolumeSource = new ConfigMapVolumeSourceBuilder()
//                .withName("config-properties")
//                .build();

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
//                .withVolumeMounts((List<VolumeMount>) configMapVolumeSource)
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
                .withNewTargetPort().withValue(8080).endTargetPort()
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

        createCatalogs(spec);

        return UpdateControl.noUpdate();
    }

    private void createCatalogs(TrinoOperatorSpec spec) {
        for (String catalog : spec.getCatalogs()) {
            log.info("Creating catalog {}", catalog);
        }
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

