package com.djt.jukeanator_engine.ui.components;

import java.awt.Dimension;

/**
 * Centralized layout, sizing, and pagination constants for all JukeANator UI components.
 *
 * <p>
 * This class is the single source of truth for every pixel size, grid dimension, page count, and
 * spacing value used across the UI layer. It is designed as a singleton so that it can eventually
 * be populated at startup from a properties file (e.g. {@code application.yml}), enabling full
 * support for multiple screen resolutions and a future <em>Portrait Mode</em> (height &gt; width)
 * in addition to the current <em>Landscape Mode</em> (1920 × 1080).
 *
 * <p>
 * <b>Usage:</b>
 *
 * <pre>
 * int cols = LayoutTheme.get().homeGridCols;
 * Dimension sz = LayoutTheme.get().navBtnSize;
 * </pre>
 *
 * <p>
 * <b>Design notes:</b>
 * <ul>
 * <li>All fields are {@code public final} so they read like named constants at call-sites while
 * still being instance members replaceable at startup.</li>
 * <li>Derived {@link Dimension} fields are computed once from the primitive fields during
 * construction to avoid repeated allocation at every paint cycle.</li>
 * <li>Sections correspond to the original per-class constant declarations; each one notes its
 * source files so the mapping is auditable.</li>
 * <li>When YML support is added, replace the default field initialisers with values injected by
 * Spring (or read from a {@code Properties} object) inside {@link #LayoutTheme()} or a static
 * factory method.</li>
 * </ul>
 *
 * @see ColorTheme
 */
public final class LayoutTheme {

  // ── Singleton ──────────────────────────────────────────────────────────────

  private static volatile LayoutTheme instance;

  /** Returns the singleton instance, creating it on first call. */
  public static LayoutTheme get() {
    if (instance == null) {
      synchronized (LayoutTheme.class) {
        if (instance == null) {
          instance = new LayoutTheme();
        }
      }
    }
    return instance;
  }

  /**
   * Replaces the singleton with a pre-built instance.
   *
   * <p>
   * Intended for startup configuration — call this once from {@code main} (or a Spring
   * {@code @PostConstruct} method) before any UI component is constructed.
   *
   * @param theme the fully configured {@link LayoutTheme} to install
   */
  public static void install(LayoutTheme theme) {
    synchronized (LayoutTheme.class) {
      instance = theme;
    }
  }

  // ── Constructor ────────────────────────────────────────────────────────────

