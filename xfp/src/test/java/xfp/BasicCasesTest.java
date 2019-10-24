package xfp;

import org.junit.Test;

public class BasicCasesTest extends AbstractXFPtest {
	
	@Test
	public void testIndex1ap3pc() throws Exception {
		runExperiment("-dbasiccases","-stest","-windex1ap3pc");
	}
	
	@Test
	public void testList1ap() throws Exception {
		runExperiment("-dbasiccases","-stest","-wlist1ap");
	}
	
	@Test
	public void testListNap() throws Exception {
		runExperiment("-dbasiccases","-stest","-wlistNap");
	}
	
	@Test
	public void testMenuList() throws Exception {
		runExperiment("-dbasiccases","-stest","-wmenu+list");
	}
	
	@Test
	public void testMenuNap() throws Exception {
		runExperiment("-dbasiccases","-stest","-wmenuNap");
	}
	
	@Test
	public void testMenusingleton() throws Exception {
		runExperiment("-dbasiccases","-stest","-wmenusingleton");
	}

	@Test
	public void testParameterW() throws Exception {
		runExperiment("-dbasiccases","-stest","-wmenusingleton,menuNap");
	}
	
	@Test
	public void testlist1apPagination() throws Exception {
		runExperiment("-dbasiccases","-stest","-wlist1apPagination");
	}
	
	@Test
	public void testpagination1ap() throws Exception {
		runExperiment("-dbasiccases","-stest","-wpagination1ap");
	}
	
	@Test
	public void testpaginationNap() throws Exception {
		runExperiment("-dbasiccases","-stest","-wpaginationNap");
	}
	
	@Test
	public void testselfrefNap() throws Exception {
		runExperiment("-dbasiccases","-stest","-wselfrefNap");
	}
	
	private void runExperiment(String... args) throws Exception {
	    Main.main(args);
	}
}
