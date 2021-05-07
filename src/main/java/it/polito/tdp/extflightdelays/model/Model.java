package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private SimpleWeightedGraph<Airport, DefaultWeightedEdge>grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer,Airport>idMap;
	//creo mappa per salvarmi da chi è stato scoperto quel vertice --> SALVO ALBERO DI VISITA
	private Map<Airport,Airport> visita;  //faccio la new nel metodo trovaPercorso
	
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

			
			Graphs.addEdgeWithVertices(grafo, r.getA1(), r.getA2(), r.getNumeroVoli());
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

	
	//collego il metodo del dao
	
	public Set<Airport> getVertici() {
		if(grafo != null)
			return grafo.vertexSet();
		
		return null;
	}
	
	
	//creo metodo NUMERO VERTICI E NUMERO ARCHI CHE RESTITUISCA NEL CONTROLLER
	//TRAMITE LA SIZE IL NUMERO RISPETTIVO
	 public int getNumeroVertici() {
		if(grafo != null)
			return grafo.vertexSet().size();
		
		return 0;
	}
	
	public int getNumeroArchi() {
		if(grafo != null)
			return grafo.edgeSet().size();
		
		return 0;
	}
	
	
	public List<Airport> trovaPercorso(Airport a1, Airport a2){
		List<Airport> percorso = new LinkedList<>();
		//creo iteratore visita in ampiezza --> TIPO VERTICI, TIPO ARCHI 
		//devo specificare grafo,nodoPartenza
		BreadthFirstIterator<Airport, DefaultWeightedEdge> bfv= new BreadthFirstIterator<>(grafo, a1);
		
		visita= new HashMap<>();
		visita.put(a1, null); //aggiungo alla mappa solo il nodo radice, l'altro è null
		
		
		//associo all'iteratore un traversalListener --> registra degli eventi --> E' UN INTERFACCIA
		//DEVO CREARE I SUOI METODI  (add unip...)
		bfv.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			/**
			 * metodo che ci interessa: arco attraversato  
			 */
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				//recupero estremi arco attraversato
				Airport airport1= grafo.getEdgeSource(e.getEdge());
				Airport airport2= grafo.getEdgeTarget(e.getEdge());
				
				//siccome il grafo è non orientato dobbiamo fare
				if(visita.containsKey(airport1) && !visita.containsKey(airport2)) {
					visita.put(airport2, airport1);
				} else if (visita.containsKey(airport2) && !visita.containsKey(airport1)) {
					visita.put(airport1, airport2);
				}
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		//visito il grafo un passo alla volta grazie ai metodi hasNext e next
		//finchè l'iteratore ha un prossimo nodo da visitare, io lo visito
		while(bfv.hasNext()) {
		bfv.next();  //restituisce un aeroporto ogni volta ma non trova il percorso --> devo usare il traversalListener
		}
		//qui colleghiamo il traversalListener 
		
		percorso.add(a2); //aggiungo la destinazione
		//risalgo la mappa 
		Airport step= a2;
		while( visita.get(step)!=null) {  //finhcè la get non arriva alla mia radice, proseguo
			step= visita.get(step);
			percorso.add(0,step);
			
		}
	
	//il percorso è al contrario, posso aggiungere in testa 
		
		return percorso;
	}
}
