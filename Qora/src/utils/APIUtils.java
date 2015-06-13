package utils;

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JOptionPane;
import javax.ws.rs.WebApplicationException;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.Crypto;
import qora.transaction.Transaction;
import api.ApiErrorFactory;
import controller.Controller;

public class APIUtils {

	public static String processPayment(String amount, String fee,
			String sender, String recipient) {
		// PARSE AMOUNT
		BigDecimal bdAmount;
		try {
			bdAmount = new BigDecimal(amount);
			bdAmount = bdAmount.setScale(8);
		} catch (Exception e) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_AMOUNT);
		}

		// PARSE FEE
		BigDecimal bdFee;
		try {
			bdFee = new BigDecimal(fee);
			bdFee = bdFee.setScale(8);
		} catch (Exception e) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_FEE);
		}

		// CHECK ADDRESS
		if (!Crypto.getInstance().isValidAddress(sender)) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_SENDER);
		}

		// CHECK IF WALLET EXISTS
		if (!Controller.getInstance().doesWalletExists()) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}

		// CHECK WALLET UNLOCKED
		if (!Controller.getInstance().isWalletUnlocked()) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_WALLET_LOCKED);
		}

		// GET ACCOUNT
		PrivateKeyAccount account = Controller.getInstance()
				.getPrivateKeyAccountByAddress(sender);
		if (account == null) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_SENDER);
		}

		// SEND PAYMENT
		Pair<Transaction, Integer> result = Controller.getInstance()
				.sendPayment(account, new Account(recipient), bdAmount, bdFee);

		switch (result.getB()) {
		case Transaction.VALIDATE_OKE:

			return result.getA().toJson().toJSONString();

		case Transaction.INVALID_NAME_LENGTH:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_NAME_LENGTH);

		case Transaction.INVALID_VALUE_LENGTH:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_VALUE_LENGTH);

		case Transaction.INVALID_ADDRESS:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_RECIPIENT);

		case Transaction.NAME_ALREADY_REGISTRED:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_NAME_ALREADY_EXISTS);

		case Transaction.NEGATIVE_FEE:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_FEE);

		case Transaction.NO_BALANCE:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_NO_BALANCE);

		default:

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_UNKNOWN);
		}
	}

	public static void askAPICallAllowed(final String messageToDisplay, HttpServletRequest request)
			throws WebApplicationException {
		// CHECK API CALL ALLOWED
		try {

			if (Controller.getInstance().checkAPICallAllowed(messageToDisplay, request) == JOptionPane.NO_OPTION) {
				throw ApiErrorFactory
						.getInstance()
						.createError(
								ApiErrorFactory.ERROR_WALLET_API_CALL_FORBIDDEN_BY_USER);
			}
		} catch (Exception e) {
			if (e instanceof WebApplicationException) {
				throw (WebApplicationException) e;
			}
			e.printStackTrace();
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_UNKNOWN);
		}

	}

}
