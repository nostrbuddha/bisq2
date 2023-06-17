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

import bisq.contract.ContractSignatureData;
import bisq.network.NetworkId;
import bisq.trade.TradeParty;

public class BisqEasyTradeParty extends TradeParty {
    public BisqEasyTradeParty(NetworkId networkId) {
        super(networkId);
    }

    public static BisqEasyTradeParty fromProto(bisq.trade.protobuf.TradeParty proto) {
        BisqEasyTradeParty tradeParty = new BisqEasyTradeParty(NetworkId.fromProto(proto.getNetworkId()));
        if (proto.hasContractSignatureData()) {
            tradeParty.setContractSignatureData(ContractSignatureData.fromProto(proto.getContractSignatureData()));
        }
        bisq.trade.protobuf.BisqEasyTradeParty bisqEasyTradePartyProto = proto.getBisqEasyTradeParty();
        if (bisqEasyTradePartyProto.hasPaymentAccountData()) {
            tradeParty.setPaymentAccountData(bisqEasyTradePartyProto.getPaymentAccountData());
        }
        return tradeParty;
    }
}