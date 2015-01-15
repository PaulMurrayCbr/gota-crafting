import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class Resource {
	String name;
	String qual;
	String pic;
	String link;
	int qty = 1;

	public String toString() {
		return "{name:\"" + name + "\", qty: " + qty + "}";
	}

	public void dump(PrintStream out) {
		out.print("{");
		Item.pp("name", name, out);
		Item.pp("qty", qty, out);
		Item.pp("qual", qual, out);
		Item.pp("pic", pic, out);
		Item.pp("link", link, out);
		out.print("z:null}");
	}

}

class Item {
	String name;
	int silver;
	String time;
	String building;
	String upgrade;
	String quality;
	String type;
	String pic;
	String special;

	int battle = 0;
	int trade = 0;
	int intrigue = 0;

	List<Resource> resource = new ArrayList<Resource>();

	Item() {
		resource.add(null); // item zero
	}

	public String toString() {
		return "{name:\"" + name + "\", resource: " + resource + "}";
	}

	public void dump(PrintStream out) {
		out.print("{");
		Item.pp("name", name, out);
		Item.pp("silver", silver, out);
		Item.pp("time", time, out);
		Item.pp("building", building, out);
		Item.pp("upgrade", upgrade, out);
		Item.pp("quality", quality, out);
		Item.pp("type", type, out);
		Item.pp("pic", pic, out);
		Item.pp("special", special, out);
		out.println();
		out.print("resource:[");
		boolean first = true;
		for (Resource r : resource) {
			if (r == null)
				continue;
			if (first)
				first = false;
			else
				out.print(",");
			r.dump(out);
		}
		out.print("]}");
	}

	public static void pp(String k, String v, PrintStream out) {
		if (v != null) {
			out.print(k + ": " + "\"" + v + "\",");
		}
	}

	public static void pp(String k, int v, PrintStream out) {
		out.print(k + ": " + +v + ",");
	}
}

public class SuckArticle {

	// get list of articles
	// http://gotascent.wikia.com/api/v1/Articles/List?category=Craftable_World_Event_Armor
	// http://gotascent.wikia.com/api/v1/Articles/List?category=Craftable_Armor&limit=999

	// http://gotascent.wikia.com/wiki/Axe?action=edit

