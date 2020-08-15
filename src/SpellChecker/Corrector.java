/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpellChecker;

import Searcher.StopWords;
import Searcher.Wiki;
import Searcher.WikiArticle;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;
import javafx.util.Pair;

/**
 *
 * @author Teteu
 */
public class Corrector
{

    HashMap<String, ArrayList<String>> dictionary = new HashMap<>();
    ArrayList<Pair<String, Double>> scores = new ArrayList<>();
    HashMap<Double, ArrayList<String>> logs = new HashMap<>();
    Wiki wiki;

    public Corrector(String dictionaryFileName, String logsFileName, Wiki wiki) throws IOException
    {
        this.wiki = wiki;

        Scanner reader = new Scanner(new BufferedInputStream(new FileInputStream(dictionaryFileName)));

        while (reader.hasNext())
        {
            String word = reader.next().toLowerCase();
            String code = toSoundex(word);

            if (!dictionary.containsKey(code))
            {
                dictionary.put(code, new ArrayList<>());
            }

            ArrayList<String> codeGroup = dictionary.get(code);
            codeGroup.add(word);
            dictionary.put(code, codeGroup);
        }

        reader.close();
        reader = new Scanner(new BufferedInputStream(new FileInputStream(logsFileName)));
        reader.nextLine();

        while (reader.hasNext())
        {
            String next = reader.nextLine();
            String[] broken = next.split("\t");
            double session = Double.parseDouble(broken[0]);

            if (!logs.containsKey(session))
            {
                logs.put(session, new ArrayList<>());
            }

            ArrayList<String> newValue = logs.get(session);
            newValue.add(broken[1]);
            logs.put(session, newValue);

        }

        reader.close();
    }

    public String toSoundex(String word)
    {
        char[] brokenWord = word.toCharArray();
        String code = "";
        code += brokenWord[0];
        code = code.toUpperCase();

        for (int i = 1; i < brokenWord.length; i++)
        {
            char current = brokenWord[i];
            if (current == 'a' || current == 'e' || current == 'i'
                    || current == 'o' || current == 'u' || current == 'y'
                    || current == 'h' || current == 'w')
            {
                brokenWord[i] = '-';
            } else if (i > 0 && current == brokenWord[i - 1])
            {
                continue;
            } else if (current == 'b' || current == 'f' || current == 'p'
                    || current == 'v')
            {
                code += '1';
            } else if (current == 'c' || current == 'g' || current == 'j'
                    || current == 'k' || current == 'q' || current == 's'
                    || current == 'x' || current == 'z')
            {
                code += '2';
            } else if (current == 'd' || current == 't')
            {
                code += '3';
            } else if (current == 'l')
            {
                code += '4';
            } else if (current == 'm' || current == 'n')
            {
                code += '5';
            } else if (current == 'r')
            {
                code += '6';
            }

            if (code.length() == 4)
            {
                break;
            }
        }
        while (code.length() < 4)
        {
            code += "0";
        }

        return code;
    }

    public int LevenshteinDistance(String a, String b)
    {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
        {
            costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++)
        {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++)
            {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];

    }

