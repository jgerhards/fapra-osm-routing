# General information
This is a project developed for a course offered by university of Stuttgart.
The task was to implement an efficient routing algorithm on a grid graph.
This graph was to be created based on OpenStreetMap data.
Overall, some applications contained in this project require a large
amount of memory at runtime. Due to this, it may be necessary to increase the
amount of memory available to the application using the Java JVM option "-Xmx".

-----------------
Information for evaluation:
- The first part of the tasks was solved together with Tim Schneider.
- The algorithm used for the second part of the project was implemented by Jan Gerhards.
- The algorithm used to speed up routing is based on hub labels and contraction hierarchies.
  - Nodes in the top levels of contraction hierarchies include labels.
  - The lower levels calculate hub labels ad hoc based on the contraction hierarchy algorithm.
  - Based on the paper "Seamless Interpolation Between Contraction Hierarchies and Hub Labels
    for Fast and Space-Efficient Shortest Path Queries in Road Networks" by Stefan Funke.

## How to...

### ... Create a grid graph
First, place a pbf file containing the coastlines in the
[src/main/resources](./src/main/resources) directory. After this, open the java file
[src/main/java/de/fmi/searouter/dijkstragrid/GridCreator.java](./src/main/java/de/fmi/searouter/dijkstragrid/GridCreator.java).
In it, it is possible to set some parameters which are to be considered in the calculation.
Finally, run the main() method of the class. The result may take some time to be calculated,
depending on both the parameters set and the performance characteristics of the machine
used to execute the application.

#### Parameters to set
- DIMENSION_LATITUDE: Number of nodes that are to be generated along any latitude.
  Sets part of the dimensions of the grid.
- DIMENSION_LONGITUDE: Number of nodes that are to be generated along any longitude.
  Sets part of the dimensions of the grid.
- NUMBER_OF_THREADS_FOR_IN_WATER_CHECK: The number of threads that will be used when
  performing the point-in-water check. The more threads, the faster the calculation will be, provided
  the hardware is capable of supporting the given number of threads. In our tests, we found that double
  the amount of logical cores works pretty well (as long as nothing else should be done at the same time).
- GRID_FMI_FILE_NAME: The name of the file the calculated grid will be stored in.
- PBF_FILE_PATH: **Located in a different file!** This parameter is used to change the name of the pbf file
  in the resources directory. By default, the name is "planet-coastlinespbf-cleaned.pbf". If that should be
  changed, it is possible to do so in
  [src/main/java/de/fmi/searouter/coastlinegrid/CoastlineWays.java](./src/main/java/de/fmi/searouter/coastlinegrid/CoastlineWays.java).


### ... Generate Data for the routing algorithm based on hub labels
First, place an fmi file containing a grid graph in the top level directory
(fapra-osm-routing). After this, open the java file
[src/main/java/de/fmi/searouter/hublabelcreation/LabelCreator.java](./src/main/java/de/fmi/searouter/hublabelcreation/LabelCreator.java).
In it, it is possible to set some parameters which are to be considered in the calculation.
Finally, run the main() method of the class. The result may take some time to be calculated,
depending on both the parameters set and the performance characteristics of the machine
used to execute the application. During calculation, intermediate results are serialized
after some steps. Due to this, running the algorithm a second time may be faster than the first
time. Do keep in mind though, that this means that if a new grid graph should be calculated,
all intermediate files should be deleted (else the new graph is ignored and the previous
intermediate results are used). **Note for the grid graph:** Due to the way initial data
structures are initialized, any node within the grid graph may only be connected to a maximum
of four other nodes at the same time. The graph should also be bidirectional. This constraint
is not relevant only in the logic, only during initialization of data structures.

#### Parameters to set
- FMI_FILE_NAME: The name of the fmi file containing the representation of the graph.
- NUM_OF_THREADS: The maximum number of threads that may be used
  during calculation. The more threads, the faster the calculation will be, provided
  the hardware is capable of supporting the given number of threads. In our tests, we found that double
  the amount of logical cores works pretty well (as long as nothing else should be done at the same time).
- NUM_OF_NO_LABEL_LVLS: The number of levels for which no hub labels will be calculated. More
  information can be found in the following section.

