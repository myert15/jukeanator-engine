package com.djt.jukeanator_engine.domain.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "user")
public class UserProperties {

  private String repositoryType; // "filesystem" or "postgres"
  private String rootPath;

  public String getRepositoryType() {
    return repositoryType;
  }

  public String getRootPath() {
    return rootPath;
  }

  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  public void setRepositoryType(String repositoryType) {
    this.repositoryType = repositoryType;
  }
}