    public double scoreWord(String scoring, String fromQuery)
    {
        //word = P(e|w) x (occurences of word/how many words in doc)
//        if(scoring.equals("screening"))
//        {
//            System.out.print("This shouldn't print");
//        }
        double frequency = 0;
        double totalWordCount = 0;
        double totalScore = 0;
        for (WikiArticle article : wiki.getArticles())
        {
            frequency += article.wordFreq(scoring);
            totalWordCount += article.getContentWords().stream()
                .filter(str -> !StopWords.contains(str))
                .count();
                    
            
        }
        totalScore = frequency / totalWordCount;

        int denominator = 0;
        for (ArrayList<String> sessionQueries : logs.values())
        {
            ArrayList<Integer> misspelledIndex = new ArrayList<Integer>();
            int indexInCurrentSession = -1;
            int queryToSkip = -1;
            for (int i = 0; i < sessionQueries.size(); i++)
            {
                if (queryToSkip < 0)
                {
                    String currentQuery = sessionQueries.get(i);
                    if (currentQuery.contains(scoring))
                    {
                        String[] splitQuery = currentQuery.split(" ");
                        for (int j = 0; j < splitQuery.length; j++)
                        {
                            if (splitQuery[j].equals(scoring))
                            {
                                indexInCurrentSession = j;
                                queryToSkip = i;
                                i = 0;
                                break;
                            }
                        }
                    }
                }
                else if (i == queryToSkip)
                {
                    continue;
                } //This branch  looks at that same index where the other word was
                // found, to see if it's the scoring word. If so, we know the
                // scoring word was changed to from the misspelled word. score++.
                else
                {
                    String currentQuery = sessionQueries.get(i);
                    if (currentQuery.split(" ").length == sessionQueries.get(queryToSkip).split(" ").length)
                    {
                        denominator++;
                    }
                }
            }
        }
        int numerator = 0;
        for (ArrayList<String> sessionQueries : logs.values())
        {
            ArrayList<Integer> misspelledIndex = new ArrayList<Integer>();
            int indexInCurrentSession = -1;
            int queryToSkip = -1;

            for (int i = 0; i < sessionQueries.size(); i++)
            {
                //This branch is just to find the misspelled word in the queries
                if (queryToSkip < 0)
                {
                    String currentQuery = sessionQueries.get(i);
                    if (currentQuery.contains(fromQuery))
                    {
                        String[] splitQuery = currentQuery.split(" ");
                        for (int j = 0; j < splitQuery.length; j++)
                        {
                            if (splitQuery[j].equals(fromQuery))
                            {
                                indexInCurrentSession = j;
                                queryToSkip = i;
                                i = 0;
                                break;
                            }
                        }

                    }
                } else if (i == queryToSkip)
                {
                    continue;
                } //This branch  looks at that same index where the other word was
                // found, to see if it's the scoring word. If so, we know the
                // scoring word was changed to from the misspelled word. score++.
                else
                {
                    String currentQuery = sessionQueries.get(i);
                    if (currentQuery.contains(scoring))
                    {
                        numerator++;
                    }
                }
            }
        }
        
        if(numerator == 0 || denominator == 0)
            return 0;
        totalScore = totalScore * ((double)numerator/(double)denominator);
        return totalScore;
    }

    public String correct(String inputQuery)
    {
        inputQuery = inputQuery.toLowerCase();
        String[] splitQuery = inputQuery.split(" ");
        String newQuery = "";
        String printResult = "";
        for (int i = 0; i < splitQuery.length; i++)
        {
            String word = splitQuery[i];
            String code = toSoundex(word);
            
            if (dictionary.get(code).contains(word))
            {
                newQuery += splitQuery[i] + " ";
                continue;
            }      
             
            
            printResult = "Soundex code: " + code + "\nSuggested Corrections: ";
            ArrayList<String> possibleWords = (ArrayList<String>) dictionary.get(code).stream()
                    .filter(str -> LevenshteinDistance(str, word) <= 2)
                    .collect(Collectors.toList());
           
            for(String suggested : possibleWords)
            {
                printResult += suggested + ",";
            }
            printResult = printResult.substring(0,printResult.length()-1);
            
            if(possibleWords.size() == 0)
            {
                newQuery += splitQuery[i] + " ";
                continue;
            }
            //Score all possible words 
            for (String possibleWord : possibleWords)
            {
                scores.add(new Pair(possibleWord, scoreWord(possibleWord, word)));
            }

            int topScore = 0;
            for (int j = 0; j < scores.size(); j++)
            {
                if (scores.get(j).getValue() > scores.get(topScore).getValue())
                {
                    topScore = j;
                }
            }

            newQuery += possibleWords.get(topScore) + " ";
        }
        //System.out.println("\033[0;1mThis Text Is Bold");
        System.out.println("Original Query: " + inputQuery + "\tCorrected Query: " + newQuery);
        System.out.println(printResult);
        return newQuery.trim();
    }
}
