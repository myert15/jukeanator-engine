package com.djt.jukeanator_engine.domain.songqueue.dto;

import com.djt.jukeanator_engine.domain.songlibrary.dto.SongDto;

public class SongQueueEntryDto {

  private SongDto song;
  private Integer priority;
  private String songPath;

  public SongQueueEntryDto(
      SongDto song,
      Integer priority,
      String songPath) {
    
    this.song = song;
    this.priority = priority;
    this.songPath = songPath;
  }

  public SongDto getSong() {
    return song;
  }

  public Integer getPriority() {
    return priority;
  }
  
  public String getSongPath() {
    return songPath;
  }  
}
