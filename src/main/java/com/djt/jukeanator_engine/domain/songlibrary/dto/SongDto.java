package com.djt.jukeanator_engine.domain.songlibrary.dto;

public class SongDto {

  private final Integer artistId;
  private final String artistName;
  private final Integer albumId;
  private final String albumName;
  private final String coverArtPath;
  private final Integer songId;
  private final String songName;
  private final Integer numPlays;

  public SongDto(
      Integer artistId, 
      String artistName, 
      Integer albumId, 
      String albumName,
      String coverArtPath, 
      Integer songId, 
      String songName, 
      Integer numPlays) {
    super();
    this.artistId = artistId;
    this.artistName = artistName;
    this.albumId = albumId;
    this.albumName = albumName;
    this.coverArtPath = coverArtPath;
    this.songId = songId;
    this.songName = songName;
    this.numPlays = numPlays;
  }

  public Integer getArtistId() {
    return artistId;
  }

  public String getArtistName() {
    return artistName;
  }

  public Integer getAlbumId() {
    return albumId;
  }

  public String getAlbumName() {
    return albumName;
  }

  public String getCoverArtPath() {
    return coverArtPath;
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
