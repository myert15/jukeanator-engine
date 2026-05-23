package com.djt.jukeanator_engine.domain.songlibrary.dto;

import java.util.List;
import java.util.Objects;

public class ArtistDto {
  
  private Integer artistId;
  private String artistName;
  private List<AlbumDto> albums;
  
  public ArtistDto(
      Integer artistId,
      String artistName, 
      List<AlbumDto> albums) {
    super();
    this.artistId = artistId;
    this.artistName = artistName;
    this.albums = albums;
  }
  
  public Integer getArtistId() {
    return artistId;
  }

  public String getArtistName() {
    return artistName;
  }

  public List<AlbumDto> getAlbums() {
    return albums;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(artistId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ArtistDto other = (ArtistDto) obj;
    return Objects.equals(artistId, other.artistId);
  }

  @Override
  public String toString() {
    return "ArtistDto []";
  }

  public String getCoverArtPath() {
    
    if (!albums.isEmpty()) {
      return albums.get(0).getCoverArtPath();
    }
    return "";
  }

  public Integer getAlbumCount() {
    return Integer.valueOf(albums.size());
  }
  
  public Integer getSongCount() {
    
    int songCount = 0;
    for (AlbumDto album: albums) {

      songCount = songCount + album.getSongs().size();
    }
    return Integer.valueOf(songCount);
  }
  
  public Integer getNumPlays() {
    
    int numPlays = 0;
    for (AlbumDto album: albums) {

      numPlays = numPlays + album.getNumPlays();
    }
    return Integer.valueOf(numPlays);
  }  
}