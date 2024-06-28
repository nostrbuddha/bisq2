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

package bisq.desktop.main.content.chat.message_container.list.message_box;

import bisq.chat.ChatChannel;
import bisq.chat.ChatMessage;
import bisq.chat.bisqeasy.offerbook.BisqEasyOfferbookMessage;
import bisq.desktop.common.Icons;
import bisq.desktop.components.containers.Spacer;
import bisq.desktop.components.controls.BisqTextArea;
import bisq.desktop.components.controls.BisqTooltip;
import bisq.desktop.main.content.chat.message_container.list.ChatMessageListItem;
import bisq.desktop.main.content.chat.message_container.list.ChatMessagesListController;
import bisq.i18n.Res;
import bisq.network.p2p.services.confidential.ack.MessageDeliveryStatus;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

public final class MyTextMessageBox extends BubbleMessageBox {
    private final static String EDITED_POST_FIX = " " + Res.get("chat.message.wasEdited");

    private final Label deliveryState;
    private final Subscription actionsBoxVisiblePropertyPin, messageDeliveryStatusIconPin;
    private Label editIcon, deleteIcon;
    private BisqTextArea editInputField;
    private Button saveEditButton, cancelEditButton;
    private HBox editButtonsHBox;

    public MyTextMessageBox(ChatMessageListItem<? extends ChatMessage, ? extends ChatChannel<? extends ChatMessage>> item,
                            ListView<ChatMessageListItem<? extends ChatMessage, ? extends ChatChannel<? extends ChatMessage>>> list,
                            ChatMessagesListController controller) {
        super(item, list, controller);

        quotedMessageVBox.setId("chat-message-quote-box-my-msg");
        setUpEditFunctionality();
        message.setAlignment(Pos.CENTER_RIGHT);
        messageBgHBox.getStyleClass().add("chat-message-bg-my-message");

        // deliveryState
        deliveryState = new Label();
        deliveryState.setCursor(Cursor.HAND);
        deliveryState.setTooltip(new BisqTooltip(true));
        deliveryState.getStyleClass().add("medium-text");

        VBox messageVBox = new VBox(quotedMessageVBox, message, editInputField);

        message.maxWidthProperty().bind(list.widthProperty().subtract(140));
        userProfileIcon.setSize(30);
        userProfileIconVbox.setAlignment(Pos.TOP_LEFT);
        HBox.setMargin(deleteIcon, new Insets(0, 10, 0, 0));
        actionsHBox.getChildren().setAll(Spacer.fillHBox(), editIcon, copyAction, deleteIcon);
        HBox.setMargin(messageVBox, new Insets(0, -15, 0, 0));
        HBox.setMargin(userProfileIconVbox, new Insets(7.5, 0, -5, 5));
        HBox.setMargin(editInputField, new Insets(6, -10, -25, 0));
        messageBgHBox.getChildren().setAll(messageVBox, userProfileIconVbox);

        HBox.setMargin(deliveryState, new Insets(0, 10, 0, 0));
        HBox deliveryStateHBox = new HBox(Spacer.fillHBox(), actionsHBox);

        actionsBoxVisiblePropertyPin = EasyBind.subscribe(actionsHBox.visibleProperty(), v -> {
            if (v) {
                deliveryStateHBox.getChildren().remove(deliveryState);
                if (!actionsHBox.getChildren().contains(deliveryState)) {
                    actionsHBox.getChildren().add(deliveryState);
                }
            } else {
                actionsHBox.getChildren().remove(deliveryState);
                if (!deliveryStateHBox.getChildren().contains(deliveryState)) {
                    deliveryStateHBox.getChildren().add(deliveryState);
                }
            }
        });

        messageDeliveryStatusIconPin = EasyBind.subscribe(item.getMessageDeliveryStatusIcon(), icon -> {
                    deliveryState.setManaged(icon != null);
                    deliveryState.setVisible(icon != null);
                    if (icon != null) {
                        AwesomeDude.setIcon(deliveryState, icon, AwesomeDude.DEFAULT_ICON_SIZE);
                        item.getMessageDeliveryStatusIconColor().ifPresent(color ->
                                Icons.setAwesomeIconColor(deliveryState, color));

                        boolean allowResend = item.getMessageDeliveryStatus() == MessageDeliveryStatus.FAILED;
                        String messageId = item.getMessageId();
                        if (allowResend && controller.canResendMessage(messageId)) {
                            deliveryState.setOnMouseClicked(e -> controller.onResendMessage(messageId));
                            deliveryState.setCursor(Cursor.HAND);
                        } else {
                            deliveryState.setOnMouseClicked(null);
                            deliveryState.setCursor(null);
                        }
                    }
                }
        );

        deliveryState.getTooltip().textProperty().bind(item.getMessageDeliveryStatusTooltip());
        editInputField.maxWidthProperty().bind(message.widthProperty());

        setMargin(deliveryStateHBox, new Insets(4, 0, -3, 0));
        messageHBox.getChildren().setAll(Spacer.fillHBox(), addedReactions, messageBgHBox);

        contentVBox.getChildren().setAll(userNameAndDateHBox, messageHBox, editButtonsHBox, deliveryStateHBox);
    }

