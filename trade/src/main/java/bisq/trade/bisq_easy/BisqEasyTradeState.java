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

package bisq.trade.bisq_easy;

import bisq.common.fsm.State;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum BisqEasyTradeState implements State {
    INIT(true, false),
    TAKER_TAKE_OFFER_REQUEST_SENT,
    MAKER_TAKE_OFFER_REQUEST_ACCEPTED,
    SELLER_ACCOUNT_DATA_SENT,
    BUYER_ACCOUNT_DATA_RECEIVED;

    private final boolean isInitialState;
    private final boolean isFinalState;

    BisqEasyTradeState() {
        this.isInitialState = false;
        this.isFinalState = false;
    }

    BisqEasyTradeState(boolean isInitialState, boolean isFinalState) {
        this.isInitialState = isInitialState;
        this.isFinalState = isFinalState;
    }
}