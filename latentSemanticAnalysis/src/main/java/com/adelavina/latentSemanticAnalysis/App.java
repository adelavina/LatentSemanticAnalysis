package com.adelavina.latentSemanticAnalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

/**
 * E Size: 11x10132 U Size: 11x11 VT Size: 11x10132
 *
 */
public class App {
	static RealMatrix E;
	public static BidiMap<Integer, String> termsId = new DualHashBidiMap<Integer, String>();
	public static BidiMap<Integer, String> docsId = new DualHashBidiMap<Integer, String>();
	public static int cantDocs = 0;
	public static int cantTerms = 0;

	public static void main(String[] args) {
		FileGiver fg = new FileGiver("src/main/resources/documents");
		ArrayList<File> fs = fg.getFiles();
		Iterator<File> fsIte = fs.iterator();
		HashMap<String, Long> tf;
		HashMap<Integer, HashMap<String, Long>> TDFs = new HashMap<Integer, HashMap<String, Long>>();
		File f;

		while (fsIte.hasNext()) {
			f = fsIte.next();
			LemmatizingFileProcessor fp = new LemmatizingFileProcessor(f);
			tf = fp.getTDF();
			docsId.put(cantDocs, f.getName());
			TDFs.put(cantDocs, tf);
			cantDocs++;
			scaleTermsID(tf);
		}

		// Terms Frequency Array
		double[][] TFA = new double[cantDocs][cantTerms];

		Iterator<Integer> TDFsIte = TDFs.keySet().iterator();
		Iterator<String> termFrequencyIte;

		Integer itePoint;
		String sTerm;

		while (TDFsIte.hasNext()) {
			itePoint = TDFsIte.next();
			tf = TDFs.get(itePoint);

			termFrequencyIte = tf.keySet().iterator();

			while (termFrequencyIte.hasNext()) {
				sTerm = termFrequencyIte.next();
				TFA[itePoint][termsId.inverseBidiMap().get(sTerm)] = tf
						.get(sTerm);
			}

		}

		E = new Array2DRowRealMatrix(TFA);
	//	E = applyTFIDF(E);

		System.out.println("E Size: " + E.getRowDimension() + "x"
				+ E.getColumnDimension());

		// inspectRealMatrix(E);

		SingularValueDecomposition SVD = new SingularValueDecomposition(E);

		System.out.println("U Size: " + SVD.getU().getRowDimension() + "x"
				+ SVD.getU().getColumnDimension());
		System.out.println("VT Size: " + SVD.getVT().getRowDimension() + "x"
				+ SVD.getVT().getColumnDimension());

		System.out.println("--------------------------------");

		System.out.println("Singular values: ");

		for (double sv : SVD.getSingularValues())
			System.out.println(sv);

		showArticlesConcepts(SVD,0.4);

	}

	public static void scaleTermsID(HashMap<String, Long> tf) {
		Iterator<String> ite = tf.keySet().iterator();
		String s;
		Integer i;
		while (ite.hasNext()) {
			s = ite.next();
			i = termsId.inverseBidiMap().get(s);
			if (i == null) {
				termsId.put(cantTerms, s);
				cantTerms++;
			}
		}
	}

	public static void inspectHM(Map<String, Number> tf) {
		Iterator<String> keyIte = tf.keySet().iterator();
		String key;
		while (keyIte.hasNext()) {
			key = keyIte.next();
			System.out.println(key + " || " + tf.get(key));
		}
	}

	private static void inspectHM(BidiMap<String, Integer> tf) {
		Iterator<String> keyIte = tf.keySet().iterator();
		String key;
		while (keyIte.hasNext()) {
			key = keyIte.next();
			System.out.println(key + " || " + tf.get(key));
		}
	}

	public static void inspectRealMatrix(RealMatrix rm) {
		int qRows = rm.getRowDimension();
		int qCols = rm.getColumnDimension();
		StringBuilder sb;
		for (int i = 0; i < qRows; i++) {
			sb = new StringBuilder();
			for (int j = 0; j < qCols; j++) {
				sb.append("| " + rm.getRow(i)[j] + " |");
			}
			System.out.println(sb);
			sb = null;
		}
	}

	public static RealMatrix applyTFIDF(RealMatrix rm) {
		int cantDocsTerm = 0;
		double idf = 0;
		double[] col;
		for (int colNum = 0; colNum < rm.getColumnDimension(); colNum++) {
			col = rm.getColumn(colNum);
			for (int row = 0; row < cantDocs; row++) {
				if (col[row] != 0) {
					cantDocsTerm++;
				}
			}
			idf = Math.log(cantDocs/cantDocsTerm);
			for (int row = 0; row < cantDocs; row++)
				col[row] = col[row] * idf;
			
			rm.setColumn(colNum, col);
			cantDocsTerm = 0;
		}
		return rm;
	}

	public static void showArticlesConcepts(SingularValueDecomposition svd, double threshold) {
		int j = 0;
		for (double sv : svd.getSingularValues()) {

			System.out.println("--------------------------------");
			System.out.println("================================");
			System.out.println("Concepto " + j + " autovalor: " + sv);
			int i = 0;
			/*
			 * for (double uCol: svd.getU().getColumn(j)) {
			 * System.out.println(docsId.get(i)+": "+uCol); i++; }
			 */
			System.out.println("--------------------------------");
			System.out.println("Articulos en > "+threshold+": ");
			i = 0;

			for (double uCol : svd.getU().getColumn(j)) {
				if (uCol > threshold)
					System.out.println(docsId.get(i) + ": " + uCol);
				i++;
			}
			System.out.println("--------------------------------");
			System.out.println("Palabras en > "+threshold+": ");
			i = 0;

			for (double vtRow : svd.getVT().getRow(j)) {
				if (vtRow > threshold)
					System.out.println(termsId.get(i) + ": " + vtRow);
				i++;
			}

			j++;
		}
	}
}
