/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.main.content.authorized_role.mediator;

import bisq.common.data.Quadruple;
import bisq.desktop.CssConfig;
import bisq.desktop.common.Layout;
import bisq.desktop.common.threading.UIThread;
import bisq.desktop.common.utils.ImageUtil;
import bisq.desktop.common.view.View;
import bisq.desktop.components.containers.Spacer;
import bisq.desktop.components.controls.Badge;
import bisq.desktop.components.controls.BisqTooltip;
import bisq.desktop.components.controls.Switch;
import bisq.desktop.components.table.*;
import bisq.desktop.main.content.bisq_easy.BisqEasyViewUtils;
import bisq.desktop.main.content.components.UserProfileDisplay;
import bisq.i18n.Res;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import java.util.Comparator;

@Slf4j
public class MediatorView extends View<ScrollPane, MediatorModel, MediatorController> {
    private final Switch showClosedCasesSwitch;
    private final VBox centerVBox, chatVBox;
    private final BisqTableView<MediationCaseListItem> tableView;
    private final Button toggleChatWindowButton;
    private final AnchorPane tableViewAnchorPane;
    private BisqTableColumn<MediationCaseListItem> closeCaseDateColumn;

    private final InvalidationListener listItemListener;
    private Subscription noOpenCasesPin, tableViewSelectionPin, selectedModelItemPin, showClosedCasesPin, chatWindowPin;

    public MediatorView(MediatorModel model,
                        MediatorController controller,
                        HBox mediationCaseHeader,
                        VBox chatMessagesComponent) {

        super(new ScrollPane(), model, controller);

        tableView = new BisqTableView<>(model.getListItems().getSortedList());
        tableView.getStyleClass().addAll("bisq-easy-open-trades", "hide-horizontal-scrollbar");
        configTableView();

        Quadruple<Label, HBox, AnchorPane, VBox> quadruple = BisqEasyViewUtils.getTableViewContainer(Res.get("authorizedRole.mediator.table.headline"), tableView);
        HBox header = quadruple.getSecond();
        tableViewAnchorPane = quadruple.getThird();
        VBox tableVBox = quadruple.getForth();
        VBox.setMargin(tableViewAnchorPane, new Insets(10, 0, 0, 0));

        toggleChatWindowButton = new Button();
        toggleChatWindowButton.setGraphicTextGap(10);
        toggleChatWindowButton.getStyleClass().add("outlined-button");
        toggleChatWindowButton.setMinWidth(120);
        toggleChatWindowButton.setStyle("-fx-padding: 5 16 5 16");
        mediationCaseHeader.getChildren().add(toggleChatWindowButton);

        showClosedCasesSwitch = new Switch(Res.get("authorizedRole.mediator.showClosedCases"));
        header.getChildren().addAll(Spacer.fillHBox(), showClosedCasesSwitch);

        chatMessagesComponent.setMinHeight(200);
        chatMessagesComponent.setPadding(new Insets(0, -30, -15, -30));

        VBox.setMargin(chatMessagesComponent, new Insets(0, 30, 15, 30));
        VBox.setVgrow(chatMessagesComponent, Priority.ALWAYS);
        chatVBox = new VBox(mediationCaseHeader, Layout.hLine(), chatMessagesComponent);
        chatVBox.getStyleClass().add("bisq-easy-container");
        centerVBox = new VBox();
        VBox.setVgrow(tableVBox, Priority.ALWAYS);
        VBox.setMargin(tableVBox, new Insets(0, 0, 10, 0));
        centerVBox.getChildren().addAll(tableVBox, chatVBox);
        centerVBox.setPadding(new Insets(0, 40, 0, 40));

        VBox.setVgrow(centerVBox, Priority.ALWAYS);
        root.setContent(centerVBox);

        root.setFitToWidth(true);
        root.setFitToHeight(true);

        listItemListener = o -> numListItemsChanged();
    }

