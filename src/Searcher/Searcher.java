/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Searcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Teteu
 */
public class Searcher
{
    private ArrayList<String> query = new ArrayList<>();
    private PorterStemmer stemmer = new PorterStemmer();
    private Wiki wiki;

    public Searcher(String filename, Wiki wiki) throws IOException
    {
        MyDictionary dic = new MyDictionary(filename);
        StopWords stop = new StopWords();
        this.wiki = wiki;
    }

    public void search(String inputQuery) throws IOException
    {
        
        prepareQuery(inputQuery.split(" "));        
        
        
        scoreDocuments();
        ArrayList<WikiArticle> topFive = getTopFive();

        System.out.println("");
        for (WikiArticle article : topFive)
        {
            System.out.println("Doc: " + article.getID());
            System.out.println(article.getSnippet() + "\n");
        }

    }

    public void prepareQuery(String[] broken)
    {
        for (int i = 0; i < broken.length; i++)
        {
            //Remove capitalization
            String word = broken[i].toLowerCase();

            //Remove punctuation
            while (word.endsWith(".") || word.endsWith(",") || word.endsWith("\"") || word.endsWith("\'"))
            {
                word = word.substring(0, word.length() - 1);

                if (word.length() == 0)
                {
                    break;
                }
                if (word.charAt(0) == '\"' || word.charAt(0) == '\'')
                {
                    word = word.substring(1, word.length());
                }
            }
            if (word.length() == 0)
            {
                continue;
            }

            //Remove the word from the query if it's a stopword
            if (StopWords.contains(broken[i]))
            {
                continue;
            }

            //Remove hyphens
            boolean joined = false;
            char[] pieces = word.toCharArray();
            for (int j = 0; j < pieces.length; j++)
            {
                if (pieces[j] == '-')
                {
                    joined = true;
                    break;
                }
            }
            if (joined)
            {
                boolean canconcat = true;
                String[] wordparts = word.split("-");
                for (int j = 0; j < wordparts.length; j++)
                {
                    if (!MyDictionary.dictionary.contains(wordparts[j]))
                    {
                        if (!StopWords.contains(word))
                        {
                            if (!word.matches("^-?\\d+$"))
                            {
                                query.add(stemmer.stem(word.trim()));
                            } else
                            {
                                query.add(word.trim());
                            }
                        }
                        canconcat = false;
                        break;
                    }
                }
                if (canconcat)
                {
                    String newWord = "";
                    for (int j = 0; j < wordparts.length; j++)
                    {
                        newWord += wordparts[j];
                    }
                    if (!StopWords.contains(newWord))
                    {
                        if (MyDictionary.dictionary.contains(newWord))
                        {
                            if (!word.matches("^-?\\d+$"))
                            {
                                query.add(stemmer.stem(newWord.trim()));
                            } else
                            {
                                query.add(newWord.trim());
                            }
                        } else
                        {
                            for (int j = 0; j < wordparts.length; j++)
                            {
                                query.add(wordparts[j]);
                            }
                        }
                    }
                }
            } else
            {
                if (!StopWords.contains(word))
                {
                    if (!word.matches("^-?\\d+$"))
                    {
                        query.add(stemmer.stem(word.trim()));
                    } else
                    {
                        query.add(word.trim());
                    }
                }
            }

        }
    }

    public void scoreDocuments()
    {
        HashMap<Integer, Integer> documents;
        int highFreq = 0;
        for (int j = 0; j < wiki.getArticles().size(); j++)
        {
            for (int i = 0; i < query.size(); i++)
            {
                if (wiki.getInverted().containsKey(query.get(i)))
                {
                    documents = wiki.getInverted().get(query.get(i));
                    double TF = 0;
                    if (documents.containsKey(wiki.getArticle(j).getID()))
                    {
                        TF = documents.get(wiki.getArticle(j).getID());
                    }
                    if(TF != 0)
                    {
                        TF = TF / wiki.getArticle(j).getHighFreq();
                    }
                    
                    double IDF = (Math.log(wiki.getArticles().size() / documents.size()) / Math.log(2));

                    wiki.getArticle(j).addScore(TF * IDF);
                } else
                {
                    System.out.println("No results found.");
                }

            }
            wiki.getArticle(j).calcTotalScore();
        }
    }

    public ArrayList<WikiArticle> getTopFive()
    {
        ArrayList<WikiArticle> sorted = wiki.getArticles();
        Collections.sort(sorted, Collections.reverseOrder());

        ArrayList<WikiArticle> topFive = new ArrayList<>();
        int i = 0;
        while (topFive.size() != 5)
        {
            sorted.get(i).generateSnippet(query);
            topFive.add(sorted.get(i));
            i++;
        }
        return topFive;
    }
}
