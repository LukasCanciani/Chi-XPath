package it.uniroma3.chixpath.model;

import static it.uniroma3.fragment.util.XPathUtils.evaluateXPath;
import org.w3c.dom.Node;

import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;

public class ValuesVector implements Comparable<ValuesVector>  {
	private String xPath;
	private PageClass pClass;
	private int n_Pag;
	private NodeList[] extractedNodes;
	public ValuesVector(String xPath, PageClass pClass, int n_Pag) throws XPathExpressionException {
		this.xPath=xPath;
		this.pClass=pClass;
		this.n_Pag=n_Pag;
		this.extractedNodes=extractNodes();
	}

	public NodeList[] extractNodes() throws XPathExpressionException {
		final NodeList[] extractedNodes = new NodeList[n_Pag]; 
		for(Page p : this.pClass.getPages()) {
			NodeList node = evaluateXPath(p.getDocument(), this.xPath);
			
			//System.out.println("estratti "+node.getLength()+" nodi dalla pagina "+p.getId());
			//inserisco i valori estratti dalla regola nelle posizioni dell'array corrispondenti all'id pagina, che è una stringa e va convertito in int
			extractedNodes[Integer.parseInt(p.getId())]= node;
			for(int i=0; i<extractedNodes.length;i++) {
			}
		}
		return extractedNodes;
	}
	//controllo stesso vettore. Creo un array di stringhe in cui inserisco "1" -e i nodi estratti su una pagina dalla regola "A"sono uguali a quelli estratti dalla regola "B"; null altrimenti.
	public boolean sameVector(ValuesVector vett) {
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
				System.out.println("Gli xPath" +this.getxPath()+" e "+vett.getxPath()+" danno gli stessi valori");
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

	public String getxPath() {
		return xPath;
	}

	public void setxPath(String xPath) {
		this.xPath = xPath;
	}

	public PageClass getpClass() {
		return pClass;
	}

	public void setpClass(PageClass pClass) {
		this.pClass = pClass;
	}

	public NodeList[] getExtractedNodes() {
		return extractedNodes;
	}

	public void setExtractedNodes(NodeList[] extractedNodes) {
		this.extractedNodes = extractedNodes;
	}



	public int getN_Pag() {
		return n_Pag;
	}

	@Override
	public int compareTo(ValuesVector that) {
		return this.getpClass().getId().compareTo(that.getpClass().getId());
	}

	@Override
	public int hashCode() {
		return this.getpClass().getId().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		final ValuesVector that = (ValuesVector)o;
		return this.sameVector(that);
	}

}
