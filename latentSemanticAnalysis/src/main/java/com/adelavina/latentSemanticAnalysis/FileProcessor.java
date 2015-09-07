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
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;


public class FileProcessor {
	private File f;
	private String content;
	ArrayList<String> tokens;
	private Set<String> stopwords = new HashSet<String>();
	Properties props;
	protected FileProcessor()
	{
	}
	/*
	 * Constructor
	 */
	public FileProcessor(File f) {
		this.f = f;
		String line;
		try (
		    InputStream fis = new FileInputStream(new File("src/main/resources/stopwords"));
		    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		    BufferedReader br = new BufferedReader(isr);
		) {
		    while ((line = br.readLine()) != null) {
		        this.stopwords.add(line.toLowerCase().trim());
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, Long> getTDF() {
		System.out.println("Parsing file: "+f.getAbsolutePath());
		HashMap<String, Long> TDF = new HashMap<String, Long>();
		this.content = readFile();
		this.tokens = tokenize();
		this.tokens = stopword();
		
		Iterator<String> tksI = this.tokens.iterator();
		String token;
		while(tksI.hasNext())
		{
			token = tksI.next();
			Long val = TDF.get(token);
			if(val == null)
				TDF.put(token, 1L);
			else
				TDF.put(token, val+1);
		}
		System.out.println("-----------------------------------");
		return TDF;
	}
	
	/*
	 * Remove Stopwords
	 */
	private ArrayList<String> stopword() {
		System.out.println("Stopwording file: "+this.f.getAbsolutePath());
		for(int i = 0; i<this.tokens.size(); i++)
		{
			if(stopwords.contains(this.tokens.get(i).toLowerCase()) || this.tokens.get(i).equals(""))
			{
				this.tokens.remove(i);
				i--;
				
			}

		}
		return this.tokens;
	}

	/*
	 * Splits stuff into words 
	 */
	private ArrayList<String> tokenize() {
		ArrayList<String> tokens = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(this.content,"-_/ \t\n\r\f");
		while (st.hasMoreTokens()) {
			tokens.add(st.nextToken().replaceAll("[^a-zA-Z ]", ""));
		}
		return tokens;
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