#### How many hub label levels should I choose?
The routing algorithm used is mainly based on the hub label algorithm. Still, it is
possible to configure that nodes below a certain level are not assigned labels (levels
referring to the levels generated by an initial step of contraction hierarchy). In this
case, the algorithm will generate temporary labels for nodes without pre-computed ones.
However, it is important to note that this is not as fast as a normal contraction
hierarchy routing. Due to this, this option is only intended as a backup if the memory
is not sufficient for the full label information. As each lower level contains more nodes,
even one level without hub labels reduces memory requirements significantly. Overall,
this means that as few levels without labels as possible but as many as necessary
should be chosen. In our test runs, we found that 2 levels without labels lowers the
amount of memory required notably while not increasing runtime by an undue amount compared
to a pure hub label algorithm.


###  ... Install and run the routing frontend
First place a .fmi file containing the dijkstra grid graph in the
[src/main/resources](./src/main/resources) directory called
```exported_grid.fmi```. If instead of the dijkstra router, the hub label
router should be used, insert the file with the name ```hub_label_data```,
which was generated by the LabelCreator.
After this, execute the following commands:
```shell
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```
Finally, open browser and access the url "http://localhost:8080/".

#### Parameters to set
- USE_HUB_LABEL_ROUTER: true if the hub label router should be used. If set to false
  the dijkstra router will be used instead.
- IS_TEST_RUN: true if a test run should be performed. In this case, the normal application
  will not be started. Instead, a number of random routes will be calculated. The results
  of these calculations will be printed to the console.
- IGNORE_RES_NO_ROUTE: If a test run is performed, it is possible to ignore the calculation
  times of routing requests for which both points were on water but no route could be found.
  If this parameter is set to true, this will be done. Otherwise these times will be considered
  when calculating the average calculating time.
- TEST_NUM_OF_ROUTES: The number of random routes which should be generated in a test run.
  Note that not all of these will necessarily be a valid routing request. As some random
  points may be on land, the total number of routes calculated will be lower than this number.


## Where to find certain task solutions (**Note**: some updates since last evaluation)

### Task 2
PBF file import is performed in [CoastlineImporter.java](./src/main/java/de/fmi/searouter/osmimport/CoastlineImporter.java).
However, the whole pre-processing (and also the call of the CoastlineImporters import function is performed in
[GridCreator.java](./src/main/java/de/fmi/searouter/dijkstragrid/GridCreator.java))

### Task 3
Since the initial implementation, we have improved the algorithm we use to perform the
point in water check. It is now based on a multi-level grid. When building the grid, we
check which coastline has to be considered within a given grid cell. When doing this, we
use different checks to ensure a very accurate representation, especially with respect
to lines following exact latitudes. This prevents problems caused by arcs between two
points on a sphere. All classes used for this can be found in the
[coastlinegrid](./src/main/java/de/fmi/searouter/coastlinegrid) package.

### Task 4
Grid Graph: Gets created in the [GridCreator.java](./src/main/java/de/fmi/searouter/dijkstragrid/GridCreator.java).
In order to improve the runtime of this algorithm, the point-in-water check is performed
in mulitple threads at the same time.
 
 #### Task 4.1
 .fmi file import/export is implemented in [Grid.java](./src/main/java/de/fmi/searouter/dijkstragrid/Grid.java)
 
 ### Task 5
 Dijkstra’s Algorithm implemented in
 [DijkstraRouter.java](./src/main/java/de/fmi/searouter/router/DijkstraRouter.java) using the
 [DijkstraHeap](./src/main/java/de/fmi/searouter/router/DijkstraHeap.java) as a heap data structure.
 
 ### Task 6
 The frontend implementation can be found in the [vue-frontend](./vue-frontend) directory.
 It is a web application available under localhost:8080 (see how to section).
 
### Task 7
The preprocessing components can mostly be found in the
[hublablecreation](./src/main/java/de/fmi/searouter/hublablecreation) package, with some usage of
util classes.

The router used is implemented in
[HubLRouter.java](./src/main/java/de/fmi/searouter/router/HubLRouter.java).