package com.djt.jukeanator_engine.ui.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import com.djt.jukeanator_engine.domain.songlibrary.dto.AlbumDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.ArtistDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.SearchResultDto;
import com.djt.jukeanator_engine.domain.songlibrary.dto.SongDto;
import com.djt.jukeanator_engine.domain.songlibrary.service.SongLibraryService;
import com.djt.jukeanator_engine.domain.songqueue.dto.AddSongToQueueRequest;
import com.djt.jukeanator_engine.domain.songqueue.service.SongQueueService;

/**
 * The "SEARCH" tab panel.
 *
 * <p>
 * Card layout:
 * <ol>
 * <li><b>ENTRY</b> — hero text + search bar + on-screen keyboard.</li>
 * <li><b>RESULTS</b> — search bar + three-column results + keyboard.</li>
 * <li><b>ARTIST</b> — {@link ArtistDetailPanel} pushed when an artist row is tapped.</li>
 * <li><b>DETAIL</b> — {@link AlbumDetailCard} pushed when an album tile/row is tapped.</li>
 * </ol>
 *
 * <p>
 * Implements {@link TabNavigator} so {@link AlbumDetailCard} can pop itself back to RESULTS without
 * knowing which tab it lives in.
 */
public class SearchPanel extends JPanel implements TabNavigator {

  private static final long serialVersionUID = 1L;

  // ── Palette ───────────────────────────────────────────────────────────────
  private static final Color BG_DARK = new Color(10, 10, 10);
  private static final Color BG_SEARCH = new Color(32, 32, 40);
  private static final Color ACCENT_BLUE = new Color(0, 210, 255);
  private static final Color TEXT_PRIMARY = Color.WHITE;

  // ── Layout constants ──────────────────────────────────────────────────────
  private static final int KEYBOARD_HEIGHT = 260;
  private static final int SEARCH_BAR_HEIGHT = 90;
  private static final int SEARCH_PREVIEW_COUNT = 6;

  // ── Card names ────────────────────────────────────────────────────────────
  private static final String CARD_ENTRY = "ENTRY";
  private static final String CARD_RESULTS = "RESULTS";
  private static final String CARD_ARTIST = "ARTIST";
  private static final String CARD_DETAIL = "DETAIL";

  // ── Layout ────────────────────────────────────────────────────────────────
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel rootPanel = new JPanel(cardLayout);

  // ── Search state ──────────────────────────────────────────────────────────
  private final StringBuilder searchBuffer = new StringBuilder();
  private SearchResultDto lastResult;
  private int artistsOffset = 0;
  private int albumsOffset = 0;
  private int songsOffset = 0;

  // ── Labels synced across both search-bar instances ────────────────────────
  private JLabel entrySearchLabel;
  private JLabel resultsSearchLabel;

  // ── Results card rebuilt on each search ───────────────────────────────────
  private final JPanel resultsCard = new JPanel(new BorderLayout());

  // ── Active detail card ────────────────────────────────────────────────────
  private AlbumDetailCard currentDetailCard;

  // ── Dependencies ──────────────────────────────────────────────────────────
  private final SongLibraryService songLibraryService;
  private final SongQueueService songQueueService;
  private final ImageLoader imageLoader;
  private final int normalPlayCost;
  private final int priorityCost;
  private final int popularityT1;
  private final int popularityT2;
  private final int popularityT3;
  private final boolean enableBigScrollBars;
  private final boolean enableTypeAheadSearch;
  private final int gridCols;
  private final int gridRows;
  private final int artW;
  private final int artH;

  // ─────────────────────────────────────────────────────────────────────────
  // CONSTRUCTOR
  // ─────────────────────────────────────────────────────────────────────────

