/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aksw.org.doodle.engine;

import aksw.org.doodle.dataset.Description;
import aksw.org.doodle.dataset.VectorDescription;
import aksw.org.doodle.lodstats.EndpointReader;
import aksw.org.doodle.lodstats.LodStatsReader;
import aksw.org.doodle.similarity.Similarity;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ngonga
 */
public class Engine {

    static Logger logger = Logger.getLogger("DOODLE");
    public Map<String, Description> datasets;
    public Map<String, Description> endpoints;     
    
    public static Engine instance = null;

    public static Engine getInstance() {
        if (instance == null) {
            System.out.println("Creating new instance");
            instance = new Engine();
        }
        return instance;
    }

    public Engine(String referenceData) {
        datasets = LodStatsReader.readStats(referenceData);
    }

    public Engine() {
        logger.info("Reading LOD Stats data");
        datasets = LodStatsReader.readStats("resources/LODStats_all_void");
        logger.info("Reading SPARQL endpoints descriptions from endpoints that are alive");
        endpoints = EndpointReader.getDescriptions("resources/endpoints.txt", "resources/sparqlEndpoints.ser");
        logger.log(Level.INFO, "Found descriptions for {0} endpoints.", endpoints.size());
        for(String key: endpoints.keySet())
            datasets.put(key, endpoints.get(key));        
    }

    public static Map<String, Double> getScores(Description d, Similarity sim) {
        Engine inst = Engine.getInstance();
        Map<String, Double> result = new HashMap<String, Double>();
        for (String key : inst.datasets.keySet()) {
            double s = sim.getSimilarity(d, inst.datasets.get(key));
            if (s > 0) {
                result.put(key, s);
            }
        }
        return result;
    }

    public static Map<String, Double> getScores(String sparqlEndpoint, Similarity sim) {
        VectorDescription d = (VectorDescription) DescriptionCollector.getDescription(sparqlEndpoint);
        logger.log(Level.INFO, "Found {0} classes for {1}", new Object[]{d.features.size(), sparqlEndpoint});        
        if (d == null) {
            return new HashMap<String, Double>();
        }
        if (d.features.isEmpty()) {
            return new HashMap<String, Double>();
        } else {
            return getScores(d, sim);
        }
    }
}
