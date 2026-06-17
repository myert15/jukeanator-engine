package com.djt.jukeanator_engine.ui.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.List;
import javax.swing.JPanel;
import com.djt.jukeanator_engine.domain.songplayer.service.SongPlayerService;
import com.djt.jukeanator_engine.domain.songqueue.dto.SongQueueEntryDto;
import com.djt.jukeanator_engine.domain.songqueue.service.SongQueueService;
import com.djt.jukeanator_engine.ui.model.CreditManager;

/**
 * The QUEUE tab panel.
 *
 * <p>
 * Renders the same diagonal screen gradient as the main frame so it looks like a seamless "window"
 * into the underlying background rather than a solid card. It hosts a {@link SongQueueCard} laid
 * out at its natural preferred size and centred with a {@link java.awt.GridBagLayout}, exactly
 * mirroring the way the overlay card system displays the card — but permanently visible as a tab
 * instead of a floating overlay.
 *
 * <p>
 * The {@code onDismiss} callback passed to {@link SongQueueCard} is a no-op here because there is
 * no overlay to dismiss; the user simply clicks another tab to navigate away.
 */
public class QueuePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // ── Colours and gradient — sourced from ColorTheme.get() ─────────────────

  private final SongQueueCard songQueueCard;

  // ─────────────────────────────────────────────────────────────────────────
  // CONSTRUCTOR
  // ─────────────────────────────────────────────────────────────────────────
  public QueuePanel(SongPlayerService songPlayerService, List<SongQueueEntryDto> initialQueue,
      SongQueueService songQueueService, CreditManager creditManager, ImageLoader imageLoader,
      int popularityT1, int popularityT2, int popularityT3, Runnable onDismiss) {

    // Transparent so our paintComponent can render the gradient cleanly
    setOpaque(false);
    // GridBagLayout centres the card both horizontally and vertically
    setLayout(new java.awt.GridBagLayout());

    songQueueCard = new SongQueueCard(songPlayerService, initialQueue, songQueueService,
        creditManager, imageLoader, popularityT1, popularityT2, popularityT3, onDismiss);

    // The card itself is transparent — the gradient paints through it
    songQueueCard.setOpaque(false);

    add(songQueueCard, new java.awt.GridBagConstraints());
  }

  // ─────────────────────────────────────────────────────────────────────────
  // BACKGROUND — matches the frame-level diagonal gradient exactly
  // ─────────────────────────────────────────────────────────────────────────
  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int w = getWidth();
    int h = getHeight();

    // Base dark fill
    g2.setColor(ColorTheme.get().appBgBase);
    g2.fillRect(0, 0, w, h);

    // Diagonal rainbow overlay — top-left to bottom-right
    g2.setPaint(new LinearGradientPaint(new Point2D.Float(0, 0), new Point2D.Float(w, h),
        ColorTheme.get().appGradFractions(), ColorTheme.get().appGradColors()));
    g2.fillRect(0, 0, w, h);

    g2.dispose();
    // Do NOT call super — we own the background entirely
  }

  // ─────────────────────────────────────────────────────────────────────────
  // PUBLIC API
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Push a fresh queue snapshot to the embedded card. Safe to call from any thread; the card does
   * its own EDT dispatch internally.
   */
  public void setQueue(List<SongQueueEntryDto> queue) {
    songQueueCard.setQueue(queue);
  }

  /**
   * Called by the tab {@code ChangeListener} whenever this tab is selected. Refreshes the queue
   * list and restarts the countdown timer.
   */
  public void onShown() {
    songQueueCard.onShown();
  }
}
