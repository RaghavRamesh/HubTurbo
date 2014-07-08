package ui;

import java.util.ArrayList;
import java.util.stream.Collectors;

import model.Model;
import model.TurboLabel;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

public class ManageLabelsTreeCell<T> extends TreeCell<LabelTreeItem> {

	private final Model model;
	private final Stage stage;
	
	public ManageLabelsTreeCell(Stage stage, Model model) {
		this.model = model;
		this.stage = stage;
	}
    
	@Override
	protected void updateItem(LabelTreeItem item, boolean empty) {
		super.updateItem(item, empty);
		
		if (item == null) {
			setText(null);
			setGraphic(null);
		}
		else {
	        setText(getItem().getValue());
	        setGraphic(getTreeItem().getGraphic());
			setContextMenu(getContextMenuForItem(getTreeItem()));
		}
	}

	private MenuItem[] createLabelContextMenu() {
		MenuItem edit = new MenuItem("Edit Label");
		edit.setOnAction((event) -> {
			assert getItem() instanceof TurboLabel;
			TurboLabel original = (TurboLabel) getItem();
			String oldName = original.toGhName();

			(new EditLabelDialog(stage, original)).show().thenApply(response -> {
		    	model.updateLabel(response, oldName);
		    	
		    	// The tree view doesn't update automatically because there is;
		    	// no binding; manually trigger the update
		    	original.copyValues(response);
		    	updateItem(original, false);
		    	
				return true;
			}).exceptionally(e -> {
				e.printStackTrace();
				return false;
			});
		});
		MenuItem delete = new MenuItem("Delete Label");
		delete.setOnAction((event) -> {
			model.deleteLabel((TurboLabel) getItem());
			for (TreeItem<LabelTreeItem> lti : getTreeItem().getParent().getChildren()) {
				if (lti.getValue().getValue().equals(getItem().getValue())) {
					getTreeItem().getParent().getChildren().remove(lti);
					break;
				}
			}
		});
		return new MenuItem[] {edit, delete};
	}

	private MenuItem[] createGroupContextMenu() {
				
		MenuItem groupMenuItem = new MenuItem("Edit Group");
		groupMenuItem.setOnAction((event) -> {
			
			TurboLabelGroup group = (TurboLabelGroup) getItem();
			
			(new EditGroupDialog(stage, group))
				.setExclusiveCheckboxVisible(false)
				.show().thenApply(response -> {
					assert response.getValue() != null;
					if (response.getValue().isEmpty()) {
						return false;
					}
	
		    		// Get all the old names
		    		ArrayList<String> oldNames = new ArrayList<>(group.getLabels().stream().map(l -> l.toGhName()).collect(Collectors.toList()));
	
		    		// Update every label using TurboLabelGroup::setValue
		    		group.setValue(response.getValue());
		    		group.setExclusive(response.isExclusive());
	
		    		// Trigger updates on all the labels
		    		for (int i=0; i<oldNames.size(); i++) {
		    			model.updateLabel(group.getLabels().get(i), oldNames.get(i));
		    		}
		    		
		    		// Manually update the treeview, since there is no binding
		    		TreeItem<LabelTreeItem> item = getTreeItem();
		    		TreeItem<LabelTreeItem> parent = item.getParent();
		    		parent.getChildren().remove(item);
		    		parent.getChildren().add(item);
		    		
					return true;
				})
				.exceptionally(ex -> {
					ex.printStackTrace();
					return false;
				});
		});

		MenuItem label = new MenuItem("New Label");
		label.setOnAction((event) -> {
			
			// Create a new label
			TurboLabel newLabel = new TurboLabel("newlabel" + ManageLabelsDialog.getUniqueId());
			
			// Set its group value to null if it's being created under the <Ungrouped> group
			String groupName = getTreeItem().getValue().getValue();
			if (groupName.equals(ManageLabelsDialog.UNGROUPED_NAME)) groupName = null;
			newLabel.setGroup(groupName);
			
			// Set its exclusivity
			newLabel.setExclusive(((TurboLabelGroup) getTreeItem().getValue()).isExclusive());
			
			newLabel = model.createLabel(newLabel);
			
			// Make sure this TurboLabelGroup has a reference to the new label
			((TurboLabelGroup) getTreeItem().getValue()).addLabel(newLabel);
			getTreeItem().getChildren().add(new TreeItem<LabelTreeItem>(newLabel));
			
			getTreeItem().setExpanded(true);
		});
		
		boolean isUngroupedHeading = getTreeItem().getValue().getValue().equals(ManageLabelsDialog.UNGROUPED_NAME);

		if (isUngroupedHeading) {
			return new MenuItem[] {label};
		} else {
			return new MenuItem[] {groupMenuItem, label};
		}
	}

	private boolean isGroupItem(TreeItem<LabelTreeItem> treeItem) {
		assert treeItem != null;
		return treeItem.getParent() != null && treeItem.getParent().getValue().getValue().equals(ManageLabelsDialog.ROOT_NAME);
	}

	private ContextMenu getContextMenuForItem(TreeItem<LabelTreeItem> treeItem) {
		if (isGroupItem(treeItem)) {
			return new ContextMenu(createGroupContextMenu());
		} else {
			return new ContextMenu(createLabelContextMenu());
		}
	}
}
