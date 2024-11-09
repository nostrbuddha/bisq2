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

package bisq.desktop.main.content.bisq_easy.open_trades.trade_details;

import bisq.bisq_easy.NavigationTarget;
import bisq.desktop.common.view.NavigationModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class TradeDetailsModel extends NavigationModel {
    private final SimpleStringProperty tradeDate = new SimpleStringProperty();
    private final SimpleStringProperty me = new SimpleStringProperty();
    private final SimpleStringProperty peer = new SimpleStringProperty();
    private final SimpleStringProperty offerType = new SimpleStringProperty();
    private final SimpleStringProperty market = new SimpleStringProperty();
    private final SimpleStringProperty fiatAmount = new SimpleStringProperty();
    private final SimpleStringProperty fiatCurrency = new SimpleStringProperty();
    private final SimpleStringProperty btcAmount = new SimpleStringProperty();
    private final SimpleStringProperty price = new SimpleStringProperty();
    private final SimpleStringProperty priceCodes = new SimpleStringProperty();
    private final SimpleStringProperty priceSpec = new SimpleStringProperty();
    private final SimpleStringProperty paymentMethod = new SimpleStringProperty();
    private final SimpleStringProperty settlementMethod = new SimpleStringProperty();
    private final SimpleStringProperty tradeId = new SimpleStringProperty();
    private final SimpleStringProperty peerNetworkAddress = new SimpleStringProperty();
    private final SimpleStringProperty btcPaymentAddress = new SimpleStringProperty();
    private final SimpleBooleanProperty isBtcPaymentDataEmpty = new SimpleBooleanProperty();
    private final SimpleStringProperty paymentAccountData = new SimpleStringProperty();
    private final SimpleBooleanProperty isPaymentAccountDataEmpty = new SimpleBooleanProperty();
    private final SimpleStringProperty assignedMediator = new SimpleStringProperty();

    @Override
    public NavigationTarget getDefaultNavigationTarget() {
        return NavigationTarget.BISQ_EASY_TRADE_DETAILS;
    }
}
