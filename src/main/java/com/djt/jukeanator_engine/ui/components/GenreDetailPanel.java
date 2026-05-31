package com.djt.jukeanator_engine.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import com.djt.jukeanator_engine.domain.songlibrary.dto.AlbumDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.GenreDto;

/**
 * Reusable panel that renders a genre header (genre icon + name + stats) above a paginated
 * {@link AlbumGridPanel}. A configurable back button sits in the header so the panel can be
 * embedded in the Genres tab CardLayout.
 *
 * <p>
 * Mirrors the layout and palette of {@link ArtistDetailPanel}: the NORTH area shows a styled header
 * with a back/close button and genre info; the CENTER area is an {@link AlbumGridPanel} showing all
 * albums that belong to the genre.
 *
 * <p>
 * Usage — inside the Genres tab CardLayout:
 *
 * <pre>
 * GenreDetailPanel panel = new GenreDetailPanel(genre, albums, imageLoader, gridCols, gridRows,
 *     artW, artH, "← GENRES", () -> genresCardLayout.show(genresContentPanel, "GRID"),
 *     album -> openAlbumDetail(album));
 * </pre>
 */
public class GenreDetailPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // ── Palette (shared with AlbumGridPanel statics) ──────────────────────────
  private static final Color BG_MAIN = AlbumGridPanel.BG_MAIN;

  // ─────────────────────────────────────────────────────────────────────────
  // CONSTRUCTOR
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * @param genre The genre to display.
   * @param albums Albums that belong to this genre (pre-fetched by the caller).
   * @param imageLoader Shared loader.
   * @param gridCols Album grid columns per page.
   * @param gridRows Album grid rows per page.
   * @param artW Tile art pixel width.
   * @param artH Tile art pixel height.
   * @param backLabel Text on the back button, e.g. "← GENRES".
   * @param onBack Runnable executed when the back button is pressed.
   * @param onAlbumClicked Called when the user selects an album tile.
   */
  public GenreDetailPanel(GenreDto genre, List<AlbumDto> albums, ImageLoader imageLoader,
      int gridCols, int gridRows, int artW, int artH, String backLabel, Runnable onBack,
      AlbumGridPanel.AlbumClickListener onAlbumClicked) {

    setLayout(new BorderLayout(0, 0));
    setBackground(BG_MAIN);

    List<AlbumDto> safeAlbums = albums != null ? albums : List.of();

    ImageIcon genreImage = null;
    try {

      String resourceName = genre.getGenreName() + ".png";

      genreImage = imageLoader.loadImage(resourceName, 72, 72);

    } catch (Exception ignored) {
    }

    String subtitle = safeAlbums.size() + " album" + (safeAlbums.size() != 1 ? "s" : "");

    add(new DetailHeaderPanel("← BACK", onBack, genreImage, "♪", genre.getGenreName(), subtitle),
        BorderLayout.NORTH);

    add(new AlbumGridPanel(safeAlbums, imageLoader, gridCols, gridRows, artW, artH, onAlbumClicked),
        BorderLayout.CENTER);
  }
}
