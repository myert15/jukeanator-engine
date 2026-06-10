package com.djt.jukeanator_engine.domain.songlibrary.model;

import static java.util.Objects.requireNonNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.djt.jukeanator_engine.domain.common.exception.EntityDoesNotExistException;
import com.djt.jukeanator_engine.domain.common.utils.OperatingSystemDetector;
import com.djt.jukeanator_engine.domain.common.utils.OperatingSystemDetector.OSType;

public class RootFolderEntity extends FolderEntity {
  private static final long serialVersionUID = 1L;

  private static final String CD_STATS_FILE_PREFIX = "CDStats";
  private static final String CD_STATS_FILE_SUFFIX = ".TXT";

  private String rootPrefix;

  private transient Map<Integer, GenreFolderEntity> genresMap;
  private transient Map<GenreFolderEntity, Set<AlbumFolderEntity>> albumsByGenreMap;
  private transient Map<Integer, ArtistFolderEntity> artistsMap;
  private transient Map<Integer, AlbumFolderEntity> albumsMap;
  private transient Map<String, SongFileEntity> songsMap;

  // Used only by SongQueueService.loadPlaylistIntoQueue()
  private transient Map<String, SongFileEntity> songsByPathMap;

  public RootFolderEntity() {}

  /**
   * Windows: C:\Users\Admin\Music rootPrefix is: C:\ Linux: /Users/Admin/Music rootPrefix is: /
   * * @param rootPrefix
   * 
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
        allAlbums.add((AlbumFolderEntity) childFolder);
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

  public Collection<GenreFolderEntity> getGenres() {
    return genresMap.values();
  }

  public Collection<AlbumFolderEntity> getAlbumsForGenre(Integer genreId) {
    GenreFolderEntity genre = genresMap.get(genreId);
    return albumsByGenreMap.get(genre);
  }

  public Collection<ArtistFolderEntity> getArtists() {
    return artistsMap.values();
  }

  public Collection<AlbumFolderEntity> getAlbums() {
    return albumsMap.values();
  }

  public Collection<SongFileEntity> getSongs() {
    return songsMap.values();
  }

  public void initialize() {

    this.genresMap = new HashMap<>();
    this.albumsByGenreMap = new HashMap<>();
    this.artistsMap = new HashMap<>();
    this.albumsMap = new HashMap<>();
    this.songsMap = new HashMap<>();

    for (AlbumFolderEntity album : getAllAlbums()) {

      GenreFolderEntity genre = album.getParentGenre();
      Integer genreId = genre.getPersistentIdentity();
      if (!this.genresMap.containsKey(genreId)) {
        this.genresMap.put(genreId, genre);
      }

      Set<AlbumFolderEntity> genreAlbums = null;
      if (!this.albumsByGenreMap.containsKey(genre)) {
        genreAlbums = new HashSet<>();
        genreAlbums.add(album);
        this.albumsByGenreMap.put(genre, genreAlbums);
      } else {
        genreAlbums = this.albumsByGenreMap.get(genre);
        if (!genreAlbums.contains(album)) {
          genreAlbums.add(album);
        }
      }

      ArtistFolderEntity artist = album.getParentArtist();
      Integer artistId = artist.getPersistentIdentity();
      if (!this.artistsMap.containsKey(artistId)) {
        this.artistsMap.put(artistId, artist);
      }

      Integer albumId = album.getPersistentIdentity();
      if (!this.albumsMap.containsKey(albumId)) {
        this.albumsMap.put(albumId, album);
      }

      for (SongFileEntity song : album.getChildSongs()) {
        this.songsMap.put(buildSongKey(albumId, song.getPersistentIdentity()), song);
      }
    }
  }

  public void restoreSongNumPlays(String scanPath) {

    String cdStatsPathName = null;
    OSType osType = OperatingSystemDetector.getOperatingSystem();
    if (osType == OSType.WINDOWS) {
      cdStatsPathName = scanPath + File.separator + CD_STATS_FILE_PREFIX + CD_STATS_FILE_SUFFIX;
    } else if (osType == OSType.MACOS) {
      cdStatsPathName =
          scanPath + File.separator + CD_STATS_FILE_PREFIX + "_mac" + CD_STATS_FILE_SUFFIX;
    } else {
      cdStatsPathName =
          scanPath + File.separator + CD_STATS_FILE_PREFIX + "_linux" + CD_STATS_FILE_SUFFIX;
    }

    Path statsFile = Path.of(cdStatsPathName);
    if (!Files.exists(statsFile)) {
      cdStatsPathName = scanPath + File.separator + CD_STATS_FILE_PREFIX + CD_STATS_FILE_SUFFIX;
      statsFile = Path.of(cdStatsPathName);
      if (!Files.exists(statsFile)) {
        System.err.println("CD stats file does not exist: " + cdStatsPathName);
        return;
      }
    }

    if (this.songsMap == null) {
      initialize();
    }

    // FIX: Populate a case-insensitive dictionary map for keys to eliminate
    // readyNAS vs ReadyNAS mount discrepancies.
    Map<String, SongFileEntity> songsByPath = new HashMap<>();
    Map<String, SongFileEntity> songsByName = new HashMap<>();
    for (SongFileEntity song : this.songsMap.values()) {

      String songPathname = song.getNaturalIdentity().toLowerCase();
      songsByPath.put(songPathname, song);

      String songName = song.getName().toLowerCase();
      songsByName.put(songName, song); // Used as a fallback
    }

    // Line format (after trim): "<numPlays> <ignored> <ignored> <songPath> [optional extras]"
    // Splitting on whitespace with limit=4 keeps the path (which may contain spaces) intact in
    // the fourth token, while any trailing tokens beyond the path are automatically discarded.
    // The old single-regex approach broke on paths that contain spaces (e.g. "Top Gun").
    // Added \s* at the start to catch leading spaces/indentations in log files
    Pattern statsLinePattern = Pattern.compile("^\\s*(\\d+)(?:\\s+\\S+){2}\\s+(.+)$");

    int restoredCount = 0;
    try (BufferedReader reader = Files.newBufferedReader(statsFile, StandardCharsets.UTF_8)) {

      String line;
      while ((line = reader.readLine()) != null) {

        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }

        Matcher matcher = statsLinePattern.matcher(line);
        if (!matcher.matches()) {
          System.err.println("Skipping malformed CD stats line: " + line);
          continue;
        }

        try {
          int numPlays = Integer.parseInt(matcher.group(1));
          if (numPlays > 0) {
            String songPath = matcher.group(2).strip();

            SongFileEntity song = songsByPath.get(songPath.toLowerCase());
            if (song != null) {

              song.setNumPlays(numPlays);

            } else {

              // Fallback strategy: If path maps changed completely, try to resolve by filename
              // alone
              int lastSlash = songPath.lastIndexOf('/');
              if (lastSlash != -1) {

                String songNameOnly = songPath.substring(lastSlash + 1).toLowerCase();
                song = songsByName.get(songNameOnly);
                if (song != null) {

                  song.setNumPlays(numPlays);

                } else {
                  System.err.println("Could not find song: " + songPath);
                }
              }

            }
          }
          restoredCount++;

        } catch (NumberFormatException e) {
          System.err.println("Could not parse num plays from line: " + line);
        } catch (Exception e) {
          System.err.println("Could not restore song stats from line: " + line);
          e.printStackTrace();
        }
      }
      System.out
          .println("Restored num plays processing completed for " + restoredCount + " log lines.");

    } catch (IOException e) {
      System.err.println("Failed to restore song num plays from CD Stats file: " + cdStatsPathName);
      e.printStackTrace();
    }
  }

  public void resetSongStatistics() {
    if (this.songsMap == null) {
      initialize();
    }
    for (SongFileEntity song : this.songsMap.values()) {
      song.setNumPlays(0);
    }
  }

  public GenreFolderEntity getGenreById(Integer id) throws EntityDoesNotExistException {
    GenreFolderEntity entity = genresMap.get(id);
    if (entity != null) {
      return entity;
    }
    throw new EntityDoesNotExistException("Genre with id: [" + id + "] not found.");
  }

  public ArtistFolderEntity getArtistById(Integer id) throws EntityDoesNotExistException {
    ArtistFolderEntity entity = artistsMap.get(id);
    if (entity != null) {
      return entity;
    }
    throw new EntityDoesNotExistException("Artist with id: [" + id + "] not found.");
  }

  public AlbumFolderEntity getAlbumById(Integer id) throws EntityDoesNotExistException {
    AlbumFolderEntity entity = albumsMap.get(id);
    if (entity != null) {
      return entity;
    }
    throw new EntityDoesNotExistException("Album with id: [" + id + "] not found.");
  }

  public SongFileEntity getSongById(Integer albumId, Integer songId)
      throws EntityDoesNotExistException {

    String songKey = buildSongKey(albumId, songId);
    SongFileEntity entity = songsMap.get(songKey);
    if (entity != null) {
      return entity;
    }
    throw new EntityDoesNotExistException(
        "Song with songId: [" + songId + "] and albumId: [" + albumId + "] not found.");
  }

  public SongFileEntity getSongByPath(String songPathname) throws EntityDoesNotExistException {

    if (songsByPathMap == null) {
      initializeSongsByPathMap();
    }

    SongFileEntity entity = songsByPathMap.get(songPathname);
    if (entity != null) {
      return entity;
    }
    throw new EntityDoesNotExistException("Song with path: [" + songPathname + "] not found.");
  }

  private void initializeSongsByPathMap() {
    this.songsByPathMap = new HashMap<>();
    for (SongFileEntity song : this.songsMap.values()) {
      String songPathname = song.getNaturalIdentity();
      if (songPathname != null && !this.songsByPathMap.containsKey(songPathname)) {
        this.songsByPathMap.put(songPathname, song);
      }
    }
  }

  private String buildSongKey(Integer albumId, Integer songId) {

    return albumId.toString() + "__" + songId.toString();
  }
}
