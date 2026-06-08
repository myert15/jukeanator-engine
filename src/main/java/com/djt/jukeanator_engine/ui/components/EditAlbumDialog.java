package com.djt.jukeanator_engine.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.djt.jukeanator_engine.domain.songlibrary.dto.AlbumDto;
import com.djt.jukeanator_engine.domain.songlibrary.service.SongLibraryService;

/**
 * Modal dialog for editing album metadata: release year, record label, and cover art path.
 * Cover art can be browsed from the filesystem via a standard file chooser.
 */
public class EditAlbumDialog extends JDialog {

  private static final long serialVersionUID = 1L;

  // ── Palette ───────────────────────────────────────────────────────────────
  private static final Color BG_DIALOG    = new Color(14, 14, 20);
  private static final Color BG_FIELD     = new Color(22, 22, 32);
  private static final Color ACCENT_BLUE  = new Color(0, 210, 255);
  private static final Color ACCENT_GOLD  = new Color(255, 200, 0);
  private static final Color TEXT_PRIMARY = Color.WHITE;
  private static final Color TEXT_MUTED   = new Color(160, 165, 180);
  private static final Color BORDER_COLOR = new Color(50, 55, 75);

  // ── State ─────────────────────────────────────────────────────────────────
  private final AlbumDto album;
  private final SongLibraryService songLibraryService;
  private final ImageLoader imageLoader;

  private JTextField releaseDateField;
  private JTextField recordLabelField;
  private JTextField coverArtPathField;
  private JLabel coverArtPreview;

  // ─────────────────────────────────────────────────────────────────────────
  // CONSTRUCTOR
  // ─────────────────────────────────────────────────────────────────────────

  public EditAlbumDialog(Frame owner, AlbumDto album, SongLibraryService songLibraryService,
      ImageLoader imageLoader) {

    super(owner, "Edit Album", true);
    this.album = album;
    this.songLibraryService = songLibraryService;
    this.imageLoader = imageLoader;

    setUndecorated(true);
    setBackground(BG_DIALOG);

    JPanel root = new JPanel(new BorderLayout(0, 0)) {
      private static final long serialVersionUID = 1L;
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(BG_DIALOG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    root.setOpaque(false);
    root.setBorder(new EmptyBorder(24, 28, 24, 28));

    root.add(buildHeader(), BorderLayout.NORTH);
    root.add(buildBody(), BorderLayout.CENTER);
    root.add(buildFooter(), BorderLayout.SOUTH);

    setContentPane(root);
    pack();
    setLocationRelativeTo(owner);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // HEADER
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildHeader() {

    JPanel header = new JPanel(new BorderLayout(16, 0));
    header.setOpaque(false);
    header.setBorder(new EmptyBorder(0, 0, 20, 0));

    // Cover art preview (left)
    coverArtPreview = new JLabel();
    coverArtPreview.setPreferredSize(new Dimension(96, 96));
    coverArtPreview.setHorizontalAlignment(SwingConstants.CENTER);
    coverArtPreview.setVerticalAlignment(SwingConstants.CENTER);
    coverArtPreview.setOpaque(true);
    coverArtPreview.setBackground(new Color(24, 24, 36));
    coverArtPreview.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    refreshPreview(album.getCoverArtPath());

    // Title block (right)
    JPanel titles = new JPanel();
    titles.setOpaque(false);
    titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

    JLabel editLabel = new JLabel("EDIT ALBUM");
    editLabel.setForeground(ACCENT_GOLD);
    editLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

    JLabel nameLabel = new JLabel(album.getAlbumName() != null ? album.getAlbumName() : "");
    nameLabel.setForeground(TEXT_PRIMARY);
    nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));

    JLabel artistLabel = new JLabel(album.getArtistName() != null ? album.getArtistName() : "");
    artistLabel.setForeground(ACCENT_BLUE);
    artistLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));

    titles.add(editLabel);
    titles.add(Box.createVerticalStrut(4));
    titles.add(nameLabel);
    titles.add(Box.createVerticalStrut(2));
    titles.add(artistLabel);

