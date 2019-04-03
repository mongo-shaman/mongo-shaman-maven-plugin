package io.github.mongoshaman.maven;

import io.github.mongoshaman.core.Shaman;
import io.github.mongoshaman.core.ShamanFactory;
import io.github.mongoshaman.core.configuration.ShamanConfiguration;
import io.github.mongoshaman.core.configuration.ShamanFluentConfiguration;
import io.github.mongoshaman.core.configuration.ShamanProperties;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

@Mojo(name = "migrate") public class MigrateMojo extends AbstractMojo {

  @Parameter(defaultValue = "test-mojo", required = true, readonly = true) String database;

  @Parameter(defaultValue = "${project}", readonly = true, required = true) MavenProject mavenProject;

  public void execute() throws MojoExecutionException, MojoFailureException {
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
    }

    ShamanConfiguration configuration = new ShamanFluentConfiguration().setDatabaseName(database).setLocation(
      "filesystem:" + mavenProject.getBasedir().getAbsolutePath() + "/src/main/resources/" + ShamanProperties.LOCATION
        .getNullSafeValue());

    getLog().info("Instantiating and connecting to db...");
    final String stringConnection = "mongodb://localhost:27017/" + configuration.getDatabaseName();

    getLog().debug("Trying to connect to " + stringConnection);
    final Shaman shaman = ShamanFactory.getInstance(stringConnection, configuration);

    getLog().info("Migrating...");
    shaman.migrate();
  }

}
