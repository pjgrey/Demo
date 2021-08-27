/**
 * 
 */
package ca.tvtrans.demo;

import ca.tvos.boreas.token.TextToken;
import ca.tvos.boreas.token.TokenRegistry;

/**
 * @author peter
 *
 */
public class CorrectionToken extends TextToken {
	
	private static final int ttype = TokenRegistry.registerNew();

	public CorrectionToken(String text) {
		super(text, 0, text.length(), ttype);
	}

}
