package it.uniroma3.chixpath.model;

import static it.uniroma3.fragment.util.XPathUtils.evaluateXPath;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;

public class Vector implements Comparable<Vector>  {
	private XPath xpath;
	private PageClass pageClass;
	private int pagNum;
	private NodeList[] extractedNodes;
	public Vector(XPath xpath, PageClass pageClass, int pagNum) throws XPathExpressionException {
		this.xpath=xpath;
		this.pageClass=pageClass;
		this.pagNum=pagNum;
		this.extractedNodes=extractNodes();
	}

	public NodeList[] extractNodes() throws XPathExpressionException {
		final NodeList[] extractedNodes = new NodeList[pagNum]; 
		for(Page p : this.pageClass.getPages()) {
			NodeList node = evaluateXPath(p.getDocument(), this.xpath.getRule());
			
			//System.out.println("estratti "+node.getLength()+" nodi dalla pagina "+p.getId());
			//inserisco i valori estratti dalla regola nelle posizioni dell'array corrispondenti all'id pagina, che è una stringa e va convertito in int
			extractedNodes[Integer.parseInt(p.getId())]= node;
			for(int i=0; i<extractedNodes.length;i++) {
			}
		}
		return extractedNodes;
	}
	//controllo stesso vettore. Creo un array di stringhe in cui inserisco "1" -e i nodi estratti su una pagina dalla regola "A"sono uguali a quelli estratti dalla regola "B"; null altrimenti.
	public boolean sameVector(Vector vett) {
		boolean stesse= false;

		String[] sameVector = new String[this.getExtractedNodes().length];
		for(int i=0;i<this.getExtractedNodes().length;i++) {
			NodeList node = this.getExtractedNodes()[i];
			NodeList node1 = vett.getExtractedNodes()[i];
			if(node == null && node1==null) {
				//System.out.println("entrambi null");
				sameVector[i]="1";
			}

			else if(node!=null) {
				if(sameNodes(node,node1)) {
					//System.out.println("uguali");
					sameVector[i]="1";
				}
			}
		}
		for(int k=0;k<sameVector.length;k++) {
			if(sameVector[k]==null) {
				//System.out.println("non ha trovato riscontro");
				return stesse=false;
			}

			else if(sameVector[k]=="1" && k==sameVector.length-1) {
				System.out.println("Gli xPath" +this.getXPath()+" e "+vett.getXPath()+" danno gli stessi valori");
				return stesse=true;
			}
		}
		return stesse;
	}

	//controllo elemento per elemento che i nodi siano gli stessi sempre tramite un array di stringhe in cui inserisco 1 se tutti i nodi sono uguali
	public boolean sameNodes(NodeList n1, NodeList n2) {
		boolean stessi=false;
		String[] same = new String[n1.getLength()];
		//per essere uguali devono essere lunghi uguali
		if(n1.getLength()==n2.getLength()) {
			for(int i=0;i<n1.getLength();i++) {
				Node nod1 = n1.item(i);
				for(int j=0; j<n2.getLength();j++) {
					Node nod2 = n2.item(j);
					//il metodo isSameNode() non è l'unico metodo utilizzabile per confrontare i nodi.
					if(nod1.isSameNode(nod2)) {
						same[i]="1";
					}
					//non ho trovato nod1 in nessun nodo di NodeList n2

					else if(!nod1.isSameNode(nod2) && j==n2.getLength()-1) {
						//System.out.println(""+nod1.getNodeName()+" non ha trovato riscontro");
						return stessi=false;
					} 
				}
			}
		}
		else return stessi;

		//controllo i valori nell'array di verifica dell'uguaglianza
		for(int k=0;k<same.length;k++) {
			if(same[k]==null)  stessi=false;
			//ultimo elemento è 1 e sono arrivato alla fine dell'array
			else if(same[k]=="1" && k==same.length-1) return stessi=true;
		}

		return stessi;
	}

	public XPath getXPath() {
		return xpath;
	}


	public PageClass getPageClass() {
		return pageClass;
	}

	

	public NodeList[] getExtractedNodes() {
		return extractedNodes;
	}

	



	public int getPagNum() {
		return pagNum;
	}

	@Override
	public int compareTo(Vector that) {
		return this.getPageClass().getId().compareTo(that.getPageClass().getId());
	}

	@Override
	public int hashCode() {
		return this.getPageClass().getId().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		final Vector that = (Vector)o;
		return this.sameVector(that);
	}

}
