/**
 * 
 */
package ca.tvtrans.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ca.tvos.util.StringUtil;

/**
 * @author peter
 *
 */
public class SimpleWordStore {
	
	private static final char COMMENT_TAG = '#';
	private static final char REPLACE_PIVOT = '=';
	
	private final Map<String, String> words = new HashMap<>();
	
	public void loadSpellings(BufferedReader source) throws IOException {
		String ln = source.readLine();
		while( ln != null ) {
			ln = ln.strip();
			if( ! is_comment(ln) ) {
			int pivot = ln.indexOf(REPLACE_PIVOT);
			if( pivot < 0 )
				putSpelling(ln);
			else
				putSpellingReplace(ln.substring(0, pivot), ln.substring(pivot+1));
			}
			ln = source.readLine();
		}
	}

	private boolean is_comment(String ln) {
		if( ln.isEmpty() )
			return false;
		return ln.charAt(0) == COMMENT_TAG;
	}

	private void putSpelling(String word) {
		word = word.strip();
		if( word.isEmpty() )
			return;
		words.put(word, StringUtil.EMPTY_STRING);
		words.put(StringUtil.capitalize(word), StringUtil.EMPTY_STRING);
		words.put(word.toUpperCase(), StringUtil.EMPTY_STRING);
	}

	private void putSpellingReplace(String word, String replacement) {
		word = word.strip();
		replacement = replacement.strip();
		if( word.isEmpty() )
			return;
		putSpelling(replacement);
		words.put(word, replacement);
		words.put(StringUtil.capitalize(word), StringUtil.capitalize(replacement));
		words.put(word.toUpperCase(), replacement.toUpperCase());
		}

	public String lookup(String word) {
		return words.get(word);
	}
	
}
