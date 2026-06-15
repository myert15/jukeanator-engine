package com.djt.jukeanator_engine.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import com.djt.jukeanator_engine.domain.songlibrary.dto.AuthenticateForAdminPanelRequest;
import com.djt.jukeanator_engine.domain.songlibrary.service.SongLibraryService;

/**
 * Full-screen overlay card that prompts for admin credentials via the shared {@link KeyboardPanel}
 * on-screen keyboard.
 *
 * <p>
 * Lifecycle:
 * <ol>
 * <li>Shown via
 * {@link com.djt.jukeanator_engine.ui.components.JukeANatorFrame#showLoginToAdminPanelCard()} when
 * the user clicks the Credits panel.</li>
 * <li>On successful authentication the {@code onSuccess} runnable is invoked (switches to the Admin
 * tab).</li>
 * <li>On Cancel or timeout the {@code onDismiss} runnable is invoked (returns to the previous
 * card).</li>
 * </ol>
 *
 * <p>
 * The area outside the credential fields and keyboard is painted with the same screen gradient used
 * by {@link AddSongToQueueCard}.
 */
public class LoginToAdminPanelCard extends JPanel {

  private static final long serialVersionUID = 1L;

  // ── Palette ───────────────────────────────────────────────────────────────
  private static final Color ACCENT_BLUE = new Color(0, 210, 255);
  private static final Color TEXT_PRIMARY = Color.WHITE;
  private static final Color TEXT_SECONDARY = new Color(180, 180, 180);
  private static final Color FIELD_BG = Color.BLACK;
  private static final Color ERROR_COLOR = new Color(220, 60, 60);

  // ── Button gradient palette (matches AlbumDetailCard back-button style) ───
  private static final Color GRAD_TOP = new Color(0, 160, 210);
  private static final Color GRAD_BOTTOM = new Color(0, 80, 130);
  private static final Color HOVER_TOP = new Color(0, 190, 240);
  private static final Color HOVER_BOTTOM = new Color(0, 100, 160);

  // ── Timeout ───────────────────────────────────────────────────────────────
  private static final int TIMEOUT_SECONDS = 120;

  // ── Field focus ───────────────────────────────────────────────────────────
  private enum ActiveField {
    USERNAME, PASSWORD
  }

  private ActiveField activeField = ActiveField.USERNAME;

  // ── State ─────────────────────────────────────────────────────────────────
  private final StringBuilder usernameBuffer = new StringBuilder();
  private final StringBuilder passwordBuffer = new StringBuilder();

  private JLabel usernameLabel;
  private JLabel passwordLabel;
  private JLabel errorLabel;

  private int secondsRemaining = TIMEOUT_SECONDS;
  private Timer countdownTimer;
  private final JLabel timeoutLabel = new JLabel();
  private final JProgressBar timeoutBar = new JProgressBar(0, TIMEOUT_SECONDS);

  // ── Dependencies ──────────────────────────────────────────────────────────
  private final SongLibraryService songLibraryService;
  private final Runnable onSuccess;
  private final Runnable onDismiss;

  // ─────────────────────────────────────────────────────────────────────────
  // CONSTRUCTOR
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * @param songLibraryService used to call {@code authenticateForAdminPanel}
   * @param onSuccess runnable executed when authentication succeeds
   * @param onDismiss runnable executed on Cancel or timeout
   */
  public LoginToAdminPanelCard(SongLibraryService songLibraryService, Runnable onSuccess,
      Runnable onDismiss) {

    this.songLibraryService = songLibraryService;
    this.onSuccess = onSuccess;
    this.onDismiss = onDismiss;

    setOpaque(false);
    setLayout(new BorderLayout());

    // ── Content area ──────────────────────────────────────────────────────
    JPanel center = new JPanel(new GridBagLayout());
    center.setOpaque(false);
    center.add(buildCredentialPanel());

    // ── Keyboard ──────────────────────────────────────────────────────────
    KeyboardPanel keyboard = new KeyboardPanel(new KeyboardPanel.KeyboardListener() {
      @Override
      public void onCharacter(String ch) {
        activeBuffer().append(ch);
        syncLabels();
        checkAndAdvanceFocus();
      }

      @Override
      public void onBackspace() {
        StringBuilder buf = activeBuffer();
        if (buf.length() > 0)
          buf.deleteCharAt(buf.length() - 1);
        syncLabels();
      }

      @Override
      public void onClear() {
        activeBuffer().setLength(0);
        syncLabels();
      }

      @Override
      public void onSpace() {
        activeBuffer().append(' ');
        syncLabels();
        checkAndAdvanceFocus();
      }
    });

    add(center, BorderLayout.CENTER);
    add(keyboard, BorderLayout.SOUTH);

    // ── Countdown timer ───────────────────────────────────────────────────
    countdownTimer = new Timer(1000, e -> {
      secondsRemaining--;
      updateTimeout();
      if (secondsRemaining <= 0) {
        onDismiss.run();
      }
    });
    countdownTimer.start();
  }

