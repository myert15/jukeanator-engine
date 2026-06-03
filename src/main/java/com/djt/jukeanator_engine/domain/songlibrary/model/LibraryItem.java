package com.djt.jukeanator_engine.domain.songlibrary.model;

import java.time.Year;

/**
 * @author tmyers
 */
public interface LibraryItem {
  
  /**
   * 
   * @return
   */
  Integer getNumPlays();
  
  /**
   * 
   * @return
   */
  GenreFolderEntity getParentGenre();
  
  /**
   * 
   * @return
   */
  String getTitle();
  
  /**
   * 
   * @return
   */
  Year getReleaseDate();  
}
