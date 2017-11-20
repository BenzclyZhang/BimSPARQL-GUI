# BimSPARQL-GUI

This project is the front end of the project in https://github.com/BenzclyZhang/BimSPARQL. It visualize IFC/ifcOWL building models, and show 
query results of BimSPARQL. It is a Google Web Toolkit application, and can work with Web servelet like Tomcat and Jetty. 

## Getting Started

To install this application, it requires the project of https://github.com/BenzclyZhang/BimSPARQL. All the dependencies are managed by Maven. To build them, you can clone these two projects in your work directory. Here assuming they are in the directories of /BimSPARQL-GUI and /BimSPARQL.

### Break down into end to end tests

Firstly build the BimSPARQL project, in /BimSPARQL run:

* mvn clean install

Then build the BimSPARQL-GUI project, in /BimSPARQL-GUI run:

* mvn clean install

The output war file is in the directory of /BimSPARQL-GUI/target/. The war file then can be put into /webapps directory of Tomcat or Jetty to run.
It should have a webpage like this:
![alt text](https://github.com/BenzclyZhang/BimSPARQL-GUI/blob/master/gui.jpg) 

There is a model (Duplex_A_20110505.ifc) pre-loaded as an example.
When this app is running, it creates a database in the directory of /uploads, to temporarily store IFC files, RDF files (in /uploads/model) and Jena TDB database in (/uploads/tdb). 

The model upload page is currently fake. Other IFC model can be put in /uploads/model in the runtime and make sure it is the newest modified model. Then refresh the web page. The app only shows the newest modified IFC model.

To run it in a Development Model, in /BimSPARQL-GUI run:

* mvn gwt:run

## Some limitations:

2.	It only supports IFC2X3_TC1 now, but it is not difficult to make it compatible with IFC4_Add1 if it is needed.
3.	The performance for big building models, including model loading, rendering geometry and SPARQL query are not satisfactory enough.

## Authors

* **Chi Zhang** 

## License

This project is licensed under the AGPL License - see the [LICENSE.md](LICENSE.md) file for details

To further develop them in Eclipse, Google Plugin for Eclipse may be needed. 
See instructions on: http://www.gwtproject.org/usingeclipse.html

