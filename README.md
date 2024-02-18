# TUI Microsevices coding challenge


## App description
The service exposes a GET endpoint `/api/v1/repos/{username}` which returns
all public repositories of a github user which are not forks.

## Architecture
A Spring Boot reactive was chosen as a framework. Outgoing requests are performed with `org.springframework.web.reactive.function.client.WebClient` wrapped in suspend functions. Incoming requests are handled by kotlin coroutines, which are supported by Spring Boot.

## Sample requests
Sample requests for manual testing can be found in the src/test/resources/github.http file

## Running

### Run a docker image

```
./gradlew bootBuildImage
docker run -p8080:8080 pl.jacekgajek/tui
```

### Run a native executable
Use `sdk` tool to configure Java runtime.

```
sdk install java 21.0.2-graal
sdk use java 21.0.2-graal
./gradlew nativeRun
```
This will generate a native executable which can be started with

```
./build/native/nativeCompile/github
```

### Run as a Java app which requires JRE.

```
./gradlew jar
java -jar build/libs/github-0.0.1-SNAPSHOT.jar
```

## API documentation

Run the app and go to http://localhost:8080/swagger-ui/index.html for Swagger UI or http://localhost:8080/v3/api-docs 
for up-to-date openapi specs. Copy of documentation in this file: [openapi.yaml](openapi.yaml)
