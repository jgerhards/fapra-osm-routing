## FaPra: Algorithms on OpenStreetMap data

Jan Gerhards und Tim Schneider (Ilias-Name: TimSchneider1)

###  How to install and run the routing frontend

First place a .fmi file containing the dijkstra grid graph in the
[src/main/resources](./src/main/resources) directory called
```ocean.fmi```.

```shell
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

Open browser with url: http://localhost:8080/.

### To execute the pre-processing

Execute the main method of the [GridCreator.java](./src/main/java/de/fmi/searouter/grid/GridCreator.java)
to start the pre-processing containing the import of a PBF file (must be placed in the [src/main/resources](./src/main/resources) directory
called ```planet-coastlines.pbf```), merging of coastlines, grid graph initialization (contain point-in-polygon water/land check) and grid graph.
The resulting grid graph file is named ```exported_grid.fmi``` and will appear
on the main directory level of this project file system.

## Where to find certain task solutions

### Task 2

PBF file import and coastline merging is performed in [CoastlineImporter.java](./src/main/java/de/fmi/searouter/osmimport/CoastlineImporter.java).
However the whole pre-processing (and also the call of the CoastlineImporters import function is performed in [GridCreator.java](./src/main/java/de/fmi/searouter/grid/GridCreator.java))

### Task 3

Distinguish between Water and Land: Is performed with a point-in-polygon check within the [GridCreator.java](./src/main/java/de/fmi/searouter/grid/GridCreator.java).
[BevisChatelainInPolygonCheck](./src/main/java/de/fmi/searouter/grid/BevisChatelainInPolygonCheck.java) contains
 the hereby used underlying algorithm.
 
 ### Task 4
 
 Grid Graph: Gets created in the [GridCreator.java](./src/main/java/de/fmi/searouter/grid/GridCreator.java) class.
 
 #### Task 4.1
 
 .fmi file import/export is implemented in [Grid.java](./src/main/java/de/fmi/searouter/grid/Grid.java)
 
 ### Task 5
 
 Dijkstra’s Algorithm implemented in [DijkstraRouter.java](./src/main/java/de/fmi/searouter/router/DijkstraRouter.java) using the
 [DijkstraHeap](./src/main/java/de/fmi/searouter/router/DijkstraHeap.java) as a heap data structure.
 
 ### Task 6
 
 The frontend implementation can be found in the [vue-frontend](./vue-frontend) directory.
 It is a web application available under localhost:8080 (see how to install section).