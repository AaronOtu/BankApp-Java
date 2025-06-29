# bankapp

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_** Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/bankapp-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

# 🏦 BankApp

This is a simple banking application built with **Quarkus**, the Supersonic Subatomic Java Framework. It uses **JDBC and raw SQL** (no ORM) to perform banking operations like creating accounts, retrieving account details, and transferring funds.

If you want to learn more about Quarkus, please visit its website: [https://quarkus.io](https://quarkus.io).

---

## 🚀 Features

- Create and retrieve bank accounts
- Transfer funds between accounts
- View account balances
- Uses raw SQL (no Hibernate/ORM)
- Modular code structure (DTO, Model, Repository, Service)

---

## 📁 Project Structure

src/
└── main/
├── java/org/gs/
│ ├── controller/ # REST endpoints
│ ├── service/ # Business logic
│ ├── repository/ # SQL query layer using JDBC
│ ├── model/ # POJOs that mirror DB tables
│ └── dto/ # Input/output data transfer objects
└── resources/
├── application.properties # Configuration (e.g., DB connection)
└── import.sql

✅ TODO
• Add delete account feature
• Implement transaction logs
• Add unit and integration tests
• Add Docker support

# deploying using docker
1.run the command below to package app in JVM mode
 ./mvnw package -DskipTests
2. build the image pointing to the generated dockerfile by quarkus and provide the name of the inage 
 docker build -f src/main/docker/Dockerfile.jvm -t my-quarkus-app .
3. run the image
docker run -p 8080:8080 my-quarkus-app 