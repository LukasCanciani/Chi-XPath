package xfp.io;

import java.net.URI;

import xfp.fixpoint.PageClass;

public interface OutputHandler<T> {
	public void storePageclass(PageClass<T> pc) throws Exception;
	
	public static OutputHandler<URI> getNFPPageClassOutputHandler() {
		return null;
	}
	
	public static OutputHandler<String> getDFPPageClassOutputHandler() {
		return null;
	}
}
