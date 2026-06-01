package com.djt.jukeanator_engine.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import com.djt.jukeanator_engine.domain.songlibrary.dto.AlbumDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.ArtistDto;

public class ArtistDetailPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // ── Palette (shared with AlbumGridPanel statics) ──────────────────────────
  private static final Color BG_MAIN = AlbumGridPanel.BG_MAIN;

  // ─────────────────────────────────────────────────────────────────────────
  // CONSTRUCTOR
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * @param artist The artist to display (must have albums list populated).
   * @param imageLoader Shared loader.
   * @param gridCols Album grid columns per page.
   * @param gridRows Album grid rows per page.
   * @param artW Tile art pixel width.
   * @param artH Tile art pixel height.
   * @param backLabel Text on the back button, e.g. "← BACK" or "← HOME".
   * @param onBack Runnable executed when the back button is pressed.
   * @param onAlbumClicked Called when the user selects an album tile.
   */
  public ArtistDetailPanel(ArtistDto artist, ImageLoader imageLoader, int gridCols, int gridRows,
      int artW, int artH, String backLabel, Runnable onBack,
      AlbumGridPanel.AlbumClickListener onAlbumClicked) {

    setLayout(new BorderLayout(0, 0));
    setBackground(BG_MAIN);

    List<AlbumDto> albums = artist.getAlbums() != null ? artist.getAlbums() : List.of();

    ImageIcon artistImage = null;
    if (artist.getCoverArtPath() != null) {
      try {
        artistImage = imageLoader.loadFilesystemImage(artist.getCoverArtPath(), 72, 72);
      } catch (Exception ignored) {
      }
    }

    int numAlbums = artist.getAlbums() != null ? artist.getAlbums().size() : 0;
    int numSongs = artist.getSongCount() != null ? artist.getSongCount() : 0;

    String subtitle = numAlbums + " albums  •  " + numSongs + " songs";

    add(new DetailHeaderPanel(backLabel, onBack, artistImage, "♪", artist.getArtistName(),
        subtitle), BorderLayout.NORTH);

    add(new AlbumGridPanel(albums, imageLoader, gridCols, gridRows, artW, artH, onAlbumClicked),
        BorderLayout.CENTER);
  }
}
