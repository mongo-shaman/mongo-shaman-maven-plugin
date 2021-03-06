package io.github.mongoshaman.maven;

import org.apache.maven.plugins.annotations.Mojo;

import io.github.mongoshaman.core.Shaman;

@Mojo(name = "migrate")
public class MigrateMojo extends AbstractMongoShamanMojo {

  protected void doExecute(Shaman shaman) {
    getLog().info("Migrating...");
    shaman.migrate();
  }

}