    @Override
    protected void onViewAttached() {
        tableView.initialize();
        tableView.getItems().addListener(listItemListener);

        selectedModelItemPin = EasyBind.subscribe(model.getSelectedItem(),
                selected -> tableView.getSelectionModel().select(selected));

        tableViewSelectionPin = EasyBind.subscribe(tableView.getSelectionModel().selectedItemProperty(),
                item -> {
                    if (item != null) {
                        controller.onSelectItem(item);
                    }
                });

        noOpenCasesPin = EasyBind.subscribe(model.getNoOpenCases(), noOpenCases -> {
            if (noOpenCases) {
                tableView.removeListeners();
                tableView.getStyleClass().add("empty-table");
                tableView.setPlaceholderText(model.getShowClosedCases().get() ?
                        Res.get("authorizedRole.mediator.noClosedCases") :
                        Res.get("authorizedRole.mediator.noOpenCases"));

                tableViewAnchorPane.setMinHeight(150);
                tableViewAnchorPane.setMaxHeight(150);
            } else {
                tableView.setPlaceholder(null);
                tableView.getStyleClass().remove("empty-table");
            }
            chatVBox.setVisible(!noOpenCases);
            chatVBox.setManaged(!noOpenCases);
        });

        showClosedCasesPin = EasyBind.subscribe(model.getShowClosedCases(), showClosedCases -> {
            showClosedCasesSwitch.setSelected(showClosedCases);
            tableView.setPlaceholderText(showClosedCases ?
                    Res.get("authorizedRole.mediator.noClosedCases") :
                    Res.get("authorizedRole.mediator.noOpenCases"));
            closeCaseDateColumn.setVisible(showClosedCases);
        });

        chatWindowPin = EasyBind.subscribe(model.getChatWindow(), this::chatWindowChanged);

        showClosedCasesSwitch.setOnAction(e -> controller.onToggleClosedCases());
        toggleChatWindowButton.setOnAction(e -> controller.onToggleChatWindow());

        numListItemsChanged();
    }

    @Override
    protected void onViewDetached() {
        tableView.getItems().removeListener(listItemListener);
        tableView.dispose();

        selectedModelItemPin.unsubscribe();
        tableViewSelectionPin.unsubscribe();
        noOpenCasesPin.unsubscribe();
        showClosedCasesPin.unsubscribe();
        chatWindowPin.unsubscribe();
        showClosedCasesSwitch.setOnAction(null);
        toggleChatWindowButton.setOnAction(null);
    }

    private void numListItemsChanged() {
        if (tableView.getItems().isEmpty()) {
            return;
        }
        // Allow table to use full height if chat is detached
        int maxNumItems = model.getChatWindow().get() == null ? 3 : Integer.MAX_VALUE;
        double height = tableView.calculateTableHeight(maxNumItems);
        tableViewAnchorPane.setMinHeight(height + 1);
        tableViewAnchorPane.setMaxHeight(height + 1);
        UIThread.runOnNextRenderFrame(() -> {
            tableViewAnchorPane.setMinHeight(height);
            tableViewAnchorPane.setMaxHeight(height);
            // Delay call as otherwise the width does not take the scrollbar width correctly into account
            UIThread.runOnNextRenderFrame(tableView::adjustMinWidth);
        });
    }

    private void chatWindowChanged(Stage chatWindow) {
        numListItemsChanged();
        if (chatWindow == null) {
            ImageView icon = ImageUtil.getImageViewById("detach");
            toggleChatWindowButton.setText(Res.get("bisqEasy.openTrades.chat.detach"));
            toggleChatWindowButton.setTooltip(new BisqTooltip(Res.get("bisqEasy.openTrades.chat.detach.tooltip")));
            toggleChatWindowButton.setGraphic(icon);
            if (!centerVBox.getChildren().contains(chatVBox)) {
                centerVBox.getChildren().add(chatVBox);
            }
        } else {
            ImageView icon = ImageUtil.getImageViewById("attach");
            toggleChatWindowButton.setText(Res.get("bisqEasy.openTrades.chat.attach"));
            toggleChatWindowButton.setTooltip(new BisqTooltip(Res.get("bisqEasy.openTrades.chat.attach.tooltip")));
            toggleChatWindowButton.setGraphic(icon);

            chatWindow.titleProperty().bind(model.getChatWindowTitle());
            ImageUtil.addAppIcons(chatWindow);
            chatWindow.initModality(Modality.NONE);

            // We open the window at the button position (need to be done before we remove the chatVBox
            // TODO we could persist the position and size of the window and use it for next time opening...
            Point2D windowPoint = new Point2D(root.getScene().getWindow().getX(), root.getScene().getWindow().getY());
            Point2D scenePoint = new Point2D(root.getScene().getX(), root.getScene().getY());
            Point2D buttonPoint = toggleChatWindowButton.localToScene(0.0, 0.0);
            double x = Math.round(windowPoint.getX() + scenePoint.getX() + buttonPoint.getX());
            double y = Math.round(windowPoint.getY() + scenePoint.getY() + buttonPoint.getY());
            chatWindow.setX(x);
            chatWindow.setY(y);
            chatWindow.setMinWidth(600);
            chatWindow.setMinHeight(400);
            chatWindow.setWidth(1000);
            chatWindow.setHeight(700);

            chatWindow.setOnCloseRequest(event -> {
                event.consume();
                chatWindow.titleProperty().unbind();
                controller.onCloseChatWindow();
                chatWindow.hide();
            });

            chatWindow.show();

            centerVBox.getChildren().remove(chatVBox);

            Layout.pinToAnchorPane(chatVBox, 0, 0, 0, 0);
            AnchorPane windowRoot = new AnchorPane(chatVBox);
            windowRoot.getStyleClass().add("bisq-popup");

            Scene scene = new Scene(windowRoot);
            CssConfig.addAllCss(scene);
            chatWindow.setScene(scene);

            // Avoid flicker
            chatWindow.setOpacity(0);
            UIThread.runOnNextRenderFrame(() -> chatWindow.setOpacity(1));
        }
    }