  public SearchPanel(
      SongLibraryService songLibraryService, 
      SongQueueService songQueueService,
      ImageLoader imageLoader, 
      int normalPlayCost, 
      int priorityCost, 
      int popularityT1,
      int popularityT2, 
      int popularityT3, 
      boolean enableBigScrollBars,
      boolean enableTypeAheadSearch, 
      int gridCols, 
      int gridRows, 
      int artW, 
      int artH) {

    this.songLibraryService = songLibraryService;
    this.songQueueService = songQueueService;
    this.imageLoader = imageLoader;
    this.normalPlayCost = normalPlayCost;
    this.priorityCost = priorityCost;
    this.popularityT1 = popularityT1;
    this.popularityT2 = popularityT2;
    this.popularityT3 = popularityT3;
    this.enableBigScrollBars = enableBigScrollBars;
    this.enableTypeAheadSearch = enableTypeAheadSearch;
    this.gridCols = gridCols;
    this.gridRows = gridRows;
    this.artW = artW;
    this.artH = artH;

    setLayout(new BorderLayout());
    setBackground(BG_DARK);

    rootPanel.setBackground(BG_DARK);
    add(rootPanel, BorderLayout.CENTER);

    resultsCard.setBackground(BG_DARK);

    rootPanel.add(buildEntryCard(), CARD_ENTRY);
    rootPanel.add(resultsCard, CARD_RESULTS);
    rootPanel.add(placeholder(), CARD_ARTIST);
    rootPanel.add(placeholder(), CARD_DETAIL);

    cardLayout.show(rootPanel, CARD_ENTRY);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // TabNavigator
  // ─────────────────────────────────────────────────────────────────────────

  @Override
  public void pushAlbumDetail(AlbumDto album) {

    Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
    AlbumDto full = fetchFull(album);
    int albumNormal = normalPlayCost * full.getSongs().size();
    int albumPriority = priorityCost * full.getSongs().size();

    if (currentDetailCard != null) {
      currentDetailCard.dismiss();
    }

    currentDetailCard = new AlbumDetailCard(owner, full, imageLoader, songQueueService, albumNormal,
        albumPriority, popularityT1, popularityT2, popularityT3, enableBigScrollBars, this);

    replaceCard(CARD_DETAIL, currentDetailCard);
    cardLayout.show(rootPanel, CARD_DETAIL);
  }

  /**
   * Returns to RESULTS if a search is active, otherwise to ENTRY. Called by AlbumDetailCard when
   * its countdown expires or CLOSE is tapped.
   */
  @Override
  public void popToRoot() {

    if (currentDetailCard != null) {
      currentDetailCard.dismiss();
      currentDetailCard = null;
    }
    cardLayout.show(rootPanel, lastResult != null ? CARD_RESULTS : CARD_ENTRY);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // ENTRY CARD
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildEntryCard() {

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(BG_DARK);

    JPanel hero = new JPanel(new GridBagLayout());
    hero.setBackground(new Color(25, 25, 35));
    hero.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE));
    hero.setPreferredSize(new Dimension(100, 300));

    JLabel heroLabel = new JLabel("Search for your favorite music.");
    heroLabel.setForeground(TEXT_PRIMARY);
    heroLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
    hero.add(heroLabel);

    JPanel searchBar = buildSearchBarPanel(false);
    searchBar.setPreferredSize(new Dimension(100, SEARCH_BAR_HEIGHT));

    root.add(searchBar, BorderLayout.NORTH);
    root.add(hero, BorderLayout.CENTER);
    root.add(buildFixedKeyboard(), BorderLayout.SOUTH);
    return root;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // SEARCH BAR
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildSearchBarPanel(boolean forResults) {

    JPanel bar = new JPanel(new BorderLayout(10, 0));
    bar.setBackground(new Color(20, 20, 30));
    bar.setBorder(new EmptyBorder(12, 20, 12, 20));

    JLabel lbl = new JLabel();
    lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
    lbl.setForeground(Color.WHITE);
    lbl.setOpaque(true);
    lbl.setBackground(new Color(40, 40, 55));
    lbl.setBorder(new EmptyBorder(8, 16, 8, 16));
    lbl.setHorizontalAlignment(SwingConstants.CENTER);
    lbl.setText(searchBuffer.length() == 0 ? " " : searchBuffer.toString());

    if (forResults)
      resultsSearchLabel = lbl;
    else
      entrySearchLabel = lbl;

    bar.add(lbl, BorderLayout.CENTER);

    if (!enableTypeAheadSearch) {
      JButton btn = new JButton("SEARCH");
      btn.setPreferredSize(new Dimension(180, 60));
      btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
      btn.setForeground(Color.BLACK);
      btn.setBackground(ACCENT_BLUE);
      btn.setFocusPainted(false);
      btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
      btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      btn.addActionListener(e -> executeSearch());

      JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
      right.setOpaque(false);
      right.add(btn);
      bar.add(right, BorderLayout.EAST);
    }

    return bar;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // KEYBOARD
  // ─────────────────────────────────────────────────────────────────────────
  private JPanel buildFixedKeyboard() {

    JPanel wrapper = new JPanel(new GridBagLayout());
    wrapper.setBackground(BG_SEARCH);
    wrapper.add(buildKeyboardPanel());
    wrapper.setPreferredSize(new Dimension(100, KEYBOARD_HEIGHT));
    wrapper.setMinimumSize(new Dimension(100, KEYBOARD_HEIGHT));
    wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, KEYBOARD_HEIGHT));
    return wrapper;
  }

