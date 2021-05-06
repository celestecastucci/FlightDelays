package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private SimpleWeightedGraph<Airport, DefaultWeightedEdge>grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer,Airport>idMap;
	
	public Model() {
		dao= new ExtFlightDelaysDAO();
		idMap= new HashMap<Integer,Airport>();
		//riempiamo l'id map con tutti gli areoporti disponibli 
		//poi la uso per scremare e mettere nel grafo solo quelli utili
	     dao.loadAllAirports(idMap);
	}
	
	//IL GRAFO RICEVE UN PARAMETRO DALL'ESTERNO
	/**
	 * Il grafo riceve un parametro dall'esterno
	 * i vertici non sono tutti gli areoporti --> DOBBIAMO FILTRARLI!!!
	 * @param x
	 */
	public void creaGrafo(int x) {
	grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
	//aggiungo vertici filtrati
	Graphs.addAllVertices(grafo, dao.getVertici(x, idMap));
	
	//aggiungo archi
	   //recupero dal database le triple di rotte: areop1, areop2, totalevoli con l'andata e il ritorno!
	  // lato codice gestirò la questione andata e ritorno
	  //creo CLASSE ROTTA (E' ANALOGO AD ADIACENZA COME CONCETTO)
	
	
	for(Rotta r: dao.getRotte(idMap)) {
		//controllo da fare: il metodo getRotte ritorna tutte le rotte, anche quelle non presenti nel grafo
		//questo perchè i vertici del grafo non sono piu tuttiu gli areoporti
		
		//solo se il grafo contiene gia i due vertici allora la rotta è di interesse 
		if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {
			//recupero l'arco
			DefaultWeightedEdge e= this.grafo.getEdge(r.getA1(), r.getA2());
			if(e==null) {  //se non c'è un arco 
				Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getNumeroVoli());
			} else {
				double pesoVecchio= this.grafo.getEdgeWeight(e);
				double pesoNuovo= pesoVecchio+ r.getNumeroVoli();
				this.grafo.setEdgeWeight(e,pesoNuovo);
				
			}
			
		}
	}
	//stampa il numero di vertici e archi del grafo creato
	System.out.println("Grafo creato");
	System.out.println("# vertici: "+ grafo.vertexSet().size());
	System.out.println("# archi: "+ grafo.edgeSet().size());
	
	
	}

	public Set<Airport> getVertici() {
		return this.grafo.vertexSet();
	}
	
	/*List<Airport> trovaPercorso(Airport a1, Airport a2){
		List<Airport> percorso = new LinkedList<>();
		//creo iteratore visita in ampiezza
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it= new 	BreadthFirstIterator<>(grafo, a1);
		
		
		while(it.hasNext()) {
		it.next();
	} 
	
	}*/
}
