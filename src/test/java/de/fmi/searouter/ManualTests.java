package de.fmi.searouter;

import de.fmi.searouter.coastlinecheck.Coastlines;
import de.fmi.searouter.domain.CoastlineWay;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ManualTests {

    public static void main(String[] args) {
        CommonEntityData data = new CommonEntityData(0, 0, new Date(), new OsmUser(0, ""), 0);
        List<WayNode> wayNodes = new ArrayList<>();
        WayNode startWayNode = new WayNode(0, 0, 0);
        wayNodes.add(startWayNode);
        wayNodes.add(new WayNode(1, 1, 1));
        wayNodes.add(new WayNode(2, 2, 2));
        wayNodes.add(startWayNode);
        Way way = new Way(data, wayNodes);

        CoastlineWay cWay = new CoastlineWay(way);

        CommonEntityData bdata = new CommonEntityData(0, 0, new Date(), new OsmUser(0, ""), 0);
        List<WayNode> bwayNodes = new ArrayList<>();
        WayNode bstartWayNode = new WayNode(3, 3, 3);
        bwayNodes.add(bstartWayNode);
        bwayNodes.add(new WayNode(4, 4, 4));
        bwayNodes.add(bstartWayNode);
        Way bway = new Way(bdata, bwayNodes);

        CoastlineWay bWay = new CoastlineWay(bway);

        Coastlines.initCoastlines(Arrays.asList(cWay, bWay));
    }
}
