# spring-jwt

The following was discovered as part of building this project:

* The original package name 'com.project.auth'.

# Run application locally

* Have java 11 and Maven installed
* Choose the local profile to launch the application with the properties of application-local.properties
    * Inside "Run/Debug Configurations" in the section "Active profile" set local
    * If the above option is not available in your IDE version within the VM Options section set "
      -Dspring.profiles.active=test"
* Inside application.properties there is the environment variable ${PWD}. Normally on Linux and Mac it is a reserved
  word to point to the current directory where the project folder is located. If your operating system does not
  recognize it, go to "Run/Debug Configurations" and in "Enviroment variables" create a variable called PWD and set it
  to any directory where you want the application logs to be stored.

# Swagger

* LINK: http://localhost:8085/api/swagger-ui/index.html
* To test userController endpoints, you can use the default user "username": "Admin", "password": "Admin"
* For the sending of emails to work, they must do what the comment added in the EmailConfiguration class indicates
* Create a new user by placing an email of yours to be able to test the sending of emails and the recovery of the
  password.

# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.3/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.5.3/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.5.3/reference/htmlsingle/#boot-features-developing-web-applications)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.5.3/reference/htmlsingle/#boot-features-jpa-and-spring-data)
* [Spring Security](https://docs.spring.io/spring-boot/docs/2.5.3/reference/htmlsingle/#boot-features-security)
* [Config Client Quick Start](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_client_side_usage)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/2.5.3/reference/htmlsingle/#production-ready)
* [Validation](https://docs.spring.io/spring-boot/docs/2.5.3/reference/htmlsingle/#boot-features-validation)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
