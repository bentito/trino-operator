
# Image URL to use all building/pushing image targets
IMG ?= controller:latest

.PHONY: install uninstall deploy undeploy

all: docker-build

##@ General

# The help target prints out all targets with their descriptions organized
# beneath their categories. The categories are represented by '##@' and the
# target descriptions by '##'. The awk commands is responsible for reading the
# entire set of makefiles included in this invocation, looking for lines of the
# file as xyz: ## something, and then pretty-format the target and help. Then,
# if there's a line with ##@ something, that gets pretty-printed as a category.
# More info on the usage of ANSI control characters for terminal formatting:
# https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_parameters
# More info on the awk command:
# http://linuxcommand.org/lc3_adv_awk.php

help: ## Display this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Build

docker-build: ## Build docker image with the manager.
	mvn clean package -Dquarkus.container-image.build=true -Dquarkus.container-image.image=${IMG}

docker-push: ## Push docker image with the manager.
	mvn package -Dquarkus.container-image.push=true -Dquarkus.container-image.image=${IMG}

docker-push-alt: ## just use container engine to do the push
	docker push ${IMG}

podman-push-alt: ## just use container engine to do the push
	podman push --tls-verify=false ${IMG}

##@ Deployment

install: ## Install CRDs into the K8s cluster specified in ~/.kube/config.
	@$(foreach file, $(wildcard target/kubernetes/*-v1.yml), kubectl apply -f $(file);)

uninstall: ## Uninstall CRDs from the K8s cluster specified in ~/.kube/config.
	@$(foreach file, $(wildcard target/kubernetes/*-v1.yml), kubectl delete -f $(file);)

deploy: ## Deploy controller to the K8s cluster specified in ~/.kube/config.
	kubectl apply -f target/kubernetes/kubernetes.yml
	kubectl apply -f deploy/rbac/trinodb_rbac.yaml

undeploy: ## Undeploy controller from the K8s cluster specified in ~/.kube/config.
	kubectl delete -f target/kubernetes/kubernetes.yml

do-all: docker-build docker-push-alt install deploy ## customized build/install/deploy NB: make sure IMG is defined

tear-down: undeploy uninstall

redo-all: tear-down sleep-5 do-all ## run with `make --keep-going` for reliable dev-run-debug cycle action

sleep-%:
	sleep $(@:sleep-%=%)