package com.djt.jukeanator_engine.domain.songlibrary.dto;

public class DownloadAlbumCoverArtRequest {

  private Integer albumId;
  private String coverArtUrl;

  public DownloadAlbumCoverArtRequest(Integer albumId, String scanPath) {
    this.albumId = albumId;
    this.coverArtUrl = scanPath;
  }

  public Integer getAlbumId() {
    return albumId;
  }

  public String getCoverArtUrl() {
    return coverArtUrl;
  }

  @Override
  public String toString() {
    return "DownloadAlbumCoverArtRequest [albumId=" + albumId + ", coverArtUrl=" + coverArtUrl
        + "]";
  }
}
