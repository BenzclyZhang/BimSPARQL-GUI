# BimSPARQL-GUI

This project is the front end of the project in https://github.com/BenzclyZhang/BimSPARQL. It visualize IFC/ifcOWL building models, and show 
query results of BimSPARQL. It is a Google Web Toolkit application, and can work with Web servelet like Tomcat and Jetty. 

## Getting Started

To install this application, it requires the project of https://github.com/BenzclyZhang/BimSPARQL. All the dependencies are managed by Maven. To build them, you can clone these two projects in your work directory. Here assuming they are in the directories of <yourworkspace>/BimSPARQL-GUI and <yourworkspace>/BimSPARQL.

### Installation

Firstly build the BimSPARQL project, in <yourworkspace>/BimSPARQL run:

```
mvn clean install
```
Then build the BimSPARQL-GUI project, in <yourworkspace>/BimSPARQL-GUI run:

```
mvn clean install
```

The output war file is in the directory of <yourworkspace>/BimSPARQL-GUI/target/. The war file then can be put into <Tomcat_root>/webapps directory of Tomcat to run. It currently may only support windows version.
It should have a webpage like this:
![alt text](https://github.com/BenzclyZhang/BimSPARQL-GUI/blob/master/gui.jpg) 

There is a model (Duplex_A_20110505.ifc) pre-loaded as an example.
When this app is running, it creates a database in the directory of <Tomcat_root>/uploads, to temporarily store IFC files and RDF files in <Tomcat_root>/uploads/model and Jena TDB database in <Tomcat_root>/uploads/tdb. 

The model upload page in the GUI is currently fake. Other *.ifc model can be put in <Tomcat_root>/uploads/model in the runtime and make sure it is the newest modified model. Then refresh the web page. The application only shows the newest modified IFC model.

To run it in a Development Model, in <yourworkspace>/BimSPARQL-GUI run:

```
mvn gwt:run
```

## Some limitations:

1.	It only supports IFC2X3_TC1 now, but it is not difficult to make it compatible with IFC4_Add1 if it is needed.
2.  Working with Linux system needs more test.

## Authors

* **Chi Zhang** 

## License

This project is licensed under the AGPL License - see the [LICENSE.md](LICENSE.md) file for details

To further develop them in Eclipse, See instructions of Mojo Maven GWT plugin and GWT official Webpage.

Instructions on: https://gwt-maven-plugin.github.io/gwt-maven-plugin/
http://www.gwtproject.org/usingeclipse.html

