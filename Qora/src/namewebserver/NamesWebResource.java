package namewebserver;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import qora.account.Account;
import qora.block.Block;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.transaction.Transaction;
import utils.AccountBalanceComparator;
import utils.BlogUtils;
import utils.GZIP;
import utils.JSonWriter;
import utils.NameUtils;
import utils.Qorakeys;
import utils.NameUtils.NameResult;
import utils.Pair;
import api.ATResource;
import api.AddressesResource;
import api.ApiErrorFactory;
import api.ArbitraryTransactionsResource;
import api.BlocksResource;
import api.NameSalesResource;
import api.NamesResource;
import api.TransactionsResource;
import controller.Controller;
import database.DBSet;
import database.NameMap;

@Path("/")
public class NamesWebResource {
	@Context
	HttpServletRequest request;

	@GET
	public Response Default() {
		return handleDefault();
	}

	@SuppressWarnings("unchecked")
	public Response handleDefault() {
		try {

			String searchValue = request.getParameter("search");
			String kind = request.getParameter("kind");
			String content = readFile("web/index.html", StandardCharsets.UTF_8);
			content = replaceWarning(content);

			if (searchValue == null) {

				return Response.ok(content, "text/html; charset=utf-8").build();
			}

			else if (searchValue != null) {
				List<String> namesByValue = NameUtils.getNamesByValue(
						searchValue, true);
				String searchlist = readFile("web/index.mini.html", StandardCharsets.UTF_8);
				String results = "<br>";
				String nameresults = "<br>";
				for (String name : namesByValue) {
					results += "<a href=/" + name.replaceAll(" ", "%20") + ">"
							+ name + "</a><br>";
					
					NameMap nameMap = DBSet.getInstance().getNameMap();
					if (nameMap.contains(name)) {
						String value = nameMap.get(name).getValue();

						JSONObject resultJson = null;
						try {

							// WEBPAGE GZIP DECOMPRESSOR
							value = GZIP.webDecompress(value);

							JSONObject jsonObject = (JSONObject) JSONValue.parse(value);
							if (jsonObject != null) {
								// Looks like valid jSon
								resultJson = jsonObject;

							}

						} catch (Exception e) {
							// no json probably
						}

						// BAD FORMAT --> NO KEYVALUE
						if (resultJson == null) {
							resultJson = new JSONObject();
							resultJson.put(Qorakeys.DEFAULT.toString(), value);

							value = jsonToFineSting(resultJson.toJSONString());
						}
						nameresults = value;
						}	
					
				}

				content = searchlist.replace("<results></results>", results).replace("<data></data>", searchValue).replace("<output></output>", nameresults);

			}

			if (kind != null) {
				if (kind != "1") {
					content = content.replace("<option value=\"2\">",
							"<option selected=\"true\" value=\"2\">");
				}

			}
			
			return Response.ok(content, "text/html; charset=utf-8").build();
		} catch (IOException e) {
			e.printStackTrace();
			return error404(request);
		}
	}

	private String replaceWarning(String content) {
		content = content.replace("<warning></warning>", getWarning(request));
		return content;
	}

	@Path("index.html")
	@GET
	public Response handleIndex() {
		return handleDefault();
	}

	@Path("favicon.ico")
	@GET
	public Response favicon() {
		File file = new File("web/favicon.ico");

		if (file.exists()) {
			return Response.ok(file, "image/vnd.microsoft.icon").build();
		} else {
			return error404(request);
		}
	}

	@Path("index/qora.png")
	@GET
	public Response qorapng() {
		File file = new File("web/qora.png");

		if (file.exists()) {
			return Response.ok(file, "image/png").build();
		} else {
			return error404(request);
		}
	}

	@Path("index/qora-user.png")
	@GET
	public Response qorauserpng() {
		File file = new File("web/qora-user.png");

		if (file.exists()) {
			return Response.ok(file, "image/png").build();
		} else {
			return error404(request);
		}
	}

	@Path("webdirectory.html")
	@GET
	public Response websites() {

		try {
			String content = readFile("web/webdirectory.html",
					StandardCharsets.UTF_8);

			content = replaceWarning(content);

			List<String> namesContainingWebsites = NameUtils
					.getNamesContainingWebsites(true);
			String linksAsHtml = "";
			for (String name : namesContainingWebsites) {
				linksAsHtml += "<a href=/" + name.replaceAll(" ", "%20") + ">"
						+ name + "</a><br>";
			}

			content = content.replace("!linkstoreplace!", linksAsHtml);

			return Response.ok(content, "text/html; charset=utf-8").build();
		} catch (IOException e) {
			e.printStackTrace();
			return error404(request);
		}
	}

