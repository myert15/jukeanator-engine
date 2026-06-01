package com.djt.jukeanator_engine.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import com.djt.jukeanator_engine.domain.songlibrary.dto.AlbumDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.ArtistDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.SongDto;

/**
 * Shared factory for the "ARTISTS / ALBUMS / SONGS" three-column result layout used by both
 * {@link SearchPanel} and {@link HotHerePanel}.
 *
 * <p>
 * Extracted from the duplicate {@code buildSearchResultColumn} / {@code buildHotHereColumn} methods
 * that previously lived in {@code JukeANatorFrame}. Callers supply:
 * <ul>
 * <li>the header label and item list,</li>
 * <li>the current scroll offset and the number of rows to show,</li>
 * <li>{@code onUp} / {@code onDown} runnables that adjust the offset and trigger a rebuild,</li>
 * <li>an {@code onItemClick} consumer that receives the raw item (cast by the panel).</li>
 * </ul>
 */
public final class ResultsColumnPanel {

  // ── Palette ───────────────────────────────────────────────────────────────
  private static final Color BG_COLUMN = new Color(15, 15, 20);
  private static final Color BG_HEADER = new Color(20, 20, 30);
  private static final Color BG_NAV = new Color(20, 20, 30);
  private static final Color BG_ROW = new Color(15, 15, 20);
  private static final Color BG_ROW_HOVER = new Color(30, 30, 45);
  private static final Color BG_THUMB = new Color(40, 40, 55);
  private static final Color COLOR_BORDER = new Color(60, 60, 80);
  private static final Color COLOR_SEP = new Color(50, 50, 65);
  private static final Color COLOR_NAV_BTN = new Color(50, 50, 70);
  private static final Color ACCENT_BLUE = new Color(0, 210, 255);
  private static final Color TEXT_PRIMARY = Color.WHITE;
  private static final Color TEXT_SECONDARY = new Color(180, 180, 180);

  private ResultsColumnPanel() {}