    header.add(coverArtPreview, BorderLayout.WEST);
    header.add(titles, BorderLayout.CENTER);

    return header;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // BODY — form fields
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildBody() {

    JPanel body = new JPanel(new GridBagLayout());
    body.setOpaque(false);

    GridBagConstraints lc = new GridBagConstraints();
    lc.anchor = GridBagConstraints.WEST;
    lc.insets = new Insets(8, 0, 2, 16);
    lc.gridx = 0;

    GridBagConstraints fc = new GridBagConstraints();
    fc.fill = GridBagConstraints.HORIZONTAL;
    fc.weightx = 1.0;
    fc.insets = new Insets(8, 0, 2, 0);
    fc.gridx = 1;

    GridBagConstraints bc = new GridBagConstraints();
    bc.insets = new Insets(8, 8, 2, 0);
    bc.gridx = 2;

    int row = 0;

    // ── Release Date ──────────────────────────────────────────────────────
    lc.gridy = row; fc.gridy = row;
    body.add(fieldLabel("Release Date"), lc);
    releaseDateField = styledField(album.getReleaseDate());
    body.add(releaseDateField, fc);
    row++;

    // ── Record Label ──────────────────────────────────────────────────────
    lc.gridy = row; fc.gridy = row;
    body.add(fieldLabel("Record Label"), lc);
    recordLabelField = styledField(album.getRecordLabel());
    body.add(recordLabelField, fc);
    row++;

    // ── Cover Art Path ────────────────────────────────────────────────────
    lc.gridy = row; fc.gridy = row; bc.gridy = row;
    body.add(fieldLabel("Cover Art Path"), lc);
    coverArtPathField = styledField(album.getCoverArtPath());
    body.add(coverArtPathField, fc);

    JButton browseBtn = adminButton("Browse…", ACCENT_BLUE);
    browseBtn.setPreferredSize(new Dimension(110, 42));
    browseBtn.addActionListener(e -> browseCoverArt());
    body.add(browseBtn, bc);
    row++;

    // ── Cover Art Search ──────────────────────────────────────────────────
    GridBagConstraints fullRow = new GridBagConstraints();
    fullRow.gridx = 0; fullRow.gridy = row;
    fullRow.gridwidth = 3;
    fullRow.fill = GridBagConstraints.NONE;
    fullRow.anchor = GridBagConstraints.EAST;
    fullRow.insets = new Insets(12, 0, 4, 0);

    JButton searchArtBtn = adminButton("Search Cover Art Online…", ACCENT_GOLD);
    searchArtBtn.setPreferredSize(new Dimension(280, 42));
    searchArtBtn.addActionListener(e -> searchCoverArtOnline());
    body.add(searchArtBtn, fullRow);

    return body;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // FOOTER — Save / Cancel
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildFooter() {

    JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 0));
    footer.setOpaque(false);
    footer.setBorder(new EmptyBorder(20, 0, 0, 0));

    JButton cancelBtn = adminButton("Cancel", TEXT_MUTED);
    cancelBtn.setPreferredSize(new Dimension(120, 48));
    cancelBtn.addActionListener(e -> dispose());

    JButton saveBtn = adminButton("Save", ACCENT_BLUE);
    saveBtn.setPreferredSize(new Dimension(120, 48));
    saveBtn.addActionListener(e -> saveChanges());

    footer.add(cancelBtn);
    footer.add(saveBtn);

    return footer;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // ACTIONS
  // ─────────────────────────────────────────────────────────────────────────

