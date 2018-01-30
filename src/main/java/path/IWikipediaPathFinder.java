package path;

/**
 * This interface represents the core algorithm for winning
 * the wikigame. Given a start and goal article, implementations
 * of this interface should return a (minimal) path between both
 * articles.
 * Created by joris on 1/26/18.
 */
public interface IWikipediaPathFinder {

    String[] find(String start, String goal);

}
