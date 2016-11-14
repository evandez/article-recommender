import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/** Driver class for the Mashable Article Recommender System. */
public class Main {
	private static final String ARTICLE_DATA_FILE = "OnlineNewsPopularity.csv";
	private static final String USER_DATA_FILE = "Users.txt";

	public static void main(String[] args) {
		// Uncomment this line to have the main function produce a file with only article URLs
		// (this is useful for distributing the articles to obtain test data - it allows us to
		// ask test participants to pick 10-15 articles that they would like/dislike):
		// writeUrlsToFile(ARTICLE_DATA_FILE, "articles.txt");
		
		// Parse files and organize data
		double[] branchingThresholds = parseBranchingThresholds(ARTICLE_DATA_FILE);
		Map<String, Article> articles = parseArticleFile(ARTICLE_DATA_FILE, branchingThresholds);
		List<User> users = parseUserFile(USER_DATA_FILE, articles);
		
		ArticleRecommender recommenderSystem = new ArticleRecommender(users, articles.values());
		
		// Run tests on recommendor
		for (User user : users) {
			Set<Article> recommendedArticles = recommenderSystem.findArticlesToRecommend(user);
			System.out.printf(
					"The system recommends the following articles to user %d:\n", user.getId());
			for (Article article : recommendedArticles) System.out.println(article);
			System.out.println();
		}
		
		// We can't exactly make assertions about the "success" of these recommendations.
		// Instead, we have to consult the participants to verify whether or not they would
		// have liked the recommended article. See final project report for results.
	}
	
	/** Parse URLs from .csv file and move them to a .txt file for distribution to test users. */
	@SuppressWarnings("unused")
	private static void writeUrlsToFile(String inFilename, String outFilename) {
		System.out.print("Writing URLs to file... ");
		List<String> URLs = new LinkedList<>();
		try (Scanner fileReader = new Scanner(new File(inFilename))) {
			fileReader.nextLine();
			String line;
			while (fileReader.hasNextLine() && !(line = fileReader.nextLine()).isEmpty())
				URLs.add(line.split(",")[0].trim());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		try (PrintWriter fileWriter = new PrintWriter(new File(outFilename))) {
			for (String URL : URLs) fileWriter.write(String.format("%s\n", URL));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("done!");
	}
	
	/** 
	 * Maps each attribute to the average double value for that article. These average values will 
	 * be used to determine whether or not an article "has" an attribute - that is, an article has 
	 * an attribute iff it's numerical value for that attribute is above average.
	 */
	private static double[] parseBranchingThresholds(String filename) {
		System.out.print("Calculating branch thresholds... ");
		double[] averageValues = null;
		try (Scanner fileReader = new Scanner(new File(filename))) {
			// First, initialize attribute names
			fileReader.next(); // skip URL
			fileReader.next(); // skip timedelta
			averageValues = new double[fileReader.nextLine().split(",").length];
			
			double articleCount = 0;
			String line;
			while (fileReader.hasNextLine() && !(line = fileReader.nextLine()).isEmpty()) {
				try (Scanner lineReader = new Scanner(line)) {
					lineReader.next(); // skip URL
					lineReader.next(); // skip timedelta
					for (int i = 0; i < averageValues.length; i++)
						averageValues[i] += Double.parseDouble(lineReader.next().replace(",", ""));
					articleCount++;
				}
			}
			
			for (int i = 0; i < averageValues.length; i++) averageValues[i] /= articleCount;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("done!");
		return averageValues;
	}
	
	private static Map<String, Article> parseArticleFile(
			String filename, double[] branchingThresholds) {
		System.out.print("Parsing article file... ");
		Map<String, Article> articles = new HashMap<>();
		try (Scanner fileReader = new Scanner(new File(filename))) {
			// First, initialize the list of attributes
			fileReader.next(); // skip URL
			fileReader.next(); // skip timedelta
			String[] attributes = fileReader.nextLine().trim().split(",");
			for (int i = 0; i < attributes.length; i++) {
				attributes[i] = attributes[i].trim();
				Article.ALL_ATTRIBUTES.add(attributes[i]);
			}
			
			// Next, read data
			String articleLine;
			while (fileReader.hasNextLine() && !(articleLine = fileReader.nextLine()).isEmpty()) {
				String URL;
				Set<String> attributesOfArticle = new HashSet<>();
				try (Scanner lineReader = new Scanner(articleLine)) {
					URL = lineReader.next().replace(",", "").trim();
					lineReader.next(); // skip timedelta
					for (int i = 0; i < branchingThresholds.length; i++) {
						double attributeValue = Double.parseDouble(lineReader.next().replace(",", ""));
						if (attributeValue >= branchingThresholds[i]) attributesOfArticle.add(attributes[i]);
					}
				} 
				articles.put(URL, new Article(URL, attributesOfArticle));
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("done!");
		return articles; 
	}
	
	private static List<User> parseUserFile(String filename, Map<String, Article> articles) { 
		System.out.print("Parsing user file... ");
		List<User> users = new LinkedList<>();
		try (Scanner fileReader = new Scanner(new File(filename))) {
			String line;
			while (fileReader.hasNextLine() && !(line = fileReader.nextLine().trim()).isEmpty()) {
				if (line.charAt(0) == '#') continue;
				try (Scanner lineReader = new Scanner(line)) {
					User user = new User(Integer.parseInt(lineReader.next()));
					int numLikedArticles = Integer.parseInt(lineReader.next());
					int numDislikedArticles = Integer.parseInt(lineReader.next());
					for (int i = 0; i < numLikedArticles; i++)
						user.likeArticle(articles.get(fileReader.nextLine().trim()));
					for (int i = 0; i < numDislikedArticles; i++)
						user.dislikeArticle(articles.get(fileReader.nextLine().trim()));
					users.add(user);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("done!");
		return users; 
	}
}
