#Trino Operator
## Maintains TrinoDB in a k8s cluster

Define IMG, for instance:
```bash
export IMG=localhost:5000/trino-operator:0.0.1-SNAPSHOT
```
or
```bash
export IMG=quay.io/btofel/trino-operator:0.0.1-SNAPSHOT
```

For normal dev code-debug cycle use:<br>
`make --keep-going redo-all`

Add a CR so the operator has something to manage:

for instance:
```bash
kubectl apply -f deploy/crds/trinoperator_sample_cr.yaml
```