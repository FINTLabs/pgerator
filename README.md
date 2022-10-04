# Fint PGerator

Fint PGerator is an operator that creates a PostgreSQL database, schema and user with a set of privileges when a `PGSchemaAndUserResource` CR is created. Username and password will be generated and stored in secrets. 

## What does the operator do?

When a `PGSchemaAndUserResource` CR is **created**:
 * The operator will create a PostgreSQL database, schema and user with a set of privileges. 
 * Username and password will be generated and stored in secrets.

When a `PGSchemaAndUserResource` CR is **deleted**:
 * The operator will delete the PostgreSQL schema and user if deleteOnCleanup is set to true.
 * The operator will not delete the PostgreSQL database as database is likely to be shared between multiple schemas and users.
 * The operator will delete the secrets containing username and password.

## How to use the operator:

### PGSchemaAndUserResource
```yaml
apiVersion: "fintlabs.no/v1alpha1"
kind: PGSchemaAndUserResource
metadata:
    name: <name>
spec:
    databaseName: <databaseName>
    schemaName: <schema name>
    deleteOnCleanup: <true / false>
```

### Example of Custom Resource

```yaml
apiVersion: "fintlabs.no/v1alpha1"
kind: PGSchemaAndUserResource
metadata:
    name: test-user
spec:
    databaseName: testDb
    schemaName: testSchema
    deleteOnCleanup: true
```

#### Prerequisites
 * Kubernetes cluster
 * PostgreSQL database
 * PostgreSQL user with privileges to create database, schema and user

### Using the operator

* Building the application will create a CRD like this: 
    
```yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: pgschemaanduserresources.fintlabs.no
spec:
  group: fintlabs.no
  names:
    kind: PGSchemaAndUserResource
    plural: pgschemaanduserresources
    singular: pgschemaanduserresource
  scope: Namespaced
  versions:
    - name: v1alpha1
      schema:
        openAPIV3Schema:
          properties:
            spec:
              properties:
                databaseName:
                  type: string
                schemaName:
                  type: string
                deleteOnCleanup:
                  type: boolean
              type: object
            status:
              properties:
                errorMessage:
                  type: string
                observedGeneration:
                  type: integer
              type: object
          type: object
      served: true
      storage: true
      subresources:
        status: {}
```

* Apply the CRD to the cluster:
```bash
kubectl apply -f <CRD>.yml
```

* Run the application.
* Create a `PGSchemaAndUserResource` CR: 
```yaml
apiVersion: "fintlabs.no/v1alpha1"
kind: PGSchemaAndUserResource
metadata:
  name: <name>
spec:
  databaseName: <databaseName>
  schemaName: <schema name>
  deleteOnCleanup: <true / false>
```
* Apply the CR to the cluster:
```bash
kubectl apply -f <CR>.yml
```
* The operator will create a PostgreSQL database, schema and user with a set of privileges, and store username and password in secrets.
* To delete the schema and user from the database, delete the `PGSchemaAndUserResource` CR:
```bash
kubectl delete -f <CR>.yml
```