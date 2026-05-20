package com.djt.jukeanator_engine.domain.songplayer.service;

import com.djt.jukeanator_engine.domain.songlibrary.dto.SongDto;
import com.djt.jukeanator_engine.domain.songplayer.dto.SongPlaybackStatusDto;

/**
 * @author tmyers
 */
public interface SongPlayerService {

  /**
   * 
   * @return
   */
  SongDto getNowPlayingSong();
  
  /**
   * 
   * @return
   */
  SongPlaybackStatusDto getPlaybackStatus();
  
  /**
   * 
   */
  void playNextTrack();

  /**
   * 
   */
  void pause();

  /**
   * 
   */
  void stop();  
}
