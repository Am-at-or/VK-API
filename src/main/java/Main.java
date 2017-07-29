import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;

public class Main {

	private static final int APP_ID = 5956550;
	private static final String CLIENT_SECRET = "6PMix22Mnfp4rbkn5JZW";
	private static final String REDIRECT_URI = "http://oauth.vk.com/authorize?";
	private static final String code = "0c30d6ce07ebce7c4b";
	private static final Pattern REG = Pattern
			.compile("([0]{1}[0-9]{9})|([3]{1}[8]{1}[0]{1}[0-9]{9})|([8]{1}[0]{1}[0-9]{9})");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
			.create();
	
	

	public static void main(String[] args) throws ApiException,
			ClientException, InterruptedException, ParseException {
//		Map<String, HashSet<String>> numbers = restoreMap("D:\\Numbers.map");
//		List<String> list = new ArrayList<String>();
//		Iterator<Map.Entry<String, HashSet<String>>> itMap = numbers.entrySet()
//				.iterator();
//		while (itMap.hasNext()) {
//			Entry<String, HashSet<String>> entry = itMap.next();
//			list.add(entry.getKey());
//		}
//		saveUser(getUser(list, numbers), "D:\\NumbersUser.map");


		// System.out.println(numbers.size());
		// // save(numbers, "D:\\Numbers.map");
		// write("D:\\Numbers2.txt", numbers.toString());

		// String s = "https://oauth.vk.com/authorize?client_id=" + APP_ID
		// + "&display=page&redirect_uri=" + REDIRECT_URI
		// + "&scope=friends&response_type=code&v=5.63";
		// TransportClient transportClient = HttpTransportClient.getInstance();
		// VkApiClient vk = new VkApiClient(transportClient);
		//
		// UserAuthResponse authResponse = vk
		// .oauth()
		// .userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, REDIRECT_URI,
		// code).execute();
		// UserActor actor = new UserActor(authResponse.getUserId(),
		// authResponse.getAccessToken());
		// System.out.println(actor.toString() + "\n");

	}

	public static List<Integer> getPosts() {
		long startTime = System.currentTimeMillis();
		List<Integer> listPostId = new ArrayList<>();
		JsonParser parserPostCount = new JsonParser();
		JsonObject objectPostCount = parserPostCount
				.parse(getJSON("https://api.vk.com/method/wall.get?owner_id=-8020120&count=1"))
				.getAsJsonObject();
		JsonArray responsePostCount = objectPostCount
				.getAsJsonArray("response");
		JsonArray arrayPostCount = responsePostCount.getAsJsonArray();
		int postAllCount = arrayPostCount.get(0).getAsInt();
		int postCount = 100;
		int postOffset = 100;
		for (int i = 0; i < postAllCount + 10; i += postOffset) {
			try {
				JsonParser parserPost = new JsonParser();
				JsonObject objectPost = parserPost
						.parse(getJSON("https://api.vk.com/method/wall.get?owner_id=-8020120&count="
								+ postCount + "&offset=" + i))
						.getAsJsonObject();
				JsonArray responsePost = objectPost.getAsJsonArray("response");
				JsonArray arrayPostItems = responsePost.getAsJsonArray();
				for (int j = 1; j < arrayPostItems.size(); j++) {
					int postId = arrayPostItems.get(j).getAsJsonObject()
							.get("id").getAsInt();
					if (!listPostId.contains(postId))
						listPostId.add(postId);
					else
						i++;
				}
			} catch (NullPointerException e) {
				i -= postOffset;
			}
		}
		long timeSpent = System.currentTimeMillis() - startTime;
		System.out.println("Вибрано " + listPostId.size()
				+ " постів для парсингу! Час: " + timeSpent + " ms");
		return listPostId;

	}

