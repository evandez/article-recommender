import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/** 
 * Learns a user's article preferences from training data and uses that data
 * to determine whether or not an article should be recommended to the user.
 */
public class DecisionTree {
	private static final String LIKE_TOKEN = "Like";
	private static final String DISLIKE_TOKEN = "Dislike";
	private static final String UNSURE_TOKEN = "Unsure";
	
	/** Represents a single branching attribute of the tree. */
	private static class Node {
		private String token;
		private Node left;
		private Node right;
		
		/** Construct a node with all null values. */
		public Node() {
			this.token = null;
			this.left = null;
			this.right = null;
		}
		
		/** Returns the name of the attribute for which this node branches. */
		public String getToken() {
			return token;
		}
		
		/** Returns the left node of the tree, or null if final branch has been reached. */
		public Node getLeft() {
			return left;
		}
		
		/** Returns the right node of the tree, or null if the final branch has been reached. */
		public Node getRight() {
			return right;
		}
		
		public void setToken(String token) {
			this.token = token;
		}
		
		/** Sets the left child of this node to the reference provided in the parameter. */
		public void setLeft(Node left) {
			if (left == null) throw new NullPointerException();
			this.left = left;
		}
		
		/** Sets the right child of this node to the reference provided in the parameter. */
		public void setRight(Node right) {
			if (right == null) throw new NullPointerException();
			this.right = right;
		}
		
		/** A Node is a leaf if both its children are null. */
		public boolean isLeaf() {
			return left == null && right == null;
		}
		
		@Override
		public String toString() {
			return String.format("<%s>", token);
		}
	}
	
	private Node root;
	private final Map<Article, Boolean> articlePreferences;
	
	/** Tree is initialized with a null root. Parameter is training data. */
	public DecisionTree(Map<Article, Boolean> articlePreferences) {
		this.articlePreferences = articlePreferences;
		learnFromTrainingData();
	}
	
	/**
	 * Returns true iff, after running the attributes of the given article against
	 * the decision tree, the tree predicts that the user would like the article.
	 */
	public boolean recommend(Article article) { 
		// In theory, when we have learned nothing about the user, it is 
		// better to not recommend a bad article than to potentially recommend 
		// a good article.
		if (root == null) return false;
		
		Node curr = root;
		while (!curr.isLeaf())
			if (article.hasAttribute(curr.getToken())) curr = curr.getRight();
			else curr = curr.getLeft();
		return curr.getToken().equals(LIKE_TOKEN);
	}
	
	/**
	 * Given a mapping from articles that a user has read to a boolean indicating whether or not
	 * the user liked each article, this method creates a decision tree based on the attributes 
	 * of those articles. The premise of the tree is a boolean value: the user likes the article, 
	 * or they do not.
	 * 
	 * Successive calls to this method will cause the tree to be completely rebuilt. 
	 */
	public void learnFromTrainingData() {
		root = new Node();
		recursiveBuildTree(root, new HashSet<>(articlePreferences.keySet()));
	}
	
	private void recursiveBuildTree(Node tree, Set<Article> dataset) {
		String branchAttribute = chooseSplitAttribute(dataset);
		if (branchAttribute == null) {
			tree.setToken(createPredicate(dataset));
			return;
		}
		
		tree.setToken(branchAttribute);
		Node leftChild = new Node();
		Node rightChild = new Node();
		tree.setLeft(leftChild);
		tree.setRight(rightChild);
		
		Set<Article> leftDataset = new HashSet<>();
		for (Article article : dataset) 
			if (!article.hasAttribute(branchAttribute)) leftDataset.add(article);
		
		Set<Article> rightDataset = new HashSet<>();
		for (Article article : dataset)
			if (article.hasAttribute(branchAttribute)) rightDataset.add(article);
		
		if (leftDataset.size() == dataset.size() || rightDataset.size() == dataset.size()) {
			tree.setToken(createPredicate(dataset));
			return;
		}
		
		recursiveBuildTree(leftChild, leftDataset);
		recursiveBuildTree(rightChild, rightDataset);
	}
	
	/** 
	 *  Returns the name of the attribute that leads to the most information gain,
	 *  or null if there is no viable attribute or the data is homogeneous.  
	 */
	private String chooseSplitAttribute(Set<Article> articlesToSplit) {
		if (resultsAreHomogeneous(articlesToSplit)) return null;
		double minEntropy = calculateEntropy(articlesToSplit);
		String attributeToSplit = null;
		for (String attribute : Article.ALL_ATTRIBUTES) {
			double entropy = calculatePostSplitEntropy(articlesToSplit, attribute);
			if (entropy < minEntropy) {
				minEntropy = entropy;
				attributeToSplit = attribute;
			}
		}
		return attributeToSplit;
	}
	
	private boolean resultsAreHomogeneous(Set<Article> articles) {
		Boolean previous = null;
		for (Article article : articles)
			if (previous == null) previous = articlePreferences.get(article);
			else if (articlePreferences.get(article) != previous) return false;
		return true;
	}
	
	private String createPredicate(Set<Article> dataset) {
		int posCount = 0;
		int negCount = 0;
		for (Article article : dataset) 
			if (articlePreferences.get(article)) posCount++;
			else negCount++;
		if (posCount > negCount) return LIKE_TOKEN;
		else if(negCount < posCount) return DISLIKE_TOKEN;
		else return UNSURE_TOKEN;
	}
	
	private double calculatePostSplitEntropy(Set<Article> articlesToSplit, String attribute) {
		Set<Article> negatives = new HashSet<>();
		Set<Article> positives = new HashSet<>();
		for (Article article : articlesToSplit)
			if (article.hasAttribute(attribute)) positives.add(article);
			else negatives.add(article);
		return ((negatives.size() * calculateEntropy(negatives)) 
						/ articlesToSplit.size())
				+ ((positives.size() * calculateEntropy(positives)) 
						/ articlesToSplit.size());
	}

	private double calculateEntropy(Set<Article> data) {
		if (data.size() == 0) return 0.0;
		double posCount = 0;
		for (Article article : data) if (articlePreferences.get(article)) posCount++;
		return B(posCount / data.size());
	}
	
	private static double B(double q) {
		if (q < 0.0 || q > 1.0) throw new IllegalArgumentException();
		double sQ = (q == 0.0) ? 0.0 : (Math.log(q) / Math.log(2));
		double sNotQ = (q == 1.0) ? 0.0 : (Math.log(1 - q) / Math.log(2));
		return -((q * sQ) + ((1 - q) * sNotQ));
	}
	
	/** Prints the tree level-by-level (not aligned). Solely used for testing. */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Queue<Node> toPrint = new LinkedList<>();
		Queue<Integer> depths = new LinkedList<>();
		toPrint.add(root);
		depths.add(0);
		int previousDepth = depths.peek();
		while (!toPrint.isEmpty() && !depths.isEmpty()) {
			Node curr = toPrint.poll();
			int depth = depths.poll();
			if (curr == null) continue;
			if (depth > previousDepth) {
				previousDepth = depth;
				sb.append("\n");
			}
				
			sb.append(String.format("%s\t", curr));
			
			toPrint.add(curr.getLeft());
			toPrint.add(curr.getRight());
			depths.add(depth + 1);
			depths.add(depth + 1);
		}
		return sb.toString();
	}
}
