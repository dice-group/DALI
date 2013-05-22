/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aksw.org.doodle.webservice;

import aksw.org.doodle.engine.DescriptionCollector;
import aksw.org.doodle.engine.Engine;
import aksw.org.doodle.similarity.QGrams;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author ngonga
 */
@Path("/getdatasets")
public class Webservice {

    static Logger logger = Logger.getLogger("DOODLE");

    @GET
    @Produces("application/json")
    public Response getJson(@QueryParam("endpoint") String endpoint) {
        Engine engine = Engine.getInstance();
        logger.info("Running on " + endpoint);
        if (DescriptionCollector.isAlive(endpoint)) {
            Map<String, Double> result = engine.getScores(endpoint, new QGrams());
            if (!result.isEmpty()) {
                String r = "[ ";
                for (String e : result.keySet()) {
                    r = r + "{\"url\" : \"" + e + "\", ";
                    r = r + "\"relevance\": " + result.get(e) + ", ";
                    if (engine.endpoints.containsKey(e)) {
                        r = r + "\"type\" : \"sparql\" },";
                    } else {
                        r = r + "\"type\" : \"file\" }, ";
                    }
                }
                r = r.substring(0, r.length() - 2);
                r = r + "]";
                ResponseBuilder builder = Response.ok(r);
                builder.header("Access-Control-Allow-Origin", "*");
                return builder.build();
            } // no matching dataset found
            else {
                return Response.status(101).build();
            }
        } //endpoint is dead
        else {
            return Response.status(100).build();
        }
    }

    @GET
    @Produces("text/html")
    public Response getHTML(@QueryParam("endpoint") String endpoint) {
        return getJson(endpoint);
    }
}
