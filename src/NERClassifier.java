import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;

import java.util.*;
import java.util.regex.*;

class NERClassifier {
	
    List<String> tags =  Arrays.asList("I-PER", "I-LOC","B-LOC","I-ORG","B-ORG");
    String serializedClassifier = "edu/stanford/nlp/models/ner/german.conll.hgc_175m_600.crf.ser.gz";
    AbstractSequenceClassifier classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
    String punctutations = ".,:;?";
	
    public String clean(String str) {
        String cleanedString = "";
        for(int i=0;i<str.length();i++) {
            if(!punctutations.contains(Character.toString(str.charAt(i)))) {
                cleanedString = cleanedString.concat(Character.toString(str.charAt(i)));
            }
        }
        return cleanedString;
    }

    public Map<String, String> classify(String sentence) {
    	Map<String, String> nerNodes = new HashMap<>();
        sentence = clean(sentence);
        String tok[] = sentence.split(" ");
        System.out.println("In NER, sentence given is:" +sentence);
        //Additional tag which maps the words to dates mentioned is used along with PER, ORG, LOC.
        Pattern p = Pattern.compile("^([0-2][0-9]||3[0-1])/(0[0-9]||1[0-2])/([0-9][0-9])?[0-9][0-9]$");
        for(String token : tok) {
            Matcher m = p.matcher(token);
            while (m.find()) {
                nerNodes.put(m.group(), "DAT");
            }
        }
        String classifyString = classifier.classifyToString(sentence);
        String tokens[] = classifyString.split(" ");
        for (int i=0;i<tokens.length;i++) {
            String words[]=tokens[i].split("/");
            if(tags.contains(words[1]))
            {
                String label[] =  words[1].split("-");
                nerNodes.put(words[0], label[1]);
            }
        }
        return nerNodes;
    }
}
