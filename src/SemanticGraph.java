import java.util.*;

class KeyNode
{
	public String keyName;
	public String keyType;
	public String relatedNode;
	public String relatedTable;
	
	public KeyNode(String keyName, String keyType, String relatedNode, String relatedTable)
	{
		this.keyName = keyName;
		this.keyType = keyType;
		this.relatedNode = relatedNode;
		this.relatedTable = relatedTable; 
	}
}

/**
 * The semantic graph is responsible for getting the FK-PK paths which are required for the natural join queries.
 * This class contains functions to return all the natural join paths which will be included in the WHERE clause.
 * @author Manu
 *
 */
public class SemanticGraph
{
	
	
	public Map<String, List<KeyNode>> relations = new HashMap<>();

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
	
	public void addRelation(String tableName, KeyNode relation)
	{
		if( relations.containsKey(tableName) )
		{
			relations.get(tableName).add(relation);
		}
		else
		{
			List<KeyNode> listOfRelations = new ArrayList<>();
			listOfRelations.add(relation);
			relations.put(tableName, listOfRelations);
		}
	}
	
	//Relations map contains only entries for the tables which has the primary keys.
	//In this way we can resolve the issues with redundancy, as there is always only
	//entry for all the FK-PK paths which is stored with PK parent name.
	public String getPath(String start, String dest, int count)
	{
	
		StringBuilder path = new StringBuilder();
		if( count > 2 )
			return path.toString();
		//There can be multiple paths between two tables which means that they might be connected with different keys.
		//The path must involve a PK and FK, there cannot be two same type nodes that connect the tables.
		List<KeyNode> relatedNodes = relations.get(start);
		if( relatedNodes != null && relatedNodes.size() != 0 )
		{
			Iterator<KeyNode> iterator = relatedNodes.iterator();
			while( iterator.hasNext() )
			{
				KeyNode curChild = iterator.next();
				if( curChild.relatedTable.equals(dest) )
				{
					//this is the path we are looking for.
					path.append(start).append("." + curChild.keyName);
					path.append(" = ");
					path.append(curChild.relatedTable).append("."+curChild.relatedNode);
					//Here we are returning when we found atleast one path.
					//TODO re-check this condition for all the PK-FK relations in the database.
					//With increase in complexity, there are chances that multiple FK-PK existing between two tables.
					return path.toString();
				}
				
			}
		}
		if( path.length() == 0 )
			return getPath(dest, start, 1);
		
		return path.toString();
	}
}
