package org.almibe.multipage.skins;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.almibe.multipage.MultiPageDisplay;
import org.almibe.multipage.Page;

public class MultiPageDisplaySkin extends SkinBase<MultiPageDisplay> {

    private final ScrollPane tabScrollPane = new ScrollPane();
    private final ImageView addTabButton = new ImageView(new Image(getClass().getResourceAsStream("tango/list-add32.png")));
    private final ImageView downArrowButton = new ImageView(new Image(getClass().getResourceAsStream("tango/go-down32.png")));
    private final ContextMenu openPagesList = new ContextMenu();
    private final HBox buttonControls = new HBox(downArrowButton, addTabButton);
    private final BorderPane header = new BorderPane();
    private final ScrollPane content = new ScrollPane();
    private final BorderPane tabPane = new BorderPane();
    private final TabAreaNode tabArea;

    public MultiPageDisplay getMultiPageDisplay() {
        return multiPageDisplay;
    }

    private final MultiPageDisplay multiPageDisplay;

    public MultiPageDisplaySkin(MultiPageDisplay multiPageDisplay) {
        super(multiPageDisplay);
        this.multiPageDisplay = multiPageDisplay;
        this.tabArea = new TabAreaNode(multiPageDisplay.selectedPageProperty(), tabPane);
        start();
    }

    public void start() {
        Platform.runLater(() -> {
            content.contentProperty().bind(Bindings.select(multiPageDisplay.selectedPageProperty(), "content"));

            content.contentProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> content.requestFocus());
            });

            tabScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            tabScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            tabScrollPane.contentProperty().setValue(tabArea);

            tabScrollPane.setStyle("-fx-background: rgb(200,200,200);");

            header.setCenter(tabScrollPane);
            header.setRight(buttonControls);

            tabPane.setTop(header);
            tabPane.setCenter(content);

            addTabButton.setOnMouseClicked(event -> addPage());
            downArrowButton.setOnMouseClicked(event -> showDropDown());

            tabArea.getPages().addListener((observable, oldPages, newPages) -> {
                openPagesList.getItems().clear();
                newPages.forEach(page -> {
                    MenuItem menuItem = new MenuItem(page.getText());
                    openPagesList.getItems().add(menuItem);
                });
            });

            this.getChildren().add(tabPane);
        });
    }

    public void addPage() {
        tabArea.addPage(multiPageDisplay.getDefaultPageFactory().createDefaultPage());
    }

    public void addPage(Page page) {
        tabArea.addPage(page);
    }

    public void removePage(Page page) {
        tabArea.removePage(page);
    }

    public ReadOnlyListProperty<Page> getPages() {
        return tabArea.getPages();
    }

    private void showDropDown() {
        openPagesList.show(downArrowButton, Side.BOTTOM, 0, 0);
    }
}