  private void browseCoverArt() {

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Select Cover Art");
    chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));

    if (album.getCoverArtPath() != null) {
      File current = new File(album.getCoverArtPath()).getParentFile();
      if (current != null && current.exists()) {
        chooser.setCurrentDirectory(current);
      }
    }

    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      String path = chooser.getSelectedFile().getAbsolutePath();
      coverArtPathField.setText(path);
      refreshPreview(path);
    }
  }

  private void searchCoverArtOnline() {

    // Build a Google Images search URL for the album name + artist
    String query = (album.getArtistName() != null ? album.getArtistName() : "") + " "
        + (album.getAlbumName() != null ? album.getAlbumName() : "") + " album cover";
    try {
      String encoded = java.net.URLEncoder.encode(query, "UTF-8");
      java.awt.Desktop.getDesktop()
          .browse(new java.net.URI("https://www.google.com/search?tbm=isch&q=" + encoded));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void saveChanges() {

    try {
      /*
      // TODO: Implement SongLibraryService.updateAlbumMetadata();
      album.setReleaseDate(releaseDateField.getText().trim());
      album.setRecordLabel(recordLabelField.getText().trim());
      album.setCoverArtPath(coverArtPathField.getText().trim());
      songLibraryService.updateAlbum(album);
      dispose();
      */
    } catch (Exception ex) {
      ex.printStackTrace();
      JLabel err = new JLabel("Save failed: " + ex.getMessage());
      err.setForeground(new Color(220, 60, 60));
      err.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    }
  }

  private void refreshPreview(String path) {

    if (path != null && !path.isBlank()) {
      try {
        ImageIcon icon = imageLoader.loadFilesystemImage(path, 96, 96);
        if (icon != null) {
          coverArtPreview.setIcon(icon);
          coverArtPreview.setText(null);
          return;
        }
      } catch (Exception ignored) {}
    }
    coverArtPreview.setIcon(null);
    coverArtPreview.setText("♫");
    coverArtPreview.setForeground(new Color(80, 80, 100));
    coverArtPreview.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
  }

  // ─────────────────────────────────────────────────────────────────────────
  // FACTORY
  // ─────────────────────────────────────────────────────────────────────────

  public static void show(Frame owner, AlbumDto album, SongLibraryService songLibraryService,
      ImageLoader imageLoader) {

    EditAlbumDialog dlg = new EditAlbumDialog(owner, album, songLibraryService, imageLoader);
    dlg.setVisible(true);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // WIDGET HELPERS
  // ─────────────────────────────────────────────────────────────────────────

  private static JLabel fieldLabel(String text) {

    JLabel lbl = new JLabel(text);
    lbl.setForeground(TEXT_MUTED);
    lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    return lbl;
  }

  private static JTextField styledField(String value) {

    JTextField field = new JTextField(value != null ? value : "") {
      private static final long serialVersionUID = 1L;
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(BG_FIELD);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new java.awt.BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    field.setOpaque(false);
    field.setForeground(TEXT_PRIMARY);
    field.setCaretColor(ACCENT_BLUE);
    field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
    field.setBorder(new EmptyBorder(6, 10, 6, 10));
    field.setPreferredSize(new Dimension(360, 42));
    return field;
  }

  /**
   * Compact gradient button matching the AdminPanel button style.
   */
  static JButton adminButton(String text, Color accent) {

    final Color GRAD_TOP    = accent.darker();
    final Color GRAD_BOTTOM = accent.darker().darker();

    JButton btn = new JButton(text) {
      private static final long serialVersionUID = 1L;
      private boolean hovered = false;
      {
        addMouseListener(new java.awt.event.MouseAdapter() {
          public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true;  repaint(); }
          public void mouseExited (java.awt.event.MouseEvent e) { hovered = false; repaint(); }
        });
      }
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        Color top = hovered ? GRAD_TOP.brighter() : GRAD_TOP;
        Color bot = hovered ? GRAD_BOTTOM.brighter() : GRAD_BOTTOM;
        g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
        g2.fillRoundRect(0, 0, w, h, 10, 10);

        g2.setColor(accent);
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        g2.drawRoundRect(1, 1, w - 3, h - 3, 10, 10);

        g2.setFont(getFont());
        g2.setColor(Color.WHITE);
        java.awt.FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(),
            (w - fm.stringWidth(getText())) / 2,
            (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
      }
    };
    btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    btn.setForeground(Color.WHITE);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setOpaque(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return btn;
  }
}