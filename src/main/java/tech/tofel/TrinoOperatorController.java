package tech.tofel;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.polling.PerResourcePollingEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@ControllerConfiguration(finalizerName = Constants.NO_FINALIZER)
public class TrinoOperatorController implements Reconciler<TrinoOperator>, ErrorStatusHandler<TrinoOperator>,
        EventSourceInitializer<TrinoOperator> {

    private final Logger log = LoggerFactory.getLogger(getClass());
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

    public TrinoOperatorController() {
        log.info("Starting operator.");
    }
}

