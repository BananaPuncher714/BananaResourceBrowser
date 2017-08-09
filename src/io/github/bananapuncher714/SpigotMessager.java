package io.github.bananapuncher714;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

public class SpigotMessager {
	static HashMap< String, Class< ? > > classCache = new HashMap< String, Class< ? > >();
	
	public static void sendClickableLink( Player p, String msg, String url, String hoverMessage  ) {
		TextComponent message = new TextComponent( msg );
		message.setColor( ChatColor.AQUA );
		message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, url ) );
		message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( hoverMessage ).create() ) );
		p.spigot().sendMessage( message );
	}
	
	static {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			Class< ? > ichat = Class.forName( "net.minecraft.server." + version + ".IChatBaseComponent" );
			classCache.put( "ChatSerializer", ichat.getDeclaredClasses()[ 0 ] );
			classCache.put( "IChatBaseComponent", ichat );
			classCache.put( "CraftBookMeta", Class.forName( "org.bukkit.craftbukkit." + version + ".inventory.CraftMetaBook" ) );
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static Class< ? > getNMSClass( String name ) {
		return classCache.get( name );
	}
	
	public static BookMeta editBook( BookMeta meta, String desc, Resource re ) {
		try {
			return editBook( getNMSClass( "IChatBaseComponent" ), meta, desc, re );
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
		}
		return meta;
	}
	
	private static < T > BookMeta editBook( Class< T > chatbasecomponent, BookMeta meta, String desc, Resource re ) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		HashMap< String, String > components = new HashMap< String, String >();
		HashMap< String, String > images = new HashMap< String, String >();
		HashMap< String, String > spoilers = new HashMap< String, String >();
		HashMap< String, String > links = new HashMap< String, String >();
		List< T > pages;

		//get the pages
		try {
		    pages = ( List< T > ) getNMSClass( "CraftBookMeta" ).getDeclaredField( "pages" ).get( meta );
		} catch ( ReflectiveOperationException ex ) {
		    ex.printStackTrace();
		    return meta;
		}
		
		// This is the first page, the intro on the plugin
		TextComponent firstPage = new TextComponent();
		firstPage.setColor( ChatColor.DARK_GREEN );
		firstPage.setBold( true );
		firstPage.setUnderlined( true );
		firstPage.addExtra( "\n\n\n" + re.getName() );
		String organizedStuff = "";
		ArrayList< String > lore;
		{
			lore = Util.lineWrap( re.getDescription(), 35, false );
			lore.add( ChatColor.WHITE + "Created by " + ChatColor.LIGHT_PURPLE + re.getAuthor() );
			lore.add( ChatColor.WHITE + "Category: " + ChatColor.YELLOW + re.getCategory() );
			StringBuilder sb = new StringBuilder();
			sb.append( ChatColor.YELLOW );
			for ( int i = 1; i < 6; i++ ) {
				if ( Double.parseDouble( re.getRating() ) >= i ) sb.append( "★" );
				else sb.append( "☆" );
			}
			sb.append( ChatColor.GRAY + " - " + ChatColor.WHITE.toString() + ChatColor.BOLD + re.getRating() + ChatColor.GRAY + " - " + ChatColor.WHITE.toString() + ChatColor.BOLD + re.getRateAmount() + " rating(s)" );
			lore.add( sb.toString() );
			lore.add( ChatColor.GOLD + "Downloads: " + ChatColor.WHITE + ChatColor.BOLD.toString() + re.downloads );
			if ( re.getLastUpdated() != null ) lore.add( ChatColor.WHITE + "Last updated on " + re.getLastUpdated() );
		}
		for ( String s : lore ) {
			if ( organizedStuff.isEmpty() ) organizedStuff = s;
			else organizedStuff = organizedStuff + "\n" + s;
		}
		firstPage.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( organizedStuff ).create() ) );
		Object fpage = getNMSClass( "ChatSerializer" ).getMethod( "a", String.class ).invoke( null, ComponentSerializer.toString( firstPage ) );
		pages.add( ( T ) fpage );
		
		// Here we convert all the images
		Pattern imgpattern = Pattern.compile( "<img.*?src=\".*?url=(.*?)\".*?>" );
		Matcher imgmatcher = imgpattern.matcher( desc );
		while ( imgmatcher.find() ) {
			String key = RandomStringUtils.random( 5, true, true );
			String url = imgmatcher.group( 1 );
			desc = desc.replaceFirst( "<img.*?>", " " + key + " " );
			images.put( key, url );
		}
		Pattern imgpattern2 = Pattern.compile( "<img.*?src=\"(.*?)\".*?>" );
		Matcher imgmatcher2 = imgpattern2.matcher( desc );
		while ( imgmatcher2.find() ) {
			String key = RandomStringUtils.random( 5, true, true );
			String url = imgmatcher2.group( 1 );
			desc = desc.replaceFirst( "<img.*?>", " " + key + " " );
			images.put( key, url );
		}
		
		/* This is the spoiler part
		desc = SpigotUtil.stripSpoilerInSpoilers( desc );
		Pattern spattern = Pattern.compile( "<div class=\"SpoilerTarget bbCodeSpoilerText\".*?>((?s).*?)</div>" );
		Matcher smatcher = spattern.matcher( desc );
		while ( smatcher.find() ) {
			String[] arr = Util.stripHTML( smatcher.group( 1 ) ).split( "\n" );
			StringBuilder sb = new StringBuilder();
			for ( String s : arr ) {
				for ( String ls : Util.lineWrap( s, 65, true ) ) {
					sb.append( ls + "\n" );
				}
			}
			String key = RandomStringUtils.random( 6, true, true );
			desc = desc.replaceFirst( "<div class=\"SpoilerTarget bbCodeSpoilerText\".*?>(?s).*?</div>", key );
			spoilers.put( key, sb.toString() );
		}*/
		
		// Here we convert all the code parts to hover text instead
		Pattern pattern = Pattern.compile( "<div class=\"code\">((?s).*?)</div>" );
		Matcher matcher = pattern.matcher( desc );
		while ( matcher.find() ) {
			String[] arr = Util.stripHTML( matcher.group( 1 ) ).split( "\n" );
			StringBuilder sb = new StringBuilder();
			for ( String s : arr ) {
				for ( String ls : Util.lineWrap( s, 65, true ) ) {
					sb.append( ls + "\n" );
				}
			}
			String key = RandomStringUtils.random( 6, true, true );
			desc = desc.replaceFirst( "<div class=\"code\">(?s).*?</div>", " " + key + " " );
			components.put( key, sb.toString().replaceAll( "//", ChatColor.WHITE + "//" ).replaceAll( "#", ChatColor.WHITE + "#" ) );
		}
		
		// Link
		Pattern linkPat = Pattern.compile( "<a.*?href=\"(.*?)\".*?>(.*?)</a>" );
		Matcher linkMat = linkPat.matcher( desc );
		while ( linkMat.find() ) {
			String key = RandomStringUtils.random( 6, true, true );
			String url = linkMat.group( 1 );
			if ( linkMat.group( 1 ).equalsIgnoreCase( linkMat.group( 2 ) ) ) {
				desc = desc.replaceFirst( "<a.*?href=\"(.*?)\".*?>(.*?)</a>", " " + key + " " );
			} else {
				desc = desc.replaceFirst( "<a.*?href=\"(.*?)\".*?>(.*?)</a>", " " + linkMat.group( 2 ) + " " + key + " " );
			}
			links.put( key, url );
		}
		
		// Here starts the making of the pages
		TextComponent newPage = new TextComponent();
		desc = desc.replaceFirst( "</dt>\n<dd><ul class=\"plainList\"><li>", " " );
		desc = desc.replaceAll( "<li>", ChatColor.DARK_BLUE + "•" + ChatColor.RESET );
		desc = Util.stripHTML( desc );
		desc = desc.trim().replaceAll( "\n^\\s+$", "" );
		desc = desc.replaceAll( " +", " " );
		desc = desc.replaceAll( "\n\\s*?\n", "\n" );
		desc = desc.replaceAll( "\n", "<newline> " );
		boolean completed = false;
		for ( String s : desc.split( "\\s" ) ) {
			if ( ChatColor.stripColor( newPage.toPlainText() + s ).length() > 200 ) {
				Object page = getNMSClass( "ChatSerializer" ).getMethod( "a", String.class ).invoke( null, ComponentSerializer.toString( newPage ) );
				pages.add( ( T ) page );
				newPage = new TextComponent();
				completed = true;
			} else {
				completed = false;
			}
			if ( images.containsKey( ChatColor.stripColor( s.replaceAll( "<newline>", "" ) ) ) ) {
				TextComponent imgComp = new TextComponent( "[IMG]\n" );
				imgComp.setColor( ChatColor.AQUA );
				String hoverText = "";
				String imgurl = java.net.URLDecoder.decode( images.get( ChatColor.stripColor( s.replaceAll( "<newline>", "" ) ) ) );
				for ( int i = 0; i < imgurl.length(); i = i + 30 ) {
					if ( hoverText.isEmpty() ) {
						hoverText = ChatColor.AQUA + imgurl.substring( i, Math.min( i + 30, imgurl.length() ) );
					} else {
						hoverText = hoverText + "\n" + ChatColor.AQUA + imgurl.substring( i, Math.min( i + 30, imgurl.length() ) );
					}
				}
				imgComp.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, imgurl ) );
				imgComp.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( hoverText ).create() ) );
				newPage.addExtra( imgComp );
				newPage.addExtra( ChatColor.RESET + " " );
			} else if ( components.containsKey( ChatColor.stripColor( s.replaceAll( "<newline>", "" ) ) ) ) {
				TextComponent code = new TextComponent( "[CODE]\n" );
				code.setColor( ChatColor.LIGHT_PURPLE );
				code.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( components.get( ChatColor.stripColor( s.replaceAll( "<newline>", "" ) ) ) ).create() ) );
				newPage.addExtra( code );
				newPage.addExtra( ChatColor.RESET + " " );
			} else if ( spoilers.containsKey( ChatColor.stripColor( s.replaceAll( "<newline>", "" ) ) ) ) {
				TextComponent spoiler = new TextComponent( "[SPOILER]" );
				spoiler.setColor( ChatColor.GOLD );
				spoiler.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( spoilers.get( ChatColor.stripColor( s.replaceAll( "<newline>", "" ) ) ) ).create() ) );
				newPage.addExtra( spoiler );
				newPage.addExtra( ChatColor.RESET + " " );
			} else if ( links.containsKey( ChatColor.stripColor( s.replaceAll( "<newline>", "" ) ) ) ) {
				TextComponent link = new TextComponent( "[LINK]\n" );
				link.setColor( ChatColor.GOLD );
				String hoverText = "";
				String imgurl = java.net.URLDecoder.decode( links.get( ChatColor.stripColor( ChatColor.stripColor( s.replaceAll( "<newline>", "" ) ) ) ) );
				for ( int i = 0; i < imgurl.length(); i = i + 30 ) {
					if ( hoverText.isEmpty() ) {
						hoverText = ChatColor.YELLOW + imgurl.substring( i, Math.min( i + 30, imgurl.length() ) );
					} else {
						hoverText = hoverText + "\n" + ChatColor.YELLOW + imgurl.substring( i, Math.min( i + 30, imgurl.length() ) );
					}
				}
				link.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, imgurl ) );
				link.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( hoverText ).create() ) );
				newPage.addExtra( link );
				newPage.addExtra( ChatColor.RESET + " " );
			} else {
				newPage.addExtra( s.replaceAll( "<newline>", "\n" ) + " " );
			}
		}
		if ( completed == false ) {
			Object page = getNMSClass( "ChatSerializer" ).getMethod( "a", String.class ).invoke( null, ComponentSerializer.toString( newPage ) );
			pages.add( ( T ) page );
		}
		
		//add the page to the list of pages
		return meta;
	}

}