	@SuppressWarnings("unchecked")
	@Path("postblog.html")
	@GET
	public Response postBlog() {

		try {
			String content = readFile("web/postblog.html",
					StandardCharsets.UTF_8);

			String title = request.getParameter("title");
			String creator = request.getParameter("creator");
			String contentparam = request.getParameter("content");
			String fee = request.getParameter("fee");

			List<Account> accounts = new ArrayList<Account>(Controller
					.getInstance().getAccounts());

			Collections.sort(accounts, new AccountBalanceComparator());
			Collections.reverse(accounts);

			String accountStrings = "";
			for (Account account : accounts) {
				accountStrings += "<option value=" + account.getAddress() + ">"
						+ account + "</option>";
			}

			content = content.replaceAll("<option></option>", accountStrings);

			if (StringUtil.isNotBlank(creator)
					&& StringUtil.isNotBlank(contentparam)
					&& StringUtil.isNotBlank(fee)) {
				JSONObject json = new JSONObject();
				json.put("creator", creator);
				json.put("service", 777);
				json.put("fee", fee);

				String params = "{\"title\":\"" + title + "\"" + ",\"post\":\""
						+ contentparam + "\"}";

				String data = Base58.encode(params.getBytes());
				json.put("data", data);

				// TODO maybe this is not the best way
				try {
					String result = new ArbitraryTransactionsResource()
							.createArbitraryTransaction(json.toJSONString());

					content = content
							.replaceAll(
									"<font></font>",
									"<div class=\"alert alert-success\" role=\"alert\">Your post was successful<br>"
											+ result + "</div>");

				} catch (WebApplicationException e) {
					content = content
							.replaceAll(
									"<font></font>",
									"<div class=\"alert alert-danger\" role=\"alert\">Your post was NOT successful<br>"
											+ e.getResponse().getEntity()
											+ "</div>");

				}

			}

			return Response.ok(content, "text/html; charset=utf-8").build();
		} catch (IOException e) {
			e.printStackTrace();
			return error404(request);
		}
	}

	@Path("blog.html")
	@GET
	public Response getBlog() {

		try {
			String content = readFile("web/blog.html", StandardCharsets.UTF_8);

			content = replaceWarning(content);

			List<Pair<String, String>> blogPosts = BlogUtils.getBlogPosts();

			String results = "<br>";

			String entryTemplate = "<li class=\"media\"><a class=\"pull-left\" href=\"#\"><img class=\"media-object\" src=\"index/qora-user.png\" alt=\"Generic placeholder image\"></a><div class=\"media-body\"><h4 class=\"media-heading\">TITLE</h4><p>CONTENT</p></div></li>";
			for (Pair<String, String> pair : blogPosts) {

				String converted = entryTemplate;
				converted = converted.replaceAll("TITLE", pair.getA());
				converted = converted.replaceAll("CONTENT", pair.getB());

				results += converted;
			}

			content = content.replace("!blogposts!", results);

			return Response.ok(content, "text/html; charset=utf-8").build();
		} catch (IOException e) {
			e.printStackTrace();
			return error404(request);
		}
	}

	@Path("libs/jquery.{version}.js")
	@GET
	public Response jquery(@PathParam("version") String version) {
		File file;
		if (version.equals("1")) {
			file = new File("web/jquery-1.11.3.min.js");
		} else if (version.equals("2")) {
			file = new File("web/jquery-2.1.4.min.js");
		} else {
			file = new File("web/jquery-2.1.4.min.js");
		}

		if (file.exists()) {
			return Response.ok(file, "text/javascript; charset=utf-8").build();
		} else {
			return error404(request);
		}
	}

	@Path("libs/angular.min.{version}.js")
	@GET
	public Response angular(@PathParam("version") String version) {
		File file;
		if (version.equals("1.3")) {
			file = new File("web/angular.min.1.3.15.js");
		} else if (version.equals("1.4")) {
			file = new File("web/angular.min.1.4.0.js");
		} else {
			file = new File("web/angular.min.1.3.15.js");
		}

		if (file.exists()) {
			return Response.ok(file, "text/javascript; charset=utf-8").build();
		} else {
			return error404(request);
		}
	}

