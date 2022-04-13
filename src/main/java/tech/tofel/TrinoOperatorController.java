package tech.tofel;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.polling.PerResourcePollingEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@ControllerConfiguration(finalizerName = Constants.NO_FINALIZER, namespaces = Constants.WATCH_CURRENT_NAMESPACE,
    name="trinooperator")
public class TrinoOperatorController implements Reconciler<TrinoOperator>, ErrorStatusHandler<TrinoOperator>,
        EventSourceInitializer<TrinoOperator> {

    private final Logger log = LoggerFactory.getLogger(getClass());
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
}

