package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.block.Block;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.Transaction;
import qora.web.blog.BlogEntry;
import api.BlogPostResource;
import controller.Controller;
import database.DBSet;
import database.NameMap;

public class BlogUtils {

	/**
	 * 
	 * @return triplet of name, title, description of all enabled blogs.
	 */
	public static List<Triplet<String, String, String>> getEnabledBlogs(
			String searchvalueOpt) {

		NameMap nameMap = DBSet.getInstance().getNameMap();
		List<Triplet<String, String, String>> results = new ArrayList<>();
		Set<String> names = nameMap.getKeys();

		for (String name : names) {
			String value = nameMap.get(name).getValue();

			value = GZIP.webDecompress(value);

			if (!value.startsWith("{")) {
				continue;
			}

			JSONObject jsonObject = null;
			try {
				jsonObject = (JSONObject) JSONValue.parse(value);
			} catch (Exception e) {
				// no valid json
			}

			if (jsonObject != null
					&& jsonObject.containsKey(BlogPostResource.BLOGENABLE_KEY)) {

				String title = (String) jsonObject
						.get(BlogPostResource.BLOGTITLE_KEY);
				String description = (String) jsonObject
						.get(BlogPostResource.BLOGDESCRIPTION_KEY);
				if (searchvalueOpt != null) {
					searchvalueOpt = searchvalueOpt.toLowerCase();
					if (name.toLowerCase().contains(searchvalueOpt)
							|| (title != null && title.toLowerCase().contains(
									searchvalueOpt))
							|| (description != null)
							&& description.toLowerCase().contains(
									searchvalueOpt)) {
						results.add(addTriplet(name, title, description));
					}
					continue;
				}
				results.add(addTriplet(name, title, description));
			}

		}

		return results;
	}

	private static Triplet<String, String, String> addTriplet(String name,
			String title, String description) {
		return new Triplet<String, String, String>(name, title == null ? ""
				: title, description == null ? "" : description);
	}

	public static List<BlogEntry> getBlogPosts(String blogOpt) {
		int height = Controller.getInstance().getHeight();
		int floor = 0;
		if (height > 10000) {
			floor = height - 10000;
		}
		Pair<Block, List<Transaction>> resultlist = Controller.getInstance()
				.scanTransactions(
						Controller.getInstance().getBlockByHeight(floor),
						10000, 1000, 10, 777, null);
		List<BlogEntry> results = new ArrayList<>();
		List<Transaction> b = resultlist.getB();

		for (Transaction transaction : b) {
			if (transaction instanceof ArbitraryTransaction) {

				byte[] data = ((ArbitraryTransaction) transaction).getData();
				String string = new String(data);

				JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
				if (jsonObject != null) {
					// MAINBLOG OR CUSTOM BLOG?
					if ((blogOpt == null && !jsonObject
							.containsKey(BlogPostResource.BLOGNAME_KEY))
							|| (jsonObject
									.containsKey(BlogPostResource.BLOGNAME_KEY) && jsonObject
									.get(BlogPostResource.BLOGNAME_KEY).equals(
											blogOpt))) {

						String title = (String) jsonObject
								.get(BlogPostResource.TITLE_KEY);
						String post = (String) jsonObject
								.get(BlogPostResource.POST_KEY);
						String nameOpt = (String) jsonObject
								.get(BlogPostResource.AUTHOR);

						if (StringUtil.isNotBlank(post)) {
							results.add(new BlogEntry(title, post, nameOpt,
									transaction.getTimestamp(), transaction
											.getCreator().getAddress()));

						}
					}
				}

			}
		}

		Collections.reverse(results);

		return results;

	}

}