	@Path("libs/bootstrap/{version}/{folder}/{filename}")
	@GET
	public Response bootstrap(@PathParam("version") String version,
			@PathParam("folder") String folder,
			@PathParam("filename") String filename) {
		String fullname = "web/bootstrap-3.3.4-dist/";
		String type = "text/html; charset=utf-8";

		switch (folder) {
		case "css": {
			fullname += "css/";
			type = "text/css";
			switch (filename) {
			case "bootstrap.css":

				fullname += "bootstrap.css";
				break;

			case "bootstrap.css.map":

				fullname += "bootstrap.css.map";
				break;

			case "bootstrap.min.css":

				fullname += "bootstrap.min.css";
				break;

			case "bootstrap-theme.css":

				fullname += "bootstrap-theme.css";
				break;

			case "bootstrap-theme.css.map":

				fullname += "bootstrap-theme.css.mapp";
				break;

			case "bootstrap-theme.min.css":

				fullname += "bootstrap-theme.min.css";
				break;
			}
			break;
		}
		case "fonts": {
			fullname += "fonts/";
			switch (filename) {
			case "glyphicons-halflings-regular.eot":

				fullname += "glyphicons-halflings-regular.eot";
				type = "application/vnd.ms-fontobject";
				break;

			case "glyphicons-halflings-regular.svg":

				fullname += "glyphicons-halflings-regular.svg";
				type = "image/svg+xml";
				break;

			case "glyphicons-halflings-regular.ttf":

				fullname += "glyphicons-halflings-regular.ttf";
				type = "application/x-font-ttf";
				break;

			case "glyphicons-halflings-regular.woff":

				fullname += "glyphicons-halflings-regular.woff";
				type = "application/font-woff";
				break;

			case "glyphicons-halflings-regular.woff2":

				fullname += "glyphicons-halflings-regular.woff2";
				type = "application/font-woff";
				break;
			}
			break;
		}
		case "js": {
			fullname += "js/";
			type = "text/javascript";
			switch (filename) {
			case "bootstrap.js":

				fullname += "bootstrap.js";
				break;

			case "bootstrap.min.js":

				fullname += "bootstrap.js";
				break;

			case "npm.js":

				fullname += "npm.js";
				break;

			}
			break;
		}
		}

		File file = new File(fullname);

		if (file.exists()) {
			return Response.ok(file, type).build();
		} else {
			return error404(request);
		}
	}

