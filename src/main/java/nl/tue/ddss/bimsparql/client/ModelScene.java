package nl.tue.ddss.bimsparql.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import nl.tue.ddss.bimsparql.client.ModelService;
import nl.tue.ddss.bimsparql.client.ModelServiceAsync;
import nl.tue.ddss.bimsparql.shared.InstanceGeometry;
import thothbot.parallax.core.client.AnimatedScene;
import thothbot.parallax.core.client.controls.TrackballControls;
import thothbot.parallax.core.shared.cameras.PerspectiveCamera;
import thothbot.parallax.core.shared.core.Face3;
import thothbot.parallax.core.shared.core.FastMap;
import thothbot.parallax.core.shared.core.Geometry;
import thothbot.parallax.core.shared.core.GeometryObject;
import thothbot.parallax.core.shared.core.Raycaster;
import thothbot.parallax.core.shared.core.Raycaster.Intersect;
import thothbot.parallax.core.shared.lights.DirectionalLight;
import thothbot.parallax.core.shared.materials.Material;
import thothbot.parallax.core.shared.materials.MeshBasicMaterial;
import thothbot.parallax.core.shared.materials.MeshFaceMaterial;
import thothbot.parallax.core.shared.math.Color;
import thothbot.parallax.core.shared.math.Vector3;
import thothbot.parallax.core.shared.objects.Mesh;

public class ModelScene extends AnimatedScene {
	PerspectiveCamera camera;
	Mesh particleLight;

	private TrackballControls control;
	double mouseDeltaX = 0, mouseDeltaY = 0;
	Intersect intersected;

	Raycaster raycaster;
    
	final static Color sceneColor=new Color(0xf0f0f0);
	final static Color highlightColor=new Color(0xFF0000);
	final static Color tempGeometryColor=new Color(0x00FF00);
	final static Color defaultColor=new Color(0x808080);
	final static Color transColor=new Color(0x00FFFF);
	static MeshBasicMaterial defaultMaterial=new MeshBasicMaterial();
	static MeshBasicMaterial transMaterial=new MeshBasicMaterial();
	static MeshBasicMaterial highlightMaterial=new MeshBasicMaterial();
	static MeshBasicMaterial tempGeometryMaterial=new MeshBasicMaterial();

	Vector3 max = new Vector3(0, 0, 0);
	Vector3 min = new Vector3(0, 0, 0);

	List<GeometryObject> objects = new ArrayList<GeometryObject>();
	HashMap<Integer, GeometryObject> objectMap = new HashMap<Integer, GeometryObject>();
	HashMap<Integer,Material> materialMap=new HashMap<Integer,Material>();
	Mesh plane;
	
	

	GeometryObject selected;

	FastMap<Integer> currentHex = new FastMap<Integer>();

	int width = 0, height = 0;

	private ModelServiceAsync myService = (ModelServiceAsync) GWT.create(ModelService.class);

	public ModelScene(ModelServiceAsync myService) {
		this.myService = myService;
	}

