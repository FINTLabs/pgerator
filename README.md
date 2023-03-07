# Fint PGerator

FINT _pgerator_ is an operator that creates a PostgreSQL database, user and PGBouncer connection pool when a `PGDatabaseAndUser` CR is created. 
Username and password will be generated and stored in secrets. 

## What does the operator do?

When a `PGDatabaseAndUser` CR is **created**:
 * The operator will create a PostgreSQL database, connection pool and user. 
 * Username, password and JDBC uri will be generated and stored in secrets.

When a `PGDatabaseAndUser` CR is **deleted**:
 * The operator will delete the PostgreSQL user and connection pool.
 * The operator will not delete the PostgreSQL database.
 * The operator will delete the secrets containing username and password.

## How to use the operator:

### PGDatabaseAndUser
```yaml
apiVersion: "fintlabs.no/v1alpha1"
kind: PGDatabaseAndUser
metadata:
    name: <name>
```

### Example of Custom Resource

```yaml
apiVersion: "fintlabs.no/v1alpha1"
kind: PGDatabaseAndUser
metadata:
    name: test-user
```

# Installation

Below is an introduction on how to set up the project. First for local development in IntelliJ, then to the cluster.

## Prerequisites
You will need an authentication token from aiven.
- Log in to https://console.azure.aiven.io/
- Go to User Information (Human icon top right of the site)
- Choose "authentication"
- Choose "Generate token"
- Give your token a name, e.g. Development
- Copy and add the token to 1password so you don't loose track of it.

## Local development

1. Clone the repository: `git clone https://github.com/FINTLabs/pgerator.git`
2. Create Run/Debug configuration `"Run -> Edit Configurations..."`
3. Add the following three prerequisites (Name - Value): 
    * fint.aiven.service - pg-alpha
    * fint.aiven.token - generated in the steps mentioned in the prerequisites
    * fint.data.pool-base-url -	jdbc:postgresql://\<aiven PgBouncer Host\> : \<aiven PgBouncer Port\>/
4. Cd to the project root and build the Gradle project: `./gradlew clean build`
5. Apply the custom-resource: `kubectl apply -f build/classes/java/main/META-INF/fabric8/pgdatabaseandusers.fintlabs.no-v1.yml`
6. Run or debug the Application in IntelliJ.
7. Check that it worked: `kubectl get secrets` and `kubectl get customresourcedefinitions`, you should see the name  you specified in the custom .yaml file in the list.
8. Also check that you find the instances created in aiven.
   * `Services -> pg-alpha -> Users` and `Services -> pg-alpha -> Databases`

## Set up in the cluster

To set te project up in a cluster we use Github Action to dockerize the application and kustomize to create the Kubernetes manifests.
All of this work is done in `github/workflows/build-and-deploy.yaml`.
These are the following steps:

1. Set a name field that describe what the action script is going to do.
2. Use on.push.branches to set which branch to listen to for the script to run. (Set this to "main" to make sure it's onnly running when you push changes to the main branch.)
3. Create environment variables for your registry, image name, cluster name and cluster resource group.
4. Set up jobs
   1. build-and-push-image needs to be set up with ubuntu-latest and read/write permissions.
   2. Steps
      1. Checkout repository `actions/checkout@v3`
      2. Log in to container registry `docker/login-action`
      3. Extract metadata for Docker `docker/metadata-action`
      4. Build and push docker image to the cluster `docker/build-push-action`
      5. Bake manifest with kustomize `azure/k8s-bake@v2.2` with renderEngine
      6. Set target cluster `azure/aks-set-context@v1` with creds, cluster-name and resource-group
      7. Deploy `azure/k8s-deploy@v4.4` with manifests, images, namespace and action
      8. To add more clusters repeat step e. to g.

## Resources

Docker login action https://github.com/marketplace/actions/docker-login
Docker metadata action https://github.com/marketplace/actions/docker-metadata-action
Azure CLI action https://github.com/marketplace/actions/azure-cli-action