	public Response error404(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();
		pathInfo = pathInfo.substring(1, pathInfo.length());

		return Response
				.status(404)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex().replace("<data></data>", pathInfo)
						+ "<h1>name \""
						+ pathInfo
						+ "\" does not exist</h1><hr>� <a href=http://www.qora.org>Qora</a></body></html>")
				.build();
	}

	private String getWarning(HttpServletRequest request) {
		if (Controller.getInstance().isWalletUnlocked()) {
			String ipAddress = request.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = request.getRemoteAddr();
			}

			if (ipAddress.equals("127.0.0.1")) {
				return "<style type=\"text/css\">" //
						+ "#message_box {" //
						+ "position:absolute;" //
						+ "left:0; right:0; top:0;" //
						+ "z-index: 10;" //
						+ "background:#ffc;" //
						+ "padding:5px;" //
						+ "border:1px solid #CCCCCC;" //
						+ "text-align:center;" //
						+ "font-weight:bold;" //
						+ "width:auto;" //
						+ "}" //
						+ "</STYLE>" //
						+ "<div id=message_box>Wallet is unlocked!</div>" //
						+ "<div style='margin-top:50px'>";
			}
		}
		return "";
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	@SuppressWarnings("unchecked")
	@Path("namepairs:{name}")
	@GET
	public Response showNamepairs(@PathParam("name") String name) {

		NameMap nameMap = DBSet.getInstance().getNameMap();

		if (nameMap.contains(name)) {
			String value = nameMap.get(name).getValue();

			JSONObject resultJson = null;
			try {

				// WEBPAGE GZIP DECOMPRESSOR
				value = GZIP.webDecompress(value);

				JSONObject jsonObject = (JSONObject) JSONValue.parse(value);
				if (jsonObject != null) {
					// Looks like valid jSon
					resultJson = jsonObject;

				}

			} catch (Exception e) {
				// no json probably
			}

			// BAD FORMAT --> NO KEYVALUE
			if (resultJson == null) {
				resultJson = new JSONObject();
				resultJson.put(Qorakeys.DEFAULT.toString(), value);

				value = jsonToFineSting(resultJson.toJSONString());
			}

			return Response
					.status(200)
					.header("Content-Type", "text/html; charset=utf-8")
					.entity(miniIndex().replace("<data></data>",
							"namepairs:" + name).replace("<output></output>",
							value)).build();

		} else {
			return error404(request);
		}

	}

	@Path("block:{block}")
	@GET
	public Response getBlock(@PathParam("block") String strBlock) {
		String str;
		try {
			if (strBlock.matches("\\d+")) {
				str = BlocksResource.getbyHeight(Integer.valueOf(strBlock));
			} else if (strBlock.equals("last")) {
				str = BlocksResource.getLastBlock();
			} else {
				str = BlocksResource.getBlock(strBlock);
			}

			str = jsonToFineSting(str);

		} catch (Exception e1) {
			str = "<h2>Block does not exist!</h2";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex().replace("<data></data>",
						"block:" + strBlock).replace("<output></output>", str))
				.build();
	}

	@SuppressWarnings("unchecked")
	@Path("tx:{tx}")
	@GET
	public Response getTx(@PathParam("tx") String strTx) {

		String str = null;
		try {

			if (Crypto.getInstance().isValidAddress(strTx)) {
				Account account = new Account(strTx);

				Pair<Block, List<Transaction>> result = Controller
						.getInstance().scanTransactions(null, -1, -1, -1, -1,
								account);

				JSONObject json = new JSONObject();
				JSONArray transactions = new JSONArray();
				for (Transaction transaction : result.getB()) {
					transactions.add(transaction.toJson());
				}
				json.put(strTx, transactions);
				str = json.toJSONString();
			} else {
				str = TransactionsResource.getTransactionsBySignature(strTx);
			}
			str = jsonToFineSting(str);
		} catch (Exception e1) {
			str = "<h2>Transaction does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex().replace("<data></data>", "tx:" + strTx)
						.replace("<output></output>", str)).build();
	}

	@Path("balance:{address}")
	@GET
	public Response getBalance(@PathParam("address") String address) {
		String str = null;
		try {

			String addressreal = "";

			if (!Crypto.getInstance().isValidAddress(address)) {
				Pair<Account, NameResult> nameToAdress = NameUtils
						.nameToAdress(address);

				if (nameToAdress.getB() == NameResult.OK) {
					addressreal = nameToAdress.getA().getAddress();
				} else {
					throw ApiErrorFactory.getInstance().createError(
							nameToAdress.getB().getErrorCode());
				}
			} else {
				addressreal = address;
			}

			str = AddressesResource.getGeneratingBalance(addressreal);

		} catch (Exception e1) {
			str = "<h2>Address does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex().replace("<data></data>",
						"balance:" + address).replace("<output></output>",
						"{\"balance\":" + str + "}")).build();
	}

	@Path("balance:{address}:{confirmations}")
	@GET
	public Response getBalance(@PathParam("address") String address,
			@PathParam("confirmations") int confirmations) {

		String str = null;
		try {

			str = AddressesResource
					.getGeneratingBalance(address, confirmations);

		} catch (Exception e1) {
			str = "<h2>Address does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex().replace("<data></data>",
						"balance:" + address + ":" + confirmations).replace(
						"<output></output>", "{\"balance\":" + str + "}"))
				.build();
	}

	@Path("name:{name}")
	@GET
	public Response getName(@PathParam("name") String strName) {

		String str = null;
		String strNameSale = null;
		try {
			str = NamesResource.getName(strName);

			if (DBSet.getInstance().getNameExchangeMap().contains(strName)) {
				strNameSale = NameSalesResource.getNameSale(strName);
			}

			str = jsonToFineSting(str);

			if (strNameSale != null) {
				str += "\n\n" + jsonToFineSting(strNameSale);
			}

		} catch (Exception e1) {
			str = "<h2>Name does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex().replace("<data></data>", "name:" + strName)
						.replace("<output></output>", str)).build();
	}

	@Path("at:{at}")
	@GET
	public Response getAt(@PathParam("at") String strAt) {

		String str = null;
		try {
			str = ATResource.getAT(strAt);
			str = jsonToFineSting(str);
		} catch (Exception e1) {
			str = "<h2>AT does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex().replace("<data></data>", "at:" + strAt)
						.replace("<output></output>", str)).build();
	}

	@Path("atbysender:{atbysender}")
	@GET
	public Response getAtTx(@PathParam("atbysender") String strAtbySender) {

		String str = null;
		try {
			str = ATResource.getATTransactionsBySender(strAtbySender);
			str = jsonToFineSting(str);
		} catch (Exception e1) {
			str = "<h2>AT does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex().replace("<data></data>",
						"atbysender:" + strAtbySender).replace(
						"<output></output>", str)).build();
	}

	@Path("atbycreator:{atbycreator}")
	@GET
	public Response getAtbyCreator(
			@PathParam("atbycreator") String strAtbyCreator) {

		String str = null;
		try {
			str = ATResource.getATsByCreator(strAtbyCreator);
			str = jsonToFineSting(str);
		} catch (Exception e1) {
			str = "<h2>AT does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex().replace("<data></data>",
						"atbycreator:" + strAtbyCreator).replace(
						"<output></output>", str)).build();
	}

	@Path("attxbyrecipient:{attxbyrecipient}")
	@GET
	public Response getATTransactionsByRecipient(
			@PathParam("attxbyrecipient") String StrAtTxbyRecipient) {

		String str = null;
		try {
			str = ATResource.getATTransactionsByRecipient(StrAtTxbyRecipient);
			str = jsonToFineSting(str);
		} catch (Exception e1) {
			str = "<h2>AT does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex().replace("<data></data>",
						"attxbyrecipient:" + StrAtTxbyRecipient).replace(
						"<output></output>", str)).build();
	}

	public String miniIndex() {
		try {
			return readFile("web/index.mini.html", StandardCharsets.UTF_8);

		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

	public String jsonToFineSting(String str) {
		Writer writer = new JSonWriter();
		Object jsonResult = JSONValue.parse(str);

		try {
			if (jsonResult instanceof JSONArray) {
				((JSONArray) jsonResult).writeJSONString(writer);
				return writer.toString();
			}
			if (jsonResult instanceof JSONObject) {
				((JSONObject) jsonResult).writeJSONString(writer);
				return writer.toString();
			}
			writer.close();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Path("{name}")
	@GET
	public Response getNames(@PathParam("name") String nameName) {
		Name name = Controller.getInstance().getName(nameName);

		// CHECK IF NAME EXISTS
		if (name == null) {
			return error404(request);
		}

		String Value = name.getValue().toString();

		// REDIRECT
		if (Value.startsWith("http://") || Value.startsWith("https://")) {
			return Response.status(302).header("Location", Value).build();
		}

		// WEBPAGE GZIP DECOMPRESSOR
		Value = GZIP.webDecompress(Value);

		String website = null;
		try {
			JSONObject jsonObject = (JSONObject) JSONValue.parse(Value);
			if (jsonObject != null
					&& jsonObject.containsKey(Qorakeys.WEBSITE.getKeyname())) {
				website = (String) jsonObject
						.get(Qorakeys.WEBSITE.getKeyname());
			}

		} catch (Exception e) {
			// no json probably
		}

		if (website == null) {
			try {
				String content = readFile("web/websitenotfound.html",
						StandardCharsets.UTF_8);
				content = content
						.replaceAll(
								"!websitenotfoundcontent!",
								"<br><font color=\"red\">If this is your name, please do a name update for the name <a href=/name:"
										+ nameName.replaceAll(" ", "%20")
										+ ">"
										+ nameName
										+ "</a> and add the key \"website\" with webcontent as value to make this work.</font><br>You can see the current value <a href=/name:"
										+ nameName.replaceAll(" ", "%20")
										+ ">here</a>");

				Value = injectValues(Value);

				return Response.ok(content, "text/html; charset=utf-8").build();
			} catch (IOException e) {
				return error404(request);
			}

		}

		website = injectValues(website);

		// SHOW WEB-PAGE
		return Response.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(getWarning(request) + website).build();
	}

	public String injectValues(String value) {
		// PROCESSING TAG INJ
		
		Pattern pattern = Pattern.compile("(?i)(<inj.*>(.*?)</inj>)");
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			Document doc = Jsoup.parse(matcher.group(1));
			Elements inj = doc.select("inj");
			Element element = inj.get(0);
			
			
			NameMap nameMap = DBSet.getInstance().getNameMap();
			String name = matcher.group(2);
			if(nameMap.contains(name))
			{
				
				Name nameinj = nameMap.get(name);
				String result = GZIP.webDecompress(nameinj.getValue().toString());
				if(element.hasAttr("key"))
				{
					String key = element.attr("key");
					try {
						
						JSONObject jsonObject = (JSONObject) JSONValue.parse(result);
						if (jsonObject != null) {
							// Looks like valid jSon
							if(jsonObject.containsKey(key))
							{
								result = (String) jsonObject.get(key);
							}
							
						}
						
					} catch (Exception e) {
						// no json probably
					}
				}
					value = value.replace(matcher.group(),result);
			}
			
		}
		return value;
	}
}
