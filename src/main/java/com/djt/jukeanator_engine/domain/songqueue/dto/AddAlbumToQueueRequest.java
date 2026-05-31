package com.djt.jukeanator_engine.domain.songqueue.dto;

import java.util.Objects;

public class AddAlbumToQueueRequest {

  private Integer albumId;
  private Integer priority;

  public AddAlbumToQueueRequest(Integer albumId, Integer priority) {
    this.albumId = albumId;
    this.priority = priority;
  }

  public Integer getAlbumId() {
    return albumId;
  }

  public Integer getPriority() {
    return priority;
  }

  @Override
  public int hashCode() {
    return Objects.hash(albumId, priority);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AddAlbumToQueueRequest other = (AddAlbumToQueueRequest) obj;
    return Objects.equals(albumId, other.albumId) && Objects.equals(priority, other.priority);
  }

  @Override
  public String toString() {
    return "AddAlbumToQueueRequest [albumId=" + albumId + ", priority=" + priority + "]";
  }
}