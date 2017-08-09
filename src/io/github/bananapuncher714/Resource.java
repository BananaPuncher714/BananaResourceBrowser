package io.github.bananapuncher714;

public class Resource {
	String resourceURL;
	String memberURL;
	String category;
	String catName;
	String name;
	String author;
	String description;
	String version;
	String rating;
	String amountOfRatings;
	String downloads;
	String lastUpdated;
	int id;
	
	public Resource( int i, String name, String author, String description, String v, String ru, String mu, String catn, String cu, String rate, String aor, String downloads, String lastUp ) {
		id = i;
		this.name = name;
		resourceURL = ru;
		memberURL = mu;
		category = cu;
		this.author = author;
		version = v;
		this.description = description;
		rating = rate;
		amountOfRatings = aor;
		this.downloads = downloads;
		catName = catn;
		lastUpdated = lastUp;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getURL() {
		return resourceURL;
	}
	
	public String getAuthorURL() {
		return memberURL;
	}
	
	public String getCategoryURL() {
		return category;
	}
	
	public String getCategory() {
		return catName;
	}
	
	public String getRating() {
		return rating;
	}
	
	public String getRateAmount() {
		return amountOfRatings;
	}
	
	public String getDownloads() {
		return downloads;
	}
	
	public String getLastUpdated() {
		return lastUpdated;
	}
	
	public String getVersion() {
		return version;
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof Resource ) {
			Resource re = ( Resource ) o;
			return re.getId() == id;
		}
		return false;
	}

}
