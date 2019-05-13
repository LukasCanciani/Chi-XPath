package it.uniroma3.chixpath.model;

import java.util.HashSet;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

public class VectorRepository {
	private Set<XPath> xpaths = new HashSet<>();
	private Set<Vector> vectors = new HashSet<>();
	private PageClass pageClass;
	private String id;
	private int pagNum;
	
	private int totalNodes[];

	public int[] getTotalNodes() {
		return totalNodes;
	}

	public VectorRepository(PageClass pageClass, int pag , String id) {
		this.pageClass = pageClass;
		this.pagNum = pag;
		this.id = id;
		this.totalNodes = new int[pagNum];
	}

	private boolean containsXPaths(XPath rule) {
		boolean found = false;
		for (XPath rule2 : this.getXPaths()) {
			if (rule2.getRule().compareTo(rule.getRule())==0) {
				found = true;
			}
		}
		return found;
	}

	private boolean containsVector(Vector vector) {
		boolean found = false;
		for (Vector v2 : this.getVectors()) {
			if (v2.equals(vector)) {
				found = true;
				break; //Consentito??
			}
		}
		return found;
	}

	public static void printTags(Node nodes){
		if(nodes.hasChildNodes()  || nodes.getNodeType()!=3){
			System.out.println(nodes.getNodeName()+" : "+nodes.getTextContent());
			NodeList nl=nodes.getChildNodes();
			for(int j=0;j<nl.getLength();j++)printTags(nl.item(j));
		}
	}

	void addUnique(XPath rule) throws XPathExpressionException {
		if (!this.containsXPaths(rule)) {
			Vector vector = new Vector(rule,this.getpageClass(),this.getPagNum());
			/*//**Ricerca di xpath specifici**
			if (rule.getRule().toLowerCase().contains("@class")) {
				System.out.println("RULE TROVATA -> "+rule.getRule());
				for (NodeList nl : vector.getExtractedNodes()) {
					if (nl != null) {
						for(int i = 0 ; i<nl.getLength(); i++) {
							Node n = nl.item(i);
							System.out.println(n.getTextContent());


						}

					}
				}

			}
			//System.out.println("Regola - "+rule);
			//*******************************/
			if(!this.containsVector(vector)) {
				boolean equals = true;
				
				String[] sameNodes = new String[vector.getExtractedNodes().length];
				for(int i=0;i<vector.getExtractedNodes().length-1;i++) {
					NodeList node = vector.getExtractedNodes()[i];
					NodeList node1 = vector.getExtractedNodes()[i+1];
					if(node == null && node1==null) {
						//System.out.println("entrambi null");
						sameNodes[i]="1";
					}

					else if(node!=null && node1!=null) {
						if(vector.sameNodes(node,node1)) {
							//System.out.println("uguali");
							sameNodes[i]="1";
						}
					}
				}
				for(int k=0;k<sameNodes.length && equals;k++) {
					if(sameNodes[k]==null) {
						//System.out.println("non ha trovato riscontro");
						equals=false;
					}
				}
				
				if(!equals) {
					Set<XPath> newxPaths = this.getXPaths();
					Set<Vector> newVectors = this.getVectors();
					newxPaths.add(rule);
					newVectors.add(vector);
					this.vectors = (newVectors);
					for(Page p : this.getpageClass().getPages()) {
						this.totalNodes[Integer.parseInt(p.getId())] += vector.getExtractedNodes()[Integer.parseInt(p.getId())].getLength();
					}
					this.xpaths = (newxPaths);
				}
				else {
					System.out.println("TROVATA XPATH NON SIGNIFICATIVA IN PAGECLASS "+this.getpageClass().getId());
				}
			}
		}
	}

	public Set<XPath> getXPaths() {
		return xpaths;
	}



	public Set<Vector> getVectors() {
		return vectors;
	}


	public PageClass getpageClass() {
		return pageClass;
	}

	public int getPagNum() {
		return pagNum;
	}
}
