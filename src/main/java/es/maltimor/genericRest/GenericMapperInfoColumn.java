package es.maltimor.genericRest;

public class GenericMapperInfoColumn {
	private String name;
	private String type;
	private String size;
	private boolean fullText;
	private boolean primary;
	private boolean foreign;// TODO : a quien pertenece
	private String secuenceName;
	private String description;

	public GenericMapperInfoColumn() {
		this.name = "";
		this.type = "";
		this.size = "";
		this.fullText = false;
		this.primary = false;
		this.foreign = false;
		this.secuenceName = "";
		this.description = "";
	}

	public GenericMapperInfoColumn(String name, String type, String size, boolean fullText, String secuence, String desc) {
		this.name = name;
		this.type = type;
		this.size = size;
		this.fullText = fullText;
		this.primary = false;
		this.foreign = false;
		this.secuenceName = secuence;
		this.description = desc;
	}
	
	public String toString(){
		String res = "{ \"name\": \""+name+"\", \"type\": \""+type+"\", \"size\": \""+size+"\", \"secuenceName\": \""+secuenceName+"\", \"description\": \""+description+"\" }";
		return res;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public boolean isForeign() {
		return foreign;
	}

	public void setForeign(boolean foreign) {
		this.foreign = foreign;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	public String getSecuenceName() {
		return secuenceName;
	}

	public void setSecuenceName(String secuenceName) {
		this.secuenceName = secuenceName;
	}

	public boolean isFullText() {
		return fullText;
	}

	public void setFullText(boolean fullText) {
		this.fullText = fullText;
	}
}
