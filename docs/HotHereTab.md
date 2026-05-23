Add the following implementation to create a fully functional AMI-style “Hot Here” tab using the existing `SearchResultDto` structure and your existing search result row renderer.

---

# 1. Replace the HOT HERE placeholder tab

Replace:

```java
tabs.addTab("HOT HERE", buildPlaceholderPanel());
```

with:

```java
tabs.addTab("HOT HERE", buildHotHerePanel());
```

---

# 2. Add HOT HERE fields

Create a new section above `GENRE TAB`:

```java
// ============================================================
// HOT HERE TAB
// ============================================================
private static final int HOT_HERE_PAGE_SIZE = 15;

private final CardLayout hotHereCardLayout = new CardLayout();
private final JPanel hotHereRootPanel = new JPanel(hotHereCardLayout);

private final JPanel hotHereContentPanel = new JPanel(new BorderLayout());

private SearchResultDto hotHereResults;

private String hotHereCategory = "SONGS";

private int hotHerePage = 0;
```

---

# 3. Add Hot Here panel builder

Add this entire section below the Search panel section.

```java
// ============================================================
// HOT HERE PANEL
// ============================================================
private JPanel buildHotHerePanel() {

  hotHereRootPanel.setBackground(BG_DARK);

  refreshHotHere();

  hotHereRootPanel.add(hotHereContentPanel, "CONTENT");

  hotHereCardLayout.show(hotHereRootPanel, "CONTENT");

  return hotHereRootPanel;
}
```

---

# 4. Add refreshHotHere()

```java
// ============================================================
// REFRESH HOT HERE
// ============================================================
private void refreshHotHere() {

  try {

    hotHereResults = songLibraryServiceClient.getMusicByPopularity();

  } catch (Exception e) {

    hotHereResults = new SearchResultDto();
  }

  rebuildHotHerePanel();
}
```

---

# 5. Add rebuildHotHerePanel()

```java
// ============================================================
// REBUILD HOT HERE PANEL
// ============================================================
private void rebuildHotHerePanel() {

  hotHereContentPanel.removeAll();

  //
  // LEFT SIDEBAR
  //
  JPanel leftPanel = buildHotHereSidebar();

  //
  // MAIN GRID
  //
  JPanel mainPanel = buildHotHereGridPanel();

  hotHereContentPanel.add(leftPanel, BorderLayout.WEST);
  hotHereContentPanel.add(mainPanel, BorderLayout.CENTER);

  hotHereContentPanel.revalidate();
  hotHereContentPanel.repaint();
}
```

---

# 6. Add sidebar builder

```java
// ============================================================
// HOT HERE SIDEBAR
// ============================================================
private JPanel buildHotHereSidebar() {

  JPanel panel = new JPanel();

  panel.setBackground(new Color(18, 18, 24));

  panel.setPreferredSize(new Dimension(260, 1));

  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

  panel.setBorder(new EmptyBorder(24, 20, 24, 20));

  //
  // TITLE
  //
  JLabel title = new JLabel("HOT HERE");

  title.setAlignmentX(CENTER_ALIGNMENT);

  title.setForeground(Color.WHITE);

  title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));

  panel.add(title);

  panel.add(Box.createVerticalStrut(30));

  //
  // CATEGORY BUTTONS
  //
  panel.add(buildHotHereCategoryButton("SONGS"));
  panel.add(Box.createVerticalStrut(14));

  panel.add(buildHotHereCategoryButton("ARTISTS"));
  panel.add(Box.createVerticalStrut(14));

  panel.add(buildHotHereCategoryButton("ALBUMS"));

  panel.add(Box.createVerticalGlue());

  return panel;
}
```

---

# 7. Add category button builder

```java
// ============================================================
// HOT HERE CATEGORY BUTTON
// ============================================================
private JButton buildHotHereCategoryButton(String category) {

  boolean selected = hotHereCategory.equals(category);

  JButton button = new JButton(category);

  button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

  button.setPreferredSize(new Dimension(200, 64));

  button.setFocusPainted(false);

  button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));

  button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

  if (selected) {

    button.setBackground(ACCENT_BLUE);
    button.setForeground(Color.BLACK);

  } else {

    button.setBackground(new Color(50, 50, 70));
    button.setForeground(Color.WHITE);
  }

  button.addActionListener(e -> {

    hotHereCategory = category;

    hotHerePage = 0;

    rebuildHotHerePanel();
  });

  return button;
}
```

