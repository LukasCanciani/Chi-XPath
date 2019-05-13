package xfp.lattice;

public class Item implements Comparable<Item> {
	
	private static int id_counter = 0;
	
	private Integer id;
	
	public Item() {
		this.id = Item.createNewID();
	}
	
	public String toString()	{
		return this.id+" ";
	}
	
	public int getId()	{
		return this.id;
	}
	
	public static synchronized int createNewID()	{
		id_counter++;
		return id_counter;
	}

	@Override
	public int compareTo(Item o) {
		return id.compareTo(o.id);
	}
}
