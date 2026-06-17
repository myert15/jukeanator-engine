package com.djt.jukeanator_engine.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.djt.jukeanator_engine.domain.songqueue.dto.SongQueueEntryDto;

/**
 * Shared queue-entry cell renderer that renders a song row with green popularity bars, a
 * queue-position / priority badge, and song name + artist/album sub-label.
 *
 * <p>
 * Used by both {@link AdminPanel} and {@link SongQueueCard}.
 */
public class SongTrackCellRenderer extends JPanel
    implements javax.swing.ListCellRenderer<SongQueueEntryDto> {

  private static final long serialVersionUID = 1L;

  // ── Popularity bar geometry (shared with AlbumViewCard) ─────────────────
  public static final int BAR_WIDTH = 5;
  public static final int BAR_GAP = 3;
  public static final int BAR_MAX_H = 18;
  public static final int[] BAR_HEIGHTS = {8, 13, 18};

  // ── Colours — sourced from ColorTheme.get() ──────────────────────────────

  // ── Popularity thresholds (configurable per use-site) ─────────────────────
  private final int t1;
  private final int t2;
  private final int t3;

  // ── Sub-widgets ───────────────────────────────────────────────────────────
  private final PopularityBarsPanel barsPanel;
  private final JLabel song = new JLabel();
  private final JLabel sub = new JLabel();

  // ─────────────────────────────────────────────────────────────────────────
  // CONSTRUCTOR
  // ─────────────────────────────────────────────────────────────────────────
  /**
   * @param t1 Minimum plays for 1 bar.
   * @param t2 Minimum plays for 2 bars.
   * @param t3 Minimum plays for 3 bars.
   */
  public SongTrackCellRenderer(int t1, int t2, int t3) {
    this.t1 = t1;
    this.t2 = t2;
    this.t3 = t3;

    barsPanel = new PopularityBarsPanel(0);

    setLayout(new BorderLayout(6, 0));
    setBorder(new EmptyBorder(4, 8, 4, 8));

    // Popularity bars — fixed size
    barsPanel.setPreferredSize(new Dimension(3 * (BAR_WIDTH + BAR_GAP) + 6, BAR_MAX_H + 4));
    barsPanel.setOpaque(false);

    // Text cluster
    JPanel text = new JPanel(new BorderLayout(0, 1));
    text.setOpaque(false);
    song.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
    text.add(song, BorderLayout.CENTER);
    text.add(sub, BorderLayout.SOUTH);

    add(barsPanel, BorderLayout.WEST);
    add(text, BorderLayout.CENTER);
  }

  @Override
  public java.awt.Component getListCellRendererComponent(JList<? extends SongQueueEntryDto> list,
      SongQueueEntryDto entry, int index, boolean isSelected, boolean cellHasFocus) {

    // ── Popularity bars ────────────────────────────────────────────────────
    int plays = entry.getSong().getNumPlays() == null ? 0 : entry.getSong().getNumPlays();
    int active = barsForPlays(plays, t1, t2, t3);
    barsPanel.setActiveBars(active);

    // ── Song / sub text ────────────────────────────────────────────────────
    song.setText(entry.getSong().getSongName());
    sub.setText(entry.getSong().getArtistName() + "  •  " + entry.getSong().getAlbumName());

    if (isSelected) {
      setBackground(ColorTheme.get().bgListSelected);
      song.setForeground(ColorTheme.get().accentBlue);
      sub.setForeground(ColorTheme.get().textPrimary);
    } else {
      setBackground(index % 2 == 0 ? ColorTheme.get().bgList : ColorTheme.get().bgListRowAlt);
      song.setForeground(ColorTheme.get().textPrimary);
      sub.setForeground(ColorTheme.get().textMuted);
    }
    setOpaque(true);
    return this;
  }

  // ── Popularity helper ──────────────────────────────────────────────────────
  public static int barsForPlays(int plays, int t1, int t2, int t3) {
    if (plays >= t3)
      return 3;
    if (plays >= t2)
      return 2;
    if (plays >= t1)
      return 1;
    return 0;
  }

  // ── Inner popularity-bars widget ───────────────────────────────────────────
  /**
   * Three staggered vertical bars painted in {@code ACCENT_GREEN}, identical to the
   * {@code PopularityBarsPanel} previously duplicated in {@code AdminPanel} and
   * {@code AlbumViewCard}.
   */
  public static class PopularityBarsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private int activeBars;

    public PopularityBarsPanel(int activeBars) {
      this.activeBars = activeBars;
      setOpaque(false);
    }

    public void setActiveBars(int n) {
      this.activeBars = n;
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int baseline = getHeight() - 2;
      for (int i = 0; i < 3; i++) {
        int barH = BAR_HEIGHTS[i];
        int x = i * (BAR_WIDTH + BAR_GAP);
        int y = baseline - barH;

        if (i < activeBars) {
          int alpha = Math.min(255, 180 + (i * 25));
          g2.setColor(new Color(ColorTheme.get().accentGreen.getRed(),
              ColorTheme.get().accentGreen.getGreen(), ColorTheme.get().accentGreen.getBlue(),
              alpha));
        } else {
          g2.setColor(ColorTheme.get().popularityBarInactive);
        }
        g2.fillRoundRect(x, y, BAR_WIDTH, barH, 2, 2);
      }
      g2.dispose();
    }
  }

  // ── Default cell height that consumers should apply to their JList ─────────
  public static final int CELL_HEIGHT = 44;

  // ── Convenience factory ────────────────────────────────────────────────────
  /**
   * Convenience method: configure a {@link JList} to use this renderer with the correct fixed cell
   * height.
   */
  public static void install(JList<SongQueueEntryDto> list, int t1, int t2, int t3) {
    list.setCellRenderer(new SongTrackCellRenderer(t1, t2, t3));
    list.setFixedCellHeight(CELL_HEIGHT);
  }
}
