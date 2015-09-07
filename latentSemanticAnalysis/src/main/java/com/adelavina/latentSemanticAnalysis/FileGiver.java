package com.adelavina.latentSemanticAnalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileGiver {
	private String route;

	public FileGiver(String route) {
		this.route = route;
	}
	
	public ArrayList<File> getFiles()
	{
		File d = new File(route);
		if(!d.isDirectory())
			return null;
		File[] fs = d.listFiles();
		ArrayList<File> al = new ArrayList<File>();
		for(File f: fs)
			al.add(f);
		return al;
	}
	
}