	@Override
	protected void onStart() {
		
		defaultMaterial.setColor(defaultColor);
		transMaterial.setColor(transColor);
		transMaterial.setTransparent(true);
		transMaterial.setOpacity(0.3);
		highlightMaterial.setColor(highlightColor);
		tempGeometryMaterial.setColor(tempGeometryColor);
		
		camera = new PerspectiveCamera(60, // fov
				getRenderer().getAbsoluteAspectRation(), // aspect
				1, // near
				1000000 // far
		);
		camera.getPosition().setZ(500);

		this.control = new TrackballControls(camera, getCanvas());
		this.control.setPanSpeed(0.2);
		this.control.setDynamicDampingFactor(0.3);

		this.control.setRotateSpeed(1.0);
		this.control.setZoomSpeed(1.2);

		this.control.setZoom(true);
		this.control.setPan(true);

		this.control.setStaticMoving(true);
		this.control.setDynamicDampingFactor(0.3);
		
		  DirectionalLight light = new DirectionalLight( 0xffffff, 1.0 );
	      light.getPosition().set( 1.0 ).normalize();
	      getScene().add( light );


		AsyncCallback<HashMap<Integer, InstanceGeometry>> callback = new AsyncCallback<HashMap<Integer, InstanceGeometry>>() {
			public void onFailure(Throwable caught) {
				Window.alert("Failure: " + caught.getMessage());
			}

			public void onSuccess(HashMap<Integer, InstanceGeometry> hashmap) {
				getRenderer().setClearColor(sceneColor);
				getRenderer().setSortObjects(false);
				getRenderer().setAutoUpdateObjects(true);
				getRenderer().setAutoUpdateScene(true);
				
				int ii=0;
				for (Integer key : hashmap.keySet()) {
					ii++;
					if(ii%1000==0){
						System.out.println("");
					}
					InstanceGeometry ig = hashmap.get(key);
					if (!(ig.getType().equals("IfcSpace") || ig.getType().equals("IfcOpeningElement"))) {
						Geometry geometry = new Geometry();
						for (int i = 0; i < ig.getPoints().length; i = i + 3) {
							Vector3 v = new Vector3(ig.getPoints()[i], ig.getPoints()[i + 1], ig.getPoints()[i + 2]);
							geometry.getVertices().add(v);
							addVector(v);
						}
						
						Mesh mesh;
						Material m;
						if (ig.getColors() != null && ig.getColors().length > 0) {
							List<Material> materials = new ArrayList<Material>();						
							for (int i = 0; i < ig.getColors().length; i = i + 4) {
								Color c=new Color();
								c.setRGB(ig.getColors()[i], ig.getColors()[i + 1], ig.getColors()[i + 2]);
								geometry.getColors().add(c);
								MeshBasicMaterial material = new MeshBasicMaterial();
								material.setColor(c);
								material.setOpacity(ig.getColors()[i + 3]);
								if (ig.getColors()[i + 3]<1){
									material.setTransparent(true);
								}							
								materials.add(material);
							}
							m = new MeshFaceMaterial(materials);
					        int size=materials.size();
							for (int i = 0; i < ig.getPointers().length; i = i + 3) {
								Face3 f = new Face3(ig.getPointers()[i], ig.getPointers()[i + 1], ig.getPointers()[i + 2]);
								
								geometry.getFaces().add(f);
								if(ig.getMaterialIndices()!=null){
								int index=geometry.getFaces().indexOf(f);
								if(ig.getMaterialIndices()[index]>=0){
								f.setMaterialIndex(ig.getMaterialIndices()[index]);
								}else{
									f.setMaterialIndex(size+ig.getMaterialIndices()[index]);
								}
								}else{
									f.setMaterialIndex(0);
								}
							}
							geometry.computeVertexNormals();
							mesh = new Mesh(geometry, m);
							mesh.setName(ig.getType()+"_"+ig.getId());
						} else {
							geometry.getColors().add(defaultColor);
							m = new MeshBasicMaterial();
							((MeshBasicMaterial)m).setColor(defaultColor);	
							for (int i = 0; i < ig.getPointers().length; i = i + 3) {
								Face3 f = new Face3(ig.getPointers()[i], ig.getPointers()[i + 1], ig.getPointers()[i + 2]);								
								geometry.getFaces().add(f);
								f.setMaterialIndex(0);								
							}
							mesh = new Mesh(geometry,m);
							mesh.setName(ig.getType()+"_"+ig.getId());
						}

						//
						getScene().add(mesh);
						materialMap.put(key, m);
						objectMap.put(key, mesh);
					}
					else{
						Geometry geometry = new Geometry();
						for (int i = 0; i < ig.getPoints().length; i = i + 3) {
							Vector3 v = new Vector3(ig.getPoints()[i], ig.getPoints()[i + 1], ig.getPoints()[i + 2]);
							geometry.getVertices().add(v);
							addVector(v);
						}
						for (int i = 0; i < ig.getPointers().length; i = i + 3) {
							Face3 f = new Face3(ig.getPointers()[i], ig.getPointers()[i + 1], ig.getPointers()[i + 2]);
							
							geometry.getFaces().add(f);
							f.setMaterialIndex(0);
						}
						geometry.computeFaceNormals();
						geometry.computeVertexNormals();
							Color c = new Color();
							c.setRGB(0, 1, 1);
							geometry.getColors().add(c);					
							Mesh mesh = new Mesh(geometry,transMaterial);
							mesh.setName(ig.getType()+"_"+ig.getId());
						getScene().add(mesh);
						mesh.setVisible(false);
						objectMap.put(key, mesh);					
					}
				}
				
			//	addLotLine();
				// raycaster = new Raycaster();
				Vector3 center = new Vector3((max.getX() + min.getX()) / 2, (max.getY() + min.getY()) / 2,
						(max.getZ() + min.getZ()) / 2);
				control.setTarget(center);
				Vector3 cameraPosition = new Vector3(center.getX() + max.getX() - min.getX(),
						center.getY() + max.getY() - min.getY(), center.getZ());
				camera.setPosition(cameraPosition);
				camera.setUp(new Vector3(0, 0, 1));
				
			}

		};

		myService.getGeometries(callback);
		
	}
	

