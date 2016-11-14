import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** 
 * Provides an interface for determining whether an article should be recommended to a user.
 * The methods provided here are relatively trivial, but it is easy to see that this component
 * could be expanded to allow for many efficient recommendations.
 */
public class ArticleRecommender {
	private static final Random RAND = new Random();
	
	private final Map<User, DecisionTree> recommenderTrees;
	private final List<Article> articles;
	
	/** Creates and maps a decision tree with each user. */
	public ArticleRecommender(Iterable<User> users, Iterable<Article> articles) {
		if (users == null || articles == null) throw new NullPointerException();
		this.recommenderTrees = new HashMap<>();
		this.articles = new ArrayList<>();
		
		// We take an Iterable of Articles to allow for diversity of implementation.
		for (Article article : articles) this.articles.add(article); 
		
		// It is essential that the user and the decision tree share a reference to the same
		// article preference map.
		for (User user : users) 
			recommenderTrees.put(user, new DecisionTree(user.getArticlePreferences()));
	}
	
	/** 
	 * Returns a set of up to five articles that (a) the user has not already liked and (b) the 
	 * tree predicts the user will like.
	 */
	public Set<Article> findArticlesToRecommend(User user) {
		Set<Article> toRecommend = new HashSet<>();
		for (int i = 0; i < articles.size() && toRecommend.size() <= 5; i++) {
			Article article = articles.get(RAND.nextInt(articles.size()));
			if (!user.getArticlePreferences().containsKey(article)
					&& recommenderTrees.get(user).recommend(article)) toRecommend.add(article);
		}
		return toRecommend;
	}
	
	/** Returns a set of users that the given article would be recommended to. */
	public Set<User> recommendArticleToUsers(Article article) {
		if (article == null) throw new NullPointerException();
		Set<User> recommendArticleTo = new HashSet<>();
		for (User user : recommenderTrees.keySet())
			if (recommenderTrees.get(user).recommend(article))
				recommendArticleTo.add(user);
		return recommendArticleTo;
	}
	
	/** 
	 * Updates users and their associated decision trees by recording that they have liked
	 * the provided article, and then rebuilding the tree with that information. This method
	 * assumes that a given user's set of read articles will be relatively sparse in practice.
	 */
	public void usersLikedArticle(Set<User> users, Article article) {
		if (users == null || article == null) throw new NullPointerException();
		for (User user : users) {
			user.likeArticle(article);
			recommenderTrees.get(user).learnFromTrainingData();
		}
	}
	
	/**
	 * Similar to the method above, except this method records that the user disliked the article.
	 */
	public void usersDislikedArticle(Set<User> users, Article article) {
		if (users == null || article == null) throw new NullPointerException();
		for (User user : users) {
			user.dislikeArticle(article);
			recommenderTrees.get(user).learnFromTrainingData();
		}
	}
	
	/** Prints all decision trees. */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (User user : recommenderTrees.keySet()) {
			sb.append(String.format("\n%s decision tree:\n", user));
			sb.append(String.format("%s\n", recommenderTrees.get(user)));
		}
		return sb.toString();
	}
}
