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
 * Uses the Osmosis pipeline to parse a PBF file. Imports and merges
 * coast lines.
 */
public class CoastlineImporter {

    private Logger logger = LoggerFactory.getLogger(CoastlineImporter.class);

    private InputStream inputStream;

    private List<CoastlineWay> coastLineWays;

    private Map<Long, Point> allNodes;

    public CoastlineImporter() {
        this.allNodes = new HashMap<>();
        this.coastLineWays = new ArrayList<>();
    }

    private void mergeCoastlines(List<CoastlineWay> coastlinesToMerge) {

        boolean[] alreadyMerged = new boolean[coastlinesToMerge.size()];
        Arrays.fill(alreadyMerged,false);

        boolean mergeStatusChanged = false;

        do {
            mergeStatusChanged = false;
            int coastlineSize = coastlinesToMerge.size();
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
        this.allNodes = new HashMap<>();

        // Get an input stream for the pbf file located in the resources directory
        Resource pbfResource = new ClassPathResource(pbfCoastlineFilePath);
        this.inputStream = pbfResource.getInputStream();

        new ParallelBinaryParser(this.inputStream, 1)
                .onHeader(this::processHeader)
                .onNode(this::processNode)
                .onWay(this::processWay)
                .onComplete(this::onCompletion)
                .parse();

        return this.coastLineWays;
    }

    private static boolean isCoastlineEntity(com.wolt.osm.parallelpbf.entity.Way way) {
        return "coastline".equals(way.getTags().get("natural")) && way.getNodes().size() > 0;
    }

    private void onCompletion() {
        // Empty the nodes list to save memory
        this.allNodes = new HashMap<>();

        mergeCoastlines(this.coastLineWays);
    }

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

    private void processNode(com.wolt.osm.parallelpbf.entity.Node node) {
        allNodes.put(node.getId(), new Point(node.getId(), (float) node.getLat(), (float) node.getLon()));
    }

    private void processHeader(Header header) { }




}