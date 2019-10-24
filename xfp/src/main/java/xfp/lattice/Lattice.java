package xfp.lattice;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
/**
 * Main class to explore the lattice of Items (Items shall be Fixpoints but should be general)
 * General idea: 
 *  - Item class wraps elements to be expressed in the lattice (provides a synchronised id and can be extended)
 *   - Solution class represents a potential solution to the problem expressed by the lattice, provides a isValid method checking if any item 
 *   	is included exactly once
 * 
 * Lattice class should support several exploration strategies and methods, basically downwards and upwards traversal:
 * - getLowerLatticeLevel(int level, Set<Integer> element): returns all sets contained in level "level" of the lattice 
 * 		starting from the element "element" of the lattice 
 * 		where "level" has to be lower than the size of input elements
 * 		Example: Input: level=3, element<1,2,3,4>; Output: <<1,2,3>,<1,2,4>,<2,3,4>>  
 * - getNextUpLatticeLevel(Set<Integer> element, Set<Integer> keys): returns all possible lattice elements included in the lattice one level up
 * 		based on the given input element, input: keys of all items plus element to start from 
 * 		Example: Input: element<1,2,3>, keys<1,2,3,4,5>; Output: <<1,2,3,4>,<1,2,3,5>> 
 * 
 * - getLatticeLevel(int level) and getLatticeLevelIndices(int level): Convenience method if all elements from a specific lattice level 
 * 		shall be retrieved; different implementations, both, obviously, don't really scale
 * 		Example: Keys <1,2,3,4,5,6> Input: 1; Output: <<1>,<2>,<3>,<4>,<5>,<6>> 
 * 
 * TODO: store traversed parts
 * */

public class Lattice<T extends Item> {
	// new based on ID
	private Map<Integer, T> items_ids = Collections.synchronizedMap(new HashMap<Integer, T>());
	
	//string representation of set is stored here, each set is a treeset, thus sorted, we store the strings sorted as well for convenience
	private Set<String> traversed = Collections.synchronizedSet(new TreeSet<>()); 
	
	@Deprecated
	private HashMap<Integer, Set<Set<Integer>>> levels_ids = new HashMap<>();
	
	public Lattice(Set<T> items)	{
		for (T item : items)	{
			items_ids.put(item.getId(), item);
		}
	}
	
	/** Lattice is incremental, but not synchronised -> not thread safe
	 * This would need to lock access to the keyset (e.g. for iterators) */
	public void addItem(T item)	{
		items_ids.put(item.getId(), item);
	}
	
	public void addTraversed(Set<Integer> lattice_element)	{
		this.traversed.add(Lattice.toTraversalID(lattice_element));
	}
	
	public boolean isTraversed(Set<Integer> lattice_element)	{
		return this.traversed.contains(Lattice.toTraversalID(lattice_element));
	}
	
	/** return all subsets in a specific lattice level
	 * computes all lower levels and stores their signature for quick access (memoization)
	 * will not scale */
	@Deprecated
	public Set<Set<Integer>> getLatticeLevelRecursive(int level)	{
		Set<Set<Integer>> lattice = new HashSet<>();
		if (level > items_ids.size() || level < 1) return lattice;
		
		if (this.levels_ids.containsKey(level)) {
			return this.levels_ids.get(level);
		}
		
		if (level==1) {
			Iterator<Integer> it = items_ids.keySet().iterator();
			while (it.hasNext())	{
				Set<Integer> singleton = new TreeSet<>();
				singleton.add(it.next());
				lattice.add(singleton);
			}
			this.levels_ids.put(1, lattice);
			return lattice;
		}
		
		List<Integer> tmp_items = new ArrayList<>(items_ids.keySet());
		
		for (int i=0; i < tmp_items.size(); i++)	{
			Set<Set<Integer>> lattice_tmp = getLatticeLevelRecursive(level-1);
			for (Set<Integer> subset : lattice_tmp)	{
				Set<Integer> tmp_subset = new TreeSet<Integer>(subset);
				tmp_subset.add(tmp_items.get(i));
				if (tmp_subset.size() == level)
					lattice.add(tmp_subset);
			}
		}
		this.levels_ids.put(level, lattice);
		return lattice;
	}
	