  // ─────────────────────────────────────────────────────────────────────────
  // FACTORY METHOD
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Builds and returns a single results column panel.
   *
   * @param <T> Item type: {@link ArtistDto}, {@link AlbumDto}, or {@link SongDto}.
   * @param header Column heading ("ARTISTS", "ALBUMS", "SONGS").
   * @param items Full item list for this column.
   * @param offset Current scroll offset (zero-based index of the first visible row).
   * @param previewCount Number of rows to display.
   * @param imageLoader Shared loader used to populate 56×56 thumbnail images.
   * @param onUp Called when the ∧ button is tapped; should decrement offset and rebuild.
   * @param onDown Called when the ∨ button is tapped; should increment offset and rebuild.
   * @param onItemClick Called with the clicked item; caller casts and handles.
   */
  public static <T> JPanel build(String header, List<T> items, int offset, int previewCount,
      ImageLoader imageLoader, Runnable onUp, Runnable onDown, Consumer<T> onItemClick) {

    JPanel column = new JPanel(new BorderLayout());
    column.setBackground(BG_COLUMN);
    column.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDER));

    // ── Header ────────────────────────────────────────────────────────────
    int total = items.size();
    JLabel headerLabel = new JLabel(header + " (" + total + ")", SwingConstants.CENTER);
    headerLabel.setForeground(ACCENT_BLUE);
    headerLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
    headerLabel.setBorder(new EmptyBorder(10, 0, 8, 0));
    headerLabel.setOpaque(true);
    headerLabel.setBackground(BG_HEADER);

    // ── Rows ──────────────────────────────────────────────────────────────
    JPanel rowsPanel = new JPanel();
    rowsPanel.setBackground(BG_COLUMN);
    rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));

    for (int slot = 0; slot < previewCount; slot++) {
      int idx = offset + slot;
      JPanel row =
          (idx < total) ? buildItemRow(idx + 1, items.get(idx), header, imageLoader, onItemClick)
              : buildEmptyRow();
      rowsPanel.add(row);
      if (slot < previewCount - 1) {
        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_SEP);
        sep.setBackground(COLOR_SEP);
        rowsPanel.add(sep);
      }
    }

    // ── Navigation ────────────────────────────────────────────────────────
    JPanel navPanel = new JPanel(new BorderLayout(4, 0));
    navPanel.setBackground(BG_NAV);
    navPanel.setBorder(new EmptyBorder(6, 8, 6, 8));

    JButton upBtn = navButton("∧");
    upBtn.setEnabled(offset > 0);
    upBtn.addActionListener(e -> onUp.run());

    JButton downBtn = navButton("∨");
    downBtn.setEnabled(offset + previewCount < total);
    downBtn.addActionListener(e -> onDown.run());

    navPanel.add(upBtn, BorderLayout.WEST);
    navPanel.add(downBtn, BorderLayout.EAST);

    column.add(headerLabel, BorderLayout.NORTH);
    column.add(rowsPanel, BorderLayout.CENTER);
    column.add(navPanel, BorderLayout.SOUTH);

    return column;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // ITEM ROW
  // ─────────────────────────────────────────────────────────────────────────
  private static <T> JPanel buildItemRow(int rowNum, T item, String category,
      ImageLoader imageLoader, Consumer<T> onItemClick) {

    JPanel row = new JPanel(new BorderLayout(10, 0));
    row.setBackground(BG_ROW);
    row.setBorder(new EmptyBorder(8, 10, 8, 10));
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
    row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // Row number
    JLabel numLabel = new JLabel(String.format("%02d", rowNum));
    numLabel.setForeground(TEXT_SECONDARY);
    numLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
    numLabel.setPreferredSize(new Dimension(36, 56));
    numLabel.setHorizontalAlignment(SwingConstants.CENTER);

    // Thumbnail
    JLabel thumb = new JLabel();
    thumb.setPreferredSize(new Dimension(56, 56));
    thumb.setHorizontalAlignment(SwingConstants.CENTER);
    thumb.setOpaque(true);
    thumb.setBackground(BG_THUMB);

    // Text lines
    JLabel line1 = new JLabel();
    JLabel line2 = new JLabel();
    line1.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
    line2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    line1.setForeground(TEXT_PRIMARY);
    line2.setForeground(TEXT_SECONDARY);

    String coverPath = extractFields(item, category, line1, line2);

    if (coverPath != null && imageLoader != null) {
      try {
        ImageIcon icon = imageLoader.loadFilesystemImage(coverPath, 56, 56);
        if (icon != null)
          thumb.setIcon(icon);
      } catch (Exception ignored) {
      }
    }

    JPanel textPanel = new JPanel();
    textPanel.setOpaque(false);
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
    textPanel.add(line1);
    textPanel.add(Box.createVerticalStrut(3));
    textPanel.add(line2);

    JPanel left = new JPanel(new BorderLayout(8, 0));
    left.setOpaque(false);
    left.add(numLabel, BorderLayout.WEST);
    left.add(thumb, BorderLayout.CENTER);

    row.add(left, BorderLayout.WEST);
    row.add(textPanel, BorderLayout.CENTER);

    // Hover
    row.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseEntered(java.awt.event.MouseEvent e) {
        row.setBackground(BG_ROW_HOVER);
        repaintChildren(row);
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent e) {
        row.setBackground(BG_ROW);
        repaintChildren(row);
      }

      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        onItemClick.accept(item);
      }
    });

    return row;
  }

  /**
   * Fills the two text labels from the item and returns the cover-art path (may be null).
   * Pattern-matching on the concrete DTO type — the {@code category} string is used only as a fast
   * pre-check guard so mixed lists don't accidentally mis-render.
   */
  private static <T> String extractFields(T item, String category, JLabel line1, JLabel line2) {

    if ("ARTISTS".equals(category) && item instanceof ArtistDto a) {
      line1.setText(a.getArtistName());
      line2.setText(a.getSongCount() + " songs, " + a.getAlbumCount() + " albums");
      return a.getCoverArtPath();
    }
    if ("ALBUMS".equals(category) && item instanceof AlbumDto a) {
      line1.setText(a.getAlbumName());
      line2.setText(a.getArtistName());
      return a.getCoverArtPath();
    }
    if ("SONGS".equals(category) && item instanceof SongDto s) {
      line1.setText(s.getSongName());
      line2.setText(s.getArtistName());
      return s.getCoverArtPath();
    }
    return null;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // EMPTY ROW (filler when fewer items than previewCount)
  // ─────────────────────────────────────────────────────────────────────────
  private static JPanel buildEmptyRow() {

    JPanel row = new JPanel(new BorderLayout());
    row.setBackground(BG_ROW);
    row.setBorder(new EmptyBorder(8, 10, 8, 10));
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
    row.setOpaque(true);
    return row;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // NAV BUTTON
  // ─────────────────────────────────────────────────────────────────────────
  private static JButton navButton(String text) {

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

  // ─────────────────────────────────────────────────────────────────────────
  // MISC
  // ─────────────────────────────────────────────────────────────────────────
  private static void repaintChildren(java.awt.Container c) {
    for (java.awt.Component child : c.getComponents())
      child.repaint();
  }
}
