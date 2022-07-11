#Trino Operator
## Maintains TrinoDB in a k8s cluster

Define IMG, for instance:
```bash
export IMG=localhost:5000/trino-operator:0.0.1-SNAPSHOT
```

For normal dev code-debug cycle use:<br>
`make --keep-going redo-all`