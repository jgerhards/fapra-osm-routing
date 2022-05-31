package de.fmi.searouter.osmimport;

import com.wolt.osm.parallelpbf.ParallelBinaryParser;
import com.wolt.osm.parallelpbf.entity.Header;
import de.fmi.searouter.domain.CoastlineWay;
import de.fmi.searouter.domain.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.*;

/**
 * Provides means to import PBF file. Imports and merges
 * coast lines.
 */
public class CoastlineImporter {

    private InputStream inputStream;

    // The imported (and later on merged) coastline (polygons)
    private List<CoastlineWay> coastLineWays;

    // Nodes need to be mapped to coastlines as in the pbf file coastlines do have only their ids but not coordinates
    private Map<Long, Point> allNodes;

    public CoastlineImporter() {
        this.allNodes = new HashMap<>();
        this.coastLineWays = new ArrayList<>();
    }

    /**
     * Merges {@link CoastlineWay} objects by updating the objects with their previous and last
     * neighbor ways which form together a polygon.
     *
     * @param coastlinesToMerge A list of CoastlineWays which should be merged
     */
    private void mergeCoastlines(List<CoastlineWay> coastlinesToMerge) {

        // Track whether a merge was already performed for a CoastlineWay obj with this boolean array, true=merged
        boolean[] alreadyMerged = new boolean[coastlinesToMerge.size()];
        Arrays.fill(alreadyMerged,false);

        boolean mergeStatusChanged = false;

        do {
            mergeStatusChanged = false;
            int coastlineSize = coastlinesToMerge.size();

            // Merge coastlines
            for (int i = 0; i < coastlineSize; i++) {
                if (alreadyMerged[i]) {
                    continue;
                }

                for (int j = 0; j < coastlineSize; j++) {

                    if (alreadyMerged[i] || alreadyMerged[j] || i == j) {
                        continue;
                    }

                    CoastlineWay coastlineOne = coastlinesToMerge.get(i);
                    CoastlineWay coastlineTwo = coastlinesToMerge.get(j);

                    // Check whether merge can be performed and if yes perform it
                    int mergeResult = coastlineOne.mergeCoastlinesIfPossible(coastlineTwo);

                    if (mergeResult == 1) {
                        alreadyMerged[j] = true;
                        mergeStatusChanged = true;
                    }  else if (mergeResult == 2) {
                        alreadyMerged[i] = true;
                        mergeStatusChanged = true;
                    }

                }
            }
        } while(mergeStatusChanged);

        // Remove coastlines which are no polygon starts
        for (int i = coastlinesToMerge.size()-1; i >= 0; i--) {
            if (alreadyMerged[i]) {
                coastlinesToMerge.remove(i);
            }
        }

    }

    /**
     * Imports osm coastlines from a given pbf file.
     *
     * @param pbfCoastlineFilePath The path of the pbf file as a string and relative to the resource directory.
     * @throws FileNotFoundException If under the passed path name no file can be found.
     */
    public List<CoastlineWay> importPBF(String pbfCoastlineFilePath) throws IOException {
        this.allNodes = new HashMap<>();

        // Get an input stream for the pbf file located in the resources directory
        Resource pbfResource = new ClassPathResource(pbfCoastlineFilePath);
        this.inputStream = pbfResource.getInputStream();

        // Start import
        new ParallelBinaryParser(this.inputStream, 1)
                .onHeader(this::processHeader)
                .onNode(this::processNode)
                .onWay(this::processWay)
                .onComplete(this::onCompletion)
                .parse();

        return this.coastLineWays;
    }

    /**
     * @param way the Way to check whether it is a coast line
     * @return True if a way is a coastline as defined by OSm
     */
    private static boolean isCoastlineEntity(com.wolt.osm.parallelpbf.entity.Way way) {
        return "coastline".equals(way.getTags().get("natural")) && way.getNodes().size() > 0;
    }

    private void onCompletion() {
        // Empty the nodes list to save memory
        this.allNodes = new HashMap<>();

        mergeCoastlines(this.coastLineWays);
    }

    /**
     * Handles an osm way when finding one during the pbf import.
     *
     * @param way The osm way
     */
    private void processWay(com.wolt.osm.parallelpbf.entity.Way way) {
        if (isCoastlineEntity(way)) {
            CoastlineWay cWay = new CoastlineWay(way);
            for (int i = 0; i < way.getNodes().size(); i++) {
                Point node = this.allNodes.get(way.getNodes().get(i));
                if (node != null) {
                    cWay.getPoints().add(node);
                }
            }
            cWay.updateAfterGet();
            this.coastLineWays.add(cWay);
        }

    }

    /**
     * Processes nodes during the PBF file import.
     * @param node The node to process
     */
    private void processNode(com.wolt.osm.parallelpbf.entity.Node node) {
        allNodes.put(node.getId(), new Point(node.getId(), (float) node.getLat(), (float) node.getLon()));
    }

    private void processHeader(Header header) { }


}