	public static void main(String[] av) throws Throwable {
		Set<String> keys = new TreeSet<String>();

		keys.add("Axe");

		Collection<Item> items = new ArrayList<Item>();

		String[] zzcategories = { "zzz",
				// "Craftable_Armor",
				"Craftable Seal" };

		String[] categories = { "zzz",//
				"Craftable Armor‏‎", //
				"Craftable Boon‏‎", //
				"Craftable Common Armor‏‎", //
				"Craftable Common Companion‏‎", //
				"Craftable Common Resource‏‎", //
				"Craftable Common Weapon‏‎", //
				"Craftable Companion‏‎", //
				"Craftable Consumable‏‎", //
				"Craftable Equipment‏‎", //
				"Craftable Fealty Seal‏‎", //
				"Craftable Item‏‎", //
				"Craftable Legendary Armor‏‎", //
				"Craftable Legendary Companion‏‎", //
				"Craftable Legendary Weapon‏‎", //
				"Craftable Limited Item‏‎", //
				"Craftable Peerless Armor‏‎", //
				"Craftable Peerless Companion‏‎", //
				"Craftable Peerless Weapon‏‎", //
				"Craftable Phase Item‏‎", //
				"Craftable Rare Armor‏‎", //
				"Craftable Rare Companion‏‎", //
				"Craftable Rare Resource‏‎", //
				"Craftable Rare Weapon‏‎", //
				"Craftable Resource‏‎", //
				"Craftable Seal‏‎", //
				"Craftable Uncommon Armor‏‎", //
				"Craftable Uncommon Companion‏‎", //
				"Craftable Uncommon Seal‏‎", //
				"Craftable Uncommon Weapon‏‎", //
				"Craftable Weapon‏‎", //
				"Craftable World Event: Braavosi‏‎", //
				"Craftable World Event: Iron Bank‏‎", //
				"Craftable World Event: Long Night‏‎", //
				"Craftable World Event: Valyrian‏‎", //
				"Craftable World Event Armor‏‎", //
				"Craftable World Event Armor: Rarity‏‎", //
				"Craftable World Event Companion‏‎", //
				"Craftable World Event Companion: Rarity‏‎", //
				"Craftable World Event Equipment‏‎", //
				"Craftable World Event Item‏‎", //
				"Craftable World Event Legendary Armor‏‎", //
				"Craftable World Event Legendary Companion‏‎", //
				"Craftable World Event Legendary Weapon‏‎", //
				"Craftable World Event Peerless Armor‏‎", //
				"Craftable World Event Peerless Companion‏‎", //
				"Craftable World Event Peerless Weapon‏‎", //
				"Craftable World Event Rare Armor‏‎", //
				"Craftable World Event Rare Companion‏‎", //
				"Craftable World Event Rare Weapon‏‎", //
				"Craftable World Event Uncommon Armor‏‎", //
				"Craftable World Event Uncommon Companion‏‎", //
				"Craftable World Event Uncommon Weapon‏‎", //
				"Craftable World Event Weapon‏‎", //
		};

		for (String category : categories) {
			System.err.println("reading category " + category);
			try {
				URL u = new URL("http://gotascent.wikia.com/api/v1/Articles/List?category="
						+ URLEncoder.encode(category, "UTF-8") + "&limit=999");
				InputStream is = u.openStream();
				Reader r = new InputStreamReader(is, "UTF-8");
				StringBuffer buf = new StringBuffer();
				int ch;
				while ((ch = r.read()) != -1) {
					buf.append((char) ch);
				}
				r.close();
				is.close();

				Map json = (Map) JSONValue.parse(buf.toString());
				for (Map item : (List<Map>) json.get("items")) {
					keys.add(item.get("title").toString());
				}
			} catch (Exception ex) {
				System.err.println("cant read category " + category + ": " + ex);
			}
		}

		Pattern p = Pattern.compile("\\|([^= ]*) *= *([^}]*)(\\}\\})?");

		for (String key : keys) {
			System.err.println("reading item " + key);
			try {
				Document doc = Jsoup.connect("http://gotascent.wikia.com/wiki/" + key + "?action=edit").get();
				Elements textarea = doc.select("textarea");
				for (Element e : textarea) {
					String urlencoded = e.text();
					if (urlencoded.indexOf("data-rte-meta=\"") == -1)
						continue;
					urlencoded = urlencoded.substring(urlencoded.indexOf("data-rte-meta=\"")
							+ "data-rte-meta=\"".length());
					urlencoded = urlencoded.substring(0, urlencoded.indexOf("\""));

					String decoded = URLDecoder.decode(urlencoded, "UTF-8");

					// move decoded stuff into JSON parser, get 'wikitxt' field

					Map json = (Map) JSONValue.parse(decoded);

					if (json.get("wikitext") == null)
						continue;

					// now look for lines begining with "{{Acquisition Item" and
					// ending with "}}"
					// the gear in between those is the money

					StringReader sr = new StringReader(json.get("wikitext").toString());

					BufferedReader rr = new BufferedReader(sr);

					cantFind: {
						String ln = null;
						while ((ln = rr.readLine()) != null) {
							if (ln.indexOf("{{Acquisition Item") != -1)
								break;
						}
						if (ln == null)
							break cantFind;

						Item i = new Item();
						items.add(i);
						i.name = key;

						while ((ln = rr.readLine()) != null) {
							try {

								Matcher m = p.matcher(ln);

								if (m.matches()) {
									String k = m.group(1);
									String v = m.group(2);

									k = k.trim();
									v = v.trim();

									if (false) {
									} else if (k.equals("silver")) {
										i.silver = Integer.parseInt(v);
									} else if (k.equals("battle")) {
										i.battle = Integer.parseInt(v);
									} else if (k.equals("trade")) {
										i.trade = Integer.parseInt(v);
									} else if (k.equals("intrigue")) {
										i.intrigue = Integer.parseInt(v);
									} else if (k.equals("name")) {
										i.name = v;
									} else if (k.equals("time")) {
										i.time = v;
									} else if (k.equals("pic")) {
										i.pic = v;
									} else if (k.equals("special")) {
										i.special = v;
									} else if (k.equals("building")) {
										i.building = v;
									} else if (k.equals("upgrade")) {
										i.upgrade = v;
									} else if (k.equals("quality")) {
										i.quality = v;
									} else if (k.equals("type")) {
										i.type = v;
									} else if (k.startsWith("resource") && k.endsWith("_qty")) {
										int n = Integer.parseInt(k.substring(8, k.length() - 4));
										while (i.resource.size() <= n) {
											i.resource.add(new Resource());
										}
										i.resource.get(n).qty = Integer.parseInt(v);
									} else if (k.startsWith("resource") && k.endsWith("_qual")) {
										int n = Integer.parseInt(k.substring(8, k.length() - 5));
										while (i.resource.size() <= n) {
											i.resource.add(new Resource());
										}
										i.resource.get(n).qual = v;
									} else if (k.startsWith("resource") && k.endsWith("_pic")) {
										int n = Integer.parseInt(k.substring(8, k.length() - 4));
										while (i.resource.size() <= n) {
											i.resource.add(new Resource());
										}
										i.resource.get(n).pic = v;
									} else if (k.startsWith("resource") && k.endsWith("_link")) {
										int n = Integer.parseInt(k.substring(8, k.length() - 5));
										while (i.resource.size() <= n) {
											i.resource.add(new Resource());
										}
										i.resource.get(n).link = v;
									} else if (k.startsWith("resource") && k.indexOf("_") != -1) {
										System.err.println("Not processing " + ln);
									} else if (k.startsWith("resource")) {
										int n = Integer.parseInt(k.substring(8, k.length()));
										while (i.resource.size() <= n) {
											i.resource.add(new Resource());
										}
										i.resource.get(n).name = v;
									} else {
										System.err.println("Not processing " + ln);
									}

								}

								if (ln.indexOf("}}") != -1)
									break;
							} catch (Exception ex) {
								System.err.println(ex + ": " + ln);
							}
						}
					}

				}

			} catch (Exception ex) {
				System.err.println("Failed to read " + key + ": " + ex);
			}
		}

		PrintStream out = new PrintStream(new FileOutputStream("all_items_" + System.currentTimeMillis() + ".js"));

		out.println("var all_dependencies = [");
		boolean first = true;

		for (Item i : items) {
			if (first)
				first = false;
			else
				out.println(",");
			if (i != null)
				i.dump(out);
		}
		out.println();
		out.println("];");

		out.flush();
		out.close();
		
		System.err.println("DONE!!!!");
	}
}