---

# 8. Add main grid panel

```java
// ============================================================
// HOT HERE GRID PANEL
// ============================================================
private JPanel buildHotHereGridPanel() {

  JPanel root = new JPanel(new BorderLayout());

  root.setBackground(BG_DARK);

  List<?> items = switch (hotHereCategory) {

    case "ARTISTS" ->
        hotHereResults.getArtists() != null
            ? hotHereResults.getArtists()
            : List.of();

    case "ALBUMS" ->
        hotHereResults.getAlbums() != null
            ? hotHereResults.getAlbums()
            : List.of();

    default ->
        hotHereResults.getSongs() != null
            ? hotHereResults.getSongs()
            : List.of();
  };

  //
  // PAGING
  //
  int total = items.size();

  int totalPages = Math.max(
      1,
      (int) Math.ceil(total / (double) HOT_HERE_PAGE_SIZE));

  hotHerePage = Math.max(
      0,
      Math.min(hotHerePage, totalPages - 1));

  int start = hotHerePage * HOT_HERE_PAGE_SIZE;

  int end = Math.min(
      start + HOT_HERE_PAGE_SIZE,
      total);

  //
  // HEADER
  //
  JLabel header = new JLabel(
      hotHereCategory + " (" + total + ")");

  header.setBorder(new EmptyBorder(18, 24, 18, 24));

  header.setForeground(ACCENT_BLUE);

  header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));

  //
  // GRID
  //
  JPanel grid = new JPanel(new GridLayout(5, 3, 1, 1));

  grid.setBackground(Color.BLACK);

  for (int i = start; i < end; i++) {

    JPanel row = buildSearchResultRow(
        i + 1,
        items.get(i),
        hotHereCategory);

    row.setMaximumSize(null);

    grid.add(row);
  }

  //
  // Fill remaining cells
  //
  int visible = end - start;

  for (int i = visible; i < HOT_HERE_PAGE_SIZE; i++) {

    JPanel filler = new JPanel();

    filler.setBackground(BG_DARK);

    grid.add(filler);
  }

  //
  // PAGINATION
  //
  JPanel pagination = new JPanel(
      new FlowLayout(FlowLayout.RIGHT, 12, 10));

  pagination.setBackground(new Color(20, 20, 30));

  JButton previous = new JButton("❮");

  styleNavButton(previous);

  previous.setEnabled(hotHerePage > 0);

  previous.addActionListener(e -> {

    hotHerePage--;

    rebuildHotHerePanel();
  });

  JButton next = new JButton("❯");

  styleNavButton(next);

  next.setEnabled(hotHerePage < totalPages - 1);

  next.addActionListener(e -> {

    hotHerePage++;

    rebuildHotHerePanel();
  });

  JLabel pageLabel = new JLabel(
      (hotHerePage + 1) + " / " + totalPages);

  pageLabel.setForeground(TEXT_SECONDARY);

  pageLabel.setFont(
      new Font(Font.SANS_SERIF, Font.PLAIN, 18));

  pagination.add(previous);
  pagination.add(pageLabel);
  pagination.add(next);

  root.add(header, BorderLayout.NORTH);
  root.add(grid, BorderLayout.CENTER);
  root.add(pagination, BorderLayout.SOUTH);

  return root;
}
```

---

# Result

This produces:

* AMI-style Hot Here tab
* Left-side category navigation
* Right-side paged result grid
* Uses existing `SearchResultDto`
* Uses existing `buildSearchResultRow(...)`
* Consistent rendering with Search screen
* 15 visible results per page
* Artists / Albums / Songs switching
* Single initial service call:

  ```java
  songLibraryServiceClient.getMusicByPopularity()
  ```
* Responsive pagination behavior
* Approximately 20–25% sidebar width like the AMI UI screenshot
