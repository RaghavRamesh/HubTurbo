# Panels

## Adding a New Panel <a name="add"></a>

Panels can be added using the menu or keyboard shortcuts.

- `Panels > Create Panel` to add a panel on the right of the currently selected panel.
    - <kbd>Ctrl</kbd> + <kbd>P</kbd> will do the same.

- `Panels > Create Panel (Left)` to add a panel on the left.
    - <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>P</kbd> will do the same.

## Closing a Panel <a name="close"></a>

- Click ![The x icon](images/panels/deletePanel.png) at the top of the panel.

- `Panels > Close Panel`
    - <kbd>Ctrl</kbd> + <kbd>W</kbd> will do the same.

## Reordering Panels <a name="reorder"></a>

Panels can be reordered simply by dragging them into the desired position.

- Click anywhere on the head or borders of the panel and drag it to the left or right.

## Renaming Panels <a name="rename"></a>

1. To __start__ renaming, double click the panel name or click ![The pencil icon](images/panels/startPanelRename.png?raw=true) at the top right corner of the panel.
2. Type the new name in the text field.
3. __Save__ the new name by pressing <kbd>Enter</kbd> or clicking ![The tick icon](images/panels/savePanelRename.png?raw=true) at the top right corner of the panel.

To __abort__ the renaming action any time before step 3, press <kbd>Esc</kbd> or click ![The undo arrow icon](images/panels/cancelPanelRename.png?raw=true) at the top right corner of the panel.

## Watch List Panels <a name="watchlist"></a>

Watch list panels are panels whose filter reflects a "watch list". More specifically, a filter reflects a watch list if either of the following conditions is met :

- It is empty (in other words, it watches all issues from the primary repo)
- It is in the form of `id:repo1#id1;repo2#id2;repo3#id3;...;repoN#idN` (you can refer [here](filters.md#operator) and [here](filters.md#id) for more explanation about this filter syntax)

To create a new watch list panel or add an issue to an existing watch list panel, you can refer [here](issuesAndPRs.md#watching).
