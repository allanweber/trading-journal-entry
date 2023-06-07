# Trading Journal Entries

## Change Log

### 1.1.0
* Improvements and image upload

### 1.0.0
* Complete solution with many entry types, journal balance and entry images

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
* File Storage properties:
  * if **journal.entries.storage.option <> s3** none of bellow needs to be provides
  * **STORAGE_ACCESS_KEY**: access to key to the cloud storage
  * **STORAGE_SECRET**: secret to the cloud storage
  * **STORAGE_ENDPOINT**: endpoint to the cloud storage
  * **STORAGE_LOCATION**: location/region to the cloud storage
  * **STORAGE_CDN**: CND url to access the stored objects

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