/**
 * 
 */
package ca.tvtrans.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import ca.tvos.app.Application;
import ca.tvos.app.ArgV;
import ca.tvos.app.RegularHandler;
import ca.tvos.base.SysIO;
import ca.tvos.boreas.token.Token;
import ca.tvos.boreas.token.TokenReader;
import ca.tvos.boreas.token.WordFormTokenizer;
import ca.tvos.util.BackupNaming;
import ca.tvos.util.OptionStore;
import ca.tvos.util.ReplacingIO;
import ca.tvos.util.StringCounter;

/**
 * @author peter
 *
 */
public class SpellCheck_v01 extends Application {

	private static final String ARGUMENT_DICTIONARY_FILE = "dictionary";
	private static final String ARGUMENT_BASE_DIRECTORY = "directory";
	private static final String ARGUMENT_HELP = "help";
	
	private static final String OPTION_DICTIONARY_FILE_NAME = "dictionary.filename";
	private static final String OPTION_BASE_DIRECTORY = "directory.home";
	
	private static final boolean MAKE_CORRECTIONS = true;
	
	private SimpleWordStore dict = null;
	private final OptionStore options = new OptionStore();
	private final RegularHandler argHandler = new RegularHandler(options);
	
	private void configureCommandLine() {
		argHandler.configureArgument('D', ARGUMENT_BASE_DIRECTORY, false, null, 
												OPTION_BASE_DIRECTORY, null, SampleData.BASE_DIRECTORY);
		argHandler.configureArgument('L', ARGUMENT_DICTIONARY_FILE, false, null, 
												OPTION_DICTIONARY_FILE_NAME, null, SampleData.DICTIONARY_FILE);
		argHandler.configureCommandArgument('?', ARGUMENT_HELP, this::displayHelp);
	}
	
	private void displayHelp() {
		SysIO.println("SpellCheck v 0.1 demonstration");
		SysIO.println("java ca.tvtrans.demo.SpellCheck_v01 [options] [filenames]");
		SysIO.println("\t-D\t--directory\tBase directory");
		SysIO.println("\t-L\t--dictionary\tDictionary file name");
		System.exit(0);
	}
	
	public SpellCheck_v01() {
		configureCommandLine();
	}
	
	private static class GlobalStats {
		public int set_count = 0;
		public int wordCount = 0;
		public int misspelledCount = 0;
		public int substitutionCount = 0;
		public void include(SpellStats partials) {
			++set_count;
			wordCount += partials.wordCount;
			misspelledCount += partials.misspelled.getTotalCount();
			substitutionCount += partials.substitutions.getTotalCount();
		}
	}
	
	private final GlobalStats grandTotal = new GlobalStats();
	
	private static class SpellStats{
		public String context;
		public int wordCount = 0;
		public final StringCounter misspelled = new StringCounter();
		public final StringCounter substitutions = new StringCounter();
		
		public void reset(String sourceName) {
			context = sourceName;
			wordCount = 0;
			misspelled.reset();
			substitutions.reset();
		}
	}

	public static void main(String[] args) {
		deploy(new SpellCheck_v01(), 
				args);
				//new String[] {"--help"} );
	}

	@Override
	public void run() {
		
		ArgV argv = argV(argHandler);
		
		while( argv.hasNext() ) {
			try {
				String operandName = argv.nextArg();
				if( !operandName.isEmpty() ) {
					spellCheckFile( operandName );
				}
			} catch (Exception e) {
				e.printStackTrace( SysIO.err() );
			}
		}

//		Path baseDir = Path.of(options.getOption(OPTION_BASE_DIRECTORY));
//
//		initDictionary( baseDir.resolve(options.getOption(OPTION_DICTIONARY_FILE_NAME)) );
		
		spellCheckFile(SampleData.TARGET_DOCUMENT);
		spellCheckFile(SampleData.SECOND_TARGET);
		
		if(grandTotal.set_count > 1 )
			displayGlobalStats(grandTotal, SysIO.out());
		
	}

