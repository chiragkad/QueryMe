import java.util.*;

/**
 * Hand-written grammar rules are incorporated in the form of key:value pairs.
 */
class Grammar
{
	public Map<String, List<String>> rules;
	public Map<String, Integer> dependents;
	public static Grammar instance;
	
	public Grammar()
	{
		rules = new HashMap<>();
		dependents = new HashMap<>();
		//This rules can be used to detect the head-child relation ships in the given sentence.
		this.rules.put("ROOT", Arrays.asList("SN", "LN"));
		this.rules.put("SN", Arrays.asList("RN", "AN"));
		this.rules.put("LN", Arrays.asList("ON"));
		this.rules.put("ON", Arrays.asList("AN","RN","VN","SN", "NTW"));
		//dependents map is used to decide while assigning RIGHT ARC rule, as we need to consider the
		// total number of dependents possible and the current number of dependents encountered.
		// In future, we can make the grammar rules and dependents count more
		// robust for handling variety of queries.
		//TODO need to handle LN nodes.
		this.dependents.put("ROOT", 2);
		this.dependents.put("SN", 1);
		this.dependents.put("RN", 0);
		this.dependents.put("AN", 0);
		this.dependents.put("VN", 0);
		this.dependents.put("LN", 2);
		this.dependents.put("ON", 2);
		this.dependents.put("NTW", 0);
	}
	
	public static Grammar getInstance()
	{
		if( instance == null )
		{
			instance = new Grammar();
		}
		return instance;
	}
}

/**
 * Combine the list of words, their tags and additonal node info for the AN and RN's to a single node
 * so that the oracle can use their information in one place
 */
class InfoNode
{
	private String nodeValue;
	private String nodeType;
	private int position;
	int dependentsFound;
	Node info;
	
	public InfoNode(String nodeValue, String nodeType, Node info, int position)
	{
		this.nodeValue = nodeValue;
		this.nodeType = nodeType;
		//Need to careful with the null nodes for the non-AN/RN nodes.
		// They do not have any additional information.
		this.info = info;
		this.dependentsFound = 0;
		this.position = position;
	}
	
	public void setNodeValue(String value)
	{
		this.nodeValue = value;
	}

	public String getNodeValue()
	{
		return this.nodeValue;
	}
	
	public void setNodeType(String type)
	{
		this.nodeType = type;
	}
	
	public String getNodeType()
	{
		return this.nodeType;
	}
	
	public int getPosition()
	{
		return this.position;
	}
	
}
/**
 * Every node in a dependency parse tree is represented as a node in n-ary tree. 
 * @author Manu
 *
 */
class TreeNode
{
	InfoNode node;
	List<TreeNode> children;
	
	public TreeNode()
	{
		children = new ArrayList<>();
	}
}
/**
 * This class is responsible for constructing the dependency(multiple) parse trees for a given sentence
 * taking their tags and grammar into consideration.
 *
 */
class DependencyParser
{
	
	TreeNode root;
	
	Stack<InfoNode> stack = new Stack<>();
	
	List<InfoNode> nodes = new ArrayList<>();
	
	Map<Integer, TreeNode> relations = new HashMap<>();
	
	private Grammar grammar;
	
	
	
	public DependencyParser(List<String> words, List<String> tags, Map<String,Node> infoNodes)
	{
		if( this.grammar == null )
		{
			this.grammar = Grammar.getInstance();
		}
		//add root into the list of nodes. Initialize the rootNode which sits on top of all relations.
		InfoNode rootNode = new InfoNode("ROOT", "ROOT", null, 0);
		this.root = new TreeNode();
		this.root.node = rootNode;
		this.relations.put(0, this.root);
		this.nodes.add(rootNode);
		constructInfoNodes(words, tags, infoNodes);
	}
	
	private void constructInfoNodes(List<String> words, List<String> tags, Map<String, Node> schemaNodes)
	{
		//Map respective words, their tags and also any additional info available.
		Node info = null;
		int numberOfWords = words.size();
		//Insert all the words and their info into the nodesList.
		for(int pos = 0; pos < numberOfWords; pos++ )
		{
			String nodeType = tags.get(pos);
			String curWord = words.get(pos);
			//TODO need to handle this more carefully. This information is needed for the succesfull parsing
			//of the tree.
			if( nodeType == "AN" || nodeType == "RN")
				info = schemaNodes.get(curWord);
			else 
				info = null;
			InfoNode infoNode = new InfoNode(curWord, nodeType, info, pos + 1);
			this.nodes.add(infoNode);
		}
	}
	
	private void initializeStack(int currentWordIndex, int pushSize)
	{
		for( ;currentWordIndex < this.nodes.size() && pushSize-- > 0; currentWordIndex++ )
		{
			this.stack.push(this.nodes.get(currentWordIndex));
		}
	}
	