	private void addVector(Vector3 v) {
		if (v.getX() > max.getX()) {
			max.setX(v.getX());
		}
		if (v.getY() > max.getY()) {
			max.setY(v.getY());
		}
		if (v.getZ() > max.getZ()) {
			max.setZ(v.getZ());
		}
		if (v.getX() < min.getX()) {
			min.setX(v.getX());
		}
		if (v.getY() < min.getY()) {
			min.setY(v.getY());
		}
		if (v.getZ() < min.getZ()) {
			min.setZ(v.getZ());
		}
	}

	/*
	 * protected void onUpdate(double duration) { camera.lookAt(
	 * getScene().getPosition() );
	 * 
	 * // find intersections
	 * 
	 * Vector3 vector = new Vector3( mouseDeltaX, mouseDeltaY, 1 );
	 * raycaster.set( camera.getPosition(), vector.sub( camera.getPosition()
	 * ).normalize() ); Color currentColor=new Color();
	 * 
	 * List<Raycaster.Intersect> intersects = raycaster.intersectObjects(
	 * getScene().getChildren(), false );
	 * 
	 * if ( intersects.size() > 0 ) { if ( intersected == null ||
	 * intersected.object != intersects.get( 0 ).object ) { if(intersected !=
	 * null) { ((MeshLambertMaterial)intersected.object.getMaterial()).setColor(
	 * currentColor); }
	 * 
	 * intersected = new Intersect(); intersected.object = (GeometryObject)
	 * intersects.get( 0 ).object; currentColor =
	 * ((MeshLambertMaterial)intersected.object.getMaterial()).getColor();
	 * ((MeshLambertMaterial)intersected.object.getMaterial()).getColor().
	 * setHex( 0xff0000 ); } } else { if ( intersected != null )
	 * ((MeshLambertMaterial)intersected.object.getMaterial()).setColor(
	 * currentColor );
	 * 
	 * intersected = null; }
	 * 
	 * getRenderer().render(getScene(), camera); }
	 */

	public void showObjects(List<Integer> ids) {
		for (Integer id : objectMap.keySet()) {
			objectMap.get(id).setVisible(false);
		}
		for (Integer id : ids) {
			GeometryObject object = objectMap.get(id);
			if (object != null) {
				object.setVisible(true);
			}
		}
	}
	
	public void markObjects(List<Integer> ids){
		for (Integer id : objectMap.keySet()) {
			objectMap.get(id).setMaterial(transMaterial);
		}
		for (Integer id : ids) {
			GeometryObject object = objectMap.get(id);
			if (object != null) {
				object.setMaterial(highlightMaterial);
			}
		}
		
	}
	
	public void showNormal() {
		for (GeometryObject geometry : objectMap.values()) {
			if(geometry.getName().contains("IfcSpace_")||geometry.getName().contains("IfcOpeningElement_")){
			geometry.setVisible(false);
			}
			else{
				geometry.setVisible(true);
			}
		}
	}

	public void showAll() {
		for (Integer id : objectMap.keySet()) {
			GeometryObject geometry=objectMap.get(id);
/*			if(geometry.getName().equals("IfcSpace")||geometry.getName().equals("IfcOpeningElement")){
			geometry.setVisible(false);
			geometry.setMaterial(transMaterial);
			}
			else{*/
				geometry.setVisible(true);
//				geometry.setMaterial(materialMap.get(id));
//			}
		}
	}

