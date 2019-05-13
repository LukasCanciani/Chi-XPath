package xfp.lattice;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import xfp.AbstractXFPtest;

public class LatticeTest extends AbstractXFPtest {

	@Test
	@Deprecated
	public void testExploreDownLattice() {
		long start = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testExploreDownLattice started.");
		System.out.println("------------------");
		Set<Item> items = generateInputItems(5);

		Lattice<Item> lt = new Lattice<Item>(items);

		Set<Integer> lattice_element = new TreeSet<>(lt.getLatticeItemKeys());
		for (int i = items.size() - 1; i > 0; i--) {
			int combinations = LatticeTest.combinations(lattice_element.size(), i - 1);
			Set<Set<Integer>> lattice = lt.getDownLatticeLevel(i - 1, lattice_element);
			System.out.println("Lattice(" + (i - 1) + "): " + lattice + " " + combinations + "=" + lattice.size());
			if (!lattice.isEmpty())
				lattice_element = lattice.iterator().next();
			// assertEquals(combinations, lattice.size());
		}
		long stop = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testExploreDownLattice finished in " + (stop - start));
		System.out.println("------------------");
	}

	@Test
	public void testExploreNextDownLattice() { // works quite ok up to
														// 300: 26 secs
		long start = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testExploreNextDownLattice started.");
		System.out.println("------------------");
		Set<Item> items = generateInputItems(15);

		Lattice<Item> lt = new Lattice<Item>(items);

		Set<Integer> lattice_element = new TreeSet<>(lt.getLatticeItemKeys());
		for (int i = items.size() - 1; i > 0; i--) {
			int combinations = LatticeTest.combinations(lattice_element.size(), i - 1);
			Set<Set<Integer>> lattice = lt.getNextDownLatticeLevel(lattice_element);
			System.out.println("Lattice(" + (i - 1) + "): " + lattice + " " + combinations + "=" + lattice.size());
			if (!lattice.isEmpty())
				lattice_element = lattice.iterator().next();
			// assertEquals(combinations, lattice.size());
		}
		long stop = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testExploreNextDownLattice finished in " + (stop - start));
		System.out.println("------------------");
	}
	
	@Test
	public void testExploreNextUpLattice() { // works quite ok up to 300:
													// 11 secs
		long start = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testExploreNextUpLattice started.");
		System.out.println("------------------");
		Set<Item> items = generateInputItems(50);

		Lattice<Item> lt = new Lattice<Item>(items);

		Set<Integer> lattice_element = new TreeSet<>();
		for (int i = 0; i < items.size(); i++) {
			int combinations = lt.getLatticeItemKeys().size() - lattice_element.size();
			Set<Set<Integer>> lattice = lt.getNextUpLatticeLevel(lattice_element, lt.getLatticeItemKeys());
			System.out.println("Lattice(" + i + "): " + lattice + "; input: (" + lattice_element + ") " + combinations
					+ "=" + lattice.size());
			if (!lattice.isEmpty())
				lattice_element = lattice.iterator().next();
		}
		long stop = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testExploreNextUpLattice finished in " + (stop - start));
		System.out.println("------------------");
	}

	@Test
	public void testLatticeStoreUpTraversal() {
		long start = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testLatticeStoreTraversal started.");
		System.out.println("------------------");
		Set<Item> items = generateInputItems(50);

		Lattice<Item> lt = new Lattice<Item>(items);

		Set<Integer> lattice_element = new TreeSet<>();
		for (int i = 0; i < items.size(); i++) {
			Set<Set<Integer>> lattice = lt.getNextUpLatticeLevel(lattice_element, lt.getLatticeItemKeys());
			lt.addTraversed(lattice_element);
			if (!lattice.isEmpty())
				lattice_element = lattice.iterator().next();
		}

		// do the same again
		lattice_element = new TreeSet<>();
		for (int i = 0; i < items.size(); i++) {
			int combinations = lt.getLatticeItemKeys().size() - lattice_element.size();

			Set<Set<Integer>> lattice = lt.getNextUpLatticeLevel(lattice_element, lt.getLatticeItemKeys());
			boolean contained = lt.isTraversed(lattice_element);
			System.out.println("Already traversed? " + contained + " Lattice(" + i + "): " + lattice + "; input: ("
					+ lattice_element + ") " + combinations + "=" + lattice.size());
			if (!lattice.isEmpty())
				lattice_element = lattice.iterator().next();
		}

		long stop = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testLatticeStoreTraversal finished in " + (stop - start));
		System.out.println("------------------");
	}
	
