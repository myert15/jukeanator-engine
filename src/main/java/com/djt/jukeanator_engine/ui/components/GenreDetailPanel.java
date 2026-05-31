package com.djt.jukeanator_engine.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
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
  private static final Color BG_HEADER = new Color(18, 18, 28);
  private static final Color ACCENT_BLUE = AlbumGridPanel.ACCENT_BLUE;
  private static final Color TEXT_PRIMARY = AlbumGridPanel.TEXT_PRIMARY;
  private static final Color TEXT_SECONDARY = AlbumGridPanel.TEXT_SECONDARY;
  private static final Color COLOR_BORDER = new Color(60, 60, 80);

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

    add(buildHeader(genre, safeAlbums.size(), backLabel, onBack), BorderLayout.NORTH);
    add(new AlbumGridPanel(safeAlbums, imageLoader, gridCols, gridRows, artW, artH, onAlbumClicked),
        BorderLayout.CENTER);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // HEADER — back button | genre icon + name + stats
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildHeader(GenreDto genre, int albumCount, String backLabel, Runnable onBack) {

    JPanel header = new JPanel(new BorderLayout(16, 0));
    header.setBackground(BG_HEADER);
    header.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
        new EmptyBorder(12, 16, 12, 16)));

    // ── Back button ───────────────────────────────────────────────────────
    JButton backBtn = new JButton(backLabel) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.setColor(ACCENT_BLUE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    backBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
    backBtn.setForeground(ACCENT_BLUE);
    backBtn.setBackground(new Color(20, 20, 30));
    backBtn.setFocusPainted(false);
    backBtn.setContentAreaFilled(false);
    backBtn.setBorderPainted(false);
    backBtn.setPreferredSize(new Dimension(160, 52));
    backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    backBtn.addActionListener(e -> onBack.run());

    backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseEntered(java.awt.event.MouseEvent e) {
        backBtn.setBackground(new Color(0, 60, 80));
        backBtn.repaint();
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent e) {
        backBtn.setBackground(new Color(20, 20, 30));
        backBtn.repaint();
      }
    });

    // ── Genre icon (▣ symbol — same icon used on the tab) ─────────────────
    JLabel icon = new JLabel("▣");
    icon.setPreferredSize(new Dimension(72, 72));
    icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    icon.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
    icon.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
    icon.setForeground(ACCENT_BLUE);
    icon.setOpaque(true);
    icon.setBackground(new Color(20, 20, 32));
    icon.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));

    // ── Text block ────────────────────────────────────────────────────────
    JPanel textBlock = new JPanel();
    textBlock.setOpaque(false);
    textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

    String genreName = genre.getGenreName() != null ? genre.getGenreName() : "";
    JLabel nameLabel = new JLabel(genreName);
    nameLabel.setForeground(TEXT_PRIMARY);
    nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));

    JLabel statsLabel = new JLabel(albumCount + " album" + (albumCount != 1 ? "s" : ""));
    statsLabel.setForeground(TEXT_SECONDARY);
    statsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

    textBlock.add(nameLabel);
    textBlock.add(Box.createVerticalStrut(4));
    textBlock.add(statsLabel);

    // ── Info cluster: icon + text ──────────────────────────────────────────
    JPanel infoCluster = new JPanel(new BorderLayout(12, 0));
    infoCluster.setOpaque(false);
    infoCluster.add(icon, BorderLayout.WEST);
    infoCluster.add(textBlock, BorderLayout.CENTER);

    header.add(backBtn, BorderLayout.WEST);
    header.add(infoCluster, BorderLayout.CENTER);

    return header;
  }
}
