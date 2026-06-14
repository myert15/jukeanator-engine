package com.djt.jukeanator_engine.ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Reusable on-screen keyboard panel.
 *
 * <p>
 * Extracted from {@link SearchPanel} so that other UI components (e.g.
 * {@link LoginToAdminPanelCard}) can embed the keyboard without duplicating code.
 *
 * <p>
 * Callers supply a {@link KeyboardListener} that receives each character appended, a backspace
 * event, a clear event, and a space event. The keyboard manages its own ABC / 123 mode toggle
 * internally.
 */
public class KeyboardPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // ── Palette ───────────────────────────────────────────────────────────────
  private static final Color ACCENT_BLUE = new Color(0, 210, 255);
  private static final Color TEXT_PRIMARY = Color.WHITE;

  // Key colours — AMI 3D style (kept identical to SearchPanel originals)
  private static final Color KEY_FACE_TOP = new Color(72, 76, 88);
  private static final Color KEY_FACE_MID = new Color(52, 55, 65);
  private static final Color KEY_FACE_BOTTOM = new Color(35, 37, 45);
  private static final Color KEY_SHELF = new Color(18, 18, 22);
  private static final Color KEY_SHADOW = new Color(6, 6, 8);
  private static final Color KEY_HIGHLIGHT = new Color(160, 162, 175, 200);
  private static final Color KEY_SIDE = new Color(100, 102, 115, 80);

  // ── Layout constants ──────────────────────────────────────────────────────
  /** Preferred height of the keyboard wrapper (matches SearchPanel). */
  public static final int KEYBOARD_HEIGHT = 260;

  /** Horizontal margin applied to the keyboard wrapper. */
  public static final int SCREEN_PADDING_HORIZONTAL = 60;

  // ── Keyboard mode ─────────────────────────────────────────────────────────
  private enum KeyboardMode {
    ABC, NUMERIC
  }

  private KeyboardMode keyboardMode = KeyboardMode.ABC;

  // ── Callback ──────────────────────────────────────────────────────────────

  /**
   * Listener interface supplied by the parent component.
   *
   * <p>
   * All methods are called on the EDT immediately when the corresponding key is pressed.
   */
  public interface KeyboardListener {
    /** A printable character (letter, digit, symbol) was pressed. */
    void onCharacter(String ch);

    /** The BACKSPACE key was pressed. */
    void onBackspace();

    /** The CLEAR key was pressed. */
    void onClear();

    /** The SPACE key was pressed. */
    void onSpace();
  }

  private final KeyboardListener listener;

  // ── Panel references rebuilt on mode toggle ────────────────────────────────
  /** The outer wrapper returned by {@link #buildKeyboardWrapper()}. */
  private final JPanel outerWrapper;

  // ─────────────────────────────────────────────────────────────────────────
  // CONSTRUCTOR
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Creates a self-contained keyboard wrapper panel sized at {@link #KEYBOARD_HEIGHT} pixels tall.
   *
   * @param listener receives key events
   */
  public KeyboardPanel(KeyboardListener listener) {
    this.listener = listener;

    setLayout(new java.awt.BorderLayout());
    setOpaque(false);
    setPreferredSize(new Dimension(100, KEYBOARD_HEIGHT));
    setMinimumSize(new Dimension(100, KEYBOARD_HEIGHT));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, KEYBOARD_HEIGHT));

    outerWrapper = buildKeyboardWrapper();
    add(outerWrapper, java.awt.BorderLayout.CENTER);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // KEYBOARD WRAPPER
  // ─────────────────────────────────────────────────────────────────────────

  private JPanel buildKeyboardWrapper() {
    JPanel wrapper = new JPanel(new java.awt.BorderLayout());
    wrapper.setOpaque(false);
    wrapper.setBorder(new EmptyBorder(0, SCREEN_PADDING_HORIZONTAL, 0, SCREEN_PADDING_HORIZONTAL));

    JPanel frostedBodyPanel = new JPanel(new java.awt.BorderLayout()) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
      }
    };
    frostedBodyPanel.setOpaque(false);

    JPanel centreShell = new JPanel(new GridBagLayout());
    centreShell.setOpaque(false);
    centreShell.add(buildKeyboardPanel());

    frostedBodyPanel.add(centreShell, java.awt.BorderLayout.CENTER);
    wrapper.add(frostedBodyPanel, java.awt.BorderLayout.CENTER);

    return wrapper;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // KEYBOARD LAYOUT
  // ─────────────────────────────────────────────────────────────────────────

  private JPanel buildKeyboardPanel() {
    JPanel p = new JPanel(new GridLayout(3, 1, 10, 10));
    p.setOpaque(false);
    p.setBorder(new EmptyBorder(20, 20, 20, 20));

    if (keyboardMode == KeyboardMode.ABC) {
      p.add(buildAbcRow1());
      p.add(buildAbcRow2());
      p.add(buildAbcRow3());
    } else {
      p.add(buildNumRow1());
      p.add(buildNumRow2());
      p.add(buildNumRow3());
    }
    return p;
  }

  private JPanel buildAbcRow1() {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    row.setOpaque(false);
    for (char c : "QWERTYUIOP".toCharArray())
      row.add(letterKey(String.valueOf(c)));

    JButton clear = styledKey("CLEAR", new Dimension(140, 60));
    clear.addActionListener(e -> listener.onClear());
    row.add(clear);

    row.add(buildBackspaceButton());
    return row;
  }

  private JPanel buildAbcRow2() {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    row.setOpaque(false);
    for (char c : "ASDFGHJKL".toCharArray())
      row.add(letterKey(String.valueOf(c)));

    row.add(letterKey("'"));
    row.add(buildModeToggleButton("123@", KeyboardMode.NUMERIC));
    row.add(buildModeToggleButton("ABC", KeyboardMode.ABC));
    return row;
  }

  private JPanel buildAbcRow3() {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    row.setOpaque(false);
    for (char c : "ZXCVBNM,.".toCharArray())
      row.add(letterKey(String.valueOf(c)));

    JButton space = styledKey("SPACE", new Dimension(420, 60));
    space.addActionListener(e -> listener.onSpace());
    row.add(space);
    return row;
  }

  private JPanel buildNumRow1() {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    row.setOpaque(false);
    for (String s : new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"})
      row.add(letterKey(s));

    JButton clear = styledKey("CLEAR", new Dimension(140, 60));
    clear.addActionListener(e -> listener.onClear());
    row.add(clear);

    row.add(buildBackspaceButton());
    return row;
  }

  private JPanel buildNumRow2() {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    row.setOpaque(false);
    for (String s : new String[] {"!", "@", "#", "$", "%", "^", "&", "*", "\"", "'"})
      row.add(letterKey(s));

    row.add(buildModeToggleButton("123@", KeyboardMode.NUMERIC));
    row.add(buildModeToggleButton("ABC", KeyboardMode.ABC));
    return row;
  }

  private JPanel buildNumRow3() {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    row.setOpaque(false);
    for (String s : new String[] {"(", ")", "[", "]", "/", "\\", "?", ":", ";"})
      row.add(letterKey(s));

    JButton space = styledKey("SPACE", new Dimension(420, 60));
    space.addActionListener(e -> listener.onSpace());
    row.add(space);
    return row;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // KEY HELPERS
  // ─────────────────────────────────────────────────────────────────────────

  private JButton buildBackspaceButton() {
    JButton back = styledKey("⌫", new Dimension(100, 60));
    back.addActionListener(e -> listener.onBackspace());
    return back;
  }

  private JButton buildModeToggleButton(String label, KeyboardMode targetMode) {
    JButton btn = styledKey(label, new Dimension(140, 60));
    if (targetMode == keyboardMode) {
      btn.setBackground(new Color(0, 160, 200));
    }
    btn.addActionListener(e -> {
      if (keyboardMode != targetMode) {
        keyboardMode = targetMode;
        refreshKeyboard();
      }
    });
    return btn;
  }

  private void refreshKeyboard() {
    JPanel frostedBody = (JPanel) outerWrapper.getComponent(0);
    frostedBody.removeAll();

    JPanel centreShell = new JPanel(new GridBagLayout());
    centreShell.setOpaque(false);
    centreShell.add(buildKeyboardPanel());
    frostedBody.add(centreShell, java.awt.BorderLayout.CENTER);

    frostedBody.repaint();
    frostedBody.revalidate();
  }

  private JButton letterKey(String text) {
    JButton btn = styledKey(text, new Dimension(70, 60));
    btn.addActionListener(e -> listener.onCharacter(text));
    return btn;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // STYLED KEY (AMI 3D — identical to SearchPanel original)
  // ─────────────────────────────────────────────────────────────────────────

  private JButton styledKey(String text, Dimension size) {
    JButton btn = new JButton(text) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 7;

        // Reserve bottom pixels for the shadow; the visible key sits in [0, visH)
        int shadowH = 4;
        int visH = h - shadowH;

        // ── Drop-shadow slab ──────────────────────────────────────────────
        g2.setColor(KEY_SHADOW);
        g2.fillRoundRect(1, shadowH, w - 2, visH, arc, arc);

        // ── Shelf band (bottom ~25 % of visible key) ──────────────────────
        int shelfH = Math.round(visH * 0.25f);
        int faceH = visH - shelfH;

        g2.setColor(KEY_SHELF);
        g2.fillRoundRect(1, faceH, w - 2, shelfH + arc / 2, arc, arc);

        // ── Face gradient ─────────────────────────────────────────────────
        boolean pressed = getModel().isArmed();
        Color fTop = pressed ? KEY_FACE_BOTTOM : KEY_FACE_TOP;
        Color fMid = KEY_FACE_MID;
        Color fBot = pressed ? KEY_FACE_TOP : KEY_FACE_BOTTOM;
        g2.setPaint(new LinearGradientPaint(0, 0, 0, faceH, new float[] {0f, 0.55f, 1f},
            new Color[] {fTop, fMid, fBot}));
        g2.fillRoundRect(1, 0, w - 2, faceH + arc / 2, arc, arc);

        // ── Specular top-edge highlight ────────────────────────────────────
        g2.setColor(KEY_HIGHLIGHT);
        g2.setStroke(new java.awt.BasicStroke(1.2f));
        g2.drawLine(arc, 1, w - arc - 1, 1);

        // ── Side-edge sheens ──────────────────────────────────────────────
        g2.setColor(KEY_SIDE);
        g2.setStroke(new java.awt.BasicStroke(1f));
        g2.drawLine(1, 2, 1, faceH - 2);
        g2.drawLine(w - 2, 2, w - 2, faceH - 2);

        // ── Label — vertically centred in faceH ───────────────────────────
        g2.setFont(getFont());
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (faceH - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(pressed ? ACCENT_BLUE : TEXT_PRIMARY);
        g2.drawString(getText(), tx, ty);

        g2.dispose();
      }

      @Override
      protected void paintBorder(Graphics g) {}
    };

    btn.setPreferredSize(size);
    btn.setFocusPainted(false);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setOpaque(false);
    btn.setForeground(TEXT_PRIMARY);
    btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return btn;
  }
}
