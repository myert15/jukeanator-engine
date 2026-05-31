package com.djt.jukeanator_engine.domain.songqueue.dto;

import java.util.List;
import java.util.Objects;

public class AddMultipleSongsToQueueRequest {

  private List<SongIdentifier> songIdentifiers;
  private Integer priority;

  public AddMultipleSongsToQueueRequest(List<SongIdentifier> songIdentifiers, Integer priority) {
    this.songIdentifiers = songIdentifiers;
    this.priority = priority;
  }

  public List<SongIdentifier> getSongIdentifiers() {
    return songIdentifiers;
  }

  public Integer getPriority() {
    return priority;
  }

  @Override
  public int hashCode() {
    return Objects.hash(priority, songIdentifiers);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AddMultipleSongsToQueueRequest other = (AddMultipleSongsToQueueRequest) obj;
    return Objects.equals(priority, other.priority)
        && Objects.equals(songIdentifiers, other.songIdentifiers);
  }

  @Override
  public String toString() {
    return "AddMultipleSongsToQueueRequest [songIdentifiers=" + songIdentifiers + ", priority="
        + priority + "]";
  }

}
