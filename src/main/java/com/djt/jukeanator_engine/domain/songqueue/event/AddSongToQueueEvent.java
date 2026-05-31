package com.djt.jukeanator_engine.domain.songqueue.event;

import java.time.Instant;
import java.util.List;
import com.djt.jukeanator_engine.domain.songqueue.dto.SongQueueEntryDto;

public record AddSongToQueueEvent(
    List<SongQueueEntryDto> queuedSongs,    
    Instant occurredAt) {
}
