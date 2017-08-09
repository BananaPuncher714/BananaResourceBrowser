package io.github.bananapuncher714;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileManager {
	
	public static void saveResource( HashMap< UUID, ArrayList< Resource > > resources, File datafolder ) {
		File save = new File( datafolder, "saves.yml" );
		datafolder.mkdir();
		if ( save.exists() ) save.delete();
		try {
			save.createNewFile();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		FileConfiguration file = YamlConfiguration.loadConfiguration( save );
		for ( UUID u : resources.keySet() ) {
			ArrayList< Resource > res = resources.get( u );
			ArrayList< String > urls = new ArrayList< String >();
			for ( Resource re : res ) {
				urls.add( re.getURL() );
			}
			file.set( u.toString(), urls );
		}
		try {
			file.save( save );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	
	public static HashMap< UUID, ArrayList< Resource > > loadResources( File datafolder ) {
		File save = new File( datafolder, "saves.yml" );
		HashMap< UUID, ArrayList< Resource > > res = new HashMap< UUID, ArrayList< Resource > >();
		if ( !save.exists() ) return res;
		FileConfiguration file = YamlConfiguration.loadConfiguration( save );
		for ( String uuid : file.getKeys( false ) ) {
			ArrayList< Resource > reslist = new ArrayList< Resource >();
			for ( String url : file.getStringList( uuid ) ) {
				Resource re = getResource( url );
				reslist.add( re );
			}
			res.put( UUID.fromString( uuid ), reslist );
		}
		return res;
	}
	
	public static Resource getResource( String url ) {
		String html = Util.getHTML( "https://www.spigotmc.org/" + java.net.URLDecoder.decode( url ) );
		String nameAndVersion = Util.getString( html, "<h1>((?s).*?)</h1>" );
		String name = Util.getString( nameAndVersion.replaceAll( "\n", "" ), "(.*?)<span.*?" );
		String version = Util.getString( nameAndVersion, "<.*?>(.*?)<.*?" );
		String tagline = Util.getString( html, "<p class=\"tagLine muted\">((?s).*?)</p>" );
		String secContent = Util.getString( html, "<div.*?class=\"section statsList\".*?>((?s).*?)</span>\\s*?</dd>\\s*?</dl>\\s.*</div>" ).replaceAll( "\n", "" );
		String authurl = Util.getLink( secContent, 0 );
		String auth = Util.getLinkText( secContent, 0 );
		String downloads = Util.getString( secContent, "Total Downloads:</dt><dd>(.*?)</dd></dl>" );
		String updated = Util.getString( secContent, "Last Update:</dt><dd><.....*?>(.*?)</....>" );
		String caturl = Util.getLink( secContent, 1 );
		String cat = Util.getLinkText( secContent, 1 );
		String rating = Util.getString( secContent, "<span class=\"Number\".*?>(.*?)</span>" );
		String ratingAmount = Util.getString( secContent, "class=\"Hint\">(.*?) ratings" );
		int id = Integer.parseInt( url.replaceAll( ".*\\.", "" ).replaceAll( "/", "" ) );
		Resource re = new Resource( id, name, auth, tagline, version, url, authurl, cat, caturl, rating, ratingAmount, downloads, updated );
		return re;
	}
}
