package com.lucene.search;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SearchIndex 
{
	private static final String INDEX_DIR = "index/train/NGramAnalyzer_LowerCaseFilter";
	private static String csvFile = "data/split/dev.tsv";
	private static String csvOutput = "output/train/NGramAnalyzer_LowerCaseFilter/dev_prediction.tsv";


	public static void main(String[] args) throws Exception 
	{
		IndexSearcher searcher = createSearcher();
		List<String> mentions = new ArrayList<>();
		
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = "\t";
        
        
        
        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] rows = line.split(cvsSplitBy);
                mentions.add(rows[1]);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        

        FileWriter csvWriter = new FileWriter(csvOutput); 
        
        for (String mention : mentions) {
        	System.out.println(mention);
    		//Search by conceptName
    		TopDocs foundDocs1 = searchByCn(mention, searcher);
    		
    		System.out.println("Total Results :: " + foundDocs1.totalHits);
    		
    		for (ScoreDoc sd : foundDocs1.scoreDocs) 
    		{
    			Document d = searcher.doc(sd.doc);
    			csvWriter.append(String.format(d.get("cui")));  
    			csvWriter.append("\t");
//    			System.out.println(d.get("conceptName"));
//    			csvWriter.append(d.get("conceptName"));  
//    			csvWriter.append("\t");  
    		}
    		csvWriter.append("\n");
        }
        csvWriter.flush();  
        csvWriter.close();  
		
	}
	

//	private static TopDocs searchByCn(String conceptName, IndexSearcher searcher) throws Exception
//	{
//        Analyzer analyzer = new Analyzer() {
//            @Override
//            protected TokenStreamComponents createComponents(String s) {
//                Tokenizer source = new NGramTokenizer(MIN_N_GRAMS, MAX_N_GRAMS);
//                TokenStream firstfilter = new LowerCaseFilter(source);
//                TokenStream filter = new SnowballFilter(firstfilter, "English");
//                return new TokenStreamComponents(source, filter);
//
//            }
//        };
//		QueryParser qp = new QueryParser("conceptName", analyzer);
//		Query idQuery = qp.parse(conceptName);
//		TopDocs hits = searcher.search(idQuery, 10);
//		return hits;
//	}
	
	private static TopDocs searchByCn(String conceptName, IndexSearcher searcher) throws Exception
	{
		NGramAnalyzer analyzer = new NGramAnalyzer();
//		StandardAnalyzer analyzer = new StandardAnalyzer();
		QueryParser qp = new QueryParser("conceptName", analyzer);
		Query idQuery = qp.parse(QueryParser.escape(conceptName));
		TopDocs hits = searcher.search(idQuery, 10);
		return hits;
	}

	private static IndexSearcher createSearcher() throws IOException {
		Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		return searcher;
	}
}
