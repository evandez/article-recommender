import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Representation of a Mashable user who has rated any number of articles.
 */
public class User {
	private final int id;
	// Here, the key is an article that the user has read, and the value
	// is whether or not the user liked, or "thumbs-uped" the article.
	private final Map<Article, Boolean> articlesRead;
	
	public User(int id) {
		this.id = id;
		this.articlesRead = new HashMap<>();
	}
	
	public void likeArticle(Article article) {
		if (article == null) throw new NullPointerException();
		articlesRead.put(article, Boolean.TRUE);
	}
	
	public void likeArticles(Set<Article> articles) {
		if (articles == null) throw new NullPointerException();
		for (Article article : articles) articlesRead.put(article, Boolean.TRUE);
	}
	
	public void dislikeArticle(Article article) {
		if (article == null) throw new NullPointerException();
		articlesRead.put(article, Boolean.FALSE);
	}
	
	public void dislikeArticles(Set<Article> articles) {
		if (articles == null) throw new NullPointerException();
		for (Article article : articles) articlesRead.put(article, Boolean.FALSE);
	}
	
	public int getId() {
		return id;
	}
	
	public Map<Article, Boolean> getArticlePreferences() {
		return articlesRead;
	}
	
	@Override
	public String toString() {
		return String.format("User %d", id);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[]{ id });
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof User)) return false;
		User other = (User) o;
		return this.id == other.id;
	}
}
