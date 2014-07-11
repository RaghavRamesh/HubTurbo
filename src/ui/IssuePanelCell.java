package ui;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;
import model.TurboUser;

public class IssuePanelCell extends ListCell<TurboIssue> {

	private final Stage mainStage;
	private final Model model;
	private final int parentColumnIndex;
	
	private ChangeListener<String> titleChangeListener;
	
	public IssuePanelCell(Stage mainStage, Model model, IssuePanel parent, int parentColumnIndex) {
		super();
		this.mainStage = mainStage;
		this.model = model;
		this.parentColumnIndex = parentColumnIndex;
	}

	private ChangeListener<String> createIssueTitleListener(TurboIssue issue, Text issueName){
		WeakReference<TurboIssue> issueRef = new WeakReference<TurboIssue>(issue);
		titleChangeListener = new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				TurboIssue issue = issueRef.get();
				if(issue != null){
					issueName.setText("#" + issue.getId() + " " + newValue);
				}
			}
		};
		
		return titleChangeListener;
	}
	@Override
	public void updateItem(TurboIssue issue, boolean empty) {
		super.updateItem(issue, empty);
		if (issue == null)
			return;
		
		Text issueTitle = new Text("#" + issue.getId() + " " + issue.getTitle());
		issueTitle.setWrappingWidth(300);
		issueTitle.getStyleClass().add("issue-panel-name");
		if (!issue.getOpen()) issueTitle.getStyleClass().add("issue-panel-closed");
		issue.titleProperty().addListener(new WeakChangeListener<String>(createIssueTitleListener(issue, issueTitle)));

		HBox titleBox = new HBox();
		titleBox.getChildren().addAll(issueTitle);
		titleBox.setOnMouseClicked((MouseEvent e) -> {
			browse(issue.getHtmlUrl());
		});

		LabelDisplayBox labels = new LabelDisplayBox(issue.getLabelsReference(), false, "");

		ParentIssuesDisplayBox parents = new ParentIssuesDisplayBox(issue.getParentsReference(), false);
		
		TurboUser assignee = issue.getAssignee();
		HBox assigneeBox = new HBox();
		if (assignee != null) {
			assigneeBox.setSpacing(3);
			Label assigneeName = new Label(assignee.getGithubName());
			assigneeBox.getChildren().addAll(assigneeName);
		}

		HBox bottom = new HBox();
		bottom.setSpacing(5);
		bottom.setAlignment(Pos.BASELINE_LEFT);
		bottom.getChildren().add(parents);
		if (assignee != null) bottom.getChildren().add(assigneeBox);
		
		VBox everything = new VBox();
		everything.setMaxWidth(350);
		
		everything.setSpacing(5);
		everything.getChildren().addAll(titleBox, labels, bottom);

		setGraphic(everything);
		
		setAlignment(Pos.CENTER);

		getStyleClass().addAll("borders", "rounded-borders");
		
		setContextMenu(new ContextMenu(createGroupContextMenu(issue)));

		registerEvents(issue);
		
		setOnDragDetected((event) -> {
			Dragboard db = startDragAndDrop(TransferMode.MOVE);
			ClipboardContent content = new ClipboardContent();
			IssuePanelDragData dd = new IssuePanelDragData(parentColumnIndex, issue.getId());
			content.putString(dd.serialise());
			db.setContent(content);
			event.consume();
		});
		
		setOnDragDone((event) -> {
//			if (event.getTransferMode() == TransferMode.MOVE) {
//			}
//			System.out.println("done");
			event.consume();
		});
	}
	
	private MenuItem[] createGroupContextMenu(TurboIssue issue) {
		MenuItem childMenuItem = new MenuItem("Create Child Issue");
		childMenuItem.setOnAction((event) -> {
			TurboIssue childIssue = new TurboIssue("New child issue", "", model);
			childIssue.addParent(issue.getId());
			model.processInheritedLabels(childIssue, new ArrayList<Integer>());
			(new IssueDialog(mainStage, model, childIssue)).show().thenApply(
					response -> {
						if (response.equals("ok")) {
							model.createIssue(childIssue);
						}
						return true;
					})
				.exceptionally(ex -> {
					ex.printStackTrace();
					return false;
				});

		});
		return new MenuItem[] {childMenuItem};
	}
	
	private void browse(String htmlUrl) {
		
		if (htmlUrl == null || htmlUrl.isEmpty()) return;

		final String osName = System.getProperty("os.name");
		
		if (osName.startsWith("Mac OS") || osName.startsWith("Windows")) {
			browseWithDesktop(htmlUrl);
		} else {
			// Assume *nix
			browseUnix(htmlUrl);
		}
	}
	
	private void browseWithDesktop(String htmlUrl) {
		try {
			if (Desktop.isDesktopSupported()) {
		        Desktop desktop = Desktop.getDesktop();
		        if (desktop.isSupported(Desktop.Action.BROWSE)) {

		            URI uri = new URI(htmlUrl);
		            desktop.browse(uri);
		        }
	        }
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    } catch (URISyntaxException ex) {
	        ex.printStackTrace();
	    }
	}

	private void browseUnix(String url) {

		final String[] UNIX_BROWSE_CMDS = new String[] {"google-chrome", "firefox", "www-browser", "opera", "konqueror", "epiphany", "mozilla", "netscape", "w3m", "lynx" };
		for (final String cmd : UNIX_BROWSE_CMDS) {
			
			if (unixCommandExists(cmd)) {
				try {
					Runtime.getRuntime().exec(new String[] {cmd, url.toString()});
				} catch (IOException e) {
				}
				return;
			}
		}
	}

	private static boolean unixCommandExists(final String cmd) {
		Process whichProcess;
		try {
			whichProcess = Runtime.getRuntime().exec(new String[] { "which", cmd });
			boolean finished = false;
			do {
				try {
					whichProcess.waitFor();
					finished = true;
				} catch (InterruptedException e) {
					return false;
				}
			} while (!finished);

			return whichProcess.exitValue() == 0;
		} catch (IOException e1) {
			return false;
		}
	}

	private void registerEvents(TurboIssue issue) {
		WeakReference<TurboIssue> issueRef = new WeakReference<TurboIssue>(issue);
		setOnMouseClicked((MouseEvent mouseEvent) -> {
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
				if (mouseEvent.getClickCount() == 2) {
					onDoubleClick(issueRef.get());
				}
			}
		});
	}

	private void onDoubleClick(TurboIssue issue) {
		TurboIssue oldIssue = new TurboIssue(issue);
		TurboIssue modifiedIssue = new TurboIssue(issue);
		(new IssueDialog(mainStage, model, modifiedIssue)).show().thenApply(
				response -> {
					if (response.equals("ok")) {
						model.updateIssue(oldIssue, modifiedIssue);
					}
					return true;
				}).exceptionally(ex -> {
					ex.printStackTrace();
					return false;
				});
	}
}
