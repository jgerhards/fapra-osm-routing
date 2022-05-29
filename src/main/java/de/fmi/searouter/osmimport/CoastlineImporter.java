package de.fmi.searouter.osmimport;

import crosby.binary.osmosis.OsmosisReader;
import de.fmi.searouter.coastlinecheck.CoastlineChecker;
import de.fmi.searouter.coastlinecheck.CoastlineGridLeaf;
import de.fmi.searouter.coastlinecheck.Coastlines;
import de.fmi.searouter.domain.CoastlineWay;
import de.fmi.searouter.domain.IntersectionHelper;
import de.fmi.searouter.domain.TimIntersectionCheck;
import de.fmi.searouter.osmexport.GeoJsonConverter;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.*;

/**
 * Uses the Osmosis pipeline to parse a PBF file. Imports and merges
 * coast lines.
 */
public class CoastlineImporter implements Sink {

    private Logger logger = LoggerFactory.getLogger(CoastlineImporter.class);

    private InputStream inputStream;

    private List<Way> coastLines;

    private List<CoastlineWay> coastLineWays;

    private Map<Long, Node> allNodes;

    public CoastlineImporter() {
        this.coastLines = new ArrayList<>();
        this.allNodes = new HashMap<>();
        this.coastLineWays = new ArrayList<>();
    }

    @Override
    public void initialize(Map<String, Object> arg0) {
    }

    @Override
    public void process(EntityContainer entityContainer) {
        if (entityContainer instanceof WayContainer) {
            Way currentWay = ((WayContainer) entityContainer).getEntity();
            for (Tag currTagOfWay : currentWay.getTags()) {
                if ("natural".equalsIgnoreCase(currTagOfWay.getKey()) && "coastline".equalsIgnoreCase(currTagOfWay.getValue())) {
                    this.coastLines.add(currentWay);
                }
            }
        } else if (entityContainer.getEntity() instanceof Node) {
            Node node = (Node) entityContainer.getEntity();
            allNodes.put(node.getId(), node);
        }
    }

    @Override
    public void complete() {
        // Map nodes to WayNodes to retrieve and save the coordinates of each WayNode
        for (Way currWay : this.coastLines) {
            for (int i = 0; i < currWay.getWayNodes().size(); i++) {
                Node node = this.allNodes.get(currWay.getWayNodes().get(i).getNodeId());
                if (node != null) {
                    currWay.getWayNodes().set(i, new WayNode(node.getId(), node.getLatitude(), node.getLongitude()));
                }
            }
        }

        // Empty the nodes list to save memory
        this.allNodes = new HashMap<>();

        // Transform all ways to coast line object and then merge
        for (Way currWay : this.coastLines) {
            this.coastLineWays.add(new CoastlineWay(currWay));
        }
        this.coastLineWays = mergeTouchingCoastlines(this.coastLineWays);

        assignNewIdsToCoastlines(this.coastLineWays);
    }

    private List<CoastlineWay> mergeTouchingCoastlines(List<CoastlineWay> coastLinesToMerge) {

        int currSizeOfAllCoastlineWays = coastLinesToMerge.size();

        int currIdx = 0;

        while (true) {
            AbstractMap.Entry<Integer, List<CoastlineWay>> result = findMergePartnerForCoastline(coastLinesToMerge, currIdx);
            List<CoastlineWay> newResult = result.getValue();
            currIdx = result.getKey();

            if (newResult.size() == currSizeOfAllCoastlineWays) {
                currIdx++;
                if (currIdx >= currSizeOfAllCoastlineWays) {
                    return newResult;
                }
            } else {
                currSizeOfAllCoastlineWays = newResult.size();
                coastLinesToMerge = newResult;
            }
        }

    }


    private AbstractMap.Entry<Integer, List<CoastlineWay>> findMergePartnerForCoastline(List<CoastlineWay> allCoastlinesToMerge, int idxOfCurrEl) {


        int coastlineNodeSize = allCoastlinesToMerge.size();
        for (int i = 0; i < coastlineNodeSize; i++) {

            if (i == idxOfCurrEl) {
                continue;
            }

            CoastlineWay mergeResult = allCoastlinesToMerge.get(idxOfCurrEl).mergeCoastlinesIfPossible(allCoastlinesToMerge.get(i));

            if (mergeResult != null) {
                // If a merge was performed

                // Remove the merged coastline, but keep the bigger new coast line
                List<CoastlineWay> retList = new ArrayList<>(allCoastlinesToMerge);
                retList.set(idxOfCurrEl, mergeResult);
                retList.remove(allCoastlinesToMerge.get(i));

                if (idxOfCurrEl > i) {
                    idxOfCurrEl--;
                }
                return new AbstractMap.SimpleEntry<>(idxOfCurrEl, retList);
            }

        }
        return new AbstractMap.SimpleEntry<>(idxOfCurrEl, allCoastlinesToMerge);

    }

    @Override
    public void close() {
        try {
            this.inputStream.close();
        } catch (IOException e) {
            logger.error("Closing the InputStream after PBF import failed.");
        }
    }


