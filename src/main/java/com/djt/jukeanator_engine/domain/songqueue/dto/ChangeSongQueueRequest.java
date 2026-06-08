package com.djt.jukeanator_engine.domain.songqueue.dto;

public class ChangeSongQueueRequest {

  private Integer albumId;
  private Integer songId;

  public ChangeSongQueueRequest() {}

  public ChangeSongQueueRequest(Integer albumId, Integer songId) {
    this.albumId = albumId;
    this.songId = songId;
  }

  public Integer getAlbumId() {
    return albumId;
  }

  public Integer getSongId() {
    return songId;
  }
}
