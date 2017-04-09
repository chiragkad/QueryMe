import java.io.*;
import java.util.*;
import java.util.logging.Logger;


class Node
{
	String nodeType;
	String value;
	String parentName;
	String defaultAttribbuteKey;
	
	public Node(String nodeType, String value, String parentName, String defaultAttributeKey)
	{
		this.nodeType = nodeType;
		this.value = value;
		this.parentName = parentName;
		this.defaultAttribbuteKey = defaultAttributeKey;		
	}
}
/**
 * This class contains the methods that helps in finding the attribute and realtional nodes in the german sentence.
 * @author Manu
 *
 */
public class WordNet {
	
	public Map<Node, List<String>> wordnet = new HashMap<>();
	public BufferedReader reader = null;
	Logger log = Logger.getLogger(this.getClass().getName());
	
	public WordNet()
	{
		//load the data here so that we can use functions which can refer this class to get the synsets.
		//read the file, load the contents and construct the map.
		try 
		{
			
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("LanguageData.txt");
			reader = new BufferedReader(new InputStreamReader(in));
			String curLine = null;
			int count = 0;
			while( ( curLine = reader.readLine()) != null )
			{
				//parse the line  to form the node value and then form the synonyms list.
				//System.out.println(++count);
				String[] values = curLine.split(" -> ");
				String[] nodeParams = values[0].split(", ");
				Node curNode = new Node(nodeParams[0], nodeParams[1], nodeParams[2], nodeParams[3]);
				String[] synonyms = values[1].split(", ");
				List<String> curList = new ArrayList<>();
				for( int index = 0; index < synonyms.length; index++ )
				{
					curList.add(synonyms[index]);
				}
				wordnet.put(curNode, curList);
			}
		} 
		catch (Exception e)
		{
			log.info("Exception occured while trying to read the contents from the file");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param synonym, word for which we need to find a schema element that is closely related. 
	 * @param pos, parts of speech tag which can be helpful for getting the correct node in case of ambiguity.
	 * @return Node, the schema element with all the information required, null in case not matched with any synonym.
	 * The synonym should be with out punctuation.
	 */
	public Node synSet(String synonym, String pos)
	{
		//remove the special characters and convert it to lowerCase before moving forward.
		synonym = synonym.toLowerCase();
		Node schemaElement = null;
		//Iterate through all the keys
		for(Map.Entry<Node, List<String>> entry :  wordnet.entrySet() )
		{
			List<String> words = entry.getValue();
			for( String word : words )
			{
				if( word.equals(synonym) )
				{
					schemaElement = entry.getKey();
					break;
				}
			}
			if( schemaElement != null )
				break;
		}
		return schemaElement;
	}
	
	public void printSize()
	{
		System.out.println("The size of the word net mapping currently is: "+wordnet.size());
	}

}
