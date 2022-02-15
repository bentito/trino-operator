package tech.tofel;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("trino-group.tofel.tech")
@Kind("Trino-operator")
@Plural("trino-operators")
public class TrinoOperator extends CustomResource<TrinoOperatorSpec, TrinoOperatorStatus> implements Namespaced {}

