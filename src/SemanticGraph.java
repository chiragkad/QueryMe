/**
 * The semantic graph is responsible for getting the FK-PK paths which are required for the natural join queries.
 * This class contains functions to return all the natural join paths which will be included in the WHERE clause.
 * @author Manu
 *
 */
public class SemanticGraph
{
	
	//You also need to construct the semantic graph first. And ten make those functions available.
	
	/**
	 * Return the weight of the path which can be used as score for resolving the ambiguity
	 * with different parse trees that are generated.
	 * @param node
	 * @param child
	 * @return
	 */
	public int getWeight(String node, String child)
	{
		return 0;
	}
	
	
	public String getPath(Node start, Node dest)
	{
		return "";
	}
}
