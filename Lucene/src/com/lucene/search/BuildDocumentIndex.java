package com.lucene.search;


import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;




public class BuildDocumentIndex 
{
	private static final String INDEX_DIR = "index/train/NGramAnalyzer_LowerCaseFilter/";
	private static String csvFile = "source/train/train_lucene_documents.tsv";

	

	public static void main(String[] args) throws Exception 
	{
		IndexWriter writer = createWriter();
		List<Document> documents = new ArrayList<>();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = "\t";
        
        
        
        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] concepts = line.split(cvsSplitBy);
        		Document document = createDocument(concepts[0], concepts[1]);
        		documents.add(document);
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
        
		//Let's clean everything first
		writer.deleteAll();
		writer.addDocuments(documents);
		writer.commit();
	    writer.close();
        
	}

	private static Document createDocument(String cui, String conceptName) 
	{
    	Document document = new Document();
    	document.add(new StringField("cui", cui , Field.Store.YES));
    	document.add(new TextField("conceptName", conceptName , Field.Store.YES));
    	return document;
    }

	private static IndexWriter createWriter() throws IOException 
	{
		FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
//		StandardAnalyzer analyzer = new StandardAnalyzer();
		NGramAnalyzer analyzer = new NGramAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(dir, config);
		return writer;
	}
}



