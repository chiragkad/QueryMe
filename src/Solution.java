import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.util.*;

public class Solution {

    public static void main(String[] args) {
        SQLTranslator sqlTranslator = new SQLTranslator("Liste Freunde, die als Informatik-Ingenieur von beruf, auf");
        sqlTranslator.parse();
    }
}

class SQLTranslator {

    private String text;
    private ArrayList<String> stopWords = new ArrayList( Arrays.asList( new String[]{"die", "auf","der","das","sich","für", "mit", "welche", "den", "aus", "jetzt", "bei", "von" } ) );
    private String punctutations = ".,:;?";
    private HashMap<String, String> logicalOperator = new HashMap<>();
    private HashMap<String, ArrayList<String >> relationalOperator = new HashMap<>();
    private ArrayList<String> wordList = new ArrayList<>();
    private ArrayList<String> tagList = new ArrayList<>();
    private WordNet wordNet = new WordNet();
    private boolean isRelational;

    SQLTranslator(String text) {
        this.text = text;
    }

    public void parse() {
        logicalOperator.put("und","AND");
        logicalOperator.put("oder","OR");
        relationalOperator.put(">",new ArrayList( Arrays.asList( new String[]{"älter", "über"} )));
        relationalOperator.put("=",new ArrayList( Arrays.asList( new String[]{"in", "am","an","als"} )));
        relationalOperator.put("<",new ArrayList( Arrays.asList( new String[]{"jünger", "unter"} )));
        Annotation germanAnnotation = new Annotation(text);
        Properties germanProperties = StringUtils.argsToProperties(
                new String[]{"-props", "StanfordCoreNLP-german.properties"});
        StanfordCoreNLP pipeline = new StanfordCoreNLP(germanProperties);
        pipeline.annotate(germanAnnotation);
        for (CoreMap sentence : germanAnnotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree sentenceTree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            //GrammaticalStructureFactory gsf = new UniversalEnglishGrammaticalStructureFactory();
            //SemanticGraph dependencyGraph = SemanticGraphFactory.generateUncollapsedDependencies(sentenceTree);
            //.generateCollapsedDependencies( gsf.newGrammaticalStructure(sentenceTree), GrammaticalStructure.Extras.NONE );
            System.out.println(sentenceTree.labeledYield());
            for(int i=0;i<sentenceTree.labeledYield().size();i++) {
                String[] words = sentenceTree.labeledYield().get(i).toString().split("/");
                if(!stopWords.contains(words[0]) && !punctutations.contains(words[0])) {
                    if (words[1].equals("KON")) {
                        wordList.add(logicalOperator.get(words[0]));
                        tagList.add("LN");
                    } else if (words[1].equals("NE") || words[1].equals("CARD")) {
                        wordList.add(words[0]);
                        tagList.add("VN");
                    } else {
                        isRelational = false;
                        Iterator it = relationalOperator.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            ArrayList<String> array = (ArrayList<String>) pair.getValue();
                            if(array.contains(words[0])) {
                                wordList.add((String) pair.getKey());
                                tagList.add("ON");
                                isRelational = true;
                                break;
                            }
                        }
                        if(!isRelational) {
                            Node node = wordNet.synSet(words[0],words[1]);
                            if(node!=null) {
                                wordList.add(words[0]);
                                tagList.add(node.nodeType);
                            } else {
                                wordList.add(words[0]);
                                tagList.add("NTW");
                            }
                        }
                    }
                }
            }
            System.out.println(wordList.toString());
            System.out.println(tagList.toString());
        }
    }
}