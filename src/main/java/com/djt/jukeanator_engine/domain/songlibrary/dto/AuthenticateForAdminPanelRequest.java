package com.djt.jukeanator_engine.domain.songlibrary.dto;

public class AuthenticateForAdminPanelRequest {

  private String username;
  private String password;

  public AuthenticateForAdminPanelRequest(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public String toString() {
    return "DownloadAlbumCoverArtRequest [username=" + username + "]";
  }
}
