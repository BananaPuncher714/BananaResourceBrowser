package io.github.bananapuncher714;

import java.util.HashMap;

import io.github.bananapuncher714.inventory.CustomMenu;

public class RenameableMenu extends CustomMenu {
	protected HashMap< String, Object > meta = new HashMap< String, Object >();

	public RenameableMenu( String identifier, int rows, String n ) {
		super( identifier, rows, n );
	}
	
	public String changeName( String newName ) {
		String oldName = name;
		name = newName;
		return oldName;
	}
	
	public HashMap< String, Object > getMeta() {
		return meta;
	}

}