	/*
	 * public void onTouchDown(int screenX, int screenY, int pointer, int
	 * button) { Vector3 vector = new Vector3(mouseDeltaX, mouseDeltaY,
	 * 0.5).unproject(camera);
	 * 
	 * Raycaster raycaster = new Raycaster(camera.getPosition(),
	 * vector.sub(camera.getPosition()).normalize());
	 * 
	 * List<Raycaster.Intersect> intersects =
	 * raycaster.intersectObjects(objects, false);
	 * 
	 * if (intersects.size() > 0) { control.setEnabled(false);
	 * 
	 * selected = intersects.get(0).object;
	 * 
	 * } }
	 * 
	 * public void checkHighlight() { // find intersections
	 * 
	 * // create a Ray with origin at the mouse position // and direction into
	 * the scene (camera direction)
	 * 
	 * Vector3 vector = new Vector3(mouseDeltaX, mouseDeltaX, 1);
	 * 
	 * // Projector.unprojectVector( vector, camera ); Raycaster ray = new
	 * Raycaster(camera.getPosition(),
	 * vector.sub(camera.getPosition()).normalize());
	 * 
	 * // create an array containing all objects in the scene with which the //
	 * ray intersects List<Intersect> intersects = ray.intersectObjects(objects,
	 * true);
	 * 
	 * // INTERSECTED = the object in the scene currently closest to the camera
	 * // and intersected by the Ray projected from the mouse position
	 * GeometryObject intersected = null; double[] mouseSphereCoords; Color
	 * baseColor = null; // if there is one (or more) intersections if
	 * (intersects.size() > 0) { // case if mouse is not currently over an //
	 * object if (intersected == null) { intersected = intersects.get(0).object;
	 * ((MeshLambertMaterial)
	 * intersected.getMaterial()).setColor(highlightedColor); } else {
	 * intersected.getGeometry().setColorsNeedUpdate(true); intersected =
	 * intersects.get(0).object; ((MeshLambertMaterial)
	 * intersected.getMaterial()).setColor(highlightedColor); } // upsdate
	 * mouseSphere coordinates and update colors mouseSphereCoords = new
	 * double[3]; mouseSphereCoords[0] = intersected.getWorldPosition().getX();
	 * mouseSphereCoords[1] = intersected.getWorldPosition().getY();
	 * mouseSphereCoords[2] = intersected.getWorldPosition().getZ();
	 * intersected.getGeometry().setColorsNeedUpdate(true); } else // there are
	 * no intersections { // restore previous intersection object (if it exists)
	 * to its // original color if (intersected != null) {
	 * ((MeshLambertMaterial) intersected.getMaterial()).setColor(baseColor);
	 * intersected.getGeometry().setColorsNeedUpdate(true); } // remove previous
	 * intersection object reference // by setting current intersection object
	 * to "nothing"
	 * 
	 * intersected = null; mouseSphereCoords = null; } }
	 * 
	 * public void checkSelection() { // find intersections // create a Ray with
	 * origin at the mouse position // and direction into the scene (camera
	 * direction) Vector3 vector = new Vector3(mouseDeltaX, mouseDeltaY, 1); //
	 * Projector.unprojectVector( vector, camera ); Raycaster ray = new
	 * Raycaster(camera.getPosition(),
	 * vector.sub(camera.getPosition()).normalize());
	 * 
	 * // create an array containing all objects in the scene with which the //
	 * ray intersects List<Intersect> intersects = ray.intersectObjects(objects,
	 * true); List<Intersect> selectedFaces = new ArrayList<Intersect>();
	 * 
	 * // if an intersection is detected if (intersects.size() > 0) { // test
	 * items in selected faces array int test = -1; for (Intersect arrayItem :
	 * selectedFaces) { // if the faceIndex and object ID are the same between
	 * the // intersect and selected faces , // the face index is recorded if
	 * (intersects.get(0).faceIndex == arrayItem.faceIndex &&
	 * intersects.get(0).object.getId() == arrayItem.object.getId()) { test =
	 * selectedFaces.indexOf(arrayItem); } } ;
	 * 
	 * // if is a previously selected face, change the color back to green, //
	 * otherswise change to blue if (test >= 0) {
	 * intersects.get(0).face.setColor(new Color(0x44dd66)); //
	 * selectedFaces.splice(test, 1); } else {
	 * intersects.get(0).face.setColor(new Color(0x222288));
	 * selectedFaces.add(intersects.get(0)); }
	 * 
	 * intersects.get(0).object.getGeometry().setColorsNeedUpdate(true); } }
	 */

	@Override
	protected void onUpdate(double duration) {
		this.control.update();
		getRenderer().render(getScene(), camera);
	}

	public void addGeometry(String wkt) {		
		AsyncCallback<InstanceGeometry> callback = new AsyncCallback<InstanceGeometry>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors.
				Window.alert("Failure: " + caught.getMessage());
			}
			public void onSuccess(InstanceGeometry ig) {
				Geometry geometry = new Geometry();
				for (int i = 0; i < ig.getPoints().length; i = i + 3) {
					Vector3 v = new Vector3(ig.getPoints()[i], ig.getPoints()[i + 1], ig.getPoints()[i + 2]);
					geometry.getVertices().add(v);
					addVector(v);
				}
				for (int i = 0; i < ig.getPointers().length; i = i + 3) {
					Face3 f = new Face3(ig.getPointers()[i], ig.getPointers()[i + 1], ig.getPointers()[i + 2]);
					
					geometry.getFaces().add(f);
					f.setMaterialIndex(0);
				}
				geometry.computeFaceNormals();
				geometry.computeVertexNormals();
					Color c = new Color();
					c.setRGB(0, 1, 1);
					geometry.getColors().add(tempGeometryColor);					
					Mesh mesh = new Mesh(geometry,tempGeometryMaterial);
					mesh.setName(ig.getType());
				getScene().add(mesh);
			}
		};
		myService.showTempGeometry(wkt, callback);
	}

}
