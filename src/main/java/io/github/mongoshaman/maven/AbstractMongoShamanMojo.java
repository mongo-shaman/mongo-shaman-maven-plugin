package io.github.mongoshaman.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import com.mongodb.MongoClientURI;

import io.github.mongoshaman.core.Shaman;
import io.github.mongoshaman.core.ShamanFactory;
import io.github.mongoshaman.core.configuration.ShamanDefaultProperties;
import io.github.mongoshaman.core.configuration.ShamanFluentConfiguration;

public abstract class AbstractMongoShamanMojo extends AbstractMojo {

  private static final String PROPERTY_PRINT = "{0}: {1}";

  @Parameter
  private String database;

  @Parameter
  private String connectionString;

  @Parameter
  private String location;

  @Parameter
  private String collectionName;

  @Parameter(defaultValue = "false")
  private boolean verbose;

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject mavenProject;

  public void execute() throws MojoExecutionException {
    loadClasspath();
    checkProperties();

    final Shaman shaman = ShamanFactory.getInstance(connectionString,
        new ShamanFluentConfiguration().database(database).location(location).collectionName(collectionName));

    doExecute(shaman);

    printFinished();

  }

  protected abstract void doExecute(Shaman shaman);

  private void loadClasspath() throws MojoExecutionException {
    try {
      Set<String> classpathElements = new HashSet<>();
      classpathElements.addAll(mavenProject.getCompileClasspathElements());
      classpathElements.addAll(mavenProject.getRuntimeClasspathElements());

      ClassRealm classLoader = (ClassRealm) Thread.currentThread().getContextClassLoader();
      for (String classpathElement : classpathElements) {
        classLoader.addURL(new File(classpathElement).toURI().toURL());
      }
    } catch (MalformedURLException | DependencyResolutionRequiredException e) {
      getLog().error(e);
      throw new MojoExecutionException("Classpath could not be loaded", e);
    }
  }

  private void checkProperties() throws MojoExecutionException {
    if (connectionString != null) {
      database = new MongoClientURI(connectionString).getDatabase();
    } else {
      if (database == null) {
        throw new MojoExecutionException("No connection properties have defined (connectionString or database)");
      }
      connectionString = "mongodb://localhost/" + database;
    }
    printProperty("database", connectionString);

    location = Optional.ofNullable(location).orElse(mavenProject.getBasedir().getAbsolutePath() + "/src/main/resources/"
        + ShamanDefaultProperties.LOCATION.getValue());
    printProperty("location", location);

    collectionName = Optional.ofNullable(collectionName).orElse(ShamanDefaultProperties.COLLECTION_NAME.getValue());
    printProperty("collectionName", collectionName);
  }

  private void printProperty(String database, String connectionString) {
    if (verbose) {
      getLog().info(MessageFormat.format(PROPERTY_PRINT, database, connectionString));
    }
  }

  private void printFinished() {
    getLog().info("");
    getLog().info("mongo-shaman has finished successfully");
    getLog().info("");
  }

}
