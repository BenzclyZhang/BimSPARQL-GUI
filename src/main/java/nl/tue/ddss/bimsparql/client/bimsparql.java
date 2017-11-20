package nl.tue.ddss.bimsparql.client;

import nl.tue.ddss.bimsparql.BimSPARQLNS;
import nl.tue.ddss.convert.Namespace;
import nl.tue.ddss.bimsparql.client.ModelScene;
import nl.tue.ddss.bimsparql.client.ModelService;
import nl.tue.ddss.bimsparql.client.ModelServiceAsync;
import thothbot.parallax.core.client.RenderingPanel;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class bimsparql implements EntryPoint {
  /**
   * The message displayed to the user when the server cannot be reached or
   * returns an error.
   */

  /**
   * This is the entry point method.
   */
  private TextArea sparqlArea = new TextArea();
	private FlexTable queryOutput = new FlexTable();
	private ModelServiceAsync myService = (ModelServiceAsync) GWT.create(ModelService.class);
	private ModelScene scene = new ModelScene(myService);
	private HTML executionTime=new HTML();
	
	private static final String prefixes="PREFIX ifcowl: <"+Namespace.IFC2X3_TC1+">\n"+"PREFIX ifc: <"+Namespace.IFC2X3_TC1+">\n"+"PREFIX list: <"+Namespace.LIST+">\n"+"PREFIX expr: <"+Namespace.EXPRESS+">\n"
			+ "PREFIX schm:<"+BimSPARQLNS.SCHM+">\n" + "PREFIX pset:<"+BimSPARQLNS.PSET+">\n"+ "PREFIX spt:<"+BimSPARQLNS.SPT+">\n"+"PREFIX pdt:<"+BimSPARQLNS.PDT+">\n"+"PREFIX qto:<"+BimSPARQLNS.QTO+">\n"+"PREFIX geom:<"+BimSPARQLNS.GEOM+">\n";
	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {
		DockLayoutPanel p = new DockLayoutPanel(Unit.EM);
		
		Image image = new Image();
      image.setWidth("140px");
		image.setUrl("http://127.0.0.1:8888/Logo.jpg");
		FlowPanel fp=new FlowPanel();
		fp.getElement().getStyle().setBackgroundColor("#D3D3D3");
		p.addNorth(fp, 2);
		fp.add(image);
		TabLayoutPanel tabPanel = new TabLayoutPanel(1.5, Unit.EM);
		VerticalPanel model=new VerticalPanel();
		FileUpload upload=new FileUpload();
		model.add(upload);
		tabPanel.add(model, "Model");
		sparqlArea.setVisibleLines(12);
		sparqlArea.setCharacterWidth(25);
		VerticalPanel sparqlQuery = new VerticalPanel();
		sparqlQuery.add(sparqlArea);
		// CellList<QueryResult> cellList=new CellList<QueryResult>(null);
		HorizontalPanel hPanel=new HorizontalPanel();
		hPanel.setSpacing(0);
		hPanel.getElement().getStyle().setBackgroundColor("#FFFFFF");
		Button queryExecution = createButton("execute");
		Button example1 = createButton("e_1");
		Button example2=createButton("e_2");
		Button example3=createButton("e_3");
		
		

		Button showResults = createButton("show results");
		Button showAll=createButton("show all");
		hPanel.add(queryExecution);
		hPanel.add(showResults);
		hPanel.add(showAll);
		hPanel.setHeight("50px");
		sparqlQuery.add(hPanel);
		
		HorizontalPanel examples=new HorizontalPanel();
		examples.getElement().getStyle().setBackgroundColor("#FFFFFF");
		examples.setSpacing(0);
		examples.add(example1);
		examples.add(example2);
		examples.add(example3);
		
		sparqlQuery.add(examples);
		
		showAll.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				showAll();
			}

			private void showAll() {
				// TODO Auto-generated method stub
				scene.showNormal();
			}

		});
		showResults.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				showResults();
			}

		});
		ScrollPanel scrollTable=new ScrollPanel();
		scrollTable.setSize("360px", "300px");
		queryOutput.setSize("360px", "300px");
		scrollTable.add(queryOutput);
		sparqlQuery.add(scrollTable);
		queryExecution.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				executeQuery();
			}
		});
		example1.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				queryExample1();
			}
		});
		
		example2.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				queryExample2();
			}
		});
		
		example3.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				queryExample3();
			}
		});

		tabPanel.add(sparqlQuery, "SPARQL");
		
		
		
