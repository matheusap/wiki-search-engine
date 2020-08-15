/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Searcher;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;

/**
 *
 * @author Teteu
 */
public class MyDictionary
{
    static HashSet<String> dictionary = new HashSet<String>();

    public MyDictionary(String dictionaryFileName) throws IOException
    {
        Scanner reader = new Scanner(new BufferedInputStream(new FileInputStream(dictionaryFileName)));

        while (reader.hasNext())
        {
            dictionary.add(reader.next().toLowerCase());
        }

        reader.close();
    }
}
