package de.fmi.searouter;

import org.junit.jupiter.api.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
class SearouterApplicationTests {

	@Test
	void contextLoads() {
		CommonEntityData data = new CommonEntityData(0, 0, new Date(), new OsmUser(0, ""), 0);
		List<WayNode> wayNodes = new ArrayList<>();
		WayNode startWayNode = new WayNode(0, -43.011474609375, -75.46134742913321);
		wayNodes.add(startWayNode);
		wayNodes.add(new WayNode(1, -42.802734375, -75.48890116641715));
		wayNodes.add(new WayNode(2, -42.528076171875, -75.47237506180137));
		wayNodes.add(new WayNode(3, -42.38525390625, -75.4226861790414));
		wayNodes.add(new WayNode(4, -42.330322265625, -75.342281944273));
		wayNodes.add(new WayNode(5, -42.51708984375, -75.29773546875683));
		wayNodes.add(new WayNode(6, -43.17626953125, -75.32837536998849));
		wayNodes.add(new WayNode(7, -43.92333984375, -75.44202936067158));
		wayNodes.add(new WayNode(8, -44.703369140625, -75.63135885023286));
		wayNodes.add(new WayNode(9, -44.505615234375, -75.80750672433345));
		wayNodes.add(new WayNode(10, -44.329833984375, -75.74542056787068));
		wayNodes.add(new WayNode(11, -44.3408203125, -75.66403878383717));
		wayNodes.add(new WayNode(12, -44.197998046875, -75.59587329063447));
		wayNodes.add(new WayNode(13, -43.824462890625, -75.63953566438667));
		wayNodes.add(new WayNode(14, -43.7255859375, -75.57399340007979));
		wayNodes.add(new WayNode(15, -43.69262695312499,  -75.52739056918155));
		wayNodes.add(new WayNode(16, -43.560791015625,  -75.54933963834821));
		wayNodes.add(startWayNode);
		Way way = new Way(data, wayNodes);
	}

}
