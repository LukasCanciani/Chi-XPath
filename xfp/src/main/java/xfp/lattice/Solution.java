package xfp.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/** a solution covers all elements of the initial set, and all items are arranged in a potential page class (set or point in the lattice)
 * Is this space efficient? Maybe not - binary representation, or array with length n and for each element id of subset? - hard for traversal */
public class Solution	{
	
	private Set<Set<Integer>> solution = new HashSet<>();
	
	public void addSolutionElement(Set<Integer> element)	{
		solution.add(element);
	}
	
	public boolean isValid(Set<Integer> search_space)	{
		HashMap<Integer, Integer>	found = new HashMap<>(search_space.size());
		// count how often each element occurs -> twice: this is not valid
		for (Set<Integer> v : solution)	{
			for (Integer i : v)	{
				Integer current = found.get(i) == null ? 0 : found.get(i);
				current++;
				if (current > 1) return false;
				found.put(i, current);
			}
		}
		// check if each element occurred once  and size is the same)
		if ((found.values().stream().filter(v -> v == 1).count() == found.keySet().size()) 
				&& (found.keySet().size()==search_space.size()))	return true;
		return false;
	}
	
	public String toString()	{
		return solution.toString();
	}
}
