import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.*;
import java.util.Map.Entry;

public class Solution
{
	private PrintWriter writer;
	
	public Solution()
	{
		 try {
			writer = new PrintWriter("OutputQueries.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e)
		 {
			e.printStackTrace();
		 }
	}
	public static void main(String[] args) {
		
		try{
			Solution solution = new Solution();
	    	InputStream in = solution.getClass().getClassLoader().getResourceAsStream("InputQueries.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String curQuery;
			while( (curQuery = reader.readLine()) != null )
			{
				SQLTranslator sqlTranslator = new SQLTranslator();
				sqlTranslator.setText(curQuery);
				String outputQuery = sqlTranslator.parse();
				solution.writer.println(outputQuery);
			}
			reader.close();
			solution.writer.close();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
}

class SQLTranslator {

    private String text;
    //Stanford parser for Named Entity recognition.
	private NERClassifier NERStanfordClassifier;
    //We use the list of stop words which do not have any information for constructing the SQL query.  
    private ArrayList<String> stopWords = new ArrayList(Arrays.asList( new String[]{"haben", "die", "auf","der","das","sich","für", "mit", "welche", "den", "aus", "jetzt", "von","f�r"} ) );
    private String punctutations = ".,:;?";
    private HashSet<String> typesOfSQLStatements = new HashSet<>();
    //TODO need to add more logical operators and words that closely mean like logical operator.
    private HashMap<String, String> logicalOperator = new HashMap<>();
    private HashMap<String, ArrayList<String >> relationalOperator = new HashMap<>();
    //The three data structures below holds the tags and Node information for each non-stop word in the
    //given Query sentence in NL.
    private Map<String, Node> resultNodes = new HashMap<>();
    private ArrayList<String> wordList = new ArrayList<>();
    private ArrayList<String> tagList = new ArrayList<>();
    //Local word net that is built for serving the purpose of actual wordnet.
    private WordNet wordNet = new WordNet();
    private boolean isRelational;

    //Need to add more sentences in future into "text"
    public SQLTranslator() {
    	//text contains the sentence that needs to be converted as SQL query.
    	if( this.NERStanfordClassifier == null )
    	{
    		this.NERStanfordClassifier = new NERClassifier();
    	}
        this.logicalOperator.put("und","AND");
        this.logicalOperator.put("oder","OR");
        //List of relational operator and their possible word list in german language.
        this.relationalOperator.put(">",new ArrayList( Arrays.asList( new String[]{"älter", "über"} )));
        this.relationalOperator.put("=",new ArrayList( Arrays.asList( new String[]{"bei", "in", "am","an","als"} )));
        this.relationalOperator.put("<",new ArrayList( Arrays.asList( new String[]{"jünger", "unter"} )));
        this.typesOfSQLStatements.addAll(Arrays.asList("liste", "erw�hnen", "machen", "zeigen", "Nennen", "welche", "wann"));
    }
    
    public void setText(String text)
    {
    	this.text = text;
    }

    public String parse()
    {
    	String finalQuery = "";
        Annotation germanAnnotation = new Annotation(this.text);
        Properties germanProperties = StringUtils.argsToProperties(
                new String[]{"-props", "StanfordCoreNLP-german.properties"});
        StanfordCoreNLP pipeline = new StanfordCoreNLP(germanProperties);
        pipeline.annotate(germanAnnotation);
        for (CoreMap sentence : germanAnnotation.get(CoreAnnotations.SentencesAnnotation.class))
        {
            Tree sentenceTree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            //GrammaticalStructureFactory gsf = new UniversalEnglishGrammaticalStructureFactory();
            //SemanticGraph dependencyGraph = SemanticGraphFactory.generateUncollapsedDependencies(sentenceTree);
            //.generateCollapsedDependencies( gsf.newGrammaticalStructure(sentenceTree), GrammaticalStructure.Extras.NONE );
            System.out.println(sentenceTree.labeledYield());
            Map<String, String> nerTags = this.NERStanfordClassifier.classify(this.text);
            System.out.println("The output from the NER parser is: ");
            for(Map.Entry<String, String> entry : nerTags.entrySet() )
            {
            	System.out.println(entry.getKey() + " " + entry.getValue());
            }
            for(int i=0;i<sentenceTree.labeledYield().size();i++)
            {
                String[] words = sentenceTree.labeledYield().get(i).toString().split("/");
                if(!stopWords.contains(words[0]) && !punctutations.contains(words[0]))
                {
                    if (words[1].equals("KON")) 
                    {
                    	//This is where we need to form a Node in the future.
                        wordList.add(logicalOperator.get(words[0]));
                        tagList.add("LN");
                    }
                    else if (words[1].equals("NE") || words[1].equals("CARD")) 
                    {
                        wordList.add(words[0]);
                        tagList.add("VN");
                        if( nerTags.containsKey(words[0]) )
                        {
                        	System.out.println("****NER classified an NTW to a some other tag.**");
                        }
                    }
                    else
                    {
                        isRelational = false;
                        Iterator it = relationalOperator.entrySet().iterator();
                        while (it.hasNext())
                        {
                            Map.Entry pair = (Entry) it.next();
                            ArrayList<String> array = (ArrayList<String>) pair.getValue();
                            if(array.contains(words[0]))
                            {
                                wordList.add((String) pair.getKey());
                                tagList.add("ON");
                                isRelational = true;
                                break;
                            }
                        }
                        if(!isRelational)
                        {
                            Node node = wordNet.synSet(words[0],words[1]);
                            if(node!=null)
                            {
                                wordList.add(words[0]);
                                tagList.add(node.nodeType);
                                //This is where you need to add the node into the hashmap, use this information
                                //for building the query.
                                resultNodes.put(words[0], node);
                            }
                            else
                            {
                            	wordList.add(words[0]);
                            	if( this.typesOfSQLStatements.contains(words[0].toLowerCase()) )
                            	{
                            		tagList.add("SN");
                            	}
                            	else
                            	{
 
	                                //Here is where we need to see if the information from the NER
	                                // is used to further tag them in some way.
	                                //TODO call NER tagger if to check if the word can be tagged further.
	                                //see if they are present.
	                                if( nerTags.containsKey(words[0]) )
	                                {
	                                	System.out.println("****NER classified an NTW to a some other tag.**");
	                                }
	                                tagList.add("NTW");
                            	}
                            }
                        }
                    }
                }
            }
            System.out.println(wordList.toString());
            System.out.println(tagList.toString());
            finalQuery = constructQuery();
            System.out.println("The final resultant Query constructed is:" +finalQuery);
            buildDependencyTree();
            return finalQuery;
        }
		return finalQuery;
    }
    
    /**
     * Creates a ConstructQuery class instance and  then uses it to build the query.
     * @return
     */
    private String constructQuery()
    {
    	ConstructQuery queryConstructor = new ConstructQuery(wordList, tagList, resultNodes);
    	//Return the query constructed by the queryConstructor instance.
    	return queryConstructor.getQueryFromTags();
    	//return "";
    }
    
    private void buildDependencyTree()
    {
    	System.out.println("Control passed to Dependency Parser");
    	DependencyParser parser = new DependencyParser(wordList, tagList, resultNodes);
    	parser.constructDependencyTree();
    	return;
    }
    
}