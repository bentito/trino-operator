# TrinoDB Operator
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
# Setup to use NGINX Ingress controller based ingress
The operator creates an ingress for TrinoDB operand on the cluster, you can
try it out with the following steps:
* Create the NGINX Ingress controller on your cluster with the following:
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.5.1/deploy/static/provider/kind/deploy.yaml
```
* Apply a Trino CR, like sample above, this creates a deployment, service & ingress
* Forward a port to the ingress controller (TrinoDB service is at 8080 in this scenario, ingress is answering on port 80):
```bash
kubectl port-forward --namespace=ingress-nginx service/ingress-nginx-controller 8080:80
```
* As a quick check, if you are running a cluster locallly, like with Kind, you can connect to TrinoDB now with the trino command-line client, like this:
```bash
trino-operator main $ trino --server=http://localhost:8080/
trino> show catalogs;
Handling connection for 8080
 Catalog
---------
 jmx
 memory
 system
 tpcds
 tpch
 ```
* Some debugging items for ingress:
```bash
 $ kubectl get ing -n default
NAME       CLASS    HOSTS   ADDRESS     PORTS   AGE
trino-db   <none>   *       localhost   80      18h
```
```bash
$ kubectl describe ing trino-db -n default
Name:             trino-db
Labels:           app.kubernetes.io/name=trino-db
Namespace:        default
Address:          localhost
Ingress Class:    <none>
Default backend:  trino-db:8080 (10.244.0.15:8080)
Rules:
  Host        Path  Backends
  ----        ----  --------
  *           *     trino-db:8080 (10.244.0.15:8080)
Annotations:  kubernetes.io/ingress.class: nginx
              nginx.ingress.kubernetes.io/rewrite-target: /
Events:       <none>
```