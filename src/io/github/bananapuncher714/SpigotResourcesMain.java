package io.github.bananapuncher714;

import io.github.bananapuncher714.inventory.ActionItem.ActionItem;
import io.github.bananapuncher714.inventory.ActionItem.ActionItemIntention;
import io.github.bananapuncher714.inventory.ActionItem.ButtonItem;
import io.github.bananapuncher714.inventory.components.BananaButton;
import io.github.bananapuncher714.inventory.components.BoxPanel;
import io.github.bananapuncher714.inventory.components.RevolvingPanel;
import io.github.bananapuncher714.inventory.items.ItemBuilder;
import io.github.bananapuncher714.inventory.panes.OptionPane;
import io.github.bananapuncher714.inventory.panes.PagedOptionPane;
import io.github.bananapuncher714.inventory.util.ICEResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotResourcesMain extends JavaPlugin implements Listener {
	HashMap< UUID, ArrayList< Resource > > favorites = new HashMap< UUID, ArrayList< Resource > >();
	
	String filled_star = "★";
	String empty_star = "☆";
	
	HashMap< Integer, Resource > resources = new HashMap< Integer, Resource >();
	HashMap< UUID, RenameableMenu > menus = new HashMap< UUID, RenameableMenu >();
	HashMap< UUID, Location > reading = new HashMap< UUID, Location >();
	
	int resourcePageCacheSize = 10;
	int page = 1;
	
	boolean isSpigot;
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents( this, this );
		try {
			Class.forName( "org.spigotmc.SpigotConfig" );
			isSpigot = true;
		} catch ( ClassNotFoundException e ) {
			isSpigot = false;
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask( this, new Runnable() {
			@Override
			public void run() {
				favorites = FileManager.loadResources( getDataFolder() );
				for ( ArrayList< Resource > re : favorites.values() ) {
					for ( Resource r : re ) {
						if ( !resources.containsKey( r.getId() ) ) {
							resources.put( r.getId(), r );
						}
					}
				}
			}
		} );
	}
	
	@Override
	public void onDisable() {
		FileManager.saveResource( favorites, getDataFolder() );
		resources.clear();
		menus.clear();
	}
	
	@Override
	public boolean onCommand( CommandSender s, Command c, String l, String[] a ) {
		if ( !( s instanceof Player ) ) return false;
		Player p = ( Player ) s;
		if ( c.getName().equalsIgnoreCase( "resources" ) ) {
			if ( p.hasPermission( "bananaresourcebrowser.use" ) ) {
				p.sendMessage( ChatColor.GREEN + "Loading the spigot resources..." );
				Bukkit.getScheduler().scheduleSyncDelayedTask( this, new Runnable() {
				@Override
				public void run() {
					RenameableMenu menu = getResourceInv( p );
					menus.put( p.getUniqueId(), menu );
					p.openInventory( menu.getInventory( true ) );
					}
				} );
				return true;
			} else {
				p.sendMessage( ChatColor.RED + "You do not have permission to use this command!" );
				return false;
			}
		} else {
			return false;
		}
	}
	
	@EventHandler
	public void onInventoryClickEvent( InventoryClickEvent e ) {
		if ( e.getSlot() != e.getRawSlot() || e.getInventory() == null || e.getInventory().getType() != InventoryType.CHEST ) return;
		Player p = ( Player ) e.getWhoClicked();
		RenameableMenu menu = menus.get( p.getUniqueId() );
		if ( menu == null ) return;
		if ( e.getInventory().getName().equalsIgnoreCase( menu.getName() ) ) {
			ICEResponse response = menu.parseICE( e );
			if ( response.getActionItem() != null ) {
				e.setCancelled( true );
				ActionItem item = response.getActionItem();
				ArrayList< String > actions = item.getActions();
				if ( menu.getIdentifier().equalsIgnoreCase( "spigot-resources" ) ) {
					// Get the page
					int page = ( int ) menu.getMeta().get( "page" );
					// Get the order
					String order = ( String ) menu.getMeta().get( "order" );
					// Get the category
					String category = ( String ) menu.getMeta().get( "category" );
					
					//Handle the buttons
					if ( actions.contains( "change page" ) ) {
						if ( !"favorites".equalsIgnoreCase( category ) ) {
							if ( item.getName().equalsIgnoreCase( "next page" ) ) {
								page++;
							} else if ( item.getName().equalsIgnoreCase( "prev page" ) ) {
								page--;
							} else if ( item.getName().equalsIgnoreCase( "next 10 pages" ) ) {
								page = page + 10;
							} else {
								page = page - 10;
							}
							if ( page < 2 ) ( ( BananaButton ) menu.getComponent( "prev page" ) ).hide( true );
							else ( ( BananaButton ) menu.getComponent( "prev page" ) ).hide( false );
							if ( page < 11 ) ( ( BananaButton ) menu.getComponent( "prev 10 page" ) ).hide( true );
							else ( ( BananaButton ) menu.getComponent( "prev 10 page" ) ).hide( false );
						} else {
							RevolvingPanel rPanel = ( RevolvingPanel ) menu.getComponent( "resource panel" );
							PagedOptionPane favorites = ( PagedOptionPane ) rPanel.getPanes().get( "favorites" );
							if ( item.getName().equalsIgnoreCase( "next page" ) ) {
								favorites.setPage( favorites.getPage() + 1 );
							} else if ( item.getName().equalsIgnoreCase( "prev page" ) ) {
								favorites.setPage( favorites.getPage() - 1 );
							}
							menu.changeName( getNewName( category, favorites.getPage() + 1 ) );
							p.openInventory( menu.getInventory( true ) );
							return;
						}
					}
					if ( actions.contains( "change cat page" ) ) {
						BoxPanel mainPanel = ( BoxPanel ) menu.getComponent( "category panel" );
						PagedOptionPane cats = ( PagedOptionPane ) mainPanel.getPanes().get( "category pane" );
						if ( item.getName().equalsIgnoreCase( "next page" ) ) {
							cats.setPage( cats.getPage() + 1 );
						} else if ( item.getName().equalsIgnoreCase( "prev page" ) ) {
							cats.setPage( cats.getPage() - 1 );
						}
						menu.updateInventory();
					}
					if ( actions.contains( "change order" ) ) {
						order = item.getName();
						page = 1;
						if ( "favorites".equalsIgnoreCase( category ) ) return;
						BoxPanel mainPanel = ( BoxPanel ) menu.getComponent( "order panel" );
						OptionPane orders = ( OptionPane ) mainPanel.getPanes().get( "order pane" );
						for ( ActionItem aitem : orders.getContents() ) {
							if ( aitem.getItem().containsEnchantment( Enchantment.DURABILITY ) ) aitem.getItem().removeEnchantment( Enchantment.DURABILITY );
						}
						item.getItem().addUnsafeEnchantment( Enchantment.DURABILITY, 1 );
						menu.getMeta().put( "order", order );
					} if ( actions.contains( "change category" ) ) {
						category = item.getName();
						int favpage = 0;
						RevolvingPanel rPanel = ( RevolvingPanel ) menu.getComponent( "resource panel" );
						if ( category != null && category.equalsIgnoreCase( "favorites" ) ) {
							PagedOptionPane favorites = ( PagedOptionPane ) rPanel.getPanes().get( "favorites" );
							rPanel.unhidePane( favorites );
							fillFavoritesPane( favorites, p );
							favpage = favorites.getPage();
						} else {
							rPanel.unhidePane( "resources" );
							BananaButton n10p = ( BananaButton ) menu.getComponent( "next 10 page" );
							n10p.hide( false );
							BananaButton np = ( BananaButton ) menu.getComponent( "next page" );
							np.hide( false );
							BananaButton pp = ( BananaButton ) menu.getComponent( "prev page" );
							pp.hide( true );
						}
						page = 1;
						BoxPanel mainPanel = ( BoxPanel ) menu.getComponent( "category panel" );
						PagedOptionPane orders = ( PagedOptionPane ) mainPanel.getPanes().get( "category pane" );
						for ( ActionItem aitem : orders.getAllContents() ) {
							if ( aitem.getItem().containsEnchantment( Enchantment.DURABILITY ) ) aitem.getItem().removeEnchantment( Enchantment.DURABILITY );
						}
						item.getItem().addUnsafeEnchantment( Enchantment.DURABILITY, 1 );
						menu.getMeta().put( "category", category );
						if ( category != null && category.equalsIgnoreCase( "favorites" ) ) {
							menu.changeName( getNewName( category, favpage + 1 ) );
							p.openInventory( menu.getInventory( true ) );
							return;
						}
					}
					if ( actions.contains( "change page" ) || actions.contains( "change order" ) || actions.contains( "change category" ) ) {
						RevolvingPanel mainPanel = ( RevolvingPanel ) menu.getComponent( "resource panel" );
						OptionPane resources = ( OptionPane ) mainPanel.getPanes().get( "resources" );
						p.sendMessage( ChatColor.GREEN + "Loading page..." );
						final String forder = order;
						final String fcat = category;
						final int fpage = page;
						final Player fplayer = p; 
						Bukkit.getScheduler().scheduleSyncDelayedTask( this, new Runnable() {
							@Override
							public void run() {
								fillResourcePane( resources, forder, fpage, fcat, fplayer );
								menu.getMeta().put( "page", fpage );
								menu.changeName( getNewName( fcat, fpage )  );
								p.openInventory( menu.getInventory( true ) );
							}
						} );
					}
					if ( actions.contains( "view resource" ) ) {
						// Aha!! Send the player a clickable link
						if ( e.getClick().isRightClick() ) {
							Resource re = resources.get( Integer.parseInt( item.getName() ) );
							ItemStack stack = item.getItem();
							if ( favorites.containsKey( p.getUniqueId() ) ) {
								ArrayList< Resource > resources = favorites.get( p.getUniqueId() );
								if ( resources.contains( re ) ) {
									if ( stack.containsEnchantment( Enchantment.DURABILITY ) ) stack.removeEnchantment( Enchantment.DURABILITY );
									p.sendMessage( ChatColor.YELLOW + "You have removed " + ChatColor.GREEN + ChatColor.BOLD + re.getName() + ChatColor.YELLOW + " from your favorites!" );
									resources.remove( re );
									RevolvingPanel rPanel = ( RevolvingPanel ) menu.getComponent( "resource panel" );
									PagedOptionPane favorites = ( PagedOptionPane ) rPanel.getPanes().get( "favorites" );
									fillFavoritesPane( favorites, p );
									p.openInventory( menu.getInventory( true ) );
								} else {
									p.sendMessage( ChatColor.YELLOW + "You have added " + ChatColor.GREEN + ChatColor.BOLD + re.getName() + ChatColor.YELLOW + " to your favorites!" );
									stack.addUnsafeEnchantment( Enchantment.DURABILITY, 1 );
									resources.add( re );
									menu.updateInventory();
								}
							} else {
								ArrayList< Resource > resources = new ArrayList< Resource >();
								p.sendMessage( ChatColor.YELLOW + "You have added " + ChatColor.GREEN + ChatColor.BOLD + re.getName() + ChatColor.YELLOW + " to your favorites!" );
								resources.add( re );
								stack.addUnsafeEnchantment( Enchantment.DURABILITY, 1 );
								favorites.put( p.getUniqueId(), resources );
							}
							return;
						}
						p.sendMessage( ChatColor.GREEN + "Loading more information..." );
						Bukkit.getScheduler().scheduleSyncDelayedTask( this, new Runnable() {
							@Override
							public void run() {
								Resource re = resources.get( Integer.parseInt( item.getName() ) );
								String url = Util.getHTML( "https://www.spigotmc.org/" + re.getURL() );
								String dl = Util.getDownloadLink( url );
								if ( isSpigot ) {
									p.sendMessage( ChatColor.GREEN + ChatColor.BOLD.toString() + "Download link:" );
									SpigotMessager.sendClickableLink( p, dl, dl, ChatColor.GOLD + "Click to download the plugin" );
								} else {
									p.sendMessage( ChatColor.GREEN + ChatColor.BOLD.toString() + "Download link:" );
									p.sendMessage( ChatColor.AQUA + dl );
								}
								displayResource( p, re );
								reading.put( p.getUniqueId(), p.getLocation() );
							}
						} );
					}
				}
			}
		}
	}
	
	public RenameableMenu getResourceInv( Player p ) {
		page = Math.max( 1, page );
		RenameableMenu menu = new RenameableMenu( "spigot-resources", 6, "RENAME ME" );
		HashMap< String, Object > meta = menu.getMeta();
		meta.put( "sort", "last_updated" );
		meta.put( "page", 1 );
		meta.put( "category", null );
		menu.changeName( getNewName( null, 1 ) );
		RevolvingPanel mainPanel = new RevolvingPanel( "resource panel", 18 );
		BoxPanel orderPanel = new BoxPanel( "order panel", 9 );
		BoxPanel catPanel = new BoxPanel( "category panel", 1 );
		BananaButton nextPage = new BananaButton( "next page", 53 );
		BananaButton prevPage = new BananaButton( "prev page", 45 );
		BananaButton next10Pages = new BananaButton( "next 10 page", 52 );
		BananaButton prev10Pages = new BananaButton( "prev 10 page", 46 );
		BananaButton catNextPage = new BananaButton( "cat next page", 8 );
		BananaButton catPrevPage = new BananaButton( "cat prev page", 0 );
		PagedOptionPane favs = new PagedOptionPane( "favorites" );
		OptionPane resourcePane = new OptionPane( "resources" );
		OptionPane orderPane = new OptionPane( "order pane" );
		PagedOptionPane categoryPane = new PagedOptionPane( "category pane" );
		categoryPane.addButtons( catNextPage, catPrevPage );
		for ( int i = 0; i < 9; i++ ) {
			switch( i ) {
			case 0: orderPane.addActionItem( new ActionItem( "last_updated", "change order", ActionItemIntention.CUSTOM, new ItemBuilder( Material.BOOK_AND_QUILL, 1, ( byte ) 0, ChatColor.RESET.toString() + ChatColor.BOLD + getOrderName( "last_updated" ), true ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 2: orderPane.addActionItem( new ActionItem( "resource_date", "change order", ActionItemIntention.CUSTOM, new ItemBuilder( Material.WORKBENCH, 1, ( byte ) 0, ChatColor.RESET.toString() + ChatColor.BOLD + getOrderName( "resource_date" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 4: orderPane.addActionItem( new ActionItem( "rating_weighted", "change order", ActionItemIntention.CUSTOM, new ItemBuilder( Material.DIAMOND, 1, ( byte ) 0, ChatColor.RESET.toString() + ChatColor.BOLD + getOrderName( "rating_weighted" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 6: orderPane.addActionItem( new ActionItem( "download_count", "change order", ActionItemIntention.CUSTOM, new ItemBuilder( Material.EMERALD, 1, ( byte ) 0, ChatColor.RESET.toString() + ChatColor.BOLD + getOrderName( "download_count" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 8: orderPane.addActionItem( new ActionItem( "title", "change order", ActionItemIntention.CUSTOM, new ItemBuilder( Material.PAPER, 1, ( byte ) 0, ChatColor.RESET.toString() + ChatColor.BOLD + getOrderName( "title" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			default: orderPane.addActionItem( new ActionItem( "NONE", "NOTHING", ActionItemIntention.NONE, new ItemBuilder( Material.STAINED_GLASS_PANE, 1, ( byte ) 15, " ", false ).getItem() ) ); break;
			}
		}
		for ( int i = -1; i < 28; i++ ) {
			switch( i ) {	
			case -1: categoryPane.addActionItem( new ActionItem( null, "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( null ), 1, ( byte ) 0, getCategoryName( null ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 0: categoryPane.addActionItem( new ActionItem( "bungee-bukkit.2/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/bungee-bukkit.2/" ), 1, ( byte ) 0, getCategoryName( "bungee-bukkit.2/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 1: categoryPane.addActionItem( new ActionItem( "transportation.5/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/transportation.5/" ), 1, ( byte ) 0, getCategoryName( "transportation.5/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 2: categoryPane.addActionItem( new ActionItem( "chat.6/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/chat.6/" ), 1, ( byte ) 0, getCategoryName( "chat.6/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 3: categoryPane.addActionItem( new ActionItem( "tools-and-utilities.7/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/tools-and-utilities.7/" ), 1, ( byte ) 0, getCategoryName( "tools-and-utilities.7/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 4: categoryPane.addActionItem( new ActionItem( "misc.8/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/misc.8/" ), 1, ( byte ) 0, getCategoryName( "misc.8/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 5: categoryPane.addActionItem( new ActionItem( "bungee-proxy.3/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/bungee-proxy.3/" ), 1, ( byte ) 0, getCategoryName( "bungee-proxy.3/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 6: categoryPane.addActionItem( new ActionItem( "libraries-apis.9/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/libraries-apis.9/" ), 1, ( byte ) 0, getCategoryName( "libraries-apis.9/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 7: categoryPane.addActionItem( new ActionItem( "transportation.10/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/transportation.10/" ), 1, ( byte ) 0, getCategoryName( "transportation.10/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 8: categoryPane.addActionItem( new ActionItem( "chat.11/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/chat.11/" ), 1, ( byte ) 0, getCategoryName( "chat.11/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 9: categoryPane.addActionItem( new ActionItem( "tools-and-utilities.12/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/tools-and-utilities.12/" ), 1, ( byte ) 0, getCategoryName( "tools-and-utilities.12/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 10: categoryPane.addActionItem( new ActionItem( "misc.13/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/misc.13/" ), 1, ( byte ) 0, getCategoryName( "misc.13/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 11: categoryPane.addActionItem( new ActionItem( "bukkit.4/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/bukkit.4/" ), 1, ( byte ) 0, getCategoryName( "bukkit.4/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 12: categoryPane.addActionItem( new ActionItem( "chat.14/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/chat.14/" ), 1, ( byte ) 0, getCategoryName( "chat.14/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 13: categoryPane.addActionItem( new ActionItem( "tools-and-utilities.15/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/tools-and-utilities.15/" ), 1, ( byte ) 0, getCategoryName( "tools-and-utilities.15/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 14: categoryPane.addActionItem( new ActionItem( "misc.16/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/misc.16/" ), 1, ( byte ) 0, getCategoryName( "misc.16/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 15: categoryPane.addActionItem( new ActionItem( "fun.17/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/fun.17/" ), 1, ( byte ) 0, getCategoryName( "fun.17/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 16: categoryPane.addActionItem( new ActionItem( "world-management.18/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/world-management.18/" ), 1, ( byte ) 0, getCategoryName( "world-management.18/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 17: categoryPane.addActionItem( new ActionItem( "mechanics.22/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/mechanics.22/" ), 1, ( byte ) 0, getCategoryName( "mechanics.22/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 18: categoryPane.addActionItem( new ActionItem( "economy.23/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/economy.23/" ), 1, ( byte ) 0, getCategoryName( "economy.23/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 19: categoryPane.addActionItem( new ActionItem( "game-mode.24/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/game-mode.24/" ), 1, ( byte ) 0, getCategoryName( "game-mode.24/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 20: categoryPane.addActionItem( new ActionItem( "skript.25/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/skript.25/" ), 1, ( byte ) 0, getCategoryName( "skript.25/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 21: categoryPane.addActionItem( new ActionItem( "libraries-apis.26/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/libraries-apis.26/" ), 1, ( byte ) 0, getCategoryName( "libraries-apis.26/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 22: categoryPane.addActionItem( new ActionItem( "standalone.19/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/standalone.19/" ), 1, ( byte ) 0, getCategoryName( "standalone.19/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 23: categoryPane.addActionItem( new ActionItem( "universal.21/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/universal.21/" ), 1, ( byte ) 0, getCategoryName( "universal.21/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 24: categoryPane.addActionItem( new ActionItem( "web.27/", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( getCategoryItem( "resources/categories/web.27/" ), 1, ( byte ) 0, getCategoryName( "web.27/" ), false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) ); break;
			case 25: categoryPane.addActionItem( new ActionItem( "favorites", "change category", ActionItemIntention.CUSTOM, new ItemBuilder( Material.GLOWSTONE_DUST, 1, ( byte ) 0, ChatColor.YELLOW + ChatColor.BOLD.toString() + "Favorites", false ).addFlags( ItemFlag.HIDE_ENCHANTS ).getItem() ) );
			default: break;
			}
		}
		fillResourcePane( resourcePane, "last_updated", 1, null, p );
		
		ItemBuilder arrow = new ItemBuilder( Material.ARROW, 1, ( byte ) 0, "Change page" );
		nextPage.setItem( new ButtonItem( "next page", "change page", ActionItemIntention.NEXT, arrow.setName( ChatColor.WHITE + ChatColor.BOLD.toString() + "Next page" ).getItem() ) );
		prevPage.setItem( new ButtonItem( "prev page", "change page", ActionItemIntention.PREVIOUS, arrow.setName( ChatColor.WHITE + ChatColor.BOLD.toString() + "Previous page" ).getItem() ) );
		next10Pages.setItem( new ButtonItem( "next 10 pages", "change page", ActionItemIntention.NEXT, arrow.setName( ChatColor.WHITE + ChatColor.BOLD.toString() + "Next 10 pages" ).getItem() ) );
		prev10Pages.setItem( new ButtonItem( "prev 10 pages", "change page", ActionItemIntention.PREVIOUS, arrow.setName( ChatColor.WHITE + ChatColor.BOLD.toString() + "Previous 10 pages" ).getItem() ) );
		catPrevPage.setItem( new ButtonItem( "prev page", "change cat page", ActionItemIntention.PREVIOUS, arrow.setName( ChatColor.WHITE + ChatColor.BOLD.toString() + "Next categories" ).getItem() ) );
		catNextPage.setItem( new ButtonItem( "next page", "change cat page", ActionItemIntention.NEXT, arrow.setName( ChatColor.WHITE + ChatColor.BOLD.toString() + "Previous categories" ).getItem() ) );
		prevPage.hide( true );
		prev10Pages.hide( true );
		prevPage.addPane( resourcePane );
		prev10Pages.addPane( resourcePane );
		nextPage.addPane( resourcePane );
		next10Pages.addPane( resourcePane );
		favs.addButtons( nextPage, prevPage );
		mainPanel.addPane( resourcePane, favs );
		mainPanel.unhidePane( resourcePane );
		orderPanel.addPane( orderPane );
		catPanel.addPane( categoryPane );
		menu.addComponent( mainPanel, orderPanel, catPanel, nextPage, prevPage, next10Pages, prev10Pages, catNextPage, catPrevPage );
		return menu;
	}
	
	public String getNewName( String category, int page ) {
		return getCategoryName( category ) + ChatColor.BLACK + ChatColor.BOLD.toString() + " - " + ChatColor.DARK_BLUE + ChatColor.BOLD.toString() + "Page " + page; 
	}
	
	public void fillResourcePane( OptionPane pane, String order, int page, String category, Player p ) {
		pane.getContents().clear();
		ArrayList< Resource > orders = getResources( page, order, category );
		for ( Resource re : orders ) {
			ArrayList< String > lore = lineWrap( re.getDescription() );
			lore.add( ChatColor.WHITE + "Created by " + ChatColor.LIGHT_PURPLE + re.getAuthor() );
			lore.add( ChatColor.WHITE + "Category: " + ChatColor.YELLOW + re.getCategory() );
			StringBuilder sb = new StringBuilder();
			sb.append( ChatColor.YELLOW );
			for ( int i = 1; i < 6; i++ ) {
				if ( Double.parseDouble( re.getRating() ) >= i ) sb.append( filled_star );
				else sb.append( empty_star );
			}
			sb.append( ChatColor.GRAY + " - " + ChatColor.WHITE.toString() + ChatColor.BOLD + re.getRating() + ChatColor.GRAY + " - " + ChatColor.WHITE.toString() + ChatColor.BOLD + re.getRateAmount() + " rating(s)" );
			lore.add( sb.toString() );
			lore.add( ChatColor.GOLD + "Downloads: " + ChatColor.WHITE + ChatColor.BOLD.toString() + re.downloads );
			if ( re.getLastUpdated() != null ) lore.add( ChatColor.WHITE + "Last updated on " + re.getLastUpdated() );
			lore.add( "" );
			lore.add( ChatColor.GRAY + "Left click to view" );
			lore.add( ChatColor.GRAY + "Right click to add/remove from favorites" );
			ItemBuilder reitem = new ItemBuilder( getCategoryItem( re.getCategoryURL() ), 1, ( byte ) 0, ChatColor.GREEN + ChatColor.BOLD.toString() + re.getName() + " " + ChatColor.GRAY + re.getVersion() + ChatColor.WHITE + " - " + ChatColor.DARK_GREEN + re.getId(), favorites.containsKey( p.getUniqueId() ) && favorites.get( p.getUniqueId() ).contains( re ), lore.toArray( new String[ lore.size() ] ) );
			reitem.addFlags( ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS );
			pane.addActionItem( new ActionItem( re.getId() + "", "view resource", ActionItemIntention.CUSTOM, reitem.getItem() ) );
		}
	}
	
	private ArrayList< Resource > getResources( int page, String order, String category ) {
		ArrayList< Resource > resourcees = new ArrayList< Resource >();
		String html;
		if ( category == null ) {
			html = Util.getHTML( "https://www.spigotmc.org/resources/?order=" + order + "&page=" + page );
		} else {
			html = Util.getHTML( "https://www.spigotmc.org/resources/categories/" + category + "/?order=" + order + "&page=" + page );
		}
		html = Util.resourceCutter( html );
		String[] resources = Util.resourceSplitter( html );
		ArrayList< String[] > resourceList = Util.resourceSplitter( resources );
		for ( String[] arr : resourceList ) {
			if ( arr.length != 4 ) continue;
			String url = Util.getLink( arr[ 2 ], 0 );
			String murl = Util.getLink( arr[ 2 ], 1 );
			String cat = Util.getLink( arr[ 2 ], 3 );
			if ( cat == null ) {
				cat = "resources/categories/" + category;
			}
			String catName = Util.getLinkText( arr[ 2 ], 3 );
			if ( catName == null ) {
				catName = ChatColor.stripColor( getCategoryName( category ) );
			}
			String name = Util.getLinkText( arr[ 2 ], 0 );
			String auth = Util.getLinkText( arr[ 2 ], 1 );
			String tag = Util.getTagline( arr[ 2 ] );
			String rating = Util.getRatings( arr[ 3 ] );
			String aor = Util.getAmountOfRatings( arr[ 3 ] );
			String downloads = Util.getDownloads( arr[ 3 ] );
			String lastu = Util.getLastUpdated( arr[ 3 ] );
			String v = Util.getVersion( arr[ 2 ] );
			int id = Integer.parseInt( Util.getResourceId( arr[ 0 ] ) );
			Resource re = new Resource( id, name, auth, tag, v, url, murl, catName, cat, rating, aor, downloads, lastu );
			this.resources.put( id, re );
			resourcees.add( re );
		}
		return resourcees;
	}
	
	private ArrayList< String > lineWrap( String verylongstring ) {
		return Util.lineWrap( verylongstring, 35, false );
	}
	
	private Material getCategoryItem( String cat ) {
		if ( cat == null ) return Material.ENDER_CHEST;
		switch( cat ) {
		case "resources/categories/bungee-bukkit.2/": return Material.JACK_O_LANTERN;
		case "resources/categories/transportation.5/": return Material.BOAT;
		case "resources/categories/chat.6/": return Material.CHEST;
		case "resources/categories/tools-and-utilities.7/": return Material.GOLD_SPADE;
		case "resources/categories/misc.8/": return Material.YELLOW_FLOWER;
		case "resources/categories/bungee-proxy.3/": return Material.EYE_OF_ENDER;
		case "resources/categories/libraries-apis.9/": return Material.BOOKSHELF;
		case "resources/categories/transportation.10/": return Material.MINECART;
		case "resources/categories/chat.11/": return Material.SIGN;
		case "resources/categories/tools-and-utilities.12/": return Material.GOLD_AXE;
		case "resources/categories/misc.13/": return Material.STRING;
		case "resources/categories/bukkit.4/": return Material.LAVA_BUCKET;
		case "resources/categories/chat.14/": return Material.PAPER;
		case "resources/categories/tools-and-utilities.15/": return Material.IRON_PICKAXE;
		case "resources/categories/misc.16/": return Material.SLIME_BALL;
		case "resources/categories/fun.17/": return Material.CAKE;
		case "resources/categories/world-management.18/": return Material.WOOD_DOOR;
		case "resources/categories/mechanics.22/": return Material.REDSTONE;
		case "resources/categories/economy.23/": return Material.DIAMOND;
		case "resources/categories/game-mode.24/": return Material.BOOK;
		case "resources/categories/skript.25/": return Material.FEATHER;
		case "resources/categories/libraries-apis.26/": return Material.RECORD_3;
		case "resources/categories/standalone.19/": return Material.ANVIL;
		case "resources/categories/universal.21/": return Material.COBBLESTONE;
		case "resources/categories/web.27/": return Material.ENDER_PORTAL_FRAME;
		default: return Material.ENDER_CHEST;
		}
	}
	
	private String getCategoryName( String category ) {
		if ( category == null ) return ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "All";
		if ( category.equalsIgnoreCase( "favorites" ) ) return ChatColor.YELLOW + ChatColor.BOLD.toString() + "Favorites";
		switch( category ) {
		case "bungee-bukkit.2/": return ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Bungee-Bukkit";
		case "transportation.5/": return ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Transportation";
		case "chat.6/": return ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Chat";
		case "tools-and-utilities.7/": return ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Tools & Utilities";
		case "misc.8/": return ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "Misc";
		case "bungee-proxy.3/": return ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Bungee-Proxy";
		case "libraries-apis.9/": return ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Libraries/APIs";
		case "transportation.10/": return ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Transportation";
		case "chat.11/": return ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Chat";
		case "tools-and-utilities.12/": return ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Tools & Utilities";
		case "misc.13/": return ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Misc";
		case "bukkit.4/": return ChatColor.GOLD + ChatColor.BOLD.toString() + "Bukkit";
		case "chat.14/": return ChatColor.GOLD + ChatColor.BOLD.toString() + "Chat";
		case "tools-and-utilities.15/": return ChatColor.GOLD + ChatColor.BOLD.toString() + "Tools & Utilities";
		case "misc.16/": return ChatColor.GOLD + ChatColor.BOLD.toString() + "Misc";
		case "fun.17/": return ChatColor.GOLD + ChatColor.BOLD.toString() + "Fun";
		case "world-management.18/": return ChatColor.GOLD + ChatColor.BOLD.toString() + "World Management";
		case "mechanics.22/": return ChatColor.GOLD + ChatColor.BOLD.toString() + "Mechanics";
		case "economy.23/": return ChatColor.GOLD + ChatColor.BOLD.toString() + "Economy";
		case "game-mode.24/": return ChatColor.GOLD + ChatColor.BOLD.toString() + "Game Mode";
		case "skript.25/": return ChatColor.GOLD + ChatColor.BOLD.toString() + "Skript";
		case "libraries-apis.26/": return "Libraries/APIs";
		case "standalone.19/": return ChatColor.DARK_BLUE + ChatColor.BOLD.toString() + "Standalone";
		case "universal.21/": return ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + "Universal";
		case "web.27/": return ChatColor.DARK_RED + ChatColor.BOLD.toString() + "Website";
		default: return ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "All";
		}
	}
	
	private String getOrderName( String order ) {
		switch( order ) {
		case "last_updated": return "Last Updated";
		case "resource_date": return "Submission Date";
		case "rating_weighted": return "Rating";
		case "download_count": return "Downloads";
		case "title": return "Title";
		default: return "Unknown";
		}
	}
	
	@EventHandler
	public void onPlayerMoveEvent( PlayerMoveEvent e ) {
		Player p = e.getPlayer();
		if ( !reading.containsKey( p.getUniqueId() ) ) return;
		Location pl = reading.get( p.getUniqueId() );
		if ( p.getLocation().getDirection().angle( pl.getDirection() ) > 0 || p.getLocation().distanceSquared( pl ) > .5 ) {
			p.openInventory( menus.get( p.getUniqueId() ).getInventory( false ) );
			reading.remove( p.getUniqueId() );
		}
	}
	
	public void displayResource( Player p, Resource re ) {
		String desc = Util.getDescription( Util.getHTML( "https://www.spigotmc.org/" + re.getURL() ) );
		ItemStack book = new ItemStack( Material.WRITTEN_BOOK );
		BookMeta meta = ( BookMeta ) book.getItemMeta();
		if ( isSpigot ) {
			meta = SpigotMessager.editBook( meta, desc, re );
		} else {
			meta.addPage( "\n\n\n" + re.getName() );
			StringBuilder sb = new StringBuilder();
			desc = desc.replaceFirst( "</dt>\n<dd><ul class=\"plainList\"><li>", " " );
			desc = desc.replaceAll( "<li>", ChatColor.DARK_BLUE + "•" + ChatColor.BLACK );
			desc = Util.stripHTML( desc );
			desc = desc.trim().replaceAll( "\n^\\s+$", "" );
			desc = desc.replaceAll( " +", " " );
			desc = desc.replaceAll( "\n\\s*?\n", "\n" );
			desc = desc.replaceAll( "\n", "<newline> " );
			boolean completed = false;
			for ( String s : desc.split( "\\s" ) ) {
				if ( ChatColor.stripColor( sb.toString() + s ).length() > 230 ) {
					meta.addPage( sb.toString().replaceAll( "<newline>", "\n" ) );
					sb = new StringBuilder();
					completed = true;
				} else {
					completed = false;
				}
				sb.append( s + " " );
			}
			if ( completed == false ) {
				meta.addPage( sb.toString().replaceAll( "<newline>", "\n" ) );
			}
		}
		book.setItemMeta( meta );
		BookUtil.openBook( book, p );
	}
	
	public void fillFavoritesPane( PagedOptionPane pane, Player p ) {
		if ( !favorites.containsKey( p.getUniqueId() ) ) return;
		pane.getAllContents().clear();
		ArrayList< Resource > res = favorites.get( p.getUniqueId() );
		for ( Resource re : res ) {
			ArrayList< String > lore = lineWrap( re.getDescription() );
			lore.add( ChatColor.WHITE + "Created by " + ChatColor.LIGHT_PURPLE + re.getAuthor() );
			lore.add( ChatColor.WHITE + "Category: " + ChatColor.YELLOW + re.getCategory() );
			StringBuilder sb = new StringBuilder();
			sb.append( ChatColor.YELLOW );
			for ( int i = 1; i < 6; i++ ) {
				if ( Double.parseDouble( re.getRating() ) >= i ) sb.append( filled_star );
				else sb.append( empty_star );
			}
			sb.append( ChatColor.GRAY + " - " + ChatColor.WHITE.toString() + ChatColor.BOLD + re.getRating() + ChatColor.GRAY + " - " + ChatColor.WHITE.toString() + ChatColor.BOLD + re.getRateAmount() + " rating(s)" );
			lore.add( sb.toString() );
			lore.add( ChatColor.GOLD + "Downloads: " + ChatColor.WHITE + ChatColor.BOLD.toString() + re.downloads );
			if ( re.getLastUpdated() != null ) lore.add( ChatColor.WHITE + "Last updated on " + re.getLastUpdated() );
			lore.add( "" );
			lore.add( ChatColor.GRAY + "Left click to view" );
			lore.add( ChatColor.GRAY + "Right click to add/remove from favorites" );
			ItemBuilder reitem = new ItemBuilder( getCategoryItem( re.getCategoryURL() ), 1, ( byte ) 0, ChatColor.GREEN + ChatColor.BOLD.toString() + re.getName() + " " + ChatColor.GRAY + re.getVersion() + ChatColor.WHITE + " - " + ChatColor.DARK_GREEN + re.getId(), lore.toArray( new String[ lore.size() ] ) );
			reitem.addFlags( ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS );
			pane.addActionItem( new ActionItem( re.getId() + "", "view resource", ActionItemIntention.CUSTOM, reitem.getItem() ) );
		}
	}
}
