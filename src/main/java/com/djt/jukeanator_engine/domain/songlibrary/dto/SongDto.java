package com.djt.jukeanator_engine.domain.songlibrary.dto;

public class SongDto {
  
  private Integer songId;
  private String songName;
  private Integer numPlays;

  public SongDto(Integer songId, String songName, Integer numPlays) {
    this.songId = songId;
    this.songName = songName;
    this.numPlays = numPlays;
  }
  
  public Integer getSongId() {
    return songId;
  }

  public String getSongName() {
    return songName;
  }

  public Integer getNumPlays() {
    return numPlays;
  }
}
