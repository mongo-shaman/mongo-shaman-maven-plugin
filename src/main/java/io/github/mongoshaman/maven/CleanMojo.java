package io.github.mongoshaman.maven;

import org.apache.maven.plugins.annotations.Mojo;

import io.github.mongoshaman.core.Shaman;

@Mojo(name = "clean")
public class CleanMojo extends AbstractMongoShamanMojo {

  protected void doExecute(Shaman shaman) {
    getLog().info("Cleaning...");
    shaman.clean();
  }

}
