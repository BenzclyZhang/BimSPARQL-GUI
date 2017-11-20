package nl.tue.ddss.bimsparql.server;


import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.TDBLoader;
import com.hp.hpl.jena.tdb.store.GraphTDB;

public class DatabaseLoader {
	
	
	public static void load(String uri,String directory,String graphName,boolean showProgress){
		Dataset dataset = TDBFactory.createDataset(directory);
		Model model=dataset.getDefaultModel();
		if(graphName!=null){
		model = dataset.getNamedModel(graphName);
		}
		GraphTDB graph=(GraphTDB)model.getGraph();
		TDBLoader.load(graph, uri, showProgress);
	}
}
