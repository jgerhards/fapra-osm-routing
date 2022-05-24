package de.fmi.searouter.osmexport;

import de.fmi.searouter.domain.CoastlineWay;
import de.fmi.searouter.grid.GridNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods to parse osm entities to GeoJSON.
 */
public class GeoJsonConverter {

    /**
     * Parses a list of {@link CoastlineWay}s to a GeoJSON object of the
     * type "FeatureCollection".
     *
     * @param waysToConvert The {@link CoastlineWay}s
     * @return The GeoJSON as FeatureCollection type
     */
    public static JSONObject coastlineWayToGeoJSON(List<CoastlineWay> waysToConvert) {
        // Outer FeatureCollection object (top-level obj)
        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");

        // Features, each representing one LineString (one Way of the coast)
        JSONArray features = new JSONArray();
        for (Way currWay : waysToConvert) {
            features.put(GeoJsonConverter.osmWayToGeoJSON(currWay));
        }

        featureCollection.put("features", features);

        return featureCollection;
    }

    public static JSONObject osmNodesToGeoJSON(List<GridNode> nodes) {
        // Outer FeatureCollection object (top-level obj)
        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");

        // Features, each representing one LineString (one Way of the coast)
        JSONArray features = new JSONArray();
        for (GridNode node : nodes) {
            features.put(GeoJsonConverter.gridNodeToGeoJSONFeature(node));
        }

        featureCollection.put("features", features);

        return featureCollection;
    }

    public static JSONObject gridNodeToGeoJSONFeature(GridNode node) {
        JSONObject topLevelobj = new JSONObject();

        topLevelobj.put("type", "Feature");
        topLevelobj.put("properties", new JSONObject());

        JSONObject geometry = new JSONObject();
        geometry.put("type", "Point");
        JSONArray arr = new JSONArray();
        arr.put(node.getLatitude());
        arr.put(node.getLongitude());

        geometry.put("coordinates", arr);

        topLevelobj.put("geometry", geometry);

        return topLevelobj;
    }

    /**
     * Parses a {@link Way} object to a GeoJSON representation using the type "Feature".
     *
     * @param wayToConvert The {@link Way} which should be parsed as GeoJSON of the type "Feature"
     * @return
     */
    public static JSONObject osmWayToGeoJSON(Way wayToConvert) {
        List<WayNode> wayNodes = wayToConvert.getWayNodes();
        // Build an json array of coordinate pairs (longitude-latitude pairs)
        JSONArray longLatArray = new JSONArray();
        for (WayNode currNode : wayNodes) {
            JSONArray longLatPair = new JSONArray()
                    .put(currNode.getLongitude())
                    .put(currNode.getLatitude());
            longLatArray.put(longLatPair);
        }

        // Geometry json obj inside  a feature obj
        JSONObject geometry = new JSONObject()
                .put("type", "LineString")
                .put("coordinates", longLatArray);

        // Outer feature obj
        JSONObject feature = new JSONObject()
                .put("type", "Feature")
                .put("properties", new JSONObject())
                .put("geometry", geometry);

        return feature;
    }


}