	@Deprecated
	public Set<Set<Integer>> getDownLatticeLevel(int level, Set<Integer> element)	{
		List<Integer> traverse_element = new ArrayList<>(element);
		Set<Set<Integer>> lattice_level = new HashSet<>();
		if (level >= element.size() || level<=0) return lattice_level;
		
		int size = (int) Math.pow(2, traverse_element.size());
		int binary_size = traverse_element.size();
		
		for (int i=0; i < size; i++)	{
			char[] binary = String.format("%0"+binary_size+"d", Long.valueOf(Long.toBinaryString(i))).toCharArray(); 
			int count = 0;
			for (int j=0; j < binary.length; j++)	{
				if (binary[j]=='1') count++;
			}
			if (count == level)	{
				Set<Integer> tmp_subset = new TreeSet<>();
				for (int j=0; j < binary.length; j++)	{
					if (binary[j]=='1') {
						tmp_subset.add(traverse_element.get(j));
					}
				}
				lattice_level.add(tmp_subset);
			}
		}
		return lattice_level;
	}
	
	// this methods works only on the input set provided, no need to synchronise
	public Set<Set<Integer>> getNextDownLatticeLevel(Set<Integer> element)	{
		Set<Set<Integer>> lattice_level = new HashSet<>();
		Iterator<Integer> it_values = element.iterator();
		while (it_values.hasNext())	{
			Set<Integer> tmp_element = new TreeSet<>(element);
			tmp_element.remove(it_values.next());
			lattice_level.add(tmp_element);
		}
		return lattice_level;
	}
	
	// this methods works only on the input set provided, no need to synchronise
	public Set<Set<Integer>> getNextUpLatticeLevel(Set<Integer> element, Set<Integer> keys)	{
		Set<Set<Integer>> lattice_level = new HashSet<>();
		Iterator<Integer> it_keys = keys.iterator();
		while (it_keys.hasNext())	{
			Set<Integer> tmp_element = new TreeSet<>(element);
			Integer tmp_key = it_keys.next();
			if (!tmp_element.contains(tmp_key))	{
				tmp_element.add(tmp_key);
				lattice_level.add(tmp_element);
			}
		}
		return lattice_level;
	}
	
	
	// this does (obviously) not scale as it explores the full lattice
	public Set<Set<Integer>> getLatticeLevel(int level)	{
		List<Integer> traverse_element = new ArrayList<>(items_ids.keySet());
		Set<Set<Integer>> lattice_level = new HashSet<>();
		if (level > items_ids.size() || level<=0) return lattice_level;
		
		int size = (int) Math.pow(2, traverse_element.size());
		int binary_size = traverse_element.size();
		
		for (int i=0; i < size; i++)	{
			char[] binary = String.format("%0"+binary_size+"d", new BigInteger(BigInteger.valueOf(i).toString(2))).toCharArray();
			int count = 0;
			for (int j=0; j < binary.length; j++)	{
				if (binary[j]=='1') count++;
			}
			if (count == level)	{
				Set<Integer> tmp_subset = new TreeSet<>();
				for (int j=0; j < binary.length; j++)	{
					if (binary[j]=='1') {
						tmp_subset.add(traverse_element.get(j));
					}
				}
				lattice_level.add(tmp_subset);
			}
		}
		return lattice_level;
	}
	
	// stillneeds to explore 
	public Set<Set<Integer>> getLatticeLevelIndices(int level)	{
		List<Integer> keys = new ArrayList<Integer>(this.getLatticeItemKeys());
		Collections.sort(keys);
		Set<Set<Integer>> lattice_level = new HashSet<>();
		int[] indices = new int[level];
		// init indices
		for (int i=0; i < indices.length; i++)
			indices[i] = i;
		
		int current = level-1;
		// iterate the current index (always last not already iterated to k)
		while (current > -1)	{
			while (indices[current] <= keys.size()-1)	{ //(level-1)
				Set<Integer> tmp_subset = new TreeSet<>();
				for (int i=0; i < level; i++) tmp_subset.add(keys.get(indices[i]));
				lattice_level.add(tmp_subset);
				indices[current]++; // move the last index
			}
			indices[current]--; // reset it 
			// reset indices
			int tmp = current-1;
			boolean search = true;
			while(search)	{ // if we hop below 0, we are done
				if ((tmp > -1) && (indices[tmp] >= indices[tmp+1]-1))	{ // this is not our next index - try lower one
					tmp = tmp-1;		
				} else {
					search=false;
				}
			}
			if (tmp > -1)	{ // reset indices
				indices[tmp] = indices[tmp]+1;
				for (int i=tmp+1; i < indices.length; i++)	{
					indices[i] = indices[i-1]+1;
				}
				current = indices.length-1;
			} else {
				current = tmp;
			}
		}
		return lattice_level;
	}
	
	public Set<Integer>	getLatticeItemKeys()	{
		return this.items_ids.keySet();
	}
	
	public Collection<T> getLatticeItems()	{
		return this.items_ids.values();
	}
	
	public static String toTraversalID(Set<Integer> items)	{
		return items.toString();
	}
}