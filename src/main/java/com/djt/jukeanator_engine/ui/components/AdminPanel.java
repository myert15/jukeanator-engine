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
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.djt.jukeanator_engine.domain.songlibrary.dto.AlbumDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.SongDto;
import com.djt.jukeanator_engine.domain.songlibrary.service.SongLibraryService;
import com.djt.jukeanator_engine.domain.songplayer.service.SongPlayerService;
import com.djt.jukeanator_engine.domain.songqueue.dto.AddAlbumToQueueRequest;
import com.djt.jukeanator_engine.domain.songqueue.dto.ChangeSongQueueRequest;
import com.djt.jukeanator_engine.domain.songqueue.dto.SongQueueEntryDto;
import com.djt.jukeanator_engine.domain.songqueue.service.SongQueueService;
import com.djt.jukeanator_engine.ui.model.CreditManager;
import com.djt.jukeanator_engine.ui.util.PlayListManager;

/**
 * Admin panel providing direct control over the song queue, album library management,
 * and jukebox operation. Styled to match the JukeANator dark UI palette.
 *
 * <p>Layout:
 * <ul>
 *   <li><b>LEFT</b>  — Scrollable album list with per-album and global action buttons below.</li>
 *   <li><b>RIGHT</b> — Scrollable song queue list with playback and queue management buttons.</li>
 * </ul>
 */
