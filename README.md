# QueryMe
Our objective is to convert natural language sentences to more formal language such as SQL which can be used to search a database.
We are trying to build a system which can take German sentences and then convert it to SQL query which can be executed to retrieve the results

Challenges:
1) No basic tools such as wordnet or dependency parsing from NLTK or StanfordNLP lib.
2) We need to embed the database related knowledge to the system.
3) There is lot of ambiguity in the natural language sentences while converting them to more formal langauge where difference in structures 
    gives us different results.
4) We need to come up with ideas to resolve ambiguities.
5) There is no concrete baseline algorithm to measure the performance of our system.

Approach:
1) Parse the sentence to get POS tags and then use that information for futher classification.
2) Classify the words in the natural language sentence to pre-defined set of tags which are closely related to SQL nodes.
3) Start with a basic slot filling algorithm which is good enough to handle multiple Condtions in "Where" clause.
4) Use a semantic graph which contains all the information to retrieve the FK-PK paths which are used for the natural join.
5) Use a NER which can be used to better the retrieval of Value Nodes involved in the sentence.
6) As there are no predefined tools to construct a dependecy parse for the natural language sentence in German, building our own dependency parse
which uses the handwritten grammar. Writing Grammar to match all types of SQL queries is not easy, so better start with moderate range of SQL queries.
6) Parse the dependency tree  to generate the SQL query.
7) Build a Evaluation system which compares the model accuracy at different points of developement which can be a good measure of how
each feature is useful or vice versa.

More to come................. :)
