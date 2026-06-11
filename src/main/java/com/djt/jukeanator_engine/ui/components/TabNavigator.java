package com.djt.jukeanator_engine.ui.components;

import com.djt.jukeanator_engine.domain.songlibrary.dto.AlbumDto;

public interface TabNavigator {

  void pushAlbumDetail(AlbumDto album);

  void popToRoot();
}
