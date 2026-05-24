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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import com.djt.jukeanator_engine.domain.songlibrary.dto.AlbumDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.ArtistDto;

/**
 * Reusable panel that renders an artist header (photo + name + stats) above a paginated
 * {@link AlbumGridPanel}. A configurable back/close button sits in the header so the panel can be
 * embedded in any navigation context.
 *
 * <p>
 * Grid layout (cols × rows) and tile art size are configurable so the same panel works in
 * landscape, portrait, or any resolution.
 *
 * <p>
 * Usage — inside a CardLayout (Search / Hot Here):
 * 
 * <pre>
 *   ArtistDetailPanel panel = new ArtistDetailPanel(
 *       artist, imageLoader,
 *       gridCols, gridRows, artW, artH,
 *       enableBigScrollBars,
 *       "← BACK",
 *       () -> cardLayout.show(rootPanel, "RESULTS"),
 *       album -> AlbumDetailDialog.show(...));
 * </pre>
 *
 * <p>
 * Usage — Home tab (back button navigates to the full album grid):
 * 
 * <pre>
 *   ArtistDetailPanel panel = new ArtistDetailPanel(
 *       artist, imageLoader,
 *       gridCols, gridRows, artW, artH,
 *       enableBigScrollBars,
 *       "← HOME",
 *       () -> showHomeGrid(),
 *       album -> AlbumDetailDialog.show(...));
 * </pre>
 */
public class ArtistDetailPanel extends JPanel {

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

    add(buildHeader(artist, imageLoader, backLabel, onBack), BorderLayout.NORTH);
    add(new AlbumGridPanel(albums, imageLoader, gridCols, gridRows, artW, artH, onAlbumClicked),
        BorderLayout.CENTER);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // HEADER — back button | artist photo + name + stats
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildHeader(ArtistDto artist, ImageLoader imageLoader, String backLabel,
      Runnable onBack) {

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
    backBtn.setPreferredSize(new Dimension(140, 52));
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

    // ── Artist photo ──────────────────────────────────────────────────────
    JLabel photo = new JLabel();
    photo.setPreferredSize(new Dimension(72, 72));
    photo.setHorizontalAlignment(SwingConstants.CENTER);
    photo.setVerticalAlignment(SwingConstants.CENTER);
    photo.setOpaque(true);
    photo.setBackground(new Color(30, 30, 42));
    photo.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));

    if (artist.getCoverArtPath() != null) {
      try {
        ImageIcon icon = imageLoader.loadFilesystemImage(artist.getCoverArtPath(), 72, 72);
        if (icon != null)
          photo.setIcon(icon);
      } catch (Exception ignored) {
      }
    }
    if (photo.getIcon() == null) {
      photo.setText("♪");
      photo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
      photo.setForeground(new Color(100, 100, 120));
    }

    // ── Text block ────────────────────────────────────────────────────────
    JPanel textBlock = new JPanel();
    textBlock.setOpaque(false);
    textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

    JLabel nameLabel = new JLabel(artist.getArtistName() != null ? artist.getArtistName() : "");
    nameLabel.setForeground(TEXT_PRIMARY);
    nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));

    int albums = artist.getAlbums() != null ? artist.getAlbums().size() : 0;
    int songs = artist.getSongCount() != null ? artist.getSongCount() : 0;
    JLabel statsLabel = new JLabel(albums + " albums  •  " + songs + " songs");
    statsLabel.setForeground(TEXT_SECONDARY);
    statsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

    textBlock.add(nameLabel);
    textBlock.add(Box.createVerticalStrut(4));
    textBlock.add(statsLabel);

    // ── Artist info cluster ───────────────────────────────────────────────
    JPanel infoCluster = new JPanel(new BorderLayout(12, 0));
    infoCluster.setOpaque(false);
    infoCluster.add(photo, BorderLayout.WEST);
    infoCluster.add(textBlock, BorderLayout.CENTER);

    header.add(backBtn, BorderLayout.WEST);
    header.add(infoCluster, BorderLayout.CENTER);

    return header;
  }
}
