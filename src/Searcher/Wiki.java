/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Searcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Teteu
 */
public class Wiki
{

    private ArrayList<WikiArticle> articles = new ArrayList<>();
    PorterStemmer stemmer = new PorterStemmer();
    private HashMap<String, HashMap<Integer, Integer>> inverted = new HashMap<>();
   // ArrayList<String> query;

    public Wiki()
    {
      //  this.query = query;
    }

    public void populateWiki(File folder) throws IOException
    {        
        for (final File fileEntry : folder.listFiles())
        {
            if (fileEntry.isDirectory())
            {
                populateWiki(fileEntry);
            } else
            {
                addArticles(fileEntry.getName());
            }
        }
    }

    private void addArticles(String FileName) throws IOException
    {
        Scanner reader = new Scanner(new BufferedInputStream(new FileInputStream("To_be_posted//" + FileName)));

        ArrayList<String> contents = new ArrayList<>();
        String sentence = "";
        String word;
        String snippet = " ";
        while (reader.hasNext())
        {
            word = reader.next() + " ";
            sentence += word;
            
            if (sentence.endsWith(". ") && !sentence.endsWith("Mr. ") && !sentence.endsWith("Mrs. ") && !sentence.endsWith("Ms. "))
            {
                contents.add(sentence);
                sentence = "";
            }
        }
        
        int id = 0;
        if (FileName.length() == 11)
        {
            id = Integer.parseInt(FileName.subSequence(5, 6).toString());
        } else if (FileName.length() == 12)
        {
            id = Integer.parseInt(FileName.subSequence(5, 7).toString());
        } else if (FileName.length() == 13)
        {
            id = Integer.parseInt(FileName.subSequence(5, 8).toString());
        }

        WikiArticle article = new WikiArticle(id, contents, stemmer);
        article.setID(id);
        reader.close();


        int highFreq = 1;
        for (String keyword : article.getKeywords())
        {

            if (!inverted.containsKey(keyword))
            {
                HashMap<Integer, Integer> temp = new HashMap<>();
                temp.put(id, 1);
                getInverted().put(keyword, temp);
            } else
            {
                if (getInverted().get(keyword).containsKey(id))
                {
                    //Add one to the frequency of the word
                    getInverted().get(keyword).put(id, getInverted().get(keyword).get(id) + 1);
                    if (getInverted().get(keyword).get(id) >= highFreq)
                    {
                        highFreq = getInverted().get(keyword).get(id);
                    }
                } else
                {
                    //Start counting the frequency of the word
                    getInverted().get(keyword).put(id, 1);
                }
            }
        }
        
        article.setHighFreq(highFreq);
        articles.add(article);
    }

    public WikiArticle getArticle(int index)
    {
        return articles.get(index);
    }

    public ArrayList<WikiArticle> getArticles()
    {
        return articles;
    }

    /**
     * @return the inverted
     */
    public HashMap<String, HashMap<Integer, Integer>> getInverted()
    {
        return inverted;
    }

}
