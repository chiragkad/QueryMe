import java.util.*;

/**
 * This class is responsible to build the query using the tag-parsing algorithm. It first constructs the partial parts
 * and then combines them to form the whole query.
 * @author Manu
 *
 */
public class ConstructQuery {

	//This lists are used through out the class, for now use the single copy to construct the query.
	//All the functions uses this lists for constructing.
	private List<String> words = new ArrayList<>();
	private List<String> tags = new ArrayList<>();
	private Map<String, Node> nodes = new HashMap<>();
	//This set contains all the attribute names and relational entities that are found in the sentence.
	//This words can be used to include all the table names in the FromClause.
	private Set<String> fromClauseNodes = new HashSet<>();
	
	
	public ConstructQuery(List<String> words, List<String> tags, Map<String, Node> resultNodes)
	{
		this.words = words;
		this.tags = tags;
		this.nodes = resultNodes;
	}
	/**
	 * @return the query which is constructed.
	 */
	public String getQueryFromTags()
	{
		/**
		 * Algorithm: get the index of the complex condition, and then using the LN tags construct the blocks 
		 * of the complex condition.
		 */
		int length = tags.size();
		int conditionClauseStartIndex = length, index = 0;
		//TODO Need to find a better solution for finding the start of complex condition.
		for( index = length - 1; index >= 0; index-- )
		{
			//The first RN from last in the tag list is used as a start point for complex condition.
			if( tags.get(index).equals("RN") )
			{
				conditionClauseStartIndex = index + 1;
				break;
			}
		}
		String conditionClause = getComplexCondition(conditionClauseStartIndex, length - 1);
		String selectClause = getSelectClause(0, index);
		StringBuilder query = new StringBuilder();
		query.append(selectClause).append(conditionClause).append(";");
		return query.toString();
	}
	
	/**
	 * This function is responsible to construct the complex condition, it may be combination of many combinations.
	 * @param startIndex, index from which the WHERE clause starts from the given German sentence.
	 * @param endIndex
	 * @return the complex condition constructed, empty when there are no valid combinations.
	 */
	private String getComplexCondition(int startIndex, int endIndex)
	{
		String condition = new String();
		if( startIndex == words.size() )
			return condition;
		StringBuilder complexCondition = new StringBuilder();
		for( int index = endIndex; index >= startIndex; index-- )
		{
			//Need to see how the list can be seperated if there is no LN itsel
			if( tags.get(index).equals("LN") )
			{
				condition = getCondition( index + 1, endIndex);
				//TODO need to be careful with the spaces before and after the logical operator.
				complexCondition.append(" " +words.get(index)).append(" ").append(condition);
				endIndex = index - 1;
				break;
			}
		}
		condition = getCondition(startIndex, endIndex);
		complexCondition.insert(0, condition).insert(0, "WHERE ");
		return complexCondition.toString();
	}
	
	
	/**
	 * 
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */
	private String getCondition(int startIndex, int endIndex)
	{
		StringBuilder condition = new StringBuilder();
		System.out.println("The start and end Indexes for the condtion are:" +startIndex + " "+endIndex);
		//TODO when  there are no AN or RN for the condition, Use NER to find
		// the attribute that this VN can be matched with.
		String leftTreeComp = "", rightTreeComp = "", comparator = "";
		int numberOfAttributeNodes = 0;
		for( int index = endIndex; index >= startIndex; index-- )
		{
			//In future, you need to get the value of the node this ON is mapped to.
			if( tags.get(index).equals("ON") )
			{
				// TODO This should be modified according the node information of the ON.
				comparator = words.get(index);
			}
			else if( tags.get(index).equals("VN") || tags.get(index).equals("NTW"))
			{
				//Need to consider the case of integers in the sentence. The other information of the VN should also
				//considered  while integrating them in the condition.
				rightTreeComp = words.get(index);
			}
			else if( tags.get(index).equals("RN") )
			{
				//You need to get the default attribute key of this node which is returned from the word net.
				//TODO assign the default attribute key.
				Node curNode = nodes.get(words.get(index));
				fromClauseNodes.add(curNode.value);
				leftTreeComp = curNode.value + "."+ curNode.defaultAttribbuteKey;
			}
			else if( tags.get(index).equals("AN") )
			{
				numberOfAttributeNodes++;
				//All the AN's and RN's should have a reserved node containing all the information.
				Node curNode = nodes.get(words.get(index));
				fromClauseNodes.add(curNode.parentName);
				if( numberOfAttributeNodes == 1 )
				{
					leftTreeComp = curNode.parentName + "." + curNode.value;
				}
				else if( numberOfAttributeNodes == 2 )
				{
					rightTreeComp = leftTreeComp;
					leftTreeComp = curNode.parentName + "." + curNode.value;
				}
			}
		}
		if( comparator.isEmpty() )
			comparator = "=";
		condition.append(leftTreeComp).append(" " + comparator + " ").append(rightTreeComp);
		return condition.toString();
	}
	
	/**
	 * This function is responsible for constructing the Sclause in the SQL query. It needs to consider all the
	 * default attribute nodes and their parents while constructing the clause.
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */
	private String getSelectClause(int startIndex, int endIndex)
	{
		//You need to parse the list of nodes
		System.out.println("The start and end Indexes that"
				+ " for the select clause function are:" +startIndex + " "+endIndex);
		StringBuilder sClause = new StringBuilder();
		StringBuilder retrieve = new StringBuilder();
		sClause.append("SELECT ");
		//if it is an AN, you can insert its parentName.attribute name in the select portion
		//if it is an RN, it's default attribute key should be inserted in this portion.
		for( int index = 0; index <= endIndex; index++ )
		{
			//get the required information required to construct the query.
			Node curNode = nodes.get(words.get(index));
			if( tags.get(index).equals("RN") )
			{
				fromClauseNodes.add(curNode.value);
				retrieve.append(curNode.value + "." + curNode.defaultAttribbuteKey).append(", ");
			}
			else if( tags.get(index).equals("AN") )
			{
				fromClauseNodes.add(curNode.parentName);
				retrieve.append(curNode.parentName + "." + curNode.value).append(", ");
			}
		}
		retrieve.setLength(retrieve.length() - 2);
		sClause.append(retrieve).append( " FROM ");
		//Now we need to insert all the parentNodes, whose attributes are encountered in the condtion or
		//any where in the sentence.
		Iterator<String> setIterator = fromClauseNodes.iterator();
		StringBuilder fromClause = new StringBuilder();
		while( setIterator.hasNext() )
		{
			fromClause.append(setIterator.next().toString()).append(", ");
		}
		fromClause.setLength(fromClause.length() - 2);
		fromClause.append(" ");
		sClause.append(fromClause);
		return sClause.toString();
	}
}
