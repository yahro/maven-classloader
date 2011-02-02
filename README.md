maven-classloader
---

Given a [Maven][1] GAV, the Maven Classloader will resolve and download all dependencies from local and remote maven repositories.

This library utilizes [Sonatype Aether][2], the library used by Maven to deal with repositories. Aether does all of the heavy lifting, this library aims to be a lightweight shim on top of it to help reduce the friction for a majority of potential uses.

[1]: http://maven.apache.org/ "Apache Maven"
[2]: http://aether.sonatype.org/ "Sonatype Aether Product Page"

### Getting Started (Maven)

Until this is put into a maven repo, download source and build a `.jar` with Maven: `mvn clean install`

Add the following to your `pom.xml`:

    <dependency>
        <groupId>com.bigfatgun</groupId>
        <artifactId>maven-classloader</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

Create a ClassLoader:

    ClassLoader classLoader = MavenClassloader.forGAV("junit:junit:4.8.1");

You're done! Assuming you wanted the classloader for a reason, such as loading a class, you just use normal reflection:

    Class<?> junitAssertClass = classLoader.loadClass("org.junit.Assert");