    private void configTableView() {
        tableView.getColumns().add(tableView.getSelectionMarkerColumn());

        tableView.getColumns().add(new BisqTableColumn.Builder<MediationCaseListItem>()
                .title(Res.get("authorizedRole.mediator.table.maker"))
                .minWidth(120)
                .left()
                .comparator(Comparator.comparing(item -> item.getMaker().getUserName()))
                .setCellFactory(getMakerCellFactory())
                .build());
        tableView.getColumns().add(new BisqTableColumn.Builder<MediationCaseListItem>()
                .minWidth(95)
                .comparator(Comparator.comparing(MediationCaseListItem::getDirectionalTitle))
                .setCellFactory(getDirectionCellFactory())
                .build());
        tableView.getColumns().add(new BisqTableColumn.Builder<MediationCaseListItem>()
                .title(Res.get("authorizedRole.mediator.table.taker"))
                .minWidth(120)
                .left()
                .comparator(Comparator.comparing(item -> item.getTaker().getUserName()))
                .setCellFactory(getTakerCellFactory())
                .build());

        tableView.getColumns().add(DateColumnUtil.getDateColumn(tableView.getSortOrder()));

        tableView.getColumns().add(new BisqTableColumn.Builder<MediationCaseListItem>()
                .title(Res.get("bisqEasy.openTrades.table.tradeId"))
                .minWidth(85)
                .comparator(Comparator.comparing(MediationCaseListItem::getTradeId))
                .valueSupplier(MediationCaseListItem::getShortTradeId)
                .tooltipSupplier(MediationCaseListItem::getTradeId)
                .build());
        tableView.getColumns().add(new BisqTableColumn.Builder<MediationCaseListItem>()
                .title(Res.get("bisqEasy.openTrades.table.quoteAmount"))
                .fixWidth(120)
                .comparator(Comparator.comparing(MediationCaseListItem::getQuoteAmount))
                .valueSupplier(MediationCaseListItem::getQuoteAmountString)
                .build());
        tableView.getColumns().add(new BisqTableColumn.Builder<MediationCaseListItem>()
                .title(Res.get("bisqEasy.openTrades.table.baseAmount"))
                .fixWidth(120)
                .comparator(Comparator.comparing(MediationCaseListItem::getBaseAmount))
                .valueSupplier(MediationCaseListItem::getBaseAmountString)
                .build());
        tableView.getColumns().add(new BisqTableColumn.Builder<MediationCaseListItem>()
                .title(Res.get("bisqEasy.openTrades.table.price"))
                .fixWidth(170)
                .comparator(Comparator.comparing(MediationCaseListItem::getPrice))
                .valueSupplier(MediationCaseListItem::getPriceString)
                .build());
        tableView.getColumns().add(new BisqTableColumn.Builder<MediationCaseListItem>()
                .title(Res.get("bisqEasy.openTrades.table.paymentMethod"))
                .minWidth(130)
                .right()
                .comparator(Comparator.comparing(MediationCaseListItem::getPaymentMethod))
                .valueSupplier(MediationCaseListItem::getPaymentMethod)
                .tooltipSupplier(MediationCaseListItem::getPaymentMethod)
                .build());
        closeCaseDateColumn = new BisqTableColumn.Builder<MediationCaseListItem>()
                .title(Res.get("authorizedRole.mediator.table.header.closeCaseDate"))
                .minWidth(130)
                .right()
                .comparator(Comparator.comparing(MediationCaseListItem::getCloseCaseDate))
                .sortType(TableColumn.SortType.DESCENDING)
                .setCellFactory(getCloseDateCellFactory())
                .build();
        tableView.getColumns().add(closeCaseDateColumn);
    }