/*		VerticalPanel ruleset=new VerticalPanel();
		tabPanel.add(ruleset, "Ruleset");
		ScrollPanel scrollPanel = new ScrollPanel();
	    scrollPanel.setSize("360px", "250px");
	    FlexTable templates=new FlexTable();
	    templates.setSize("360px", "250px");
	    scrollPanel.setWidget(templates);
	    ruleset.add(scrollPanel);
	    HorizontalPanel templateManage=new HorizontalPanel();
	    templateManage.add(createButton("add"));
	    templateManage.add(createButton("upload"));
	    templateManage.add(createButton("delete"));
	    ruleset.add(templateManage);
	    
	    ScrollPanel scrollPanel2 = new ScrollPanel();
	    scrollPanel2.setSize("360px", "250px");
	    FlexTable rules=new FlexTable();
	    rules.setSize("360px", "250px");
	    scrollPanel2.setWidget(rules);
	    ruleset.add(scrollPanel2);
	    HorizontalPanel ruleManage=new HorizontalPanel();
	    ruleManage.add(createButton("add"));
	    ruleManage.add(createButton("upload"));
	    ruleManage.add(createButton("delete"));
	    ruleset.add(ruleManage);
	    
		tabPanel.add(new HTML("the other content"), "Report");
		tabPanel.add(new HTML("plugin"),"Plugin");
		tabPanel.add(new HTML("help"),"Help");
//		tabPanel.add(new HTML("plugin"),"Plugin");*/
		
		p.addWest(tabPanel, 30);
		
		p.addSouth(executionTime, 3);
        
		RenderingPanel renderingPanel = new RenderingPanel();
		renderingPanel.setAnimatedScene(scene);
		p.add(renderingPanel);
		RootLayoutPanel.get().add(p);
		
		queryOutput.addClickHandler(new ClickHandler() {

		    public void onClick(ClickEvent event) {
		        int rowIndex = queryOutput.getCellForEvent(event).getRowIndex();
		        List<Integer> ids = new ArrayList<Integer>();
		        List<String> wkts=new ArrayList<String>();
		        if (rowIndex>0){
		        	for (int j=0;j<queryOutput.getCellCount(rowIndex);j++){
		    			String iri = queryOutput.getText(rowIndex, j);
		    			if(iri.contains("_")){
		    			Integer id = Integer.parseInt(iri.substring(iri.indexOf("_") + 1));
		    			ids.add(id);
		    			}
		    			else if(iri.startsWith("TIN Z")){
		    			wkts.add(iri);
		    			scene.addGeometry(iri);
		    			}
		    		scene.showObjects(ids);
		        }		        
		    }
		}});

	}
	
	private Button createButton(String name){
		Button button=new Button(name);
		button.getElement().getStyle().setColor("#000000");
		return button;
		
	}
	

	private void showResults() {
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 1; i < queryOutput.getRowCount(); i++) {
			for (int j=0;j<queryOutput.getCellCount(i);j++){
			String iri = queryOutput.getText(i, j);
			if(iri.contains("_")){
			Integer id = Integer.parseInt(iri.substring(iri.indexOf("_") + 1));
			ids.add(id);
			}
			}
		}

		scene.showObjects(ids);
	}

	public void executeQuery() {
		String queryText = prefixes+sparqlArea.getText();
		AsyncCallback<List<String[]>> callback = new AsyncCallback<List<String[]>>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
				Window.alert("Failure: " + caught.getMessage());
			}
			public void onSuccess(List<String[]> results) {
				queryOutput.removeAllRows();
			for (int i = 0; i < results.size()-1; i++) {
					for (int j=0;j<results.get(i).length;j++){
					queryOutput.setText(i, j, results.get(i)[j]);
					}
				}
			String modelsize="Model size: "+results.get(results.size()-1)[0];
			String resultcount="Result count: "+(results.size()-1);
			String execTime="Execution time: "+results.get(results.size()-1)[1]+" second(s)";
				executionTime.setText(modelsize+"\n"+resultcount+"\n"+execTime);

			}
		};
		myService.getResults(queryText, callback);
	}

	public void queryExample1() {
		String example = "SELECT ?wall \n" + "WHERE{\n" + "?wall a ifcowl:IfcWall .\n" + "}";
		sparqlArea.setText(example);
	}
	
	public void queryExample2(){
		String example = "SELECT ?wall ?slab\n" + "WHERE{\n"
				+ "?wall a ifcowl:IfcWall .\n" + "?slab a ifcowl:IfcSlab .\n" + "?wall spt:touches ?slab .\n" + "}";
				
		sparqlArea.setText(example);
		
	}
	
	public void queryExample3(){
		String example = "SELECT ?space ?b ?slab\n"
				+"WHERE{\n"
				+"?space a ifcowl:IfcSpace .\n"
			+"?space schm:hasSpaceBoundary ?b .\n"
			+"?b a ifcowl:IfcDoor .\n"
			+"?storey schm:hasSpatialDecomposition ?space .\n"
			+"?slab schm:isContainedIn ?storey .\n"
			+"?slab a ifcowl:IfcSlab .\n"
			+"}";
		sparqlArea.setText(example);
		
	}

	public class QueryResult {

	}
}