	@Test
	@Deprecated
	public void testMemoLattice() {
		long start = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testMemoLattice started.");
		System.out.println("------------------");
		Set<Item> items = generateInputItems(5);

		Lattice<Item> lt = new Lattice<Item>(items);

		for (int i = 0; i <= items.size() + 1; i++) {
			Set<Set<Integer>> lattice = lt.getLatticeLevelRecursive(i);
			int combinations = LatticeTest.combinations(items.size(), i);
			System.out.println("Lattice(" + i + "): " + lattice + " " + combinations + "=" + lattice.size());
			// assertEquals(combinations, lattice.size());
		}
		long stop = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testMemoLattice finished in " + (stop - start));
		System.out.println("------------------");
	}

	@Test
	public void testGetLatticeLevel() {
		long start = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testGetLatticeLevel started.");
		System.out.println("------------------");
		Set<Item> items = generateInputItems(5);

		Lattice<Item> lt = new Lattice<Item>(items);

		for (int i = 0; i <= items.size() + 1; i++) {
			Set<Set<Integer>> lattice = lt.getLatticeLevel(i);
			int combinations = LatticeTest.combinations(items.size(), i);
			System.out.println("Lattice(" + i + "): " + lattice + " " + combinations + "=" + lattice.size());
		}
		long stop = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testGetLatticeLevel finished in " + (stop - start));
		System.out.println("------------------");
	}

	@Test
	public void testGetLatticeLevelIndices() {
		long start = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testGetLatticeLevelIndices started.");
		System.out.println("------------------");
		Set<Item> items = generateInputItems(5);

		Lattice<Item> lt = new Lattice<Item>(items);
		for (int i = 0; i <= items.size() + 1; i++) {
			int combinations = LatticeTest.combinations(items.size(), i);
			Set<Set<Integer>> lattice = lt.getLatticeLevelIndices(i);
			System.out.println("Lattice(" + i + "): " + lattice + " " + combinations + "=" + lattice.size());
		}
		long stop = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testGetLatticeLevelIndices finished in " + (stop - start));
		System.out.println("------------------");
	}
	
	@Test
	// move to JUnit tests
	public void testSolution() {
		long start = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testSolution started.");
		System.out.println("------------------");
		Set<Item> items = generateInputItems(5);

		Lattice<Item> lt = new Lattice<Item>(items);

		Solution s = new Solution();
		// a correct one
		Set<Integer> element = new TreeSet<>();
		element.add(1);
		element.add(2);
		element.add(3);
		s.addSolutionElement(element);
		element = new TreeSet<>();
		element.add(4);
		element.add(5);
		s.addSolutionElement(element);
		System.out.println("Valid solution " + s + " is valid: " + s.isValid(lt.getLatticeItemKeys()));

		// a missing item
		s = new Solution();
		element = new TreeSet<>();
		element.add(2);
		element.add(3);
		s.addSolutionElement(element);
		element = new TreeSet<>();
		element.add(4);
		element.add(5);
		s.addSolutionElement(element);
		System.out.println("Invalid solution " + s + " is valid: " + s.isValid(lt.getLatticeItemKeys()));

		// a duplicate item
		s = new Solution();
		element = new TreeSet<>();
		element.add(1);
		element.add(2);
		element.add(3);
		s.addSolutionElement(element);
		element = new TreeSet<>();
		element.add(2);
		element.add(4);
		element.add(5);
		s.addSolutionElement(element);
		System.out.println("Invalid solution " + s + " is valid: " + s.isValid(lt.getLatticeItemKeys()));
		long stop = System.currentTimeMillis();
		System.out.println("------------------");
		System.out.println("testSolution finished in " + (stop - start));
		System.out.println("------------------");
	}

	private Set<Item> generateInputItems(int number) {
		Set<Item> items = new TreeSet<>();
		for (int i = 0; i < number; i++) {
			items.add(new Item());
		}
		return items;
	}

	private static int combinations(int n, int k) {
		int numerator = factorial(n, (n - k + 1));
		int denominator = factorial(k, 1);
		if (denominator == 0)
			return 0;
		return numerator / denominator;
	}

	private static int factorial(int k, int stop) {
		if (k > stop)
			return k * factorial(k - 1, stop);
		return k;
	}
}
