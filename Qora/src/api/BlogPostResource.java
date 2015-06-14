package api;

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.transaction.Transaction;
import utils.APIUtils;
import utils.GZIP;
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;
import controller.Controller;
import database.DBSet;

@Path("blogpost")
@Produces(MediaType.APPLICATION_JSON)
public class BlogPostResource {

	public static final String AUTHOR = "author";
	public static final String BLOGNAME_KEY = "blogname";
	public static final String TITLE_KEY = "title";
	public static final String POST_KEY = "post";
	public static final String BLOGENABLE_KEY = "blogenable";
	public static final String BLOGTITLE_KEY = "blogtitle";
	public static final String BLOGDESCRIPTION_KEY = "blogdescription";

	@Context
	HttpServletRequest request;

	@SuppressWarnings("unchecked")
	@POST
	@Path("/{blogname}")
	public String addBlogEntry(String x, @PathParam("blogname") String blogname) {
		try {

			// READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String fee = (String) jsonObject.get("fee");
			String creator = (String) jsonObject.get("creator");
			String title = (String) jsonObject.get("title");
			String body = (String) jsonObject.get("body");

			if (StringUtil.isBlank(body)) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_BODY_EMPTY);
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

			Pair<Account, NameResult> nameToAdress = NameUtils
					.nameToAdress(creator);
			String name = null;
			if (nameToAdress.getB() == NameResult.OK) {
				name = creator;
				creator = nameToAdress.getA().getAddress();
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

			// GET ACCOUNT
			PrivateKeyAccount account = Controller.getInstance()
					.getPrivateKeyAccountByAddress(creator);
			if (account == null) {
				throw ApiErrorFactory.getInstance().createError(
						ApiErrorFactory.ERROR_INVALID_ADDRESS);
			}

			isPostAllowed(blogname);

			APIUtils.askAPICallAllowed("POST blogpost/" + blogname + "\n" + x,
					request);

			JSONObject dataStructure = new JSONObject();

			dataStructure.put(TITLE_KEY, title);
			dataStructure.put(POST_KEY, body);

			if (blogname != null) {
				dataStructure.put(BLOGNAME_KEY, blogname);
			}

			// NAME != CREATOR -> NAME BLOGPOST
			if (name != null) {
				dataStructure.put(AUTHOR, name);
			}

			// SEND PAYMENT
			Pair<Transaction, Integer> result = Controller.getInstance()
					.createArbitraryTransaction(account, 777,
							dataStructure.toJSONString().getBytes(), bdFee);

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

	private void isPostAllowed(String blogname) {
		Name name = DBSet.getInstance().getNameMap().get(blogname);
		if (name == null) {
			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_BLOG_DISABLED);
		}
		String value = GZIP.webDecompress(name.getValue());
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) JSONValue.parse(value);
		} catch (Exception e) {
			// no valid json
		}

		if (jsonObject == null
				|| !jsonObject.containsKey(BlogPostResource.BLOGENABLE_KEY)) {

			throw ApiErrorFactory.getInstance().createError(
					ApiErrorFactory.ERROR_BLOG_DISABLED);
		}

	}

}
