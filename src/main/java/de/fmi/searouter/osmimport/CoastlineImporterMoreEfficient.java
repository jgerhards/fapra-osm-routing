package de.fmi.searouter.osmimport;

import com.wolt.osm.parallelpbf.ParallelBinaryParser;
import com.wolt.osm.parallelpbf.entity.Header;
import de.fmi.searouter.domain.CoastlineWay;
import de.fmi.searouter.domain.Point;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
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
public class CoastlineImporterMoreEfficient  {

    private Logger logger = LoggerFactory.getLogger(CoastlineImporterMoreEfficient.class);

    private InputStream inputStream;

    private List<Way> coastLines;

    private List<CoastlineWay> coastLineWays;

    private Map<Long, Point> allNodes;

    public CoastlineImporterMoreEfficient() {
        this.coastLines = new ArrayList<>();
        this.allNodes = new HashMap<>();
        this.coastLineWays = new ArrayList<>();
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

        new ParallelBinaryParser(this.inputStream, 1)
                .onHeader(this::processHeader)
                .onNode(this::processNode)
                .onWay(this::processWay)
                .onComplete(this::onCompletion)
                .parse();

        return this.coastLineWays;
    }

    private static boolean isCoastlineEntity(com.wolt.osm.parallelpbf.entity.Way way) {
        return "coastline".equals(way.getTags().get("natural"));
    }

    private void onCompletion() {
        // Empty the nodes list to save memory
        this.allNodes = new HashMap<>();

        this.coastLineWays = mergeTouchingCoastlines(this.coastLineWays);
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
            this.coastLineWays.add(cWay);
        }

    }

    private void processNode(com.wolt.osm.parallelpbf.entity.Node node) {
        allNodes.put(node.getId(), new Point(node.getId(), (float) node.getLat(), (float) node.getLon()));
    }

    private void processHeader(Header header) { }




}