	/**
	 * This method is responsible for constructing the dependency tree provided the head-child relations
	 * for the given set of sentences.
	 * @return
	 */
	public TreeNode constructDependencyTree()
	{
		//Need to use the fetchOperation function and then depending on the type of operation,
		//we need to perform the respective operation and include the head-child relationships.
		//Need to take care of the stack and list configuration before performing each of the operation.
		int currentWordIndex = 0;
		//The initialization ensures that the stack is feeded with the root and atleast an another node
		//before calling the fetch operation function.
		initializeStack(currentWordIndex, 2);
		currentWordIndex += 2;
		int totalNumberOfWords = nodes.size();
		// TODO When we come out of this loop, the list of words is empty but there may be still some
		//elements in the stack.
		while( currentWordIndex < totalNumberOfWords || this.stack.size() > 1  )
		{
			String operation = fetchOperation();
			if( operation.equals("SHIFT") )
			{
				this.stack.push(nodes.get(currentWordIndex));
				currentWordIndex++;
			}
			else if( operation.equals("LEFT ARC") )
			{
				//left arc
				InfoNode head = this.stack.pop();
				InfoNode child = this.stack.pop();
				addRelation(head, child);
				//remove the childNode from the stack and just insert back the head;
				this.stack.push(head);
			}
			else
			{
				//RIGHT ARC
				InfoNode child = this.stack.pop();
				InfoNode head = this.stack.pop();
				addRelation(head, child);
				//remove the childNode from the stack and just insert back the head;
				this.stack.push(head);				
			}
		}
		this.printDependencyTree();
		return root;
	}
	
	
	private void addRelation(InfoNode head, InfoNode child)
	{
		System.out.println("Adding relation");
		TreeNode headNode = this.relations.get(head.getPosition());
		if(	headNode == null )
		{
			headNode = new TreeNode();
			this.relations.put(head.getPosition(), headNode);
			headNode.node = head;
		}
		TreeNode childNode = this.relations.get(child.getPosition());
		if( childNode == null )
		{
			childNode = new TreeNode();
			this.relations.put(child.getPosition(), childNode);
			childNode.node = child;
		}
		headNode.children.add(childNode);
	}
	
	/**
	 * Returns "RIGHT ARC", if there is head-child relationship between
	 *  the second top element and top element. And also checks if all the dependents of the top element are
	 *  considered. Returns "LEFT ARC" operation when there is a head-child relationship between the top element
	 *  and the second top element.  Else returns the "SHIFT" operation.
	 *  This process replicates the functionality of the "oracle" which is trained to function efficiently.
	 * 
	 * @return operation type -  RIGHT ARC, LEFT ARC, SHIFT
	 */
	public String fetchOperation()
	{
		int count = 0;
		InfoNode topElem = null, secondTopElem = null;
		LinkedList<InfoNode> topElements = new LinkedList<>();
		//Get the top two elements from the stack.
		if( this.stack.size() < 2 )
			return "SHIFT";
		while( count < 2 && !this.stack.isEmpty() )
		{
			topElements.offerFirst(stack.pop());
			count++;
		}
		if( topElements.size() < 2 )
		{
			pushBack(topElements);
			return "SHIFT";
		}
		else
		{
			topElem = topElements.get(1);
			secondTopElem = topElements.get(0);
		}
		//check for the LA.
		
		if( this.grammar.rules.containsKey(topElem.getNodeType()) )
		{
			if( this.grammar.rules.get(topElem.getNodeType()).contains(secondTopElem.getNodeType()) )
			{
				//We need to change the dependency count for the topElem
				topElem.dependentsFound++;
				pushBack(topElements);
				return "LEFT ARC";
			}
		}
		//check for the right arc condition, now we need to consider the dependents count.
		if( this.grammar.rules.containsKey(secondTopElem.getNodeType()) )
		{
			if( this.grammar.rules.get(secondTopElem.getNodeType()).contains(topElem.getNodeType()) )
			{
				//matching rule has been found.
				//Now we need to check for the dependent counts of the child node.
				System.out.println(topElem.getNodeType());
				Integer expectedDependentCount = this.grammar.dependents.get(topElem.getNodeType());
				if( topElem.dependentsFound >= expectedDependentCount )
				{
					//This means that the child node has no further dependents coming up.
					secondTopElem.dependentsFound++;
					pushBack(topElements);
					return "RIGHT ARC";
				}
			}
		}
		//If the program reaches this point, then the current configuration is not supported for the
		//either of the reduction rules. So simply shift the next node on to the stack.
		pushBack(topElements);
		return "SHIFT";
	}
	
	
	private void pushBack(LinkedList<InfoNode> topElements)
	{
		//the fetchOperation function will ensure that the order of elements is preserved, it would
		//be same as the function is called.
		int index = 0;
		while( index < 2 && index < topElements.size() )
		{
			if( topElements.get(index) != null )
			{
				this.stack.push(topElements.get(index++));
			}
		}
	}
	
	
	private void printDependencyTree()
	{
		//this function helps us to visualize the head-child relation ships between
		//different words in the sentences.
		//The dependency tree can be printed level order, starting from root.
		Queue<TreeNode> queue = new LinkedList<>();
		queue.offer(this.root);
		while( !queue.isEmpty() )
		{
			int curLevelSize = queue.size();
			while( curLevelSize > 0 )
			{
				//Here we get the parents.
				TreeNode curNode = queue.poll();
				curLevelSize--;
				if( curNode.children != null && curNode.children.size() > 0 )
				{
					List<TreeNode> childs = curNode.children;
					for( TreeNode child : childs )
					{
						queue.offer(child);
						//Print both the parent and child details.
						System.out.println(curNode.node.getNodeValue()+ "( " + curNode.node.getNodeType()+" )" + " -> " 
								+ child.node.getNodeValue() + "( "+ child.node.getNodeType() + " )");
					}
				}
			}
		}
	}

}
