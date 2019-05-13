package it.uniroma3.fragment.step;

import lombok.ToString;

public interface CaseHandler {
    
    @ToString
    public class ConfigurableCaseHandler implements CaseHandler {
        /**
         * Default as suggested in https://www.w3.org/TR/html4/about.html#h-1.2.1 
         */
        private boolean elementNamesUpperCase = true;    // true iff uppercase
        private boolean attributeNamesUpperCase = false; // true iff uppercase
        private boolean forceCase = false;               // true iff the case should be forced
        
        public ConfigurableCaseHandler() {
            this(true,false,false); // do not force case at all
        }
        public ConfigurableCaseHandler(boolean elementCase, boolean attributeCase) {
            this(elementCase,attributeCase,true); // force case
        }
        /* MGC */
        private ConfigurableCaseHandler(boolean elementCase, boolean attributeCase, boolean forceCase) {
            this.elementNamesUpperCase = elementCase;
            this.attributeNamesUpperCase = attributeCase;
            this.forceCase = forceCase;
        }

        @Override
        public String elementCase(String element) { 
            return forceCase(this.elementNamesUpperCase, element);
        }
    
        @Override
        public String attributeCase(String attributeCase) { 
            return forceCase(this.attributeNamesUpperCase, attributeCase);
        }

        protected String forceCase(boolean upperOrLower, String string) {
            if (!forceCase) return string;
            return ( upperOrLower ? string.toUpperCase() : string.toLowerCase() );
        }
        
   }
    /**
     *  Adopts conventions as specified in:
     *  <A href="https://www.w3.org/TR/html4/about.html#h-1.2.1">HTML 4 specification</A>
     */
    static final public CaseHandler HTML_STANDARD_CASEHANDLER = new ConfigurableCaseHandler(true,false);

    static final public CaseHandler LOWER_CASEHANDLER = new ConfigurableCaseHandler(false,false);
    
    static final public CaseHandler LEAVE_CASE_AS_IT_IS = new ConfigurableCaseHandler();

    /**
     * @param elementName - an element name
     * @return the element in the correct case (to match as a XPath expression)
     */
    public String elementCase(String elementName);

    /**
     * 
     * @param attributeName - an attribute name
     * @return the attribute in thet correct case (to match as a XPath expression)
     */
    public String attributeCase(String attributeName);
    
}