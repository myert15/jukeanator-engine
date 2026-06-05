package com.djt.jukeanator_engine.domain.songqueue.model;

import static java.util.Objects.requireNonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.djt.jukeanator_engine.domain.common.model.AbstractPersistentEntity;
import com.djt.jukeanator_engine.domain.songlibrary.model.SongFileEntity;

public class SongQueueRootEntity extends AbstractPersistentEntity {
  private static final long serialVersionUID = 2L;

  public static final String SONG_QUEUE_FILENAME = "JukeANator.PL";

  private String location;

  private ArrayList<SongQueueEntryEntity> songs = new ArrayList<>();

  public SongQueueRootEntity() {}

  public SongQueueRootEntity(String location) {
    super();
    requireNonNull(location, "location cannot be null");
    this.location = location;
  }

  public String getLocation() {
    return location;
  }

  @Override
  public String getNaturalIdentity() {
    return location;
  }

  public List<SongQueueEntryEntity> getSongs() {
    return songs;
  }

  public Integer flushQueue() {

    Integer numSongsFlushed = Integer.valueOf(this.songs.size());
    this.songs.clear();
    return numSongsFlushed;
  }

  public Integer randomizeQueue() {

    Collections.shuffle(songs);
    return Integer.valueOf(this.songs.size());
  }

  public SongQueueEntryEntity addSongToQueue(SongFileEntity song, Integer priority) {

    SongQueueEntryEntity entry = new SongQueueEntryEntity(song, priority);

    // Insert AFTER all songs with the same or higher priority, and BEFORE any song with a
    // strictly lower priority. Walk forward until we find the first entry whose priority
    // is less than the new song's priority; that position is our insertion point.
    int index = songs.size(); // default: append at end if no lower-priority song is found
    for (int i = 0; i < songs.size(); i++) {

      if (songs.get(i).getPriority() < priority) {
        index = i;
        break;
      }
    }

    songs.add(index, entry);
    return entry;
  }

  public boolean removeSongFromQueue(SongQueueEntryEntity songQueueEntry) {

    return this.songs.remove(songQueueEntry);
  }
}
