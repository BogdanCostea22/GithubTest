# springboot-sample-app

[![Build Status](https://travis-ci.org/codecentric/springboot-sample-app.svg?branch=master)](https://travis-ci.org/codecentric/springboot-sample-app)


I build this app using [Spring Boot](http://projects.spring.io/spring-boot/) in a combination with Kotlin and coroutines.
## Requirements

For building and running the application you need:

- [JDK 1.8](https://www.oracle.com/java/technologies/downloads/#java17)
- [Kotlin 1.6.21](https://kotlinlang.org/)

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `ro.githubdemo.demo.DemoApplication` class from your IDE.

Alternatively you can use the [Spring Boot Gradle plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins.html#build-tool-plugins.gradle) like so:

```shell
./gradlew bootRun
```

## Create Docker image

The easiest way to create a docker image is using the [jib-gradle-plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin), but the docker daemon needs to run:

```shell
./gradlew jibDockerBuild
```

Another option is using the defined Dokerfile but two commands are needed
```shell
./gradlew bootjar
docker build -t github-proxy/test . 
```


## Api Documentation
The api documentation can be found [here](http://localhost:8080/api-docs) after you run the app localy

## Test 
The functionality of the app was covered by unit test and integration tests. In order to test it you need to run the following command:

```shell
./gradlew test
```

## Other Information
In the requirements of the challenged was mentioned that the returned message for the errors needs to have 406 or 404 messages. I saw this redundancy, And after I talked with Vlad we decided to slightly change the format.
```
HTTP status code: 404
HTTP body: {“status”: 404, “Message”: ${whyHasItHappened_responseMessageFromGithubApisResponseBody} }
```
So for each error response we will have the format depicted above.