    private Callback<TableColumn<MediationCaseListItem, MediationCaseListItem>,
            TableCell<MediationCaseListItem, MediationCaseListItem>> getCloseDateCellFactory() {
        return column -> new TableCell<>() {

            private final Label label = new Label();

            @Override
            protected void updateItem(MediationCaseListItem item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    Label date = new Label(item.getCloseCaseDateString());
                    date.getStyleClass().add("table-view-date-column-date");
                    Label time = new Label(item.getCloseCaseTimeString());
                    time.getStyleClass().add("table-view-date-column-time");
                    VBox vBox = new VBox(3, date, time);
                    vBox.setAlignment(Pos.CENTER);
                    setAlignment(Pos.CENTER);
                    setGraphic(vBox);
                } else {
                    setGraphic(null);
                }
            }
        };
    }

    private Callback<TableColumn<MediationCaseListItem, MediationCaseListItem>,
            TableCell<MediationCaseListItem, MediationCaseListItem>> getDirectionCellFactory() {
        return column -> new TableCell<>() {

            private final Label label = new Label();

            @Override
            protected void updateItem(MediationCaseListItem item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    label.setText(item.getDirectionalTitle());
                    label.setPadding(new Insets(-9, -20, 0, -20));
                    setGraphic(label);
                } else {
                    setGraphic(null);
                }
            }
        };
    }

    private Callback<TableColumn<MediationCaseListItem, MediationCaseListItem>,
            TableCell<MediationCaseListItem, MediationCaseListItem>> getMakerCellFactory() {
        return column -> new TableCell<>() {

            private UserProfileDisplay userProfileDisplay;

            @Override
            protected void updateItem(MediationCaseListItem item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    userProfileDisplay = applyTraderToTableCell(this, item, item.isMakerRequester(), item.getMaker());
                } else {
                    if (userProfileDisplay != null) {
                        userProfileDisplay.dispose();
                        userProfileDisplay = null;
                    }
                    setGraphic(null);
                }
            }
        };
    }

    private Callback<TableColumn<MediationCaseListItem, MediationCaseListItem>,
            TableCell<MediationCaseListItem, MediationCaseListItem>> getTakerCellFactory() {
        return column -> new TableCell<>() {

            private UserProfileDisplay userProfileDisplay;

            @Override
            protected void updateItem(MediationCaseListItem item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    userProfileDisplay = applyTraderToTableCell(this, item, !item.isMakerRequester(), item.getTaker());
                } else {
                    if (userProfileDisplay != null) {
                        userProfileDisplay.dispose();
                        userProfileDisplay = null;
                    }
                    setGraphic(null);
                }
            }
        };
    }

    private static UserProfileDisplay applyTraderToTableCell(TableCell<MediationCaseListItem, MediationCaseListItem> tableCell,
                                                             MediationCaseListItem item,
                                                             boolean isRequester,
                                                             MediationCaseListItem.Trader trader) {
        UserProfileDisplay userProfileDisplay = new UserProfileDisplay();
        userProfileDisplay.setUserProfile(trader.getUserProfile());
        if (isRequester) {
            userProfileDisplay.getStyleClass().add("mediator-table-requester");
        }
        userProfileDisplay.getTooltip().setText(Res.get("authorizedRole.mediator.hasRequested",
                userProfileDisplay.getTooltipText(),
                isRequester ? Res.get("confirmation.yes") : Res.get("confirmation.no")
        ));
        Badge badge = trader.equals(item.getMaker()) ? item.getMakersBadge() : item.getTakersBadge();
        badge.setControl(userProfileDisplay);
        badge.getStyleClass().add("open-trades-badge");
        badge.setPosition(Pos.BOTTOM_LEFT);
        badge.setBadgeInsets(new Insets(0, 0, 7.5, 20));
        // Label color does not get applied from badge style when in a list cell even we use '!important' in the css.
        badge.getLabel().setStyle("-fx-text-fill: black !important;");
        tableCell.setGraphic(badge);
        return userProfileDisplay;
    }

}
