include src/main/resources/application.properties

uber-jar:
	 mvn package -Dquarkus.package.type=uber-jar -DskipTests

start:
	./mvnw compile quarkus:dev

uber-jar-clean:
	mvn clean package -Dquarkus.package.type=uber-jar -DskipTests