    @Override
    protected void setUpUserNameAndDateTime() {
        super.setUpUserNameAndDateTime();

        userNameAndDateHBox = new HBox(10, dateTime, userName);
        userNameAndDateHBox.setAlignment(Pos.CENTER_RIGHT);
        setMargin(userNameAndDateHBox, new Insets(-5, 10, -5, 0));
    }

    @Override
    protected void setUpActions() {
        super.setUpActions();

        editIcon = getIconWithToolTip(AwesomeIcon.EDIT, Res.get("action.edit"));
        deleteIcon = getIconWithToolTip(AwesomeIcon.REMOVE_SIGN, Res.get("action.delete"));

        HBox.setMargin(editIcon, ACTION_ITEMS_MARGIN);
        HBox.setMargin(deleteIcon, ACTION_ITEMS_MARGIN);
    }

    private void setUpEditFunctionality() {
        // edit
        editInputField = new BisqTextArea();
        editInputField.setId("chat-messages-edit-text-area");
        editInputField.setMinWidth(150);
        editInputField.setVisible(false);
        editInputField.setManaged(false);

        // edit buttons
        saveEditButton = new Button(Res.get("action.save"));
        saveEditButton.setDefaultButton(true);
        cancelEditButton = new Button(Res.get("action.cancel"));
        editButtonsHBox = new HBox(15, Spacer.fillHBox(), cancelEditButton, saveEditButton);
        editButtonsHBox.setVisible(false);
        editButtonsHBox.setManaged(false);
        setMargin(editButtonsHBox, new Insets(10, 25, -15, 0));
        handleEditBox();
    }

    @Override
    protected void addActionsHandlers() {
        ChatMessage chatMessage = item.getChatMessage();
        boolean isPublicChannel = item.isPublicChannel();
        boolean allowEditing = isPublicChannel;
        if (chatMessage instanceof BisqEasyOfferbookMessage) {
            BisqEasyOfferbookMessage bisqEasyOfferbookMessage = (BisqEasyOfferbookMessage) chatMessage;
            allowEditing = allowEditing && bisqEasyOfferbookMessage.getBisqEasyOffer().isEmpty();
        }

        copyAction.setOnAction(e -> onCopyMessage(chatMessage));
        if (allowEditing) {
            editIcon.setOnMouseClicked(e -> onEditMessage());
        }
        if (isPublicChannel) {
            deleteIcon.setOnMouseClicked(e -> controller.onDeleteMessage(chatMessage));
        }

        editIcon.setVisible(allowEditing);
        editIcon.setManaged(allowEditing);
        deleteIcon.setVisible(isPublicChannel);
        deleteIcon.setManaged(isPublicChannel);
    }

    private void handleEditBox() {
        saveEditButton.setOnAction(e -> {
            controller.onSaveEditedMessage(item.getChatMessage(), editInputField.getText());
            onCloseEditMessage();
        });
        cancelEditButton.setOnAction(e -> onCloseEditMessage());
    }

    private void onEditMessage() {
        actionsHBox.setVisible(false);
        editInputField.setVisible(true);
        editInputField.setManaged(true);
        editInputField.setInitialHeight(message.getBoundsInLocal().getHeight());
        editInputField.setText(message.getText().replace(EDITED_POST_FIX, ""));
        editInputField.requestFocus();
        editInputField.positionCaret(message.getText().length());
        editButtonsHBox.setVisible(true);
        editButtonsHBox.setManaged(true);
        message.setVisible(false);
        message.setManaged(false);

        editInputField.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                keyEvent.consume();
                if (keyEvent.isShiftDown()) {
                    editInputField.appendText(System.getProperty("line.separator"));
                } else if (!editInputField.getText().isEmpty()) {
                    controller.onSaveEditedMessage(item.getChatMessage(), editInputField.getText().trim());
                    onCloseEditMessage();
                }
            }
        });
    }

    private void onCloseEditMessage() {
        editInputField.setVisible(false);
        editInputField.setManaged(false);
        editButtonsHBox.setVisible(false);
        editButtonsHBox.setManaged(false);
        message.setVisible(true);
        message.setManaged(true);
        editInputField.setOnKeyPressed(null);
    }

    @Override
    public void cleanup() {
        super.cleanup();

        message.maxWidthProperty().unbind();
        editInputField.maxWidthProperty().unbind();
        deliveryState.getTooltip().textProperty().unbind();

        saveEditButton.setOnAction(null);
        cancelEditButton.setOnAction(null);
        copyAction.setOnAction(null);

        userName.setOnMouseClicked(null);
        userProfileIcon.setOnMouseClicked(null);

        editIcon.setOnMouseClicked(null);
        deleteIcon.setOnMouseClicked(null);

        editInputField.setOnKeyPressed(null);
        userProfileIcon.releaseResources();

        if (actionsBoxVisiblePropertyPin != null) {
            actionsBoxVisiblePropertyPin.unsubscribe();
        }

        if (messageDeliveryStatusIconPin != null) {
            messageDeliveryStatusIconPin.unsubscribe();
        }
    }
}
