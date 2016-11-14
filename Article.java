import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Representation of a Mashable article. */
public class Article {
	/** 
	 * Collection of all 58 possible attributes an article can have. 
	 * Note: MUST be initialized by the file parser in main!
	 */
	public static final List<String> ALL_ATTRIBUTES = new ArrayList<>();
	
	// We will not consider the URL an attribute; instead, we will consider it an ID
	private final String URL; 
	
	// If an attribute is in the set, then it has the value "true"; otherwise, we
	// assume the article does not have the attribute. This lets us standardize 
	// the collection of possible attributes across articles
	private final Set<String> attributes;
	
	public Article(String URL, Set<String> attributes) {
		if (URL == null || attributes == null) throw new NullPointerException();
		this.URL = URL;
		this.attributes = attributes;
	}
	
	public String getURL() {
		return URL;
	}
	
	public boolean hasAttribute(String attribute) {
		return attributes.contains(attribute);
	}
	
	@Override
	public String toString() {
		return this.URL;
	}
	
	@Override
	public int hashCode() {
		return URL.hashCode();
	}
	
	/** Two articles are equal iff they have the same URL. */
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Article)) return false;
		Article other = (Article) o;
		return this.URL.equals(other.URL);
	}
	
}