package com.djt.jukeanator_engine.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.djt.jukeanator_engine.domain.songlibrary.dto.AlbumDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.ArtistDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.GenreDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.SearchResultDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.SongDto;
import com.djt.jukeanator_engine.domain.songqueue.dto.AddSongToQueueRequest;
import com.djt.jukeanator_engine.domain.songqueue.service.SongQueueService;

/**
 * Reusable panel that renders a genre header (genre icon + name + stats) above a three-column
 * ResultsColumnPanel layout (Artists / Albums / Songs) — matching the Hot Here tab layout.
 *
 * <p>
 * The NORTH area shows a styled {@link DetailHeaderPanel} with a back/close button and genre info.
 * The CENTER area is a three-column {@link ResultsColumnPanel} showing the artists, albums, and
 * songs that belong to the genre, sourced from a {@link SearchResultDto}.
 *
 * <p>
 * Usage — inside the Genres tab CardLayout:
 *
 * <pre>
 * GenreDetailPanel panel = new GenreDetailPanel(genre, results, imageLoader, songQueueService,
 *     normalPlayCost, priorityCost, "← GENRES",
 *     () -> genresCardLayout.show(genresContentPanel, "GRID"), album -> openAlbumDetail(album));
 * </pre>
 */
public class GenreDetailPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // ── Preview row count (matches HotHerePanel) ──────────────────────────────
  private static final int PREVIEW_COUNT = 10;

  // ── Palette (shared with AlbumGridPanel statics) ──────────────────────────
  private static final Color BG_MAIN = AlbumGridPanel.BG_MAIN;

  // ── Offset state per column ───────────────────────────────────────────────
  private int artistsOffset = 0;
  private int albumsOffset = 0;
  private int songsOffset = 0;

  // ── Live column container (rebuilt on nav) ────────────────────────────────
  private final JPanel columnsPanel = new JPanel(new GridLayout(1, 3, 2, 0));

  // ── Data ──────────────────────────────────────────────────────────────────
  private final List<ArtistDto> artists;
  private final List<AlbumDto> albums;
  private final List<SongDto> songs;

  // ── Dependencies needed for row-click handling ────────────────────────────
  private final ImageLoader imageLoader;
  private final SongQueueService songQueueService;
  private final int normalPlayCost;
  private final int priorityCost;
  private final AlbumGridPanel.AlbumClickListener onAlbumClicked;
  private final ArtistClickListener onArtistClicked;

  // ── Callback types ────────────────────────────────────────────────────────
  public interface ArtistClickListener {
    void onArtistClicked(ArtistDto artist);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // CONSTRUCTOR
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * @param genre The genre to display.
   * @param results Popularity/content data for this genre (artists, albums, songs).
   * @param imageLoader Shared loader.
   * @param songQueueService Service used to queue individual songs when a song row is tapped.
   * @param normalPlayCost Credits for a normal-priority queue insertion.
   * @param priorityCost Credits for a high-priority queue insertion.
   * @param backLabel Text on the back button, e.g. "← GENRES".
   * @param onBack Runnable executed when the back button is pressed.
   * @param onAlbumClicked Called when the user selects an album row.
   * @param onArtistClicked Called when the user selects an artist row.
   */
  public GenreDetailPanel(GenreDto genre, SearchResultDto results, ImageLoader imageLoader,
      SongQueueService songQueueService, int normalPlayCost, int priorityCost, String backLabel,
      Runnable onBack, AlbumGridPanel.AlbumClickListener onAlbumClicked,
      ArtistClickListener onArtistClicked) {

    setLayout(new BorderLayout(0, 0));
    setBackground(BG_MAIN);

    this.imageLoader = imageLoader;
    this.songQueueService = songQueueService;
    this.normalPlayCost = normalPlayCost;
    this.priorityCost = priorityCost;
    this.onAlbumClicked = onAlbumClicked;
    this.onArtistClicked = onArtistClicked;

    SearchResultDto safe = results != null ? results : new SearchResultDto();
    this.artists = safeList(safe.getArtists());
    this.albums = safeList(safe.getAlbums());
    this.songs = safeList(safe.getSongs());

    // ── Header ────────────────────────────────────────────────────────────
    ImageIcon genreImage = null;
    try {
      String resourceName = genre.getGenreName() + ".png";
      genreImage = imageLoader.loadImage(resourceName, 72, 72);
    } catch (Exception ignored) {
    }

    // Subtitle: total counts across all three categories
    String subtitle =
        artists.size() + " artists  •  " + albums.size() + " albums  •  " + songs.size() + " songs";

    add(new DetailHeaderPanel(backLabel, onBack, genreImage, "♪", genre.getGenreName(), subtitle),
        BorderLayout.NORTH);

    // ── Columns ───────────────────────────────────────────────────────────
    columnsPanel.setOpaque(false);
    add(columnsPanel, BorderLayout.CENTER);

    rebuildColumns();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // COLUMN RENDERING (mirrors HotHerePanel#rebuildColumnsPanel)
  // ─────────────────────────────────────────────────────────────────────────
  private void rebuildColumns() {

    columnsPanel.removeAll();

    columnsPanel.add(ResultsColumnPanel.build("ARTISTS", artists, artistsOffset, PREVIEW_COUNT,
        imageLoader, () -> {
          artistsOffset = Math.max(0, artistsOffset - 1);
          rebuildColumns();
        }, () -> {
          artistsOffset++;
          rebuildColumns();
        }, item -> handleRowClick("ARTISTS", item)));

    columnsPanel.add(
        ResultsColumnPanel.build("ALBUMS", albums, albumsOffset, PREVIEW_COUNT, imageLoader, () -> {
          albumsOffset = Math.max(0, albumsOffset - 1);
          rebuildColumns();
        }, () -> {
          albumsOffset++;
          rebuildColumns();
        }, item -> handleRowClick("ALBUMS", item)));

    columnsPanel.add(
        ResultsColumnPanel.build("SONGS", songs, songsOffset, PREVIEW_COUNT, imageLoader, () -> {
          songsOffset = Math.max(0, songsOffset - 1);
          rebuildColumns();
        }, () -> {
          songsOffset++;
          rebuildColumns();
        }, item -> handleRowClick("SONGS", item)));

    columnsPanel.revalidate();
    columnsPanel.repaint();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // ROW CLICK DISPATCH (mirrors HotHerePanel#handleRowClick)
  // ─────────────────────────────────────────────────────────────────────────
  private <T> void handleRowClick(String category, T item) {
    switch (category) {
      case "ARTISTS" -> {
        if (item instanceof ArtistDto a && onArtistClicked != null)
          onArtistClicked.onArtistClicked(a);
      }
      case "ALBUMS" -> {
        if (item instanceof AlbumDto a && onAlbumClicked != null)
          onAlbumClicked.onAlbumClicked(a);
      }
      case "SONGS" -> {
        if (item instanceof SongDto song) {
          Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
          AddSongToQueueDialog.show(owner, song, imageLoader, normalPlayCost, priorityCost,
              () -> songQueueService.addSongToQueue(
                  new AddSongToQueueRequest(song.getAlbumId(), song.getSongId(), 0)),
              () -> songQueueService.addSongToQueue(
                  new AddSongToQueueRequest(song.getAlbumId(), song.getSongId(), 1)));
        }
      }
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // HELPERS
  // ─────────────────────────────────────────────────────────────────────────
  private static <T> List<T> safeList(List<T> list) {
    return list != null ? list : List.of();
  }
}