  /** Must be called when the card is removed from view so the timer doesn't leak. */
  public void dismiss() {
    countdownTimer.stop();
  }

  /** Called whenever this card is shown — restarts the countdown. */
  public void onShown() {
    secondsRemaining = TIMEOUT_SECONDS;
    updateTimeout();
    if (!countdownTimer.isRunning())
      countdownTimer.start();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // CREDENTIAL PANEL
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Builds the centred panel containing the title, username / password fields, the Login / Cancel
   * buttons, and the timeout bar.
   */
  private JPanel buildCredentialPanel() {

    JPanel panel = new JPanel(new BorderLayout()) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Accent-blue rounded border — matches AddSongToQueueCard border style
        g2.setColor(ACCENT_BLUE);
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
        g2.dispose();
      }
    };
    panel.setPreferredSize(new Dimension(700, 340));
    panel.setBackground(new Color(22, 22, 28));
    panel.setOpaque(true);
    panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 20, 32));

    // Title
    JLabel title = new JLabel("ADMIN LOGIN", SwingConstants.CENTER);
    title.setForeground(new Color(255, 220, 0));
    title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
    title.setBorder(new EmptyBorder(0, 0, 16, 0));
    panel.add(title, BorderLayout.NORTH);

    // Fields + buttons
    JPanel body = new JPanel(new BorderLayout(0, 12));
    body.setOpaque(false);

    body.add(buildFieldsPanel(), BorderLayout.CENTER);
    body.add(buildButtonRow(), BorderLayout.SOUTH);

    panel.add(body, BorderLayout.CENTER);

    // Timeout strip
    panel.add(buildTimeoutStrip(), BorderLayout.SOUTH);

    return panel;
  }

  /** Two labelled text-display fields (Username and Password). */
  private JPanel buildFieldsPanel() {

    JPanel fields = new JPanel(new java.awt.GridLayout(2, 1, 0, 10));
    fields.setOpaque(false);

    // Username
    JPanel userRow = buildFieldRow("USERNAME:", false);
    fields.add(userRow);

    // Password
    JPanel passRow = buildFieldRow("PASSWORD:", true);
    fields.add(passRow);

    return fields;
  }

  /**
   * Builds a single labelled field row.
   *
   * @param labelText caption shown to the left of the field
   * @param isPassword when {@code true} this field receives the password display label and clicking
   *        it switches focus to PASSWORD
   */
  private JPanel buildFieldRow(String labelText, boolean isPassword) {

    JPanel row = new JPanel(new BorderLayout(12, 0));
    row.setOpaque(false);

    JLabel caption = new JLabel(labelText);
    caption.setForeground(TEXT_SECONDARY);
    caption.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
    caption.setPreferredSize(new Dimension(140, 48));
    caption.setHorizontalAlignment(SwingConstants.RIGHT);

    JLabel field = new JLabel();
    field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
    field.setForeground(TEXT_PRIMARY);
    field.setBackground(FIELD_BG);
    field.setOpaque(true);
    field.setBorder(
        BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 1, 1, 1, Color.WHITE),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    field.setPreferredSize(new Dimension(100, 48));

    // Tap the field to switch keyboard focus to it
    field.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    field.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        activeField = isPassword ? ActiveField.PASSWORD : ActiveField.USERNAME;
        refreshFieldBorders();
        // Reset timeout on interaction
        secondsRemaining = TIMEOUT_SECONDS;
        updateTimeout();
      }
    });

    if (isPassword) {
      passwordLabel = field;
    } else {
      usernameLabel = field;
      // Username is active by default
      field.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(2, 1, 1, 1, ACCENT_BLUE),
          BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    row.add(caption, BorderLayout.WEST);
    row.add(field, BorderLayout.CENTER);
    return row;
  }

  /** Login / Cancel buttons and error label row. */
  private JPanel buildButtonRow() {

    // Error / feedback label
    errorLabel = new JLabel(" ", SwingConstants.CENTER);
    errorLabel.setForeground(ERROR_COLOR);
    errorLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

    JButton loginBtn = createActionButton("LOGIN", () -> attemptLogin());
    JButton cancelBtn = createActionButton("CANCEL", onDismiss);

    JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 0));
    buttons.setOpaque(false);
    buttons.add(loginBtn);
    buttons.add(cancelBtn);

    JPanel row = new JPanel(new BorderLayout(0, 6));
    row.setOpaque(false);
    row.add(errorLabel, BorderLayout.NORTH);
    row.add(buttons, BorderLayout.CENTER);
    return row;
  }

  /** Timeout progress bar + label strip along the bottom of the credential panel. */
  private JPanel buildTimeoutStrip() {

    JPanel strip = new JPanel(new BorderLayout(8, 0));
    strip.setOpaque(false);
    strip.setBorder(new EmptyBorder(10, 0, 0, 0));

    timeoutBar.setValue(TIMEOUT_SECONDS);
    timeoutBar.setForeground(ACCENT_BLUE);
    timeoutBar.setOpaque(false);
    timeoutBar.setBorderPainted(false);
    timeoutBar.setStringPainted(false);

    timeoutLabel.setForeground(TEXT_SECONDARY);
    timeoutLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));

    updateTimeout();

    strip.add(timeoutBar, BorderLayout.CENTER);
    strip.add(timeoutLabel, BorderLayout.EAST);
    return strip;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // ACTION BUTTON (shared gradient style)
  // ─────────────────────────────────────────────────────────────────────────

  private JButton createActionButton(String text, Runnable action) {

    JButton button = new JButton(text) {
      private static final long serialVersionUID = 1L;
      private boolean hovered = false;

      {
        addMouseListener(new java.awt.event.MouseAdapter() {
          @Override
          public void mouseEntered(java.awt.event.MouseEvent e) {
            hovered = true;
            repaint();
          }

          @Override
          public void mouseExited(java.awt.event.MouseEvent e) {
            hovered = false;
            repaint();
          }
        });
      }

      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color top = hovered ? HOVER_TOP : GRAD_TOP;
        Color bottom = hovered ? HOVER_BOTTOM : GRAD_BOTTOM;
        g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        g2.setColor(ACCENT_BLUE);
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

        g2.dispose();
        super.paintComponent(g);
      }
    };

    button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
    button.setForeground(Color.WHITE);
    button.setContentAreaFilled(false);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setOpaque(false);
    button.setPreferredSize(new Dimension(160, 52));
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.addActionListener(e -> action.run());
    return button;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // AUTHENTICATION
  // ─────────────────────────────────────────────────────────────────────────

  private void attemptLogin() {
    // Reset timeout on explicit login attempt
    secondsRemaining = TIMEOUT_SECONDS;
    updateTimeout();

    String username = usernameBuffer.toString();
    String password = passwordBuffer.toString();

    try {
      Boolean result = songLibraryService
          .authenticateForAdminPanel(new AuthenticateForAdminPanelRequest(username, password));
      if (Boolean.TRUE.equals(result)) {
        countdownTimer.stop();
        onSuccess.run();
      } else {
        errorLabel.setText("Invalid credentials. Please try again.");
        // Clear password field on failure, keep username
        passwordBuffer.setLength(0);
        syncLabels();
        activeField = ActiveField.PASSWORD;
        refreshFieldBorders();
      }
    } catch (Exception ex) {
      errorLabel.setText("Authentication error. Please try again.");
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // HELPERS
  // ─────────────────────────────────────────────────────────────────────────

  /** Returns the buffer that currently has keyboard focus. */
  private StringBuilder activeBuffer() {
    return activeField == ActiveField.USERNAME ? usernameBuffer : passwordBuffer;
  }

  /**
   * Checks if the username has reached 5 characters and auto-advances focus to the password field.
   */
  private void checkAndAdvanceFocus() {
    if (activeField == ActiveField.USERNAME && usernameBuffer.length() == 5) {
      activeField = ActiveField.PASSWORD;
      refreshFieldBorders();
    }
  }

  /** Syncs display labels from the internal buffers. */
  private void syncLabels() {
    if (usernameLabel != null) {
      String u = usernameBuffer.toString();
      usernameLabel.setText(u.isEmpty() ? " " : u);
    }
    if (passwordLabel != null) {
      // Mask password with bullets
      String mask = "•".repeat(passwordBuffer.length());
      passwordLabel.setText(mask.isEmpty() ? " " : mask);
    }
  }

  /** Highlights the active field's border with the accent colour. */
  private void refreshFieldBorders() {
    if (usernameLabel != null) {
      Color borderColor = (activeField == ActiveField.USERNAME) ? ACCENT_BLUE : Color.WHITE;
      usernameLabel.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(2, 1, 1, 1, borderColor),
          BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }
    if (passwordLabel != null) {
      Color borderColor = (activeField == ActiveField.PASSWORD) ? ACCENT_BLUE : Color.WHITE;
      passwordLabel.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(2, 1, 1, 1, borderColor),
          BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }
  }

  private void updateTimeout() {
    timeoutBar.setValue(secondsRemaining);
    timeoutLabel.setText("Closes in " + secondsRemaining + "s");
  }
}
