# Postman Collection Builder

The postman collection builder captures http requests using Unirest's interceptor and 
builds a collection based on the postman schema

This project demonstrates building structured collections based on unique paths, and custom Country and City request headers

## Requirements
- Java 11
- Maven
- Unirest

## Build and Run Tests
Build - `mvn clean test-compile -U`

Run Tests - `mvn clean test`

## How to use
This project can be used as part of any API automation framework that uses Unirest as the underlying http library for making requests.
For example, this can be easily used with frameworks such as Gauge and Cucumber.

It can be enabled by toggling `POSTMAN_COLLECTION = true` in .properties and then building collections as part of after execution hooks. 

See example of hook implementation below:

`TODO`

## Improvements
- Make implementation generic so that any number of headers of any type can be passed
- Better pattern matching on identifying unique paths that include path parameters
- Add redaction of sensitive data
- Build and publish artifact to maven
- Add checkstyle 