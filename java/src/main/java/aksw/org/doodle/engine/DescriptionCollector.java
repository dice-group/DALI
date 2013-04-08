/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aksw.org.doodle.engine;

import aksw.org.doodle.dataset.Description;
import aksw.org.doodle.dataset.VectorDescription;
import aksw.org.doodle.similarity.QGrams;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author ngonga
 */
public class DescriptionCollector {

    static Logger logger = Logger.getLogger("DOODLE");

    public static boolean isAlive(String sparqlEndpoint) {
        try {
            String query = "SELECT ?x WHERE {?x ?y ?z.} LIMIT 1";
            Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, sparqlQuery);
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Error reading description");
            return false;
        }
        return false;
    }

    public static Description getDescription(String sparqlEndpoint) {
        VectorDescription d = new VectorDescription(sparqlEndpoint);
        if(!isAlive(sparqlEndpoint))
        {
            logger.info(sparqlEndpoint +" is dead!");
            return d;
        }
        try {
            String query = "SELECT  ?class (COUNT(?s) AS ?count ) { ?s a ?class } GROUP BY ?class ORDER BY ?count";
            Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, sparqlQuery);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                    QuerySolution sln = results.nextSolution();
                    d.features.put(sln.getResource("?class").getLocalName(), Double.parseDouble(sln.getLiteral("?count").getLexicalForm()));
                }            
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Error reading description");
            return null;
        }
        logger.info("Description for "+sparqlEndpoint+"\n"+d.features);
        return d;
    }

    public static void main(String args[]) {
        Engine e = Engine.getInstance();
        System.out.println(e.getScores("http://vocabulary.semantic-web.at/PoolParty/sparql/AustrianSkiTeam", new QGrams()));        
    }
}
