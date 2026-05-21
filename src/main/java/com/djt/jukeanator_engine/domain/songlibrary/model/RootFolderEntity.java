package com.djt.jukeanator_engine.domain.songlibrary.model;

import static java.util.Objects.requireNonNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class RootFolderEntity extends FolderEntity {
  private static final long serialVersionUID = 1L;
  
  private String rootPrefix;

  private transient List<String> genres;
  private transient List<ArtistFolderEntity> artists;
  private transient List<AlbumFolderEntity> albums;
  private transient List<SongFileEntity> songs;
  
  public RootFolderEntity() {}

  /**
   * Windows: C:\Users\Admin\Music rootPrefix is: C:\
   * 
   * Linux: /Users/Admin/Music rootPrefix is: /
   * 
   * @param rootPrefix
   * @param name
   */
  public RootFolderEntity(String rootPrefix, String name) {
    super(null, name);
    requireNonNull(rootPrefix, "rootPrefix cannot be null");
    this.rootPrefix = rootPrefix;
  }

  public String getRootPrefix() {
    return this.rootPrefix;
  }

  public Set<FolderEntity> pruneNonAlbumContainingChildFolders() {

    Set<FolderEntity> foldersToPrune = new TreeSet<>();

    for (FolderEntity childFolder : getChildFolders()) {

      if (childFolder.getChildFolders().isEmpty()
          && childFolder instanceof AlbumFolderEntity == false) {

        System.out.println("Pruning candidate: " + childFolder.getName());
        foldersToPrune.add(childFolder);

      } else {

        childFolder.pruneNonAlbumContainingChildFolders(foldersToPrune);

      }
    }

    if (!foldersToPrune.isEmpty()) {

      for (FolderEntity folderToPrune : foldersToPrune) {

        System.out.println("Pruning: " + folderToPrune.getNaturalIdentity());
        FolderEntity parentFolder = folderToPrune.getParentFolder();
        while (parentFolder != null && folderToPrune.getChildFolders().isEmpty()) {

          boolean removed = parentFolder.removeChild(folderToPrune);
          if (!removed) {
            System.err.println("Could not remove: " + folderToPrune.getName());
          }

          folderToPrune = parentFolder;
          if (parentFolder instanceof RootFolderEntity == false) {
            parentFolder = parentFolder.getParentFolder();
          } else {
            parentFolder = null;
          }
        }
      }
    }

    return foldersToPrune;
  }

  public List<AlbumFolderEntity> getAllAlbums() {
    
    Set<AlbumFolderEntity> allAlbums = new TreeSet<>();
    
    for (FolderEntity childFolder : getChildFolders()) {

      if (childFolder instanceof AlbumFolderEntity) {

        allAlbums.add((AlbumFolderEntity)childFolder);

      } else {

        childFolder.getAllAlbums(allAlbums);

      }
    }
    
    ArrayList<AlbumFolderEntity> list = new ArrayList<>();
    list.addAll(allAlbums);    
    return list;
  }

  @Override
  public FolderEntity getParentFolder() {
    throw new IllegalStateException("getParentFolder() cannot be called on the Root");
  }
  
  @Override
  public String getNaturalIdentity() {

    StringBuilder sb = new StringBuilder();
    if (this.rootPrefix != null) {
      sb.append(this.rootPrefix);
    } else {
      sb.append(File.separatorChar);
    }
    sb.append(getName());
    return sb.toString();
  }

  public List<String> getGenres() {
    return genres;
  }

  public List<ArtistFolderEntity> getArtists() {
    return artists;
  }

  public List<AlbumFolderEntity> getAlbums() {
    return albums;
  }

  public List<SongFileEntity> getSongs() {
    return songs;
  }
  
  public void initialize() {
    
    this.genres = new ArrayList<>();
    this.artists = new ArrayList<>();
    this.albums = getAllAlbums();
    this.songs = new ArrayList<>();
    
    for (AlbumFolderEntity album : this.albums) {
                
      String genre = album.getParentGenre().getName();
      if (!this.genres.contains(genre)) {
        this.genres.add(genre);
      }
      
      ArtistFolderEntity artist = album.getParentArtist();
      if (!this.artists.contains(artist)) {
          this.artists.add(artist);
      }
      
      this.songs.addAll(album.getChildSongs());
    }    
  }
}
