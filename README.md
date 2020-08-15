# wiki-search-engine

Given a Wikipedia document collection (322 .txt files), this project first implements and runs a 'word tokenizer', 
removes stopwords from the Wiki, reduces non-stopwords to their grammatical stems using the Porter Stemmer 
algorithm, and ultimately creates an indexed structure for the Wiki document collection.
Once all documents have been indexed, the search engine accepts a search query from the user. This query is put
through a spell checker that uses Soundex code, a dictionary .txt file, and the Noisy Channel model to determine
the most appropriate word for any typos in the user's query. Once this is complete, the query is run through the
system.
Each Wiki document is then retrieved and ranked, with the top 10 ranking documents being returned along with a 
relevant snipped generated from the document and the user's query.
  
  
 Results when given the query "actor appeared in movie premir":
 
 Query: actor appeared in movie premiere
Doc: 205 Score: 7.169925001442313 TF_Score_1: 0.0 TF_Score_2: 0.0 TF_Score_3:
3.5849625007211565 TF_Score_4: 3.5849625007211565
 LIZ HURLEY basked in reflected glory at the London premier of her new movie Mickey Blue
Eyes last night.

Doc: 320 Score: 3.5849625007211565 TF_Score_1: 0.0 TF_Score_2: 0.0 TF_Score_3:
3.5849625007211565 TF_Score_4: 0.0
 BRIDE-TO-be Catherine Zeta Jones was celebrating again yesterday after signing a multi
million-pound deal to make up to 12 movies.

Doc: 86 Score: 3.5849625007211565 TF_Score_1: 0.0 TF_Score_2: 0.0 TF_Score_3:
1.7924812503605783 TF_Score_4: 1.7924812503605783
 SKY Television has scooped the rights to screen the Blackadder movie before the BBC.
 
Doc: 206 Score: 3.584962500721156 TF_Score_1: 0.0 TF_Score_2: 0.0 TF_Score_3:
1.1949875002403854 TF_Score_4: 2.3899750004807707
 IT WASN'T quite as outrageous as That Dress, the barely believable gown (with the emphasis
on barely) that made her an overnight sensation five years ago.

Doc: 312 Score: 2.7443199808749794 TF_Score_1: 0.9518387305144009 TF_Score_2: 0.0
TF_Score_3: 1.7924812503605783 TF_Score_4: 0.0
 THOMAS The Tank Engine is being made into a blockbuster movie.
 
Doc: 321 Score: 2.4641058075929196 TF_Score_1: 1.2691183073525345 TF_Score_2: 0.0
TF_Score_3: 1.1949875002403854 TF_Score_4: 0.0
 WELSH actress Catherine Zeta Jones is on her way to becoming a movie mogul.
 
Doc: 293 Score: 2.4276487941887197 TF_Score_1: 0.7614709844115208 TF_Score_2:
0.23219280948873622 TF_Score_3: 0.7169925001442313 TF_Score_4: 0.7169925001442313
 AS THE boy who grows up into he most evil man in the universe, the young Darth Vader
fears little and respects even less.

Doc: 1 Score: 2.321928094887362 TF_Score_1: 0.0 TF_Score_2: 2.321928094887362
TF_Score_3: 0.0 TF_Score_4: 0.0
 A MAN of 80 appeared before Birmingham JPs yesterday charged with 14 indecent assaults
on boys at a children's home.

Doc: 10 Score: 2.321928094887362 TF_Score_1: 0.0 TF_Score_2: 2.321928094887362
TF_Score_3: 0.0 TF_Score_4: 0.0
 A BUSINESSMAN accused of indulging in a passionate clinch with a woman stranger on a
transatlantic flight was sent for trial when he appeared in court yesterday.

Doc: 113 Score: 2.321928094887362 TF_Score_1: 0.0 TF_Score_2: 2.321928094887362
TF_Score_3: 0.0 TF_Score_4: 0.0
 Two Egyptians appeared at Bow Street magistrates' court yesterday in connectionwith the
bombings last year of two American embassies in Africa.
 
 