  private JPanel buildKeyboardPanel() {

    JPanel p = new JPanel(new GridLayout(3, 1, 10, 10));
    p.setOpaque(false);
    p.setBorder(new EmptyBorder(20, 50, 20, 50));
    p.add(buildKeyRow1());
    p.add(buildKeyRow2());
    p.add(buildKeyRow3());
    return p;
  }

  private JPanel buildKeyRow1() {

    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    row.setOpaque(false);
    for (char c : "QWERTYUIOP".toCharArray())
      row.add(letterKey(String.valueOf(c)));
    row.add(letterKey("'"));

    JButton clear = styledKey("CLEAR", new Dimension(140, 60));
    clear.addActionListener(e -> resetSearch());
    row.add(clear);

    JButton back = styledKey("⌫", new Dimension(100, 60));
    back.addActionListener(e -> {
      if (searchBuffer.length() > 0) {
        searchBuffer.deleteCharAt(searchBuffer.length() - 1);
        syncSearchLabel();
        if (enableTypeAheadSearch)
          executeSearch();
      }
    });
    row.add(back);
    return row;
  }

  private JPanel buildKeyRow2() {

    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    row.setOpaque(false);
    for (char c : "ASDFGHJKL".toCharArray())
      row.add(letterKey(String.valueOf(c)));
    row.add(styledKey("123@", new Dimension(140, 60)));
    row.add(styledKey("ABC", new Dimension(140, 60)));
    return row;
  }

  private JPanel buildKeyRow3() {

    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
    row.setOpaque(false);
    for (char c : "ZXCVBNM".toCharArray())
      row.add(letterKey(String.valueOf(c)));

    JButton space = styledKey("SPACE", new Dimension(420, 60));
    space.addActionListener(e -> {
      searchBuffer.append(' ');
      syncSearchLabel();
      if (enableTypeAheadSearch)
        executeSearch();
    });
    row.add(space);
    return row;
  }

  private JButton letterKey(String text) {

    JButton btn = styledKey(text, new Dimension(70, 60));
    if (text.length() == 1 && !text.equals(" ")) {
      btn.addActionListener(e -> {
        searchBuffer.append(text);
        syncSearchLabel();
        if (enableTypeAheadSearch)
          executeSearch();
      });
    }
    return btn;
  }