  /**
   * Creates a {@code LayoutTheme} with the default 1920×1080 landscape-mode sizes.
   *
   * <p>
   * To support a different resolution or orientation (e.g. 1080×1920 portrait), create a sub-class
   * or extend this class, override the relevant fields, and pass the result to
   * {@link #install(LayoutTheme)}.
   */
  public LayoutTheme() {
    // Derived Dimension fields must be initialised after the primitives they depend on.
    navBtnSize = new Dimension(navBtnW, navBtnH);
    adminBtnSize = new Dimension(adminBtnW, adminBtnH);
    detailHeaderImageSize = new Dimension(detailHeaderImageW, detailHeaderImageH);
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // FRAME / TOP-PANEL
  // Origin: JukeANatorFrame#buildTopPanel
  // ═══════════════════════════════════════════════════════════════════════════

  /** Preferred height of the top panel (credits + banner + now-playing). */
  public final int topPanelHeight = 110;

  /** Fixed width of the credits panel (left side of the top panel). */
  public final int creditsPanelW = 485;

  /** Fixed height of the credits panel (left side of the top panel). */
  public final int creditsPanelH = 100;

  /** Fixed width of the now-playing panel (right side of the top panel). */
  public final int nowPlayingPanelW = 450;

  /** Fixed height of the now-playing panel. */
  public final int nowPlayingPanelH = 100;

  /**
   * Width reserved for the now-playing wrapper (matches credits panel for symmetric layout).
   * Origin: JukeANatorFrame — {@code nowPlayingWrapper}
   */
  public final int nowPlayingWrapperW = 485;

  /** Height of the now-playing wrapper. */
  public final int nowPlayingWrapperH = 100;

  /** Size of the location logo / cover-art icon in the top panel (square). */
  public final int topPanelIconSize = 96;

  // ═══════════════════════════════════════════════════════════════════════════
  // TAB BAR (JukeANatorFrame — custom BasicTabbedPaneUI)
  // ═══════════════════════════════════════════════════════════════════════════

  /** Width of each tab header button. */
  public final int tabWidth = 200;

  /** Height of each tab header. */
  public final int tabHeight = 96;

  /** Height of the painted separator line between the tab bar and content area. */
  public final int tabSeparatorHeight = 2;

  /** Icon font size inside each JukeboxTabComponent. */
  public final int tabIconFontSize = 34;

  /** Text font size inside each JukeboxTabComponent. */
  public final int tabTextFontSize = 20;

  // ═══════════════════════════════════════════════════════════════════════════
  // COUNTDOWN TIMEOUT (AlbumDetailCard, AddSongToQueueCard, SongQueueCard,
  // LoginToAdminPanelCard)
  // ═══════════════════════════════════════════════════════════════════════════

  /** Seconds before an overlay card or detail card auto-dismisses. */
  public final int overlayTimeoutSeconds = 120;

  // ═══════════════════════════════════════════════════════════════════════════
  // HOME / ALBUM GRID (JukeANatorFrame → HomePanel → AlbumGridPanel)
  // ═══════════════════════════════════════════════════════════════════════════

  /** Number of columns in the main album grid. */
  public final int homeGridCols = 4;

  /** Number of rows in the main album grid. */
  public final int homeGridRows = 3;

  /** Width of each album cover-art thumbnail in the main grid. */
  public final int homeArtW = 190;

  /** Height of each album cover-art thumbnail in the main grid. */
  public final int homeArtH = 190;

  // ═══════════════════════════════════════════════════════════════════════════
  // ALBUM GRID PANEL (AlbumGridPanel)
  // ═══════════════════════════════════════════════════════════════════════════

  /** Horizontal gap between tiles in AlbumGridPanel. */
  public final int albumGridGapH = 10;

  /** Vertical gap between tiles in AlbumGridPanel. */
  public final int albumGridGapV = 10;

  /** Inner padding (all sides) inside each album tile border. */
  public final int albumTileInnerPad = 1;

  // ═══════════════════════════════════════════════════════════════════════════
  // ALBUM VIEW CARD — left sidebar and track list
  // Origin: AlbumViewCard
  // ═══════════════════════════════════════════════════════════════════════════

  /** Width of the left sidebar (cover art + metadata) in AlbumViewCard. */
  public final int albumViewSidebarW = 320;

  /** Pixel size (square) of the cover-art image displayed in the sidebar. */
  public final int albumViewCoverSize = 320;

  /** Number of track rows shown per page in the paginated track listing. */
  public final int albumViewTracksPerPage = 15;

  /** Width allocated for the "# Plays" (popularity bars) column header and cells. */
  public final int albumViewPlaysColW = 64;

  /** Width allocated for the track-number column. */
  public final int albumViewTrkNumColW = 48;

  /**
   * Width of the artist column when displaying a compilation album (column is split: artist |
   * song).
   */
  public final int albumViewCompilationArtistW = 260;

  /**
   * Width of the song column when displaying a compilation album.
   */
  public final int albumViewCompilationSongW = 520;

  // ═══════════════════════════════════════════════════════════════════════════
  // NAVIGATION / PAGE BUTTONS (AlbumGridPanel, GenrePanel, ButtonFactory)
  // ═══════════════════════════════════════════════════════════════════════════

  /** Preferred width of the standard navigation button (❮ / ❯). */
  public final int navBtnW = 140;

  /** Preferred height of the standard navigation button. */
  public final int navBtnH = 36;

  /** Preferred {@link Dimension} for the standard navigation button (derived). */
  public final Dimension navBtnSize;

  // ═══════════════════════════════════════════════════════════════════════════
  // DETAIL HEADER PANEL (DetailHeaderPanel)
  // ═══════════════════════════════════════════════════════════════════════════

  /** Width of the cover-art / icon image in the detail header. */
  public final int detailHeaderImageW = 72;

  /** Height of the cover-art / icon image in the detail header. */
  public final int detailHeaderImageH = 72;

  /** Preferred size of the detail header image label (derived). */
  public final Dimension detailHeaderImageSize;

  /**
   * Preferred width of the back button inside DetailHeaderPanel (and AlbumDetailCard footer,
   * LoginToAdminPanelCard).
   */
  public final int detailBackBtnW = 140;

  /** Preferred height of the back button inside DetailHeaderPanel / AlbumDetailCard footer. */
  public final int detailBackBtnH = 52;

  // ═══════════════════════════════════════════════════════════════════════════
  // GENRE PANEL (GenrePanel)
  // Origin: GenrePanel
  // ═══════════════════════════════════════════════════════════════════════════

  /**
   * Number of genre tiles per page in the genre grid. Default: 12 (2 rows × 6 columns).
   */
  public final int genresPerPage = 12;

  /** Number of columns in the genre grid. */
  public final int genreGridCols = 6;

  /** Number of rows in the genre grid. */
  public final int genreGridRows = 2;

  /** Horizontal gap between genre tiles. */
  public final int genreGridGapH = 20;

  /** Vertical gap between genre tiles. */
  public final int genreGridGapV = 20;

  /** Outer horizontal padding for the genre grid page wrapper. */
  public final int genrePagePadH = 60;

  /** Outer vertical (top) padding for the genre grid page wrapper. */
  public final int genrePagePadV = 30;

  /** Inner padding (all sides) inside each genre tile. */
  public final int genreTileInnerPad = 16;

  /** Pixel size (square) of the genre image loaded and displayed in each tile. */
  public final int genreImageSize = 240;

  /**
   * Width and height for the prev/next wrapper panels in the genre pagination row. Matches
   * {@link #navBtnW} × {@link #navBtnH}.
   */
  public final int genrePaginationBtnW = 140;

  /** Height of the genre pagination nav wrapper. */
  public final int genrePaginationBtnH = 36;

  // ═══════════════════════════════════════════════════════════════════════════
  // GENRE DETAIL PANEL (GenreDetailPanel)
  // Origin: GenreDetailPanel
  // ═══════════════════════════════════════════════════════════════════════════

  /**
   * Number of result rows visible per column page in the genre detail (artists / albums / songs).
   * Tune this value if the screen resolution changes the visible row count.
   */
  public final int genreDetailPreviewCount = 9;

  /** Preferred width of each sort button in the genre detail header. */
  public final int sortBtnW = 170;

  /** Preferred height of each sort button in the genre detail header. */
  public final int sortBtnH = 42;

  // ═══════════════════════════════════════════════════════════════════════════
  // HOT HERE PANEL (HotHerePanel)
  // Origin: HotHerePanel
  // ═══════════════════════════════════════════════════════════════════════════

  /**
   * Number of result rows visible per column page in the Hot Here tab. Tune this value if the
   * screen resolution changes the visible row count.
   */
  public final int hotHerePreviewCount = 10;

  // ═══════════════════════════════════════════════════════════════════════════
  // SEARCH PANEL (SearchPanel)
  // Origin: SearchPanel
  // ═══════════════════════════════════════════════════════════════════════════

  /** Preferred height of the search bar panel (query display + optional search button). */
  public final int searchBarHeight = 90;

  /**
   * Number of result rows visible per column page in search results. Tune this value if the screen
   * resolution changes the visible row count.
   */
  public final int searchPreviewCount = 5;

  /**
   * Outer horizontal screen margin padding (left + right) applied to the search bar wrapper,
   * keyboard wrapper, and hero panel. Exposing background gradient on both sides.
   */
  public final int screenPaddingHorizontal = 60;

  /**
   * Internal edge gap inside each result column (left + right, each side). Must match the value
   * used in {@code ResultsColumnPanel}. Used in SearchPanel to compute the unified column padding:
   * {@code screenPaddingHorizontal - columnInternalEdgeGap}.
   */
  public final int columnInternalEdgeGap = 10;

  /** Preferred height of the hero / entry-state panel in the Search tab. */
  public final int searchHeroHeight = 300;

  /** Preferred width of the search button (manual search mode). */
  public final int searchBtnW = 180;

  /** Preferred height of the search button. */
  public final int searchBtnH = 60;

  // ═══════════════════════════════════════════════════════════════════════════
  // KEYBOARD PANEL (KeyboardPanel)
  // Origin: KeyboardPanel
  // ═══════════════════════════════════════════════════════════════════════════

  /** Preferred height of the keyboard wrapper panel. */
  public final int keyboardHeight = 260;

  /**
   * Horizontal margin (left + right) applied to the keyboard wrapper. Exposes the background
   * gradient on both sides.
   */
  public final int keyboardPaddingHorizontal = 60;

  /** Standard letter key preferred size — width. */
  public final int keyLetterW = 70;

  /** Standard letter key preferred size — height. */
  public final int keyLetterH = 60;

  /** CLEAR key width. */
  public final int keyClearW = 140;

  /** Backspace key width. */
  public final int keyBackspaceW = 100;

  /** Mode toggle button (ABC / 123) width. */
  public final int keyModeToggleW = 140;

  /** SPACE key width. */
  public final int keySpaceW = 420;

  /** Key grid row gap. */
  public final int keyRowGap = 10;

  /** Key grid column gap. */
  public final int keyColGap = 8;

  /** Inner padding around the full keyboard layout. */
  public final int keyboardInnerPad = 20;

  // ═══════════════════════════════════════════════════════════════════════════
  // RESULT ROW / RESULTS COLUMN PANEL (ResultsColumnPanel)
  // Origin: ResultsColumnPanel
  // ═══════════════════════════════════════════════════════════════════════════

  /** Maximum row height for items in a results column. */
  public final int resultRowMaxH = 72;

  /** Thumbnail image size (square) displayed in each result row. */
  public final int resultThumbSize = 56;

  /** Width reserved for the row number label. */
  public final int resultNumLabelW = 36;

  /** Result column outer left/right padding. */
  public final int resultColumnPadH = 10;

  /** Nav button preferred size — width (up/down arrows in result columns). */
  public final int resultNavBtnW = 75;

  /** Nav button preferred size — height. */
  public final int resultNavBtnH = 45;

  // ═══════════════════════════════════════════════════════════════════════════
  // SONG TRACK CELL RENDERER (SongTrackCellRenderer)
  // Origin: SongTrackCellRenderer
  // ═══════════════════════════════════════════════════════════════════════════

  /** Width of each popularity bar in pixels. */
  public final int popularityBarWidth = 5;

  /** Gap between adjacent popularity bars in pixels. */
  public final int popularityBarGap = 3;

  /** Maximum height of the tallest (3rd) popularity bar. */
  public final int popularityBarMaxH = 18;

  /** Fixed cell height for the queue/song-track list renderer. */
  public final int songTrackCellHeight = 44;

  // ═══════════════════════════════════════════════════════════════════════════
  // ADD SONG TO QUEUE CARD (AddSongToQueueCard)
  // Origin: AddSongToQueueCard
  // ═══════════════════════════════════════════════════════════════════════════

  /** Preferred width of the AddSongToQueueCard panel. */
  public final int addSongCardW = 900;

  /** Preferred height of the AddSongToQueueCard panel. */
  public final int addSongCardH = 420;

  /** Size (square) of the song cover-art image in the info row. */
  public final int addSongCoverSize = 160;

  /** Preferred width of each queue action button (Play / Priority Play). */
  public final int addSongQueueBtnW = 200;

  /** Preferred height of each queue action button. */
  public final int addSongQueueBtnH = 88;

  /** Preferred width of the Cancel button in AddSongToQueueCard. */
  public final int addSongCancelBtnW = 200;

  /** Preferred height of the Cancel button. */
  public final int addSongCancelBtnH = 62;

  // ═══════════════════════════════════════════════════════════════════════════
  // SONG QUEUE CARD (SongQueueCard)
  // Origin: SongQueueCard
  // ═══════════════════════════════════════════════════════════════════════════

  /** Preferred width of the SongQueueCard panel. */
  public final int songQueueCardW = 900;

  /** Preferred height of the SongQueueCard panel. */
  public final int songQueueCardH = 660;

  /**
   * Maximum number of queued song entries visible at once in the SongQueueCard list. Determines the
   * fixed list height (no scroll bar).
   */
  public final int songQueueMaxVisible = 5;

  /** Size (square) of the now-playing cover-art icon in SongQueueCard. */
  public final int songQueueCoverSize = 96;

  /** Preferred width of the move-up/move-down/remove action buttons in SongQueueCard. */
  public final int songQueueActionBtnW = 200;

  /** Preferred height of the action buttons. */
  public final int songQueueActionBtnH = 80;

  /** Preferred width of the Cancel button in SongQueueCard. */
  public final int songQueueCancelBtnW = 200;

  /** Preferred height of the Cancel button. */
  public final int songQueueCancelBtnH = 52;

  // ═══════════════════════════════════════════════════════════════════════════
  // ADMIN PANEL (AdminPanel)
  // Origin: AdminPanel
  // ═══════════════════════════════════════════════════════════════════════════

  /** Fixed width of every sidebar button in AdminPanel. */
  public final int adminBtnW = 84;

  /** Fixed height of every sidebar button in AdminPanel. */
  public final int adminBtnH = 42;

  /** Fixed preferred and maximum {@link Dimension} for AdminPanel sidebar buttons (derived). */
  public final Dimension adminBtnSize;

  /** Fixed cell height for the album list in AdminPanel. */
  public final int adminAlbumCellH = 36;

  /** Preferred width of the filter text field in AdminPanel. */
  public final int adminFilterFieldW = 160;

  /** Preferred height of the filter text field. */
  public final int adminFilterFieldH = 24;

  /** Thumbnail size (square) for album cover-art in the AdminPanel album list. */
  public final int adminThumbSize = 30;

  // ═══════════════════════════════════════════════════════════════════════════
  // LOGIN TO ADMIN PANEL CARD (LoginToAdminPanelCard)
  // Origin: LoginToAdminPanelCard
  // ═══════════════════════════════════════════════════════════════════════════

  /** Preferred width of the credential panel. */
  public final int loginPanelW = 700;

  /** Preferred height of the credential panel. */
  public final int loginPanelH = 340;

  /** Preferred width of each action button (LOGIN / CANCEL) in the login card. */
  public final int loginActionBtnW = 160;

  /** Preferred height of each action button. */
  public final int loginActionBtnH = 52;

  /** Width of the field-label caption in the login card. */
  public final int loginCaptionW = 140;

  /** Height of each field row (Username / Password) in the login card. */
  public final int loginFieldH = 48;

  // ═══════════════════════════════════════════════════════════════════════════
  // EDIT ALBUM CARD (EditAlbumCard)
  // Origin: EditAlbumCard
  // ═══════════════════════════════════════════════════════════════════════════

  /** Preferred width of the EditAlbumCard main panel. */
  public final int editAlbumCardW = 860;

  /** Preferred height of the EditAlbumCard main panel. */
  public final int editAlbumCardH = 660;

  /** Square size of the current cover-art and search-result cover-art labels. */
  public final int editAlbumCoverSize = 250;

  // ═══════════════════════════════════════════════════════════════════════════
  // POPULARITY THRESHOLDS (JukeANatorFrame → all panels)
  // These drive the SongTrackCellRenderer bar count.
  // Origin: JukeANatorFrame constants, propagated through constructors
  // ═══════════════════════════════════════════════════════════════════════════

  /**
   * Minimum play count to display 1 popularity bar. Origin: JukeANatorFrame#POPULARITY_THRESHOLD_1
   */
  public final int popularityThreshold1 = 10;

  /**
   * Minimum play count to display 2 popularity bars. Origin: JukeANatorFrame#POPULARITY_THRESHOLD_2
   */
  public final int popularityThreshold2 = 25;

  /**
   * Minimum play count to display 3 popularity bars (fully popular). Origin:
   * JukeANatorFrame#POPULARITY_THRESHOLD_3
   */
  public final int popularityThreshold3 = 50;

  // ═══════════════════════════════════════════════════════════════════════════
  // FONT SIZES
  // Consolidated here so a portrait or small-screen theme can scale all text
  // from a single place rather than hunting through paintComponent overrides.
  // ═══════════════════════════════════════════════════════════════════════════

  // Navigation & header
  public final int fontSizeNavBtn = 18; // ButtonFactory, AlbumDetailCard back button
  public final int fontSizeDetailTitle = 26; // DetailHeaderPanel title label
  public final int fontSizeDetailSubtitle = 14; // DetailHeaderPanel subtitle label
  public final int fontSizeAdminHeader = 22; // AdminPanel header title
  public final int fontSizeAdminSection = 14; // AdminPanel section header labels
  public final int fontSizeAlbumLabel = 14; // AlbumGridPanel tile album name
  public final int fontSizeArtistLabel = 12; // AlbumGridPanel tile artist name
  public final int fontSizePageLabel = 15; // Pagination page labels
  public final int fontSizeSortBtn = 18; // GenreDetailPanel sort buttons

  // Search
  public final int fontSizeSearchHero = 42; // SearchPanel hero label
  public final int fontSizeSearchBar = 32; // Search bar query display
  public final int fontSizeSearchBtn = 22; // Manual search button

  // Track list / result rows
  public final int fontSizeTrackSong = 17; // AlbumViewCard song label
  public final int fontSizeTrackArtist = 14; // AlbumViewCard artist/header label
  public final int fontSizeResultLine1 = 17; // ResultsColumnPanel primary line
  public final int fontSizeResultLine2 = 13; // ResultsColumnPanel secondary line
  public final int fontSizeResultNum = 16; // ResultsColumnPanel row number label
  public final int fontSizeResultHeader = 22; // ResultsColumnPanel column header

  // Queue / overlay cards
  public final int fontSizeAddSongTitle = 32; // AddSongToQueueCard song title
  public final int fontSizeAddSongArtist = 22; // AddSongToQueueCard artist / album
  public final int fontSizeQueueBtn = 17; // SongQueueCard action buttons
  public final int fontSizeQueueCancelBtn = 20; // SongQueueCard cancel button
  public final int fontSizeTimeoutLabel = 13; // Countdown "Closes in Xs" label

  // Keyboard
  public final int fontSizeKeyLabel = 22; // KeyboardPanel key labels

  // Admin
  public final int fontSizeAdminAlbum = 15; // AdminPanel album list cell
  public final int fontSizeAdminArtist = 12; // AdminPanel album list sub-label
  public final int fontSizeAdminSideBtn1 = 13; // AdminPanel side button line 1 (symbol)
  public final int fontSizeAdminSideBtn2 = 10; // AdminPanel side button line 2 (text)
  public final int fontSizeCreditTitle = 18; // JukeANatorFrame credits title
  public final int fontSizeCreditDesc = 15; // JukeANatorFrame credits description

  // Login
  public final int fontSizeLoginTitle = 26; // LoginToAdminPanelCard title
  public final int fontSizeLoginCaption = 16; // LoginToAdminPanelCard field captions
  public final int fontSizeLoginField = 22; // LoginToAdminPanelCard field text
  public final int fontSizeLoginBtn = 18; // LoginToAdminPanelCard buttons

  // Genre
  public final int fontSizeGenreTileLabel = 24; // GenrePanel tile text label
}
