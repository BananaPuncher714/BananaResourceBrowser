package io.github.bananapuncher714;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigotUtil {
	
	public static String stripSpoilerInSpoilers( String html ) {
		Pattern pattern = Pattern.compile( "<div class=\"SpoilerTarget bbCodeSpoilerText\".*?>((?s).*?)<div class=\"SpoilerTarget bbCodeSpoilerText\".*?>((?s).*?)</div>" );
		Matcher matcher = pattern.matcher( html );
		String content = html;
		while ( matcher.find() ) {
			content = content.replaceAll( "<div class=\"SpoilerTarget bbCodeSpoilerText\".*?>((?s).*?)<div class=\"SpoilerTarget bbCodeSpoilerText\".*?>((?s).*?)</div>", "<div class=\"SpoilerTarget bbCodeSpoilerText\".*?>" + matcher.group( 1 ) + "\n" + matcher.group( 2 ) );
		}
		return content;
	}
}
