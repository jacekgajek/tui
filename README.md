# TUI Microsevices coding challenge


## App description
The service exposes a GET endpoint `/api/v1/repos/{username}` which returns
all public repositories of a github user which are not forks.

## Running

### Run a docker image

```
docker build -t pl.jacekgajek/tui .
docker run -p8081:8081 pl.jacekgajek/tui
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

Run the app and go to http://localhost:8081/swagger-ui/index.html for Swagger UI or http://localhost:8081/v3/api-docs 
for up-to-date openapi specs. Copy of documentation in this file: [openapi.yaml](openapi.yaml)
