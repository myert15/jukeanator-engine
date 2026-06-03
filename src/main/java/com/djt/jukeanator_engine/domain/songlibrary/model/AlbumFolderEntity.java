package com.djt.jukeanator_engine.domain.songlibrary.model;

import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import com.djt.jukeanator_engine.domain.common.exception.EntityAlreadyExistsException;
import com.djt.jukeanator_engine.domain.common.exception.EntityDoesNotExistException;

public class AlbumFolderEntity extends FolderEntity implements LibraryItem {
  private static final long serialVersionUID = 1L;

  public static final String METADATA_FILENAME = "metadata.txt";
  public static final String COVER_ART_FILENAME = "cover.jpg";
  public static final String JPG_EXTENSION = ".jpg";

  private AlbumCoverArtFileEntity coverArt;
  private AlbumMetaDataFileEntity metaData;
  private Set<SongFileEntity> childSongs = new TreeSet<SongFileEntity>();
  
  private transient GenreFolderEntity parentGenre;
  private transient Year releaseDate;

  public AlbumFolderEntity() {}

  public AlbumFolderEntity(FolderEntity parentFolder, String name) {
    super(parentFolder, name);
  }

  public AlbumCoverArtFileEntity getCoverArt() {
    return this.coverArt;
  }

  public AlbumMetaDataFileEntity getMetaData() {
    return this.metaData;
  }

  @Override
  public Integer getNumPlays() {

    int numPlays = 0;
    for (SongFileEntity childSong : childSongs) {

      numPlays = numPlays + childSong.getNumPlays();
    }
    return Integer.valueOf(numPlays);
  }

  public boolean addChildSong(SongFileEntity childSong) throws EntityAlreadyExistsException {
    return addChild(childSongs, childSong, this);
  }

  public List<SongFileEntity> getChildSongs() {

    return childSongs.stream().sorted(Comparator.comparing(SongFileEntity::getTrackNumber,
        Comparator.nullsLast(Integer::compareTo))).toList();
  }

  public SongFileEntity getChildSong(Integer persistentIdentity)
      throws EntityDoesNotExistException {

    for (SongFileEntity childSong : childSongs) {
      if (childSong.getPersistentIdentity().equals(persistentIdentity)) {
        return childSong;
      }
    }
    throw new EntityDoesNotExistException("Child Song with persistentIdentity: ["
        + persistentIdentity + "] not found in [" + this.getNaturalIdentity() + "].");
  }

  public SongFileEntity getChildSongByName(String name) throws EntityDoesNotExistException {

    for (SongFileEntity childSong : childSongs) {
      if (childSong.getName().equals(name)) {
        return childSong;
      }
    }
    throw new EntityDoesNotExistException(
        "Child Song with name: [" + name + "] not found in [" + this.getNaturalIdentity() + "].");
  }

  public ArtistFolderEntity getParentArtist() {

    FolderEntity parentFolder = this.getParentFolder();
    while (parentFolder instanceof RootFolderEntity == false) {

      if (parentFolder instanceof ArtistFolderEntity) {
        return (ArtistFolderEntity) parentFolder;
      } else {
        parentFolder = parentFolder.getParentFolder();
      }
    }
    ArtistFolderEntity dummyArtist = new ArtistFolderEntity(parentFolder, "Compilations");
    dummyArtist.setPersistentIdentity(999999);
    return dummyArtist;
  }

  @Override
  public GenreFolderEntity getParentGenre() {

    if (parentGenre == null) {

      FolderEntity parentFolder = this.getParentFolder();
      while (parentFolder instanceof RootFolderEntity == false) {

        if (parentFolder instanceof GenreFolderEntity) {
          parentGenre = (GenreFolderEntity) parentFolder;
          break;
        } else {
          parentFolder = parentFolder.getParentFolder();
        }
      }
      if (parentGenre == null) {
        parentGenre = new GenreFolderEntity(parentFolder, "None");  
      }
    }
    return parentGenre;
  }

  public boolean hasValidCoverArt() {
    return this.coverArt.isValid();
  }

  public boolean hasValidMetadata() {
    return this.metaData.isValid();
  }

  public void createCoverArtEntity() {
    this.coverArt = new AlbumCoverArtFileEntity(this, COVER_ART_FILENAME);
  }

  public void createMetadataEntity() {
    this.metaData = new AlbumMetaDataFileEntity(this, METADATA_FILENAME);
  }

  public boolean hasExplicit() {
    return this.metaData.hasExplicit();
  }

  public String getRecordLabel() {
    return this.metaData.getRecordLabel();
  }

  @Override
  public Year getReleaseDate() {

    if (releaseDate == null) {

      String strReleaseDate = this.metaData.getReleaseDate();
      if (strReleaseDate == null || strReleaseDate.isEmpty()) {
        releaseDate = Year.parse("1985");
      } else {
        try {
          releaseDate = Year.parse(strReleaseDate);
        } catch (Exception e) {
          System.err.println(
              "Could not parse release date: " + strReleaseDate + " into Year: " + e.getMessage());
          releaseDate = Year.parse("1985");
        }
      }
    }
    return releaseDate;
  }
  
  @Override
  public String getTitle() {
    return this.getName();
  }

  public String getCoverArtPath() {
    return this.coverArt.getNaturalIdentity();
  }
}
