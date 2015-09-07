package com.adelavina.latentSemanticAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class LemmatizingFileProcessor extends FileProcessor {
	private File f;
	private String content;
	ArrayList<String> tokens;
	private Set<String> stopwords = new HashSet<String>();
	Properties props;
	StanfordCoreNLP pipeline;

	/*
	 * Constructor
	 */
	public LemmatizingFileProcessor(File f) {
		this.f = f;
		String line;
		try (InputStream fis = new FileInputStream(new File(
				"src/main/resources/stopwords"));
				InputStreamReader isr = new InputStreamReader(fis,
						Charset.forName("UTF-8"));
				BufferedReader br = new BufferedReader(isr);) {
			while ((line = br.readLine()) != null) {
				this.stopwords.add(line.toLowerCase().trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");

		// StanfordCoreNLP loads a lot of models, so you probably
		// only want to do this once per execution
		this.pipeline = new StanfordCoreNLP(props);
	}

	public HashMap<String, Long> getTDF() {
		System.out.println("Parsing file: " + f.getAbsolutePath());
		HashMap<String, Long> TDF = new HashMap<String, Long>();
		this.content = readFile();
		this.tokens = tokenize();
		this.tokens = filter();
		this.tokens = stopword();

		Iterator<String> tksI = this.tokens.iterator();
		String token;
		while (tksI.hasNext()) {
			token = tksI.next();
			Long val = TDF.get(token);
			if (val == null)
				TDF.put(token, 1L);
			else
				TDF.put(token, val + 1);
		}
		System.out.println("-----------------------------------");
		return TDF;
	}

	/*
	 * Remove Stopwords
	 */
	private ArrayList<String> stopword() {
		System.out.println("Stopwording file: " + this.f.getAbsolutePath());
		for (int i = 0; i < this.tokens.size(); i++) {
			if (stopwords.contains(this.tokens.get(i).toLowerCase())
					|| this.tokens.get(i).equals("")) {
				this.tokens.remove(i);
				i--;

			}

		}
		return this.tokens;
	}
	
	/*
	 * Clears noise from NLP 
	 */
	private ArrayList<String> filter() {
		ArrayList<String> tks = new ArrayList<String>();
		String ns;
		for(String s : this.tokens)
		{
			ns = s.replaceAll("[^a-zA-Z ]", "");
			if(ns.length() > 1)
				tks.add(ns);
		}
		return tks;
	}
	

	/*
	 * Splits stuff into words
	 */
	private ArrayList<String> tokenize() {
		ArrayList<String> lemmas = new ArrayList<String>();

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(this.content);

		// run all Annotators on this text
		this.pipeline.annotate(document);

		// Iterate over all of the sentences found
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			// Iterate over all tokens in a sentence
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// Retrieve and add the lemma for each word into the list of
				// lemmas
				lemmas.add(token.get(LemmaAnnotation.class));
			}
		}

		return lemmas;
	}

	/*
	 * Reads File
	 */
	private String readFile() {
		FileReader reader;
		String content;
		try {
			reader = new FileReader(this.f);
			char[] chars = new char[(int) this.f.length()];
			try {
				reader.read(chars);
				content = new String(chars);
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return content.toLowerCase();
	}

}
