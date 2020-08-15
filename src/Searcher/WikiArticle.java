/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Searcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Teteu
 */
public class WikiArticle implements Comparable<WikiArticle>
{

    private ArrayList<String> keywords = new ArrayList<>();
    private ArrayList<Double> score = new ArrayList<>();

    private double totalScore;
    private ArrayList<String> contents;
    private String snippet;
    private int ID;
    private int highFreq;
    PorterStemmer stemmer;

    public WikiArticle(int ID, ArrayList<String> contents, PorterStemmer stemmer)
    {
        this.ID = ID;
        this.contents = contents;
        this.stemmer = stemmer;
        totalScore = 0;
        snippet = "";
        generateKeywords();
    }

    /**
     * @return the keywords
     */
    public ArrayList<String> getKeywords()
    {
        return keywords;
    }

    /**
     * @return the ID
     */
    public int getID()
    {
        return ID;
    }

    /**
     * @param ID the ID to set
     */
    public void setID(int ID)
    {
        this.ID = ID;
    }

    /**
     * @return the contents
     */
    public ArrayList<String> getContents()
    {
        return contents;
    }

    /**
     * @param contents the contents to set
     */
    public void setContents(ArrayList<String> contents)
    {
        this.contents = contents;
    }

    public void generateKeywords()
    {
        for (String sentence : contents)
        {
            String[] broken = sentence.split(" ");
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
                                    keywords.add(stemmer.stem(word.trim()));
                                } else
                                {
                                    keywords.add(word.trim());
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
                                    keywords.add(stemmer.stem(newWord.trim()));
                                } else
                                {
                                    keywords.add(newWord.trim());
                                }
                            } else
                            {
                                for (int j = 0; j < wordparts.length; j++)
                                {
                                    keywords.add(wordparts[j]);
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
                            keywords.add(stemmer.stem(word.trim()));
                        } else
                        {
                            keywords.add(word.trim());
                        }
                    }
                }

            }
        }
    }

    public void addScore(double score)
    {
        this.score.add(score);
    }

    public ArrayList<Double> getScores()
    {
        return score;
    }

    public void calcTotalScore()
    {
        for (int i = 0; i < score.size(); i++)
        {
            totalScore += score.get(i);
        }
    }

    public double getTotalScore()
    {
        return totalScore;
    }

    public int compareTo(WikiArticle t)
    {
        return Double.compare(this.totalScore, t.totalScore);
    }

    /**
     * @return the highFreq
     */
    public int getHighFreq()
    {
        return highFreq;
    }

    /**
     * @param highFreq the highFreq to set
     */
    public void setHighFreq(int highFreq)
    {
        this.highFreq = highFreq;
    }

    public String getSnippet()
    {
        return snippet;
    }

    public ArrayList<String> getContentWords()
    {
        ArrayList<String> words = new ArrayList<>();
        for (String sentence : contents)
        {
            for (int i = 0; i < sentence.split(" ").length; i++)
            {
                words.add(sentence.split(" ")[i]);
            }
        }
        return words;
    }

    public int wordFreq(String word)
    {
        ArrayList<String> doc = getContentWords();
        return (int) doc.stream()
                .filter(str -> str.equals(word))
                .count();
    }

    public ArrayList<ArrayList<String>> breakSentences()
    {
        ArrayList<ArrayList<String>> rwcontents = new ArrayList<>();
        for (String sentence : contents)
        {
            rwcontents.add(new ArrayList<>(Arrays.asList(sentence.split(" "))));
        }

        return rwcontents;
    }

    public void generateSnippet(ArrayList<String> query)
    {
        ArrayList<Double> scores = new ArrayList<>();

        //(I) A density measure of significant words
        final double threshhold;
        if (contents.size() < 25)
        {
            threshhold = 7 - .1 * (25 - contents.size());
        } else if (contents.size() >= 25 && contents.size() <= 40)
        {
            threshhold = 7;
        } else
        {
            threshhold = 7 + .1 * (contents.size() - 40);
        }

        ArrayList<String> doc = getContentWords();

        //List of all the significant words
        ArrayList<String> significant = (ArrayList<String>) doc.stream()
                .filter(str -> !StopWords.contains(str))
                .filter(str -> wordFreq(str) >= threshhold)
                .collect(Collectors.toList());

        //Get how many significant words each sentence has
        ArrayList<Long> tempSig = (ArrayList<Long>) breakSentences().stream()
                .map(sent -> sent.stream().filter(w -> significant.contains(w))
                .count() / sent.size())
                .collect(Collectors.toList());

        for (long num : tempSig)
        {
            scores.add(Double.longBitsToDouble(num));
        }

        //(IV) the total number of query terms occurring in the sentence
        ArrayList<Long> tempOccurences = (ArrayList<Long>) breakSentences().stream()
                .map(sent -> sent.stream().filter(w -> query.contains(w))
                .count() / sent.size())
                .collect(Collectors.toList());

        for (int i = 0; i < tempOccurences.size(); i++)
        {
            scores.set(i, scores.get(i) + Double.longBitsToDouble(tempOccurences.get(i)));
        }

        for (int i = 0; i < contents.size(); i++)
        {
            String[] broken = contents.get(i).split(" ");

            for (int j = 0; j < query.size(); j++)
            {

                if (contents.get(i).contains(query.get(j)))
                {
                    scores.set(i, scores.get(i) + 1);

                    //(II) The longest contiguous run of query words in the sentence
                    for (int k = 0; k < broken.length; k++)
                    {
                        if (broken[k].equals(query.get(j)))
                        {
                            if ((k + 1) < broken.length)
                            {
                                if (query.contains(broken[k + 1]))
                                {
                                    scores.set(i, scores.get(i) + 1);
                                }
                            }
                        }
                    }
                }
            }

            //(III) The number of unique query terms in the sentence
            TreeSet<String> found = new TreeSet<>();
            ArrayList<String> sentence = breakSentences().get(i);
            for (int j = 0; j < sentence.size(); j++)
            {
                if (query.contains(sentence.get(j)))
                {
                    found.add(sentence.get(j));
                }
            }
            scores.set(i, scores.get(i) + found.size());

            //(V) Whether a given setnene is the 1st or 2nd line of the corresponding doc || //First sentence vs. last sentence
            if (i == 0 || i == contents.size() - 1)
            {
                scores.set(i, scores.get(i) + 2);
            } else if (i == 1 || i == contents.size() - 2)
            {
                scores.set(i, scores.get(i) + 1);
            }
        }
        HashMap<String, Double> scoredSents = new HashMap<>();
        for (int i = 0; i < contents.size(); i++)
        {
            scoredSents.put(contents.get(i), scores.get(i));
        }

        int biggest = 0;
        int second = 0;
        if(contents.size() > 1)
            second = 1;
        for (int i = 0; i < scores.size(); i++)
        {
            if (scores.get(i) > scores.get(biggest))
            {
                second = biggest;
                biggest = i;
            }
        }
        snippet += contents.get(biggest);

        if (biggest == 0 && contents.size() > 1)
        {
            for (int i = 1; i < scores.size(); i++)
            {
                if (scores.get(i) > scores.get(second))
                {
                    second = i;
                }
            }
        }
        if (biggest != second)
        {
            snippet += contents.get(second);
        }
    }
}
