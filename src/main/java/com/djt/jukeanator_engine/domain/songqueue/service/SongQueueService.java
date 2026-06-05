package com.djt.jukeanator_engine.domain.songqueue.service;

import java.util.List;
import com.djt.jukeanator_engine.domain.songqueue.dto.AddAlbumToQueueRequest;
import com.djt.jukeanator_engine.domain.songqueue.dto.AddMultipleSongsToQueueRequest;
import com.djt.jukeanator_engine.domain.songqueue.dto.AddSongToQueueRequest;
import com.djt.jukeanator_engine.domain.songqueue.dto.SongQueueEntryDto;

/**
 * @author tmyers
 */
public interface SongQueueService {
  
  /**
   * 
   * @return a priority value that is one higher than the highest priority that is currently in the queue.
   * For example, if the largest priority value of a song that is currently in the queue is 2, then return 3 
   * If there are no songs in the queue, then return 2 (a random song will be always be priority 0 and a 
   * normal cost user selected song will always be priority 1).
   */
  Integer getHighestPriority();

  /**
   * 
   * @return
   */
  List<SongQueueEntryDto> getQueuedSongs();

  /**
   * @param addSongToQueueRequest
   * @return
   */
  SongQueueEntryDto addSongToQueue(AddSongToQueueRequest addSongToQueueRequest);

  /**
   * 
   * @param addAlbumToQueueRequest
   * @return
   */
  List<SongQueueEntryDto> addAlbumToQueue(AddAlbumToQueueRequest addAlbumToQueueRequest);
  
  /**
   * 
   * @param addMultipleSongsToQueueRequest
   * @return
   */
  List<SongQueueEntryDto> addMultipleSongsToQueue(AddMultipleSongsToQueueRequest addMultipleSongsToQueueRequest);
  
  /**
   * @return
   */
  Integer flushQueue();

  /**
   * @return
   */
  Integer randomizeQueue();
  
  /**
   * 
   * @return
   */
  SongQueueEntryDto dequeueNextSong();
}
