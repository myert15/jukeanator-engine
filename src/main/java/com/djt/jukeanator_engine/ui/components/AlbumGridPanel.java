package com.djt.jukeanator_engine.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import com.djt.jukeanator_engine.domain.songlibrary.dto.AlbumDto;

public class AlbumGridPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // ── Palette ───────────────────────────────────────────────────────────────
  static final Color BG_MAIN = new Color(12, 12, 18);
  static final Color BG_TILE = new Color(22, 22, 30);
  static final Color BG_TILE_HOVER = new Color(38, 38, 55);
  static final Color ACCENT_BLUE = new Color(0, 210, 255);
  static final Color TEXT_PRIMARY = Color.WHITE;
  static final Color TEXT_SECONDARY = new Color(180, 180, 180);
  static final Color COLOR_NAV_BG = new Color(18, 18, 26);
  static final Color COLOR_NAV_BTN = new Color(50, 50, 70);
  static final Color COLOR_BORDER = new Color(55, 55, 72);

  // ── State ─────────────────────────────────────────────────────────────────
  private final List<AlbumDto> albums;
  private final ImageLoader imageLoader;
  private final int cols;
  private final int rows;
  private final int artW;
  private final int artH;
  private final AlbumClickListener listener;

  private int currentPage = 0;

  // ── Panels rebuilt on each page turn ──────────────────────────────────────
  private final JPanel gridPanel = new JPanel();
  private final JPanel navPanel = new JPanel(new BorderLayout(8, 0));

  // ── Callback ──────────────────────────────────────────────────────────────
  public interface AlbumClickListener {
    void onAlbumClicked(AlbumDto album);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // CONSTRUCTOR
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * @param albums Full album list to paginate across.
   * @param imageLoader Shared loader.
   * @param cols Number of tile columns per page.
   * @param rows Number of tile rows per page.
   * @param artW Pixel width of each album art image.
   * @param artH Pixel height of each album art image.
   * @param listener Called when the user taps/clicks an album tile.
   */
  public AlbumGridPanel(List<AlbumDto> albums, ImageLoader imageLoader, int cols, int rows,
      int artW, int artH, AlbumClickListener listener) {

    this.albums = albums != null ? albums : List.of();
    this.imageLoader = imageLoader;
    this.cols = cols;
    this.rows = rows;
    this.artW = artW;
    this.artH = artH;
    this.listener = listener;

    setLayout(new BorderLayout(0, 0));
    setBackground(BG_MAIN);

    gridPanel.setBackground(BG_MAIN);
    navPanel.setBackground(COLOR_NAV_BG);
    navPanel.setBorder(new EmptyBorder(8, 16, 8, 16));

    add(gridPanel, BorderLayout.CENTER);
    add(navPanel, BorderLayout.SOUTH);

    refresh();
  }

  // ── Public API ────────────────────────────────────────────────────────────

  /** Go to page 0 and repaint — call this when the album list is replaced. */
  public void reset() {
    currentPage = 0;
    refresh();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // PAGE RENDERING
  // ─────────────────────────────────────────────────────────────────────────
  private void refresh() {

    int pageSize = cols * rows;
    int totalPages = Math.max(1, (int) Math.ceil(albums.size() / (double) pageSize));
    currentPage = Math.max(0, Math.min(currentPage, totalPages - 1));

    int start = currentPage * pageSize;
    int end = Math.min(start + pageSize, albums.size());

    // ── Grid ──────────────────────────────────────────────────────────────
    gridPanel.removeAll();
    gridPanel.setLayout(new GridLayout(rows, cols, 10, 10));
    gridPanel.setBorder(new EmptyBorder(12, 12, 4, 12));

    for (int i = start; i < end; i++) {
      gridPanel.add(buildTile(albums.get(i)));
    }

    // Fill remaining slots with blank panels so the grid stays uniform
    int filled = end - start;
    for (int i = filled; i < pageSize; i++) {
      JPanel blank = new JPanel();
      blank.setBackground(BG_MAIN);
      gridPanel.add(blank);
    }

    // ── Navigation ────────────────────────────────────────────────────────
    navPanel.removeAll();

    JButton prevBtn = buildNavButton("❮");
    prevBtn.setEnabled(currentPage > 0);
    prevBtn.addActionListener(e -> {
      currentPage--;
      refresh();
    });

    JButton nextBtn = buildNavButton("❯");
    nextBtn.setEnabled(currentPage < totalPages - 1);
    nextBtn.addActionListener(e -> {
      currentPage++;
      refresh();
    });

    JLabel pageLabel = new JLabel((currentPage + 1) + " / " + totalPages, SwingConstants.CENTER);
    pageLabel.setForeground(TEXT_SECONDARY);
    pageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));

    navPanel.add(prevBtn, BorderLayout.WEST);
    navPanel.add(pageLabel, BorderLayout.CENTER);
    navPanel.add(nextBtn, BorderLayout.EAST);

    // Only show nav bar when there is more than one page
    navPanel.setVisible(totalPages > 1);

    gridPanel.revalidate();
    gridPanel.repaint();
    navPanel.revalidate();
    navPanel.repaint();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // ALBUM TILE
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildTile(AlbumDto album) {

    JPanel tile = new JPanel(new BorderLayout(0, 0)) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Subtle rounded-corner border
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(COLOR_BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        g2.dispose();
      }
    };
    tile.setBackground(BG_TILE);
    tile.setOpaque(true);
    tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    tile.setBorder(new EmptyBorder(1, 1, 1, 1)); // breathing room inside border

    // ── Cover art ─────────────────────────────────────────────────────────
    JLabel artLabel = new JLabel();
    artLabel.setPreferredSize(new Dimension(artW, artH));
    artLabel.setHorizontalAlignment(SwingConstants.CENTER);
    artLabel.setVerticalAlignment(SwingConstants.CENTER);
    artLabel.setOpaque(true);
    artLabel.setBackground(new Color(30, 30, 42));

    if (album.getCoverArtPath() != null) {
      try {
        ImageIcon icon = imageLoader.loadFilesystemImage(album.getCoverArtPath(), artW, artH);
        if (icon != null)
          artLabel.setIcon(icon);
      } catch (Exception ignored) {
      }
    }

    if (artLabel.getIcon() == null) {
      artLabel.setText("♫");
      artLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, artH / 3));
      artLabel.setForeground(new Color(80, 80, 100));
    }

    // ── Text panel ────────────────────────────────────────────────────────
    JPanel textPanel = new JPanel();
    textPanel.setOpaque(false);
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
    textPanel.setBorder(new EmptyBorder(6, 8, 6, 8));

    JLabel albumLabel = new JLabel(truncate(album.getAlbumName(), 24));
    albumLabel.setForeground(TEXT_PRIMARY);
    albumLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    albumLabel.setAlignmentX(LEFT_ALIGNMENT);

    JLabel artistLabel = new JLabel(truncate(album.getArtistName(), 24));
    artistLabel.setForeground(TEXT_SECONDARY);
    artistLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    artistLabel.setAlignmentX(LEFT_ALIGNMENT);

    textPanel.add(albumLabel);
    textPanel.add(Box.createVerticalStrut(2));
    textPanel.add(artistLabel);

    tile.add(artLabel, BorderLayout.CENTER);
    tile.add(textPanel, BorderLayout.SOUTH);

    // ── Hover + click ─────────────────────────────────────────────────────
    tile.addMouseListener(new java.awt.event.MouseAdapter() {

      @Override
      public void mouseEntered(java.awt.event.MouseEvent e) {
        tile.setBackground(BG_TILE_HOVER);
        tile.repaint();
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent e) {
        tile.setBackground(BG_TILE);
        tile.repaint();
      }

      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        if (listener != null)
          listener.onAlbumClicked(album);
      }
    });

    return tile;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // HELPERS
  // ─────────────────────────────────────────────────────────────────────────
  private JButton buildNavButton(String text) {

    JButton btn = new JButton(text);
    btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
    btn.setForeground(Color.WHITE);
    btn.setBackground(COLOR_NAV_BTN);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setPreferredSize(new Dimension(60, 40));
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    btn.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseEntered(java.awt.event.MouseEvent e) {
        if (btn.isEnabled())
          btn.setBackground(ACCENT_BLUE);
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent e) {
        btn.setBackground(COLOR_NAV_BTN);
      }
    });

    return btn;
  }

  private static String truncate(String s, int maxChars) {
    if (s == null)
      return "";
    if (s.length() <= maxChars)
      return s;
    return s.substring(0, maxChars - 1) + "…";
  }
}
