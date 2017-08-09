package io.github.bananapuncher714;

import java.util.ArrayList;

import org.bukkit.Material;

public enum ResourceCategory {
	// Be prepared to implement this...
	// All of those case statements were driving me crazy!!
	
	bungeebukkit( "none", "bukkit", Material.LAVA_BUCKET );
	
	private static ArrayList< ResourceCategory > values = new ArrayList< ResourceCategory >();
	
	private String url, name;
	private Material mat;
	
	private ResourceCategory( String url, String name, Material mat ) {
		this.url = url;
		this.name = name;
		this.mat = mat;
	}
	
	public String getURL() {
		return url;
	}
	
	public String getName() {
		return name;
	}
	
	public Material getMaterial() {
		return mat;
	}
	
}