    /**
     * Imports osm coastlines from a given pbf file.
     *
     * @param pbfCoastlineFilePath The path of the pbf file as a string and relative to the resource directory.
     * @throws FileNotFoundException If under the passed path name no file can be found.
     */
    public List<CoastlineWay> importPBF(String pbfCoastlineFilePath) throws IOException {
        this.coastLines = new ArrayList<>();
        this.allNodes = new HashMap<>();

        // Get an input stream for the pbf file located in the resources directory
        Resource pbfResource = new ClassPathResource(pbfCoastlineFilePath);
        this.inputStream = pbfResource.getInputStream();

        // Import the pbf file contents
        OsmosisReader reader = new OsmosisReader(inputStream);
        reader.setSink(this);
        reader.run();

        return this.coastLineWays;
    }

    /**
     * Iterates over a given list of coastlines and assigns IDs from 0 to coastlines.size().
     *
     * @param coastlinesThatNeedNewIDs The coastlines that should get new ids.
     */
    private void assignNewIdsToCoastlines(List<CoastlineWay> coastlinesThatNeedNewIDs) {
        for (int currCoastlineIdx = 0; currCoastlineIdx < coastlinesThatNeedNewIDs.size(); currCoastlineIdx++) {
            coastlinesThatNeedNewIDs.get(currCoastlineIdx).setId(currCoastlineIdx);
        }
    }

    public static void main(String[] args) throws IOException {
        // Import coastlines
        CoastlineImporter importer = new CoastlineImporter();
        List<CoastlineWay> coastlines = importer.importPBF("antarctica-latest.osm.pbf");

        // Write a geo json file for test visualization reasons
        String json = GeoJsonConverter.coastlineWayToGeoJSON(coastlines).toString();
        BufferedWriter writer = new BufferedWriter(new FileWriter("antarctica_geoJson.json"));
        writer.write(json);
        writer.close();

        boolean test = IntersectionHelper.linesIntersect(1.0, 100.2, 1.32, 110.3,
                -2.0, 105.123, 10.0, 105.321);
        System.out.println("ttt: testbool " + test);

        //Coastlines.initCoastlines(coastlines);

        // land
        double latToCheck = 	-61.9983;
        double longToCheck = 	-58.3704;

        boolean land = false;
        for (CoastlineWay polygon : coastlines) {
            TimIntersectionCheck check = new TimIntersectionCheck(polygon);
            if (!check.isPointInWater(latToCheck, longToCheck)) {
                if (polygon.getWayNodes().get(0).getNodeId() == 275496715) {
                    continue;
                }
                land = true;
                break;
            }
        }

        System.out.println("Is on land: " + land);
        // water
        latToCheck = 		-62.3878;
        longToCheck = 		-58.4637;

        land = false;
        for (CoastlineWay polygon : coastlines) {
            TimIntersectionCheck check = new TimIntersectionCheck(polygon);
            if (!check.isPointInWater(latToCheck, longToCheck)) {
                if (polygon.getWayNodes().get(0).getNodeId() == 275496715) {
                    continue;
                }
                land = true;
                break;
            }
        }
        System.out.println("Is on land: " + land);


        // water
        latToCheck = 			-61.2094;
        longToCheck = 			-57.4146;

        land = false;
        for (CoastlineWay polygon : coastlines) {
            TimIntersectionCheck check = new TimIntersectionCheck(polygon);
            if (!check.isPointInWater(latToCheck, longToCheck)) {
                if (polygon.getWayNodes().get(0).getNodeId() == 275496715) {
                    continue;
                }
                land = true;
                break;
            }
        }

        System.out.println("Is on land: " + land);

        //Coastlines.testSetValues();
        //Coastlines.correctValues();

        /*Set<Integer> testSet = new LinkedHashSet<>();
        CoastlineGridLeaf leaf = new CoastlineGridLeaf(-82.2, 50.3, testSet);
        leaf = new CoastlineGridLeaf(1.32, 110.3, testSet);
        leaf = new CoastlineGridLeaf(-64.217508, -59.390335, testSet);
        leaf = new CoastlineGridLeaf(-76.6654, -45.327835, testSet);
        /*System.out.println("ttt: coastline: "+Coastlines.getStartLatitude(314346)+" "+
                Coastlines.getStartLongitude(314346)+" "+Coastlines.getEndLatitude(314346)+" "+
                Coastlines.getEndLongitude(314346)+" ");*/
        //System.out.println("ttt: coastline: " + Coastlines.getStartLatitude(314353) + " " +
        //        Coastlines.getStartLongitude(314353) + " " + Coastlines.getEndLatitude(314353) + " " +
        //        Coastlines.getEndLongitude(314353) + " ");

        //CoastlineChecker coastlineChecker = new CoastlineChecker();
        //System.out.println(Coastlines.getNumberOfWays());

        //test some points
        //false
        /*System.out.println("second point in water: " + coastlineChecker.pointIsInWater(-82.229, -58.34));
        System.out.println("third point in water: " + coastlineChecker.pointIsInWater(-70.591921, -64.172278));
        //true
        System.out.println("first point in water: " + coastlineChecker.pointIsInWater(-19.34, -41));
        System.out.println("fourth point in water: " + coastlineChecker.pointIsInWater(-76.6, -39.299));

        System.out.println("second point in water: " + coastlineChecker.pointIsInWater(82.229, -58.34));
        System.out.println("third point in water: " + coastlineChecker.pointIsInWater(70.591921, -64.172278));
        System.out.println("first point in water: " + coastlineChecker.pointIsInWater(-0.34, -41));
        System.out.println("fourth point in water: " + coastlineChecker.pointIsInWater(0.6, -39.299));*/
    }
}