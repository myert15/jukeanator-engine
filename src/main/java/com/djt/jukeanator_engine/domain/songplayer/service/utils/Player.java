package com.djt.jukeanator_engine.domain.songplayer.service.utils;

import com.djt.jukeanator_engine.domain.songplayer.dto.SongPlayerStatus;

public interface Player {

  boolean playSongMedia(String songPath);

  SongPlayerStatus getStatus();

  long getElapsedSeconds();

  long getTotalLengthSeconds();

  /**
   * 0: Completely muted
   * 100: 100% of the normal volume (unity gain, no digital amplification)
   * 200: 200% volume (up to 2x software amplification)
   * 
   * @return
   */
  int getVolume();

  /**
   * 0: Completely muted
   * 100: 100% of the normal volume (unity gain, no digital amplification)
   * 200: 200% volume (up to 2x software amplification) 
   * 
   * @param volume
   */
  void setVolume(int volume);
  
  void pause();

  void stop();

  void release();
  
  void setOnFinished(Runnable callback);
}
