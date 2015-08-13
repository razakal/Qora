package qora.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import qora.crypto.Base58;
import qora.transaction.ArbitraryTransaction;
import utils.DateTimeFormat;

public class NameStorageTransactionHistory {

	private final List<NamestorageKeyValueHistory> keyvalueList;
	private final ArbitraryTransaction tx;

	public NameStorageTransactionHistory(ArbitraryTransaction tx) {
		keyvalueList = new ArrayList<>();
		this.tx = tx;
	}

	public ArbitraryTransaction getTx() {
		return tx;
	}

	public List<NamestorageKeyValueHistory> getKeyValueHistoryList() {
		return Collections.unmodifiableList(keyvalueList);
	}

	public void addEntry(NamestorageKeyValueHistory entry) {
		if (!keyvalueList.contains(entry)) {
			keyvalueList.add(entry);
		}
	}

	public String getCreationTime() {
		return DateTimeFormat.timestamptoString(tx.getTimestamp());
	}

	public String getSignature() {
		return Base58.encode(tx.getSignature());
	}

	public String getAllKeys() {
		String result = "";

		for (NamestorageKeyValueHistory namestorageKeyValueHistory : keyvalueList) {
			result += namestorageKeyValueHistory.getKey() + ", ";
		}

		return StringUtils.removeEnd(result, ", ");

	}

}
