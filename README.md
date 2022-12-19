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
TODO