	private void spellCheckFile( String fileName ) {
		Path baseDir = Path.of(options.getOption(OPTION_BASE_DIRECTORY));
		initDictionary( baseDir.resolve(options.getOption(OPTION_DICTIONARY_FILE_NAME)) );
		
		runSpellCheck(	baseDir.resolve(fileName),
						baseDir.resolve(SampleData.REJECT_FILE), 
						baseDir.resolve(SampleData.REPLACE_FILE));
				
	}
	
	private void initDictionary(Path wordFile) {
		if( dict == null ) {
			dict = new SimpleWordStore();
			//this version initialize once
			try (
					BufferedReader wordsrc = Files.newBufferedReader(wordFile, 
							SampleData.TEXT_CHARSET);	
					) {
				
				dict.loadSpellings(wordsrc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void runSpellCheck(Path target, Path reject, Path replace) {
		BackupNaming names = BackupNaming.byAugment(target);
		
		SpellStats stats = new SpellStats();

		try (
				ReplacingIO io = new ReplacingIO(names, SampleData.TEXT_CHARSET);
				PrintWriter reject_out = new PrintWriter(
						Files.newBufferedWriter( reject, SampleData.TEXT_CHARSET ) );
				PrintWriter replace_out = new PrintWriter(
						Files.newBufferedWriter( replace, SampleData.TEXT_CHARSET ) );

		) {

			stats.reset(names.primary.getFileName().toString());

			checkSpellings(io.in(), io.out(), stats );
			
			stats.misspelled.printStrings(reject_out);
			stats.substitutions.printStrings(replace_out);
			
		} catch (IOException e) {
			e.printStackTrace(SysIO.err());
		}

		grandTotal.include(stats);
		displayStats(stats, SysIO.out() );
		
	}

	private void displayStats(SpellStats stats, PrintWriter out) {
			StringBuilder sb = new StringBuilder();
			sb.append(stats.context);
			sb.append(":\t");
			sb.append( Integer.toString(stats.wordCount));
			sb.append( " words");
			if( MAKE_CORRECTIONS ) {
				sb.append( ";\t" );
				sb.append( Integer.toString(stats.substitutions.getTotalCount()));
				sb.append( " replacements");
			}
			sb.append(";\t");
			sb.append( Integer.toString( stats.misspelled.getTotalCount() ) );
			sb.append( " misspellings");
			sb.append( ".");
			SysIO.println(sb.toString());		
	}

	private void displayGlobalStats( GlobalStats stats, PrintWriter out) {
		StringBuilder sb = new StringBuilder();
		sb.append("Total: \t");
		sb.append( Integer.toString(stats.wordCount));
		sb.append( " words");
		if( MAKE_CORRECTIONS ) {
			sb.append( ";\t" );
			sb.append( Integer.toString(stats.substitutionCount));
			sb.append( " replacements");
		}
		sb.append( ";\t");
		sb.append( Integer.toString(stats.misspelledCount));
		sb.append( " misspellings");
		sb.append( ".");
		out.println( sb.toString() );
	}
	
	private void checkSpellings(	BufferedReader in, 
									PrintWriter out, 
									SpellStats stats
									) throws IOException {
		TokenReader tokenIn = new TokenReader(in);
		tokenIn.addTokenizer( new WordFormTokenizer() );
		Token token = tokenIn.readToken();
		while( token != null ) {
			String token_text = token.text().toString();
			if( is_candidate( token_text ) ) {
				++stats.wordCount;
				String repl = dict.lookup( token_text );
				if( repl == null ) {
					// unknown spelling
					stats.misspelled.add(token_text);
				}
				else if( repl.isEmpty() ) {
					//spelling is correct
					/* no action */
				}
				else {
					if (MAKE_CORRECTIONS) {
						//spelling substitution 
						stats.substitutions.add(token_text);
						//replace token with new word
						token = new CorrectionToken(repl);
					}
					else {
						//same as misspelling
						stats.misspelled.add(token_text);
					}
				}
			}
			token.writeTo(out);
			token = tokenIn.readToken();
		}
	}

	private boolean is_candidate(String wordForm) {
		if( wordForm.isEmpty() )
			return false;
		return Character.isLetter( wordForm.charAt(0) );
	}

}
