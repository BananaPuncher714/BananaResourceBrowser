package io.github.bananapuncher714;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;

public class Util {
	
	public static String getHTML( String url ) {
		String content = null;
		URLConnection connection = null;
		try {
			connection =  new URL( url ).openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");

			Scanner scanner = new Scanner(connection.getInputStream());
			scanner.useDelimiter("\\Z");
			content = StringEscapeUtils.unescapeHtml( scanner.next() );
			scanner.close();
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
		return content;
	}
	
	public static String resourceCutter( String html ) {
		String content = null;
		String pre = "<ol class=\"resourceList\">";
		String suf = "</ol>";
		content = html.substring( html.lastIndexOf( pre ) + pre.length() );
		content = content.substring( 0, content.indexOf( suf ) );
		return content;
	}
	
	public static String[] resourceSplitter( String resources ) {
		String[] split = resources.split( "</li>" );
		return split;
	}
	
	public static ArrayList< String[] > resourceSplitter( String[] resources ) {
		ArrayList< String[] > resourceList = new ArrayList< String[] >();
		for ( String s : resources ) {
			resourceList.add( s.split( "<div class=\"listBlock .*\">" ) );
		}
		return resourceList;
	}
	
	public static String getResourceId( String resource ) {
		return getString( resource, "id=\"resource-(.*?)\"" );
	}
	
	public static String getLink( String html, int index ) {
		Pattern pattern = Pattern.compile( "<a href=\"(.*?)\"" ) ;
		Matcher matcher = pattern.matcher( html );
		int idx = 0;
		while ( idx < index && matcher.find() ) {
			idx++;
			matcher.group();
		}
		if ( matcher.find() ) return matcher.group( 1 );
		else return null;
	}
	
	public static String getLinkText( String html, int index ) {
		Pattern pattern = Pattern.compile( "<a .*?>((?s).*?)</a>" );
		Matcher matcher = pattern.matcher( html );
		int idx = 0;
		while ( idx < index && matcher.find() ) {
			idx++;
			matcher.group();
		}
		if ( matcher.find() ) return matcher.group( 1 );
		else return null;
	}
	
	public static String getTagline( String html ) {
		return getString( html, "<div class=\"tagLine\">\n(.*?)\n</div>" );
	}
	
	public static String getRatings( String html ) {
		Pattern pattern = Pattern.compile( "class=\"ratings\" title=\"(.*?)\"" );
		Matcher matcher = pattern.matcher( html );
		if ( matcher.find() ) return matcher.group( 1 );
		else return null;
	}
	
	public static String getAmountOfRatings( String html ) {
		return getString( html, "class=\"Hint\">(.*?) rating" );
	}
	
	public static String getDownloads( String html ) {
		return getString( html, "Downloads.</dt> <dd>(.*?)</dd></dl>" );
	}
	
	public static String getLastUpdated( String html ) {
		return getString( html, "<.... class=\"DateTime\".*?>((?s).*?)</....>" );
	}
	
	public static String getVersion( String html ) {
		return getString( html, "class=\"version\">(.*?)</span>" );
	}
	
	public static String getString( String html, String regex ) {
		Pattern pattern = Pattern.compile( regex );
		Matcher matcher = pattern.matcher( html );
		if ( matcher.find() ) return matcher.group( 1 );
		else return null;
	}
	
	public static String getDescription( String html ) {
		Pattern pattern = Pattern.compile( "<blockquote.*?>((?s).*?)</blockquote>" );
		Matcher matcher = pattern.matcher( html );
		String content = "";
		if ( matcher.find() ) content = matcher.group( 1 );
		content = content.replaceAll( "<b>", ChatColor.BOLD.toString() );
		content = content.replaceAll( "</b>", ChatColor.RESET.toString() );
		return content;
	}
	
	public static String showCode( String html ) {
		Pattern pattern = Pattern.compile( "<div class=\"code\">((?s).*?)</div>" );
		Matcher matcher = pattern.matcher( html );
		String content = html;
		while ( matcher.find() ) {
			content = content.replaceFirst( "<div class=\"code\">((?s).*?)</div>", ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + matcher.group( 1 ) + ChatColor.RESET );
		}
		return content;
	}
	
	public static String stripHTML( String html ) {
		String stripped = html.replaceAll( "> .SpoilerTarget", "" );
		return stripped.replaceAll( "<.*?>", "" );
	}
	
	public static String replaceImages( String html ) {
		String content = html;
		Pattern imgpat = Pattern.compile( "<img.*?src=\".*?url=(.*?)\".*?>" );
		Matcher imgmat = imgpat.matcher( content );
		while ( imgmat.find() ) {
			content = content.replaceFirst( "<img.*?src=\".*?\".*?>", ChatColor.AQUA + java.net.URLDecoder.decode( imgmat.group( 1 ) ) + ChatColor.RESET );
		}
		return content;
	}
	
	public static String getDownloadLink( String html ) {
		String link = getString( html, "<label class=\"downloadButton \">((?s).*?)</label>" );
		link = getLink( link, 0 );
		return "https://www.spigotmc.org/" + link;
	}
	
	public static ArrayList< String > lineWrap( String verylongstring, int width, boolean indent ) {
		ArrayList< String > lore = new ArrayList< String >();
		String[] array = verylongstring.split( " " );
		StringBuilder sb = new StringBuilder();
		sb.append( ChatColor.AQUA );
		boolean end = false;
		for ( int i = 0; i < array.length; i++ ) {
			if ( sb.length() + array[ i ].length() > width ) {
				lore.add( sb.toString() );
				sb = new StringBuilder();
				sb.append( ChatColor.AQUA );
				if ( indent ) sb.append( "→" );
				end = true;
			} else {
				end = false;
			}
			sb.append( array[ i ] + " " );
		}
		if ( !end ) {
			lore.add( sb.toString() );
		}
		return lore;
	}
}
