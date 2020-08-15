/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpellChecker;

import Searcher.Searcher;
import Searcher.Wiki;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Teteu
 */
public class Main
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        String dictionaryFileName = "dictionary.txt";
        String logsFileName = "query_log.txt";
        
        String inputQuery = "scheduled moving screning";
        
        Wiki wiki = new Wiki();
        wiki.populateWiki(new File("To_be_posted"));
        
        Searcher searcher = new Searcher(dictionaryFileName,wiki);
        
        Corrector myCorrector = new Corrector(dictionaryFileName, logsFileName, wiki);
        String correctedQuery = myCorrector.correct(inputQuery);
        
        
        //searcher.search(correctedQuery);
        searcher.search(correctedQuery);
    }

}
