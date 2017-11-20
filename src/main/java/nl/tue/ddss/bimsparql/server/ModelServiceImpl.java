package nl.tue.ddss.bimsparql.server;


import nl.tue.ddss.bimsparql.BimSPARQL;
import nl.tue.ddss.bimsparql.client.ModelService;
import nl.tue.ddss.bimsparql.function.geom.GEOM;
import nl.tue.ddss.bimsparql.geometry.Geometry;
import nl.tue.ddss.bimsparql.geometry.GeometryType;
import nl.tue.ddss.bimsparql.geometry.TriangulatedSurface;
import nl.tue.ddss.bimsparql.geometry.algorithm.Polyhedron;
import nl.tue.ddss.bimsparql.geometry.convert.GeometryConverter;
import nl.tue.ddss.bimsparql.geometry.ewkt.EwktReader;
import nl.tue.ddss.bimsparql.geometry.ewkt.WktWriteException;
import nl.tue.ddss.bimsparql.shared.FieldVerifier;
import nl.tue.ddss.bimsparql.shared.InstanceGeometry;
import nl.tue.ddss.convert.IfcVersion;
import nl.tue.ddss.convert.ifc2rdf.Ifc2RdfConverter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;



/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ModelServiceImpl extends RemoteServiceServlet implements
    ModelService {
	
	
	private Model model;
	private Model schema;

	public HashMap<Integer, InstanceGeometry> getGeometries() {
		

 //       loadTestModel("Duplex_A_20110505");
		File dir = new File(System.getProperty("catalina.base"), "uploads");
		String path=dir.getAbsolutePath();
		File modeldir=new File(path+"/model");
		modeldir.mkdirs();
        loadModelTDB(lastFileModified(modeldir.getAbsolutePath()));
		
//		InputStream input = getServletContext()
//				.getResourceAsStream("/Duplex_A_20110505.ifc");
//		ColladaParser parser = new ColladaParser();
//		GeometryGenerator gg=new GeometryGenerator();
//		HashMap<Integer, InstanceGeometry> hashmap = gg.generateGeometry(input, baseuri);
		HashMap<Integer, InstanceGeometry> hashmap = parseGeometry(model);
//		Model geometryModel=gg.getGeometryModel();
		InputStream ins = getServletContext()
				.getResourceAsStream("/database/schema/IFC2X3_TC1.ttl");
		schema=ModelFactory.createDefaultModel();
		schema.read(ins,null,"TTL");
		/*		try {
			parser.buildMapsForRender(input);

			HashMap<String, List<Point3d>> pointMap = parser.getPointMap();
			HashMap<Element, int[]> pointerMap = parser.getPointerMap();
			HashMap<Element, double[]> materialMap = parser.getMaterialMap();
			HashMap<String, Element> idMap = parser.getIdGeometryMap();
			
			for (String key : pointMap.keySet()) {
				InstanceGeometry geometry = new InstanceGeometry();				
				List<Point3d> points = pointMap.get(key);
				double[] ps = new double[points.size() * 3];
				for (int i = 0; i < points.size(); i++) {
					ps[i * 3] = points.get(i).x;
					ps[i * 3 + 1] = points.get(i).y;
					ps[i * 3 + 2] = points.get(i).z;
				}
				int[] pointers = pointerMap.get(idMap.get(key));
				geometry.setColor(materialMap.get(idMap.get(key)));
				geometry.setPoints(ps);
				geometry.setPointers(pointers);
				hashmap.put(key, geometry);
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
			try {
					BimSPARQL.init(model, model);
				} catch (ClassNotFoundException | IOException | ParserConfigurationException | SAXException
						| URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		return hashmap;
	}
	
	public String lastFileModified(String dir) {
	    File fl = new File(dir);
	    File[] files = fl.listFiles(new FileFilter() {          
	        public boolean accept(File file) {
	            return file.getName().endsWith(".ifc");
	        }
	    });
	    File choice = null;
	    if(files.length==0){
	    	choice=new File(dir+"/Duplex_A_20110505.ifc");
	    	try {
				FileUtils.copyFile(new File(getServletContext().getRealPath("/database/model/Duplex_A_20110505.ifc")), choice);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }else{
	    long lastMod = Long.MIN_VALUE;

	    for (File file : files) {
	        if (file.lastModified() > lastMod) {
	            choice = file;
	            lastMod = file.lastModified();
	        }
	    }
	    }
	    return choice.getName().substring(0,choice.getName().length()-4);
	}
	
	
	private void loadTestModelTDB(String graphName) {

		Dataset dataset = TDBFactory
				.createDataset(getServletContext().getRealPath("/database/tdb/"));
//		dataset.begin(ReadWrite.READ);
		Model m=dataset.getNamedModel(graphName);
		Model gm = dataset.getNamedModel(graphName+"_geometry");	
		Model mm = dataset.getNamedModel(graphName+"_material");
		MultiUnion union = new MultiUnion(new Graph[] {m.getGraph(),
				gm.getGraph(),mm.getGraph()});
		model = ModelFactory.createModelForGraph(union);
	}
	
	private void loadModelTDB(String modelName){
		File dir = new File(System.getProperty("catalina.base"), "uploads");
		String path=dir.getAbsolutePath();
		File modeldir=new File(path+"/model");
		
		modeldir.mkdirs();
		String inputModel = modeldir+"/"+modelName+".ifc";
		String geomName=modelName+"_geometry";
		String mtlName=modelName+"_material";
		File tdbdir=new File(path+"/tdb");
		tdbdir.mkdirs();
		Dataset dataset = TDBFactory
				.createDataset(tdbdir.getAbsolutePath());
		if(dataset.getNamedModel(modelName).size()>0&&dataset.getNamedModel(geomName).size()>0&&dataset.getNamedModel(mtlName).size()>0){
			Model m=dataset.getNamedModel(modelName);
			System.out.println("size: "+m.size());
			Model gm = dataset.getNamedModel(geomName);	
			System.out.println("size: "+gm.size());
			Model mm = dataset.getNamedModel(mtlName);
			System.out.println("size: "+gm.size());
			MultiUnion union = new MultiUnion(new Graph[] {m.getGraph(),
					gm.getGraph(),mm.getGraph()});
			model = ModelFactory.createModelForGraph(union);
		}
		else{
		String baseuri="http://default/";
		String outputModel=path+"/model/"+modelName+".ttl";
		String geomModel=path+"/model/"+geomName+".ttl";
		String mtlModel=path+"/model/"+mtlName+".ttl";
		Ifc2RdfConverter converter = new Ifc2RdfConverter();
		try {
			converter.convert(inputModel, outputModel,baseuri);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
		InputStream	in = new FileInputStream(inputModel);

		OutputStream out=new FileOutputStream(geomModel);
		GeometryConverter geomConverter=new GeometryConverter(baseuri);
		geomConverter.parseModel2GeometryStream(in, out, IfcVersion.IFC2X3_TC1, true);
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WktWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			InputStream	in = new FileInputStream(inputModel);

			OutputStream out=new FileOutputStream(mtlModel);
			GeometryConverter mtlConverter=new GeometryConverter(baseuri);
			mtlConverter.parseModel2MaterialStream(in, out, IfcVersion.IFC2X3_TC1);
			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WktWriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		DatabaseLoader.load(outputModel, tdbdir.getAbsolutePath(), modelName, true);
		DatabaseLoader.load(geomModel, tdbdir.getAbsolutePath(), geomName, true);
		DatabaseLoader.load(mtlModel, tdbdir.getAbsolutePath(), mtlName, true);
		

		Model m=dataset.getNamedModel(modelName);
		Model gm = dataset.getNamedModel(geomName);	
		Model mm = dataset.getNamedModel(mtlName);
		MultiUnion union = new MultiUnion(new Graph[] {m.getGraph(),
				gm.getGraph(),mm.getGraph()});
		model = ModelFactory.createModelForGraph(union);
		}
		System.out.println("size: "+model.size());
	}


	public HashMap<Integer,InstanceGeometry> parseGeometry(Model model){
		HashMap<Integer,InstanceGeometry> hashmap=new HashMap<Integer,InstanceGeometry>();
		StmtIterator stmts=model.listStatements(null,GEOM.hasGeometry,(RDFNode)null);
		while(stmts.hasNext()){
			Statement s=stmts.next();
			String name=s.getSubject().getLocalName();
			InstanceGeometry ig=new InstanceGeometry();
			int id;
			try{
			id=Integer.parseInt(name.substring(name.lastIndexOf("_")+1));
			ig.setId(id);
			}catch (NumberFormatException e){
				continue;
			}

			ig.setType(name.substring(0,name.lastIndexOf("_")));
			Resource geometry=s.getObject().asResource();
			String ewkt=geometry.getProperty(GEOM.asBody).getObject().asLiteral().getString();
			setGeometryData(ewkt,ig);
			if(geometry.getProperty(GEOM.hasMaterials)!=null){
			String materials=geometry.getProperty(GEOM.hasMaterials).getObject().asLiteral().getString();
			String materialIndices=geometry.getProperty(GEOM.hasMaterialIndices).getObject().asLiteral().getString();		
			setMaterialData(materials,materialIndices,ig);
			}
			hashmap.put(id, ig);
		}
		return hashmap;
	}
	
	public void loadTestModel(String name){
		InputStream in = getServletContext()
				.getResourceAsStream("/database/model/"+name+".ttl");
		model=ModelFactory.createDefaultModel();
		model.read(in,null,"TTL");
		InputStream in2 = getServletContext()
				.getResourceAsStream("/database/model/"+name+"_geometry.ttl");
		model.read(in2,null,"TTL");
		InputStream in3 = getServletContext()
				.getResourceAsStream("/database/model/"+name+"_material.ttl");
		model.read(in3,null,"TTL");
	}
	
    public void setGeometryData(String ewkt,InstanceGeometry ig){
    	Geometry geometry=EwktReader.parseGeometry(ewkt);
    	
        if(geometry.geometryTypeId()==GeometryType.TYPE_TRIANGULATEDSURFACE){
        	Polyhedron p=new Polyhedron((TriangulatedSurface)geometry);
        	double[] points=new double[p.getVertices().size()*3];
        	ig.setPoints(points);
        	for(int i=0;i<p.getVertices().size();i++){
        		points[i*3]=p.getVertices().get(i).pnt.x();
        		points[i*3+1]=p.getVertices().get(i).pnt.y();
        		points[i*3+2]=p.getVertices().get(i).pnt.z();
        	}
        	int[] pointers=new int[p.getFaces().size()*3];
        	ig.setPointers(pointers);
        	for(int i=0;i<p.getFaces().size();i++){
        		pointers[i*3]=p.getFaces().get(i).getVertexIndices().get(0);
        		pointers[i*3+1]=p.getFaces().get(i).getVertexIndices().get(1);
        		pointers[i*3+2]=p.getFaces().get(i).getVertexIndices().get(2);
        	}
        }
    }
    
    public void setMaterialData(String materials,String materialIndices,InstanceGeometry ig){
    	String[] indices=materialIndices.split(" ");
    	int[] result=new int[indices.length];
    	for(int i=0;i<indices.length;i++){
    		result[i]=Integer.parseInt(indices[i]);
    	}
    	ig.setMaterialIndices(result);
    	
    	String[] ms=materials.split(" ");
    	float[] colors=new float[ms.length];
    	for(int i=0;i<ms.length;i++){
    		colors[i]=Float.parseFloat(ms[i]);
    	}
    	ig.setColors(colors);
    	
    }

	public String uploadModel() {
		// TODO Auto-generated method stub
		return null;
	}


	public String uploadQuery() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public InstanceGeometry showTempGeometry(String wkt){
		InstanceGeometry ig=new InstanceGeometry();
		ig.setType("Temp");
		setGeometryData(wkt,ig);
		return ig;
	}

	public List<String[]> getResults(String query) {
		
		MultiUnion union = new MultiUnion(new Graph[] {model.getGraph(),
				schema.getGraph()});
		Model unionModel = ModelFactory.createModelForGraph(union);
		OntModel ontModel = ModelFactory.createOntologyModel();
		ontModel.add(unionModel);
		long start=System.currentTimeMillis();
		Query q = QueryFactory.create(query);
		QueryExecution qe = QueryExecutionFactory.create(q, ontModel);
		ResultSet qresults = qe.execSelect();
		String[] rVar=new String[qresults.getResultVars().size()];
		for(int i=0;i<qresults.getResultVars().size();i++){
			rVar[i]=qresults.getResultVars().get(i);
		}
		List<String[]> results=new ArrayList<String[]>();
		results.add(rVar);
		while(qresults.hasNext()){
			String[] result=new String[rVar.length];
			QuerySolution qs=qresults.next();
			for (int i=0;i<rVar.length;i++){
				RDFNode node=qs.get(rVar[i]);
				if(node.isResource()){
				result[i]=qs.get(rVar[i]).asResource().getLocalName();
				}
				else if(node.isLiteral()){
					result[i]=qs.get(rVar[i]).asLiteral().toString();
				}
				else{
					result[i]="";
				}
			}
			results.add(result);
		}
		long end=System.currentTimeMillis();
		double time=((double)(end-start))/1000;
		String[] s=new String[2];
		s[0]=Long.toString(model.size());
		s[1]=Double.toString(time);
		results.add(s);
		return results;
	}
	

  public String greetServer(String input) throws IllegalArgumentException {
    // Verify that the input is valid.
    if (!FieldVerifier.isValidName(input)) {
      // If the input is not valid, throw an IllegalArgumentException back to
      // the client.
      throw new IllegalArgumentException(
          "Name must be at least 4 characters long");
    }

    String serverInfo = getServletContext().getServerInfo();
    String userAgent = getThreadLocalRequest().getHeader("User-Agent");

    // Escape data from the client to avoid cross-site script vulnerabilities.
    input = escapeHtml(input);
    userAgent = escapeHtml(userAgent);

    return "Hello, " + input + "!<br><br>I am running " + serverInfo
        + ".<br><br>It looks like you are using:<br>" + userAgent;
  }

  /**
   * Escape an html string. Escaping data received from the client helps to
   * prevent cross-site script vulnerabilities.
   *
   * @param html the html string to escape
   * @return the escaped string
   */
  private String escapeHtml(String html) {
    if (html == null) {
      return null;
    }
    return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(
        ">", "&gt;");
  }


@Override
public String modelServer(String name) throws IllegalArgumentException {
	// TODO Auto-generated method stub
	return null;
}
}