public class AdminPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  // ── Palette ───────────────────────────────────────────────────────────────
  private static final Color ACCENT_BLUE   = new Color(0, 210, 255);
  private static final Color ACCENT_GOLD   = new Color(255, 200, 0);
  private static final Color ACCENT_GREEN  = new Color(60, 210, 80);
  private static final Color ACCENT_RED    = new Color(220, 60, 60);
  private static final Color ACCENT_ORANGE = new Color(255, 140, 0);
  private static final Color ACCENT_VIOLET = new Color(180, 80, 255);
  private static final Color TEXT_PRIMARY  = Color.WHITE;
  private static final Color TEXT_MUTED    = new Color(160, 165, 180);
  private static final Color LIST_BG       = new Color(10, 12, 18);
  private static final Color LIST_SEL_BG   = new Color(0, 60, 80);
  private static final Color ROW_ALT       = new Color(18, 20, 28);
  private static final Color SEPARATOR     = new Color(40, 44, 60);

  // ── Button sizing ─────────────────────────────────────────────────────────
  private static final Dimension BTN_WIDE   = new Dimension(Integer.MAX_VALUE, 52);
  private static final Dimension BTN_NARROW = new Dimension(Integer.MAX_VALUE, 48);

  // ── Dependencies ──────────────────────────────────────────────────────────
  private final SongLibraryService songLibraryService;
  private final SongQueueService songQueueService;
  private final SongPlayerService songPlayerService;
  private final CreditManager creditManager;
  private final ImageLoader imageLoader;
  private final Frame ownerFrame;
  //private final Runnable onMinimize;
  //private final Runnable onExit;
  //private final Runnable onRescan;

  // ── Album list ────────────────────────────────────────────────────────────
  private final DefaultListModel<AlbumDto> albumListModel = new DefaultListModel<>();
  private final JList<AlbumDto> albumList = new JList<>(albumListModel);

  // ── Queue list ────────────────────────────────────────────────────────────
  private final DefaultListModel<SongQueueEntryDto> queueListModel = new DefaultListModel<>();
  private final JList<SongQueueEntryDto> queueList = new JList<>(queueListModel);

  // ── Header credit label (refreshed on change) ─────────────────────────────
  private JLabel creditCountLabel;

  // ─────────────────────────────────────────────────────────────────────────
  // CONSTRUCTOR
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * @param ownerFrame         Parent frame (for dialogs).
   * @param songLibraryService Library service.
   * @param songQueueService   Queue service.
   * @param songPlayerService  Player service.
   * @param creditManager      Credit manager (shared with main UI).
   * @param imageLoader        Shared image loader.
   */
  public AdminPanel(Frame ownerFrame, SongLibraryService songLibraryService,
      SongQueueService songQueueService, SongPlayerService songPlayerService,
      CreditManager creditManager, ImageLoader imageLoader) {

    this.ownerFrame = ownerFrame;
    this.songLibraryService = songLibraryService;
    this.songQueueService = songQueueService;
    this.songPlayerService = songPlayerService;
    this.creditManager = creditManager;
    this.imageLoader = imageLoader;

    setLayout(new BorderLayout(0, 0));
    setOpaque(false);

    add(buildHeaderBar(), BorderLayout.NORTH);

    JPanel body = new JPanel(new GridLayout(1, 2, 6, 0));
    body.setOpaque(false);
    body.setBorder(new EmptyBorder(6, 8, 8, 8));
    body.add(buildAlbumPane());
    body.add(buildQueuePane());
    add(body, BorderLayout.CENTER);

    // Register for credit changes so the header counter stays live
    creditManager.addListener(() -> SwingUtilities.invokeLater(() -> {
      if (creditCountLabel != null) {
        creditCountLabel.setText(String.valueOf(creditManager.getCredits()));
      }
    }));

    // Initial data load
    refreshAlbumList();
    refreshQueueList();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // HEADER BAR
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildHeaderBar() {

    JPanel bar = new JPanel(new BorderLayout(16, 0)) {
      private static final long serialVersionUID = 1L;
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(8, 8, 14));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(SEPARATOR);
        g2.fillRect(0, getHeight() - 1, getWidth(), 1);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    bar.setOpaque(false);
    bar.setBorder(new EmptyBorder(10, 14, 10, 14));

    // Left: title
    JLabel title = new JLabel("⚙  ADMIN PANEL");
    title.setForeground(ACCENT_GOLD);
    title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));

    // Right: credits badge
    JPanel creditBadge = new JPanel(new BorderLayout(6, 0));
    creditBadge.setOpaque(false);
    JLabel crLabel = new JLabel("CREDITS:");
    crLabel.setForeground(TEXT_MUTED);
    crLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
    creditCountLabel = new JLabel(String.valueOf(creditManager.getCredits()));
    creditCountLabel.setForeground(ACCENT_GOLD);
    creditCountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
    creditBadge.add(crLabel, BorderLayout.WEST);
    creditBadge.add(creditCountLabel, BorderLayout.CENTER);

    bar.add(title, BorderLayout.WEST);
    bar.add(creditBadge, BorderLayout.EAST);

    return bar;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // LEFT — ALBUM PANE
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildAlbumPane() {

    JPanel pane = new JPanel(new BorderLayout(0, 6));
    pane.setOpaque(false);

    // ── Section header ────────────────────────────────────────────────────
    pane.add(sectionHeader("JUKEBOX LIST", ACCENT_BLUE), BorderLayout.NORTH);

    // ── Album list ────────────────────────────────────────────────────────
    albumList.setOpaque(true);
    albumList.setBackground(LIST_BG);
    albumList.setForeground(TEXT_PRIMARY);
    albumList.setSelectionBackground(LIST_SEL_BG);
    albumList.setSelectionForeground(Color.WHITE);
    albumList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
    albumList.setFixedCellHeight(36);
    albumList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    albumList.setCellRenderer(new AlbumCellRenderer());

    JScrollPane albumScroll = darkScrollPane(albumList);

    // ── Per-album buttons ─────────────────────────────────────────────────
    JPanel albumActions = new JPanel(new GridLayout(1, 2, 6, 0));
    albumActions.setOpaque(false);

    JButton addBtn = sideButton("Add Album to Queue", ACCENT_GREEN);
    addBtn.addActionListener(e -> doAddAlbumToQueue());

    JButton editBtn = sideButton("Edit CD", ACCENT_GOLD);
    editBtn.addActionListener(e -> doEditAlbum());

    albumActions.add(addBtn);
    albumActions.add(editBtn);

    // ── Global utility buttons ────────────────────────────────────────────
    JPanel globalActions = new JPanel(new GridLayout(3, 1, 0, 6));
    globalActions.setOpaque(false);

    JButton minimizeBtn = sideButton("⊟  Minimize Screen", ACCENT_BLUE);
    minimizeBtn.addActionListener(e -> {
      SwingUtilities.invokeLater(() -> {
        GraphicsDevice gd =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(null);
        ownerFrame.setState(JFrame.ICONIFIED);        
      });
    });

    JButton rescanBtn = sideButton("↺  Rescan Library", ACCENT_VIOLET);
    rescanBtn.addActionListener(e -> {
      songLibraryService.scanFileSystemForSongs();
    });

    JButton exitBtn = sideButton("✕  Exit", ACCENT_RED);
    exitBtn.addActionListener(e -> {
      System.exit(0);
    });

    globalActions.add(minimizeBtn);
    globalActions.add(rescanBtn);
    globalActions.add(exitBtn);

    JPanel bottom = new JPanel(new BorderLayout(0, 6));
    bottom.setOpaque(false);
    bottom.add(albumActions, BorderLayout.NORTH);
    bottom.add(globalActions, BorderLayout.CENTER);

    pane.add(albumScroll, BorderLayout.CENTER);
    pane.add(bottom, BorderLayout.SOUTH);

    return pane;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // RIGHT — QUEUE PANE
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildQueuePane() {

    JPanel pane = new JPanel(new BorderLayout(0, 6));
    pane.setOpaque(false);

    // ── Section header ────────────────────────────────────────────────────
    pane.add(sectionHeader("SONG QUEUE", ACCENT_GREEN), BorderLayout.NORTH);

    // ── Queue list ────────────────────────────────────────────────────────
    queueList.setOpaque(true);
    queueList.setBackground(LIST_BG);
    queueList.setForeground(TEXT_PRIMARY);
    queueList.setSelectionBackground(LIST_SEL_BG);
    queueList.setSelectionForeground(Color.WHITE);
    queueList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    queueList.setFixedCellHeight(40);
    queueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    queueList.setCellRenderer(new QueueCellRenderer());

    JScrollPane queueScroll = darkScrollPane(queueList);

    // ── Playback controls (row 1) ──────────────────────────────────────────
    JPanel playbackRow = new JPanel(new GridLayout(1, 3, 6, 0));
    playbackRow.setOpaque(false);

    JButton nextBtn  = sideButton("▶▶  Next Track",  ACCENT_GREEN);
    JButton pauseBtn = sideButton("⏸  Pause",        ACCENT_BLUE);
    JButton playBtn  = sideButton("▶  Play Song",    ACCENT_GREEN);

    nextBtn.addActionListener(e  -> doPlayNextTrack());
    pauseBtn.addActionListener(e -> doPause());
    playBtn.addActionListener(e  -> doPlaySelected());

    playbackRow.add(nextBtn);
    playbackRow.add(pauseBtn);
    playbackRow.add(playBtn);

    // ── Queue position controls (row 2) ───────────────────────────────────
    JPanel posRow = new JPanel(new GridLayout(1, 2, 6, 0));
    posRow.setOpaque(false);

    JButton upBtn   = sideButton("▲  Move Up",   ACCENT_BLUE);
    JButton downBtn = sideButton("▼  Move Down", ACCENT_BLUE);

    upBtn.addActionListener(e   -> doMoveUp());
    downBtn.addActionListener(e -> doMoveDown());

    posRow.add(upBtn);
    posRow.add(downBtn);

    // ── Queue management (row 3) ───────────────────────────────────────────
    JPanel mgmtRow = new JPanel(new GridLayout(1, 2, 6, 0));
    mgmtRow.setOpaque(false);

    JButton flushBtn    = sideButton("🗑  Flush Queue",      ACCENT_RED);
    JButton randomBtn   = sideButton("⇌  Randomize Queue", ACCENT_VIOLET);

    flushBtn.addActionListener(e  -> doFlushQueue());
    randomBtn.addActionListener(e -> doRandomizeQueue());

    mgmtRow.add(flushBtn);
    mgmtRow.add(randomBtn);

    // ── Playlist I/O (row 4) ──────────────────────────────────────────────
    JPanel playlistRow = new JPanel(new GridLayout(1, 2, 6, 0));
    playlistRow.setOpaque(false);

    JButton loadBtn = sideButton("📂  Load PlayList", ACCENT_GOLD);
    JButton saveBtn = sideButton("💾  Save PlayList", ACCENT_GOLD);

    loadBtn.addActionListener(e -> doLoadPlayList());
    saveBtn.addActionListener(e -> doSavePlayList());

    playlistRow.add(loadBtn);
    playlistRow.add(saveBtn);

    // ── Credits row ───────────────────────────────────────────────────────
    JPanel creditsRow = new JPanel(new GridLayout(1, 2, 6, 0));
    creditsRow.setOpaque(false);

    JButton incBtn = sideButton("＋  Add Credit",    ACCENT_GREEN);
    JButton decBtn = sideButton("－  Remove Credit", ACCENT_ORANGE);

    incBtn.addActionListener(e -> doIncrementCredits());
    decBtn.addActionListener(e -> doDecrementCredits());

    creditsRow.add(incBtn);
    creditsRow.add(decBtn);

    // ── Stack all button rows ─────────────────────────────────────────────
    JPanel allButtons = new JPanel(new GridLayout(5, 1, 0, 6));
    allButtons.setOpaque(false);
    allButtons.add(playbackRow);
    allButtons.add(posRow);
    allButtons.add(mgmtRow);
    allButtons.add(playlistRow);
    allButtons.add(creditsRow);

    pane.add(queueScroll, BorderLayout.CENTER);
    pane.add(allButtons, BorderLayout.SOUTH);

    return pane;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // ALBUM ACTIONS
  // ─────────────────────────────────────────────────────────────────────────

  private void doAddAlbumToQueue() {

    AlbumDto selected = albumList.getSelectedValue();
    if (selected == null) {
      JOptionPane.showMessageDialog(this, "Please select an album first.", "No Selection",
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    // Fetch full album (with songs) before queuing
    CompletableFuture.runAsync(() -> {
      try {
        AlbumDto full = songLibraryService.getAlbumById(selected.getAlbumId());
        songQueueService.addAlbumToQueue(new AddAlbumToQueueRequest(full.getAlbumId(), 1));
        SwingUtilities.invokeLater(this::refreshQueueList);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    });
  }

  private void doEditAlbum() {

    AlbumDto selected = albumList.getSelectedValue();
    if (selected == null) {
      JOptionPane.showMessageDialog(this, "Please select an album first.", "No Selection",
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    EditAlbumDialog.show(ownerFrame, selected, songLibraryService, imageLoader);
    // Refresh so any cover-art changes are reflected in the list
    albumList.repaint();
  }


  // ─────────────────────────────────────────────────────────────────────────
  // QUEUE ACTIONS
  // ─────────────────────────────────────────────────────────────────────────

  private void doPlayNextTrack() {

    try {
      songPlayerService.playNextTrack();
      refreshQueueList();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void doPause() {

    try {
      songPlayerService.pause();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void doPlaySelected() {

    SongQueueEntryDto selected = queueList.getSelectedValue();
    if (selected == null) {
      JOptionPane.showMessageDialog(this, "Please select a song in the queue first.",
          "No Selection", JOptionPane.WARNING_MESSAGE);
      return;
    }

    try {
      // Move selected entry to the front by moving it up until it reaches position 0
      int idx = queueList.getSelectedIndex();
      for (int i = 0; i < idx; i++) {
        
        ChangeSongQueueRequest changeSongQueueRequest = new ChangeSongQueueRequest(selected.getSong().getAlbumId(), selected.getSong().getSongId()); 
        songQueueService.moveSongUpInQueue(changeSongQueueRequest);
      }
      songPlayerService.playNextTrack();
      refreshQueueList();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void doMoveUp() {

    SongQueueEntryDto selected = queueList.getSelectedValue();
    if (selected == null) return;

    int idx = queueList.getSelectedIndex();
    try {
      
      ChangeSongQueueRequest changeSongQueueRequest = new ChangeSongQueueRequest(selected.getSong().getAlbumId(), selected.getSong().getSongId()); 
      songQueueService.moveSongUpInQueue(changeSongQueueRequest);
      
      refreshQueueList();
      // Keep same logical item selected after refresh
      int newIdx = Math.max(0, idx - 1);
      queueList.setSelectedIndex(newIdx);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void doMoveDown() {

    SongQueueEntryDto selected = queueList.getSelectedValue();
    if (selected == null) return;

    int idx = queueList.getSelectedIndex();
    try {
      
      ChangeSongQueueRequest changeSongQueueRequest = new ChangeSongQueueRequest(selected.getSong().getAlbumId(), selected.getSong().getSongId()); 
      songQueueService.moveSongDownInQueue(changeSongQueueRequest);
      
      refreshQueueList();
      int newIdx = Math.min(queueListModel.getSize() - 1, idx + 1);
      queueList.setSelectedIndex(newIdx);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void doFlushQueue() {

    int confirm = JOptionPane.showConfirmDialog(this,
        "Clear the entire song queue?", "Flush Queue", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
      try {
        songQueueService.flushQueue();
        refreshQueueList();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private void doRandomizeQueue() {

    try {
      songQueueService.randomizeQueue();
      refreshQueueList();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  // ── Playlist ──────────────────────────────────────────────────────────────

  private void doLoadPlayList() {

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Load PlayList");
    chooser.setFileFilter(new FileNameExtensionFilter("PlayList files (*.txt)", "txt"));
    chooser.setCurrentDirectory(PlayListManager.defaultPlayListFile().getParentFile());

    int result = chooser.showOpenDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) return;

    File file = chooser.getSelectedFile();
    CompletableFuture.runAsync(() -> {
      try {
        List<String> paths = PlayListManager.loadPlayList(file);
        int loaded = 0;
        for (String path : paths) {
          try {
            // TODO:  Implement SongLibraryService.getSongByFilePath(path)
            //SongDto song = songLibraryService.getSongByFilePath(path);
            SongDto song = null;
            if (song != null) {
              songQueueService.addSongToQueue(
                  new com.djt.jukeanator_engine.domain.songqueue.dto.AddSongToQueueRequest(
                      song.getAlbumId(), song.getSongId(), 1));
              loaded++;
            }
          } catch (Exception ignored) {}
        }
        final int finalLoaded = loaded;
        SwingUtilities.invokeLater(() -> {
          refreshQueueList();
          JOptionPane.showMessageDialog(this,
              "Loaded " + finalLoaded + " of " + paths.size() + " songs.", "PlayList Loaded",
              JOptionPane.INFORMATION_MESSAGE);
        });
      } catch (Exception ex) {
        ex.printStackTrace();
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, "Failed to load playlist: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  private void doSavePlayList() {

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Save PlayList");
    chooser.setFileFilter(new FileNameExtensionFilter("PlayList files (*.txt)", "txt"));
    chooser.setSelectedFile(PlayListManager.defaultPlayListFile());

    int result = chooser.showSaveDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) return;

    CompletableFuture.runAsync(() -> {
      try {
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < queueListModel.getSize(); i++) {
          SongQueueEntryDto entry = queueListModel.getElementAt(i);
          
          String fp = null;
          
          // TODO: Implement service method to get Song Path
          //String fp = entry.getSong()..getFilePath();
          
          if (fp != null) paths.add(fp);
        }
        
        File selectedFile = chooser.getSelectedFile();

        final File file;
        if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
          file = new File(selectedFile.getAbsolutePath() + ".txt");
        } else {
          file = selectedFile;
        }        
        
        
        PlayListManager.savePlayList(file, paths);
        
        final File saved = file;
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this,
                "Saved " + paths.size() + " songs to:\n" + saved.getAbsolutePath(),
                "PlayList Saved", JOptionPane.INFORMATION_MESSAGE));
      } catch (Exception ex) {
        ex.printStackTrace();
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(this, "Failed to save playlist: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  // ── Credits ───────────────────────────────────────────────────────────────

  private void doIncrementCredits() {
    // addDollar() is the standard way to add credits (also used by the bill acceptor key binding)
    creditManager.addDollar();
  }

  private void doDecrementCredits() {
    creditManager.deductCredits(1);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // DATA REFRESH
  // ─────────────────────────────────────────────────────────────────────────

  /** Re-populates the album list from the library service. */
  public void refreshAlbumList() {

    CompletableFuture.runAsync(() -> {
      try {
        List<AlbumDto> albums = songLibraryService.getAlbums();
        SwingUtilities.invokeLater(() -> {
          albumListModel.clear();
          if (albums != null) albums.forEach(albumListModel::addElement);
        });
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    });
  }

  /** Re-populates the queue list from the queue service. */
  public void refreshQueueList() {

    try {
      List<SongQueueEntryDto> queue = songQueueService.getQueuedSongs();
      SwingUtilities.invokeLater(() -> {
        int sel = queueList.getSelectedIndex();
        queueListModel.clear();
        if (queue != null) queue.forEach(queueListModel::addElement);
        if (sel >= 0 && sel < queueListModel.getSize()) {
          queueList.setSelectedIndex(sel);
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // CELL RENDERERS
  // ─────────────────────────────────────────────────────────────────────────

  private class AlbumCellRenderer extends JPanel
      implements javax.swing.ListCellRenderer<AlbumDto> {

    private static final long serialVersionUID = 1L;
    private final JLabel thumb  = new JLabel();
    private final JLabel name   = new JLabel();
    private final JLabel artist = new JLabel();

    AlbumCellRenderer() {
      setLayout(new BorderLayout(8, 0));
      setBorder(new EmptyBorder(3, 8, 3, 8));

      thumb.setPreferredSize(new Dimension(30, 30));
      thumb.setHorizontalAlignment(SwingConstants.CENTER);
      thumb.setOpaque(true);
      thumb.setBackground(new Color(24, 26, 38));

      JPanel text = new JPanel(new BorderLayout(0, 0));
      text.setOpaque(false);
      name.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
      artist.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
      name.setForeground(TEXT_PRIMARY);
      artist.setForeground(TEXT_MUTED);
      text.add(name, BorderLayout.CENTER);
      text.add(artist, BorderLayout.SOUTH);

      add(thumb, BorderLayout.WEST);
      add(text, BorderLayout.CENTER);
    }

    @Override
    public java.awt.Component getListCellRendererComponent(
        JList<? extends AlbumDto> list, AlbumDto album,
        int index, boolean isSelected, boolean cellHasFocus) {

      name.setText(AlbumGridPanel.albumDisplayName(album.getAlbumName(), album.getGenreName()));
      artist.setText(album.getArtistName() != null ? album.getArtistName() : "");

      try {
        if (album.getCoverArtPath() != null) {
          ImageIcon icon = imageLoader.loadFilesystemImage(album.getCoverArtPath(), 30, 30);
          thumb.setIcon(icon);
        } else {
          thumb.setIcon(null);
          thumb.setText("♫");
          thumb.setForeground(TEXT_MUTED);
          thumb.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        }
      } catch (Exception ignored) {
        thumb.setIcon(null);
      }

      if (isSelected) {
        setBackground(LIST_SEL_BG);
        name.setForeground(ACCENT_BLUE);
      } else {
        setBackground(index % 2 == 0 ? LIST_BG : ROW_ALT);
        name.setForeground(TEXT_PRIMARY);
      }
      setOpaque(true);
      return this;
    }
  }

  private static class QueueCellRenderer extends JPanel
      implements javax.swing.ListCellRenderer<SongQueueEntryDto> {

    private static final long serialVersionUID = 1L;
    private final JLabel position = new JLabel();
    private final JLabel song     = new JLabel();
    private final JLabel sub      = new JLabel();

    QueueCellRenderer() {
      setLayout(new BorderLayout(8, 0));
      setBorder(new EmptyBorder(4, 8, 4, 8));

      position.setPreferredSize(new Dimension(28, 32));
      position.setHorizontalAlignment(SwingConstants.CENTER);
      position.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
      position.setForeground(TEXT_MUTED);

      JPanel text = new JPanel(new BorderLayout(0, 0));
      text.setOpaque(false);
      song.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
      sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
      text.add(song, BorderLayout.CENTER);
      text.add(sub, BorderLayout.SOUTH);

      add(position, BorderLayout.WEST);
      add(text, BorderLayout.CENTER);
    }

    @Override
    public java.awt.Component getListCellRendererComponent(
        JList<? extends SongQueueEntryDto> list, SongQueueEntryDto entry,
        int index, boolean isSelected, boolean cellHasFocus) {

      position.setText(String.valueOf(index + 1));
      song.setText(entry.getSong().getSongName());
      sub.setText(entry.getSong().getArtistName() + "  •  " + entry.getSong().getAlbumName());

      int p = entry.getPriority() == null ? 0 : entry.getPriority();
      position.setForeground(p >= 8 ? ACCENT_RED : p >= 4 ? ACCENT_ORANGE : TEXT_MUTED);

      if (isSelected) {
        setBackground(LIST_SEL_BG);
        song.setForeground(ACCENT_BLUE);
        sub.setForeground(Color.WHITE);
      } else {
        setBackground(index % 2 == 0 ? LIST_BG : ROW_ALT);
        song.setForeground(TEXT_PRIMARY);
        sub.setForeground(TEXT_MUTED);
      }
      setOpaque(true);
      return this;
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // WIDGET HELPERS
  // ─────────────────────────────────────────────────────────────────────────

  private static JPanel sectionHeader(String text, Color accent) {

    JPanel header = new JPanel(new BorderLayout()) {
      private static final long serialVersionUID = 1L;
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(8, 8, 14));
        g2.fillRect(0, 0, getWidth(), getHeight());
        // Accent underline
        g2.setColor(accent);
        g2.fillRect(0, getHeight() - 2, getWidth(), 2);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    header.setOpaque(false);
    header.setBorder(new EmptyBorder(6, 10, 6, 10));

    JLabel lbl = new JLabel(text);
    lbl.setForeground(accent);
    lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    header.add(lbl, BorderLayout.WEST);

    return header;
  }

  /**
   * Full-width side-panel button using the same AMI 3D gradient style as the rest of the UI.
   */
  private static JButton sideButton(String label, Color accent) {

    final Color GRAD_TOP    = accent.darker();
    final Color GRAD_BOTTOM = accent.darker().darker();

    JButton btn = new JButton(label) {
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
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int arc = 8;
        int shadowH = 3;
        int visH    = h - shadowH;
        int shelfH  = Math.round(visH * 0.22f);
        int faceH   = visH - shelfH;

        // Drop-shadow
        g2.setColor(new Color(2, 2, 6));
        g2.fillRoundRect(1, shadowH, w - 2, visH, arc, arc);

        // Shelf
        g2.setColor(new Color(6, 6, 12));
        g2.fillRoundRect(1, faceH, w - 2, shelfH + arc / 2, arc, arc);

        // Face gradient
        Color top = hovered ? GRAD_TOP.brighter() : GRAD_TOP;
        Color bot = hovered ? GRAD_BOTTOM.brighter() : GRAD_BOTTOM;
        g2.setPaint(new GradientPaint(0, 0, top, 0, faceH, bot));
        g2.fillRoundRect(1, 0, w - 2, faceH + arc / 2, arc, arc);

        // Specular top edge
        g2.setColor(new Color(
            Math.min(255, accent.getRed()   + 80),
            Math.min(255, accent.getGreen() + 80),
            Math.min(255, accent.getBlue()  + 80), 160));
        g2.setStroke(new java.awt.BasicStroke(1f));
        g2.drawLine(arc, 1, w - arc - 1, 1);

        // Border
        g2.setColor(hovered ? accent.brighter() : accent);
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        g2.drawRoundRect(1, 1, w - 3, visH - 2, arc, arc);

        // Label
        g2.setFont(getFont());
        java.awt.FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        int tx = (w  - fm.stringWidth(label)) / 2;
        int ty = (faceH - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(label, tx, ty);

        g2.dispose();
      }
    };

    btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    btn.setForeground(Color.WHITE);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setOpaque(false);
    btn.setMaximumSize(BTN_WIDE);
    btn.setPreferredSize(BTN_NARROW);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return btn;
  }

  private static JScrollPane darkScrollPane(java.awt.Component view) {

    JScrollPane sp = new JScrollPane(view);
    sp.setOpaque(false);
    sp.getViewport().setOpaque(false);
    sp.setBorder(BorderFactory.createLineBorder(SEPARATOR, 1));
    sp.getVerticalScrollBar().setBackground(new Color(20, 20, 30));
    sp.getHorizontalScrollBar().setVisible(false);
    return sp;
  }
}
  
  /*
  private void doRescan() {

    int confirm = JOptionPane.showConfirmDialog(this,
        "Rescan the music library? This may take a moment.", "Rescan Library",
        JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
      if (onRescan != null) onRescan.run();
      // Re-populate album list after rescan
      refreshAlbumList();
    }
  }

  private void doExit() {

    int confirm = JOptionPane.showConfirmDialog(this,
        "Exit JukeANator?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
      if (onExit != null) onExit.run();
    }
  }
  */
