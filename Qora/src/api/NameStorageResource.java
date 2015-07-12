package api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.account.PrivateKeyAccount;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.transaction.Transaction;
import utils.APIUtils;
import utils.Pair;
import controller.Controller;
import database.DBSet;

@Path("namestorage")
@Produces(MediaType.APPLICATION_JSON)
public class NameStorageResource {

	@Context
	HttpServletRequest request;

	@SuppressWarnings("unchecked")
	@POST
	@Path("/update/{creator}/{name}")
	public String updateEntry(String x, @PathParam("name") String name,
			@PathParam("creator") String creator) {
		try {

			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);

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

			// CHECK ADDRESS
			if (!Crypto.getInstance().isValidAddress(creator)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}

			// CHECK ACCOUNT IN WALLET
			if (Controller.getInstance().getAccountByAddress(creator) == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
			}

			Name nameObj = DBSet.getInstance().getNameMap().get(name);
			
			if(nameObj == null)
			{
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_NAME_NOT_REGISTERED);
			}

			// Name is not owned by creator!
			if (!nameObj.getOwner().getAddress().equals(creator)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_NAME_NOT_OWNER);
			}

			// GET ACCOUNT
			PrivateKeyAccount account = Controller.getInstance()
					.getPrivateKeyAccountByAddress(creator);
			if (account == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}


			jsonObject.put("name", name);
			String jsonString = jsonObject.toJSONString();
			APIUtils.askAPICallAllowed("POST namestorage/" + creator + "/" + name + "\n" + jsonString,
					request);


			// SEND PAYMENT
			byte[] bytes = jsonString.getBytes();
			Pair<Transaction, Integer> result = Controller.getInstance()
					.createArbitraryTransaction(account, 10,
							bytes, Controller.getInstance().calcRecommendedFeeForArbitraryTransaction(bytes).getA());

			return ArbitraryTransactionsResource
					.checkArbitraryTransaction(result);

		} catch (NullPointerException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		} catch (ClassCastException e) {
			// JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_JSON);
		}

	}
	
}
