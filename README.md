# Trading Journal Entries 

## Swagger

[http://localhost:8081/swagger-ui/index.htm](http://localhost:8081/swagger-ui/index.html)
Or just [http://localhost:8081](http://localhost:8081)

## Running locally

Set the active profile as local:

```bash
-Dspring.active.profiles=local
```

### Environment Variables

Default application properties used on deployed/container run require a set of Environment Variables:

* Generic
    * **PORT**: default is 8080
    * **ENVIRONMENT**: Environment name, mostly used for logging in logback file, default is DEFAULT
* Database
  * **MONGO_USER**: user to connect mongo
  * **MONGO_PASS**: password to connect mongo
  * **MONGO_HOST**: host name, and port if applicable
  * **MONGO_DATABASE**: database name
* JWT Properties
    * **JWT_PUBLIC_KEY**: public key file based on private key used to read access tokens used to sign the JWT
    * **JWT_ISSUER**: Access token issuer must be the same of JWT received
    * **JWT_AUDIENCE**: Access token audience must be the same of JWT received

### Container Dependencies

Run mongo on docker

`docker run -p 27017:27017 --name trading-journal -d mongo`

## Docker

### Build Locally or for Pipeline test

This docker file copies the sample public keys in **/src/main/resources/** to the image, so you can refer the key from **/etc/ssl/certs/public.pem**

```docker build -t allanweber/trading-journal-entry:1.0.0 -f docker/DockerfileTest .```

### Build for deployment

For this option, you must provide your own private and public keys, add it to the image and configure the proper environment variables to read those files

```docker build -t allanweber/trading-journal-entry:1.0.0 -f docker/Dockerfile .```

Tag your image to latest: ```docker tag allanweber/trading-journal-entry:1.0.0 allanweber/trading-journal-entry:latest```

Push image to registry: ```docker push allanweber/trading-journal-entry:latest```

### Run it with env variables

* Get mongo container ip: ```docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' CONTAINER_ID```
* 
```bash
docker run -p 8080:8080 --name trading-journal-entry \
-e MONGO_USER= \
-e MONGO_PASS= \
-e MONGO_HOST= \
-e MONGO_DATABASE= \
-e JWT_AUDIENCE= \
-e JWT_ISSUER= \
-e JWT_PRIVATE_KEY= \
-e JWT_PUBLIC_KEY= \
allanweber/trading-journal-entry:VERSION
```

## Kubernetes

### Enable Kubernetes on local Docker

![](imgs/enable-docker-kubernetes.png)

The scripts for Kubernetes are inside the folder **k8s**, remember to change the names, labels etc. as you like.

Also, remember to replace where you find placeholders for you own values, such as _<SECRET>_, for example

### Config Maps

Create config map: ```kubectl apply -f k8s/config-maps.yml```

Get config map: ```kubectl get cm -n trading-journal trading-journal-entry-prd -o yaml```

Delete config map: ```kubectl delete cm -n trading-journal trading-journal-entry-prd```

### Secrets

Create the secrets: ```kubectl apply -f  k8s/secrets.yml```

Check created secrets: ```kubectl get secret -n trading-journal trading-journal-entry-prd -o yaml```

Delete secrets if you like: ```kubectl delete secrets -n trading-journal trading-journal-entry-prd```

### Deployment

Create deployment: ```kubectl apply -f ./k8s/deployment.yml```

Check deployment: ```kubectl logs -n trading-journal deployment/trading-journal-entry```

Get deployment: ```kubectl get deploy -n trading-journal```

Delete deployment: ```kubectl delete deploy -n trading-journal trading-journal-entry```

Get pods: ```kubectl get pods -n trading-journal```

Describe pods: ```kubectl describe pod -n trading-journal trading-journal-entry```

Set a variable with pod generated name: ```POD=$(kubectl get pod -n trading-journal -l app=trading-journal-authentication -o jsonpath="{.items[0].metadata.name}")```

## Deploys

### Create completely new

```kubectl apply -f  k8s/secrets.yml```

```kubectl apply -f k8s/config-maps.yml```

```kubectl apply -f ./k8s/deployment.yml```

```kubectl logs -n trading-journal deployment/trading-journal-entry```

### Delete all (except namespace)

```kubectl delete deploy -n trading-journal trading-journal-entry```

```kubectl delete cm -n trading-journal trading-journal-entry-prd```

```kubectl delete secrets -n trading-journal trading-journal-entry-prd```

## Access application

### From local to trading-journal-authentication

```kubectl port-forward  -n trading-journal $POD 8080```