  private JButton styledKey(String text, Dimension size) {

    JButton btn = new JButton(text);
    btn.setPreferredSize(size);
    btn.setFocusPainted(false);
    btn.setBackground(new Color(70, 70, 80));
    btn.setForeground(TEXT_PRIMARY);
    btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
    btn.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 1));
    return btn;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // SEARCH EXECUTION
  // ─────────────────────────────────────────────────────────────────────────
  private void executeSearch() {

    String query = searchBuffer.toString().trim();
    if (query.isEmpty())
      return;

    try {
      lastResult = songLibraryService.getMusicBySearch(query);
      artistsOffset = 0;
      albumsOffset = 0;
      songsOffset = 0;
      rebuildResultsCard();
      cardLayout.show(rootPanel, CARD_RESULTS);
    } catch (Exception ignored) {
      // TODO: surface error state
    }
  }

  private void resetSearch() {

    searchBuffer.setLength(0);
    syncSearchLabel();
    lastResult = null;
    artistsOffset = 0;
    albumsOffset = 0;
    songsOffset = 0;
    resultsCard.removeAll();
    resultsCard.revalidate();
    resultsCard.repaint();
    cardLayout.show(rootPanel, CARD_ENTRY);
  }

  private void syncSearchLabel() {

    String display = searchBuffer.toString().isEmpty() ? " " : searchBuffer.toString();
    if (entrySearchLabel != null)
      entrySearchLabel.setText(display);
    if (resultsSearchLabel != null)
      resultsSearchLabel.setText(display);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // RESULTS CARD
  // ─────────────────────────────────────────────────────────────────────────
  private void rebuildResultsCard() {

    resultsCard.removeAll();

    List<ArtistDto> artists = safeList(lastResult.getArtists());
    List<AlbumDto> albums = safeList(lastResult.getAlbums());
    List<SongDto> songs = safeList(lastResult.getSongs());

    JPanel columns = new JPanel(new GridLayout(1, 3, 2, 0));
    columns.setBackground(Color.BLACK);

    columns.add(ResultsColumnPanel.build("ARTISTS", artists, artistsOffset, SEARCH_PREVIEW_COUNT,
        imageLoader, () -> {
          artistsOffset = Math.max(0, artistsOffset - 1);
          rebuildResultsCard();
        }, () -> {
          artistsOffset++;
          rebuildResultsCard();
        }, (item) -> handleRowClick("ARTISTS", item)));

    columns.add(ResultsColumnPanel.build("ALBUMS", albums, albumsOffset, SEARCH_PREVIEW_COUNT,
        imageLoader, () -> {
          albumsOffset = Math.max(0, albumsOffset - 1);
          rebuildResultsCard();
        }, () -> {
          albumsOffset++;
          rebuildResultsCard();
        }, (item) -> handleRowClick("ALBUMS", item)));

    columns.add(ResultsColumnPanel.build("SONGS", songs, songsOffset, SEARCH_PREVIEW_COUNT,
        imageLoader, () -> {
          songsOffset = Math.max(0, songsOffset - 1);
          rebuildResultsCard();
        }, () -> {
          songsOffset++;
          rebuildResultsCard();
        }, (item) -> handleRowClick("SONGS", item)));

    resultsCard.add(buildSearchBarPanel(true), BorderLayout.NORTH);
    resultsCard.add(columns, BorderLayout.CENTER);
    resultsCard.add(buildFixedKeyboard(), BorderLayout.SOUTH);
    resultsCard.revalidate();
    resultsCard.repaint();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // ROW CLICK DISPATCH
  // ─────────────────────────────────────────────────────────────────────────
  private <T> void handleRowClick(String category, T item) {

    switch (category) {
      case "ARTISTS" -> {
        if (item instanceof ArtistDto a)
          pushArtist(a);
      }
      case "ALBUMS" -> {
        if (item instanceof AlbumDto a)
          pushAlbumDetail(a);
      }
      case "SONGS" -> {
        if (item instanceof SongDto song) {
          Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
          AddSongToQueueDialog.show(owner, song, imageLoader, normalPlayCost, priorityCost,
              () -> songQueueService.addSongToQueue(
                  new AddSongToQueueRequest(song.getAlbumId(), song.getSongId(), 0)),
              () -> songQueueService.addSongToQueue(
                  new AddSongToQueueRequest(song.getAlbumId(), song.getSongId(), 1)));
        }
      }
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // ARTIST CARD
  // ─────────────────────────────────────────────────────────────────────────
  private void pushArtist(ArtistDto artist) {

    ArtistDto full;
    try {
      full = songLibraryService.getArtistById(artist.getArtistId());
    } catch (Exception e) {
      return;
    }

    ArtistDetailPanel panel =
        new ArtistDetailPanel(full, imageLoader, gridCols, gridRows, artW, artH, "← BACK",
            () -> cardLayout.show(rootPanel, CARD_RESULTS), album -> pushAlbumDetail(album));

    replaceCard(CARD_ARTIST, panel);
    cardLayout.show(rootPanel, CARD_ARTIST);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // HELPERS
  // ─────────────────────────────────────────────────────────────────────────
  private AlbumDto fetchFull(AlbumDto album) {
    try {
      return songLibraryService.getAlbumById(album.getAlbumId());
    } catch (Exception e) {
      return album;
    }
  }

  private void replaceCard(String name, JPanel newPanel) {
    for (int i = rootPanel.getComponentCount() - 1; i >= 0; i--) {
      if (name.equals(rootPanel.getComponent(i).getName())) {
        rootPanel.remove(i);
        break;
      }
    }
    newPanel.setName(name);
    rootPanel.add(newPanel, name);
    rootPanel.revalidate();
    rootPanel.repaint();
  }

  private static <T> List<T> safeList(List<T> list) {
    return list != null ? list : List.of();
  }

  private JPanel placeholder() {
    JPanel p = new JPanel();
    p.setBackground(BG_DARK);
    return p;
  }
}
