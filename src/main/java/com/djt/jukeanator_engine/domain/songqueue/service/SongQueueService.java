package com.djt.jukeanator_engine.domain.songqueue.service;

import java.util.List;
import com.djt.jukeanator_engine.domain.songqueue.dto.AddMultipleSongsToQueueRequest;
import com.djt.jukeanator_engine.domain.songqueue.dto.AddSongToQueueRequest;
import com.djt.jukeanator_engine.domain.songqueue.dto.SongQueueEntryDto;

/**
 * @author tmyers
 */
public interface SongQueueService {

  /**
   * 
   * @return
   */
  List<SongQueueEntryDto> getQueuedSongs();

  /**
   * @param addSongToQueueRequest
   * @return
   */
  Integer addSongToQueue(AddSongToQueueRequest addSongToQueueRequest);
  
  /**
   * 
   * @param addMultipleSongsToQueueRequest
   * @return
   */
  List<Integer> addMultipleSongsToQueue(AddMultipleSongsToQueueRequest addMultipleSongsToQueueRequest);
  
  /**
   * 
   * @return
   */
  SongQueueEntryDto dequeueNextSong();
}
