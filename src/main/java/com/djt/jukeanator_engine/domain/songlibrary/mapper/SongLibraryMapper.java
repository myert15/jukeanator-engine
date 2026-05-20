package com.djt.jukeanator_engine.domain.songlibrary.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.djt.jukeanator_engine.domain.songlibrary.dto.AlbumDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.ArtistDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.SongDto;
import com.djt.jukeanator_engine.domain.songlibrary.model.AlbumFolderEntity;
import com.djt.jukeanator_engine.domain.songlibrary.model.ArtistFolderEntity;
import com.djt.jukeanator_engine.domain.songlibrary.model.SongFileEntity;

/**
 * @author tmyers
 */
public final class SongLibraryMapper {

  public static List<ArtistDto> toArtistDtoList(Collection<ArtistFolderEntity> artistEntities) {
    
    List<ArtistDto> artistDtos = new ArrayList<>();
    for (ArtistFolderEntity artistEntity: artistEntities) {
      
      artistDtos.add(new ArtistDto(
          artistEntity.getName(),
          SongLibraryMapper.toAlbumDtoList(artistEntity.getAlbums())));
    }    
    return artistDtos;
  }
  
  public static List<AlbumDto> toAlbumDtoList(Collection<AlbumFolderEntity> albumEntities) {
    
    List<AlbumDto> albumDtos = new ArrayList<>();
    for (AlbumFolderEntity albumEntity: albumEntities) {
      
      albumDtos.add(new AlbumDto(
          albumEntity.getPersistentIdentity(), 
          albumEntity.getName(),
          albumEntity.getParentGenre().getName(),
          albumEntity.getParentArtist().getName(), 
          albumEntity.hasExplicit(), 
          albumEntity.getRecordLabel(),
          albumEntity.getReleaseDate(), 
          albumEntity.getCoverArtPath(), 
          SongLibraryMapper.toSongDtoList(albumEntity.getChildSongs())));
    }
    return albumDtos;
  }
  
  public static List<SongDto> toSongDtoList(Collection<SongFileEntity> songEntities) {
    
    List<SongDto> songDtos = new ArrayList<>();
    for (SongFileEntity songEntity: songEntities) {
      
      AlbumFolderEntity album = songEntity.getAlbum();
      ArtistFolderEntity artist = album.getParentArtist();
      
      songDtos.add(new SongDto(
          artist.getPersistentIdentity(),
          artist.getName(),
          album.getPersistentIdentity(),
          album.getName(),
          album.getCoverArtPath(),
          songEntity.getPersistentIdentity(),
          songEntity.getSongName(),
          songEntity.getNumPlays()));
    }
    return songDtos;
  }  
}