	public static Map<String, List<String>> getComments(List<Integer> list,
			Map<String, List<String>> mapRestore) {
		long startTime = System.currentTimeMillis();
		int commentCount = 100;
		int commentOffset = 100;
		for (Integer postId : list) {
			JsonParser parserCommentCount = new JsonParser();
			JsonObject objectCommentCount = parserCommentCount
					.parse(getJSON("https://api.vk.com/method/wall.getComments?owner_id=-8020120&post_id="
							+ postId + "&count=1&v=5.63")).getAsJsonObject();
			int commentAllCount = objectCommentCount
					.getAsJsonObject("response").get("count").getAsInt();
			for (int i = 0; i < commentAllCount + 10; i += commentOffset) {
				try {
					JsonParser parserComment = new JsonParser();
					JsonObject mainObject = parserComment
							.parse(getJSON("https://api.vk.com/method/wall.getComments?owner_id=-8020120&post_id="
									+ postId
									+ "&offset="
									+ i
									+ "&count="
									+ commentCount + "&extended=1&v=5.63"))
							.getAsJsonObject();
					JsonObject response = mainObject
							.getAsJsonObject("response");
					JsonObject itemsArr = response.getAsJsonObject();
					JsonArray itemsArr2 = itemsArr.getAsJsonArray("items");
					for (int k = 1; k < itemsArr2.size(); k++) {
						String id = itemsArr2.get(k).getAsJsonObject()
								.get("from_id").getAsString();
						Matcher matcher = REG.matcher(itemsArr2.get(k)
								.getAsJsonObject().get("text").getAsString()
								.replace("0⃣", "0").replace("1⃣", "1")
								.replace("2⃣", "2").replace("3⃣", "3")
								.replace("4⃣", "4").replace("5⃣", "5")
								.replace("6⃣", "6").replace("7⃣", "7")
								.replace("8⃣", "8").replace("9⃣", "9")
								.replace("🔟", "10").replace("💯", "100")
								.replace("(", "").replace(")", "")
								.replace("-", "").replace(" ", ""));
						while (matcher.find()) {
							if (!mapRestore.containsKey(id))
								mapRestore.put(id, new ArrayList<>());
							String number = matcher.group();
							if (!mapRestore.get(id).contains(number))
								mapRestore.get(id).add(number);
						}
					}
				} catch (NullPointerException e) {
					i -= commentOffset;
				}
			}
			long timeSpent = System.currentTimeMillis() - startTime;
			System.out.println("Збір коментів успішно завершено! Час: "
					+ timeSpent + " ms");
		}
		return mapRestore;
	}

	// public void getCodeForOAuth2_0FromURI(String uri) {
	// code = uri.substring(uri.indexOf("#code=", 0) + 6, uri.length());
	// }

	public static Map<User, HashSet<String>> getUser(List<String> listId,
			Map<String, HashSet<String>> map) {
		Map<User, HashSet<String>> map2 = new HashMap<>();
		int listSize = listId.size();
		for (int i = 0, n = 100; i < listSize; i += 100) {
			if (i > listSize - 100)
				n = listSize % 100;
			List<String> list = listId.subList(i, i + n);
			StringBuilder sb = new StringBuilder();
			for (String id : list) {
				sb.append(id + ",");
			}
			JsonParser parserUser = new JsonParser();
			JsonObject objectUser = parserUser
					.parse(getJSON("https://api.vk.com/api.php?oauth=1&method=users.get&user_ids="
							+ sb + "&name_case=Nom&v=5.63")).getAsJsonObject();
			for (int j = 0; j < list.size(); j++) {
				try {
					JsonObject user = objectUser.getAsJsonArray("response")
							.get(j).getAsJsonObject();
					String id = user.get("id").getAsString();
					String lastName = user.get("last_name").getAsString();
					String firstName = user.get("first_name").getAsString();
					map2.put(new User(id, lastName, firstName), map.get(id));
				} catch (IndexOutOfBoundsException e) {
					System.out.println("Помилка на елементі" + j);
				}

			}
		} 
		write("D:\\NumbersUser.txt", map2.toString());
		return map2;
	}

	public static String getJSON(String urle) {

		try {
			URL url = new URL(urle);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("GET");
			con.setRequestProperty("Content-length", "0");
			con.setConnectTimeout(30000);

			con.connect();

			int resp = con.getResponseCode();
			if (resp == 200) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						con.getInputStream()));

				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line);
					sb.append("\n");
				}
				br.close();

				return sb.toString();
			} else {
				System.out.println("Error");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void write(String fileName, String text) {
		// Определяем файл
		File file = new File(fileName);

		try {
			// проверяем, что если файл не существует то создаем его
			if (!file.exists()) {
				file.createNewFile();
			}

			// PrintWriter обеспечит возможности записи в файл
			PrintWriter out = new PrintWriter(file.getAbsoluteFile());

			try {
				// Записываем текст у файл
				out.print(text);
			} finally {
				// После чего мы должны закрыть файл
				// Иначе файл не запишется
				out.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void save(Map<String, HashSet<String>> map, String filename) {
		File file = new File(filename);
		try (OutputStream os = new FileOutputStream(file);
				ObjectOutputStream oos = new ObjectOutputStream(os)) {
			oos.writeObject(map);
			oos.flush();
		} catch (IOException e) {
		}
	}

	public static void saveUser(Map<User, HashSet<String>> map, String filename) {
		File file = new File(filename);
		try (OutputStream os = new FileOutputStream(file);
				ObjectOutputStream oos = new ObjectOutputStream(os)) {
			oos.writeObject(map);
			oos.flush();
		} catch (IOException e) {
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Integer> restoreList(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			try (InputStream is = new FileInputStream(file);
					ObjectInputStream ois = new ObjectInputStream(is)) {
				return (List<Integer>) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("Error!");
			}
		}
		return new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, HashSet<String>> restoreMap(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			try (InputStream is = new FileInputStream(file);
					ObjectInputStream ois = new ObjectInputStream(is)) {
				return (Map<String, HashSet<String>>) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("Error!");
			}
		}
		return new HashMap<>();
	}

	public static void parseCurrentWeatherJson(String resultJson) {
		try {
			// конвертируем строку с Json в JSONObject для дальнейшего его
			// парсинга
			JSONObject weatherJsonObject = (JSONObject) JSONValue
					.parseWithException(resultJson);

			// получаем название города, для которого смотрим погоду
			System.out.println("Название города: "
					+ weatherJsonObject.get("name"));

			// получаем массив элементов для поля weather
			/*
			 * ... "weather": [ { "id": 500, "main": "Rain", "description":
			 * "light rain", "icon": "10d" } ], ...
			 */
			JSONArray weatherArray = (JSONArray) weatherJsonObject
					.get("weather");
			// достаем из массива первый элемент
			JSONObject weatherData = (JSONObject) weatherArray.get(0);

			// печатаем текущую погоду в консоль
			System.out.println("Погода на данный момент: "
					+ weatherData.get("main"));
			// и описание к ней
			System.out.println("Более детальное описание погоды: "
					+ weatherData.get("description"));

		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}
	}

	public static void getComments23() {

		JsonParser parserCommentCount = new JsonParser();
		JsonObject objectCommentCount = parserCommentCount
				.parse(getJSON("https://api.vk.com/method/wall.getComments?owner_id=-8020120&post_id="
						+ "4315339" + "&count=1&v=5.63")).getAsJsonObject();
		int commentAllCount = objectCommentCount.getAsJsonObject("response")
				.get("count").getAsInt();
		for (int i = 0; i < commentAllCount + 10; i += 100) {
			try {
				JsonParser parserComment = new JsonParser();
				JsonObject mainObject = parserComment
						.parse(getJSON("https://api.vk.com/method/wall.getComments?owner_id=-8020120&post_id="
								+ "4315339"
								+ "&offset="
								+ i
								+ "&count="
								+ 100
								+ "&extended=1&v=5.63")).getAsJsonObject();
				JsonObject response = mainObject.getAsJsonObject("response");
				JsonObject itemsArr = response.getAsJsonObject();
				JsonArray itemsArr2 = itemsArr.getAsJsonArray("items");
				for (int k = 1; k < itemsArr2.size(); k++) {
					String id = itemsArr2.get(k).getAsJsonObject()
							.get("from_id").getAsString();
					Matcher matcher = REG.matcher(itemsArr2.get(k)
							.getAsJsonObject().get("text").getAsString());
					System.out
							.println(itemsArr2.get(k).getAsJsonObject()
									.get("text").getAsString()
									.replace("0⃣", "0").replace("1⃣", "1")
									.replace("2⃣", "2").replace("3⃣", "3")
									.replace("4⃣", "4").replace("5⃣", "5")
									.replace("6⃣", "6").replace("7⃣", "7")
									.replace("8⃣", "8").replace("9⃣", "9")
									.replace("(", "").replace(")", "")
									.replace("-", "").replace(" ", ""));
					System.out.println("id= " + id);
					while (matcher.find()) {
						System.out.println(matcher.group());
					}

				}
			} catch (NullPointerException e) {
				i -= 100;
			}
		}
	}

}