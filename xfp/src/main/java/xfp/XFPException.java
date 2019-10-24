package xfp;

public class XFPException extends RuntimeException {

    static final private long serialVersionUID = 1L;

    public XFPException(String string) {
        super(string);
    }

    public XFPException(Throwable e) {
        super(e);
    }

}
