package api;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import controller.Controller;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.crypto.Crypto;
import qora.payment.Payment;
import qora.transaction.Transaction;
import utils.APIUtils;
import utils.Pair;

@Path("multipayment")
@Produces(MediaType.APPLICATION_JSON)
public class MultiPaymentResource 
{
	@Context
	HttpServletRequest request;
	
	@POST
	@Consumes(MediaType.WILDCARD)
	public String createPayment(String x)
	{
		try
		{
			APIUtils.askAPICallAllowed("POST multipayment\n" + x, request );

			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String sender = (String) jsonObject.get("sender");
			String asset = (String) jsonObject.get("asset");
			
			Asset defaultAsset;
			if(asset != null) {
				try {
					defaultAsset = Controller.getInstance().getAsset(new Long(asset));
				} catch (Exception e) {
					throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ASSET_ID);
				}
			} else {
				defaultAsset = Controller.getInstance().getAsset(0L);
			}
			
			List<Payment> payments = jsonPaymentParser((JSONArray)jsonObject.get("payments"), defaultAsset);
			
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
			
			Pair<BigDecimal, Integer> recommendedFee = Controller.getInstance().calcRecommendedFeeForMultiPayment(payments);
			
			BigDecimal bdFee = recommendedFee.getA().setScale(0, BigDecimal.ROUND_CEILING).setScale(8);
			
			Pair<Transaction, Integer> result = Controller.getInstance().sendMultiPayment(account, payments, bdFee);
			
			switch (result.getB()) {
			case Transaction.VALIDATE_OK:

				return result.getA().toJson().toJSONString();

			case Transaction.INVALID_ADDRESS:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_RECIPIENT);

			case Transaction.NEGATIVE_FEE:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_FEE);

			case Transaction.FEE_LESS_REQUIRED:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_FEE_LESS_REQUIRED);

			case Transaction.NO_BALANCE:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_NO_BALANCE);

			case Transaction.INVALID_AMOUNT:
				throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_AMOUNT);
				
			case Transaction.NEGATIVE_AMOUNT:
				throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_AMOUNT);
				
			default:

				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_UNKNOWN);
			}
		}
		catch(NullPointerException e)
		{
			//JSON EXCEPTION
			//e.printStackTrace();
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
		catch(ClassCastException e)
		{
			//JSON EXCEPTION
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}

	public List<Payment> jsonPaymentParser(JSONArray jsonArray)
	{
		return jsonPaymentParser(jsonArray, Controller.getInstance().getAsset(0L));
	}
	
	public List<Payment> jsonPaymentParser(JSONArray jsonArray, Asset defaultAsset)
	{
		List<Payment> payments = new ArrayList<Payment>();
		
		for(int i=0; i<jsonArray.size(); i++)
		{
			JSONObject jsonPayment = (JSONObject) jsonArray.get(i);
			
			String recipient = jsonPayment.get("recipient").toString();
			if (!Crypto.getInstance().isValidAddress(recipient)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_RECIPIENT);
			}
			Account paymentRecipient = new Account(jsonPayment.get("recipient").toString());
			
			Asset paymentAsset = defaultAsset;
			if(jsonPayment.containsKey("asset")) {
				try {
					paymentAsset = Controller.getInstance().getAsset(new Long(jsonPayment.get("asset").toString()));
				} catch (Exception e) {
					throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ASSET_ID);
				}
			}
			
			BigDecimal bdAmount;
			try 
			{
				bdAmount = new BigDecimal(jsonPayment.get("amount").toString());
				bdAmount = bdAmount.setScale(8);
			} catch (Exception e) {
				throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_INVALID_AMOUNT);
			}
			
			Payment payment = new Payment(paymentRecipient, paymentAsset.getKey(), bdAmount);
			
			payments.add(payment);
		}	
		
		return payments;
	}
}
