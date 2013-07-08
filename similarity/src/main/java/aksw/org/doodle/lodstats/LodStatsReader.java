/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aksw.org.doodle.lodstats;

import aksw.org.doodle.dataset.Description;
import aksw.org.doodle.dataset.VectorDescription;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ngonga
 */
public class LodStatsReader {

    static Logger logger = Logger.getLogger("DOODLE");

    public static Map<String, Description> readStats(String folder) {
        Map<String, Description> result = new HashMap<String, Description>();
        File f = new File(folder);
        List<String> fileNames = new ArrayList<String>();
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                fileNames.add(files[i] + "");
            }
        } else {
            fileNames.add(folder);
        }

        for (String fileName : fileNames) {
            Model m = getModel(fileName);
            String name = getName(m);
            if (name == null) {
                name = fileName;
            }
            result.put(name, readSingleStatFile(m, name));
        }
        return result;
    }

    public static Model getModel(String file) {
        InputStream in;
        Model model = ModelFactory.createDefaultModel();
        VectorDescription d = new VectorDescription(file);
        RDFReader r = model.getReader("TURTLE");
        try {
            in = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(in, "UTF8");
            r.read(model, reader, null);
            logger.info("RDF model read from " + file + " is of size " + model.size());
        } catch (Exception e) {
            logger.warn("Error reading model " + file);
        }
        return model;
    }

    public static String getName(Model model) {
        try {
            //get class stats
            String query = "SELECT ?x WHERE {?x <http://rdfs.org/ns/void#classPartition> ?y }";
            Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
            QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, model);
            ResultSet results = qexec.execSelect();
            QuerySolution sln;
            String name;
            double count;
            while (results.hasNext()) {
                sln = results.nextSolution();
                return sln.getResource("?x").toString();
            }
        } catch (Exception e) {
            logger.warn("Error reading label for model " + model.hashCode());
        }
        return null;
    }

    public static VectorDescription readSingleStatFile(Model model, String label) {
        VectorDescription d = new VectorDescription(label);
        try {
            //get class stats
            String query = "SELECT ?a ?b WHERE {?x <http://rdfs.org/ns/void#classPartition> ?y. "
                    + " ?y <http://rdfs.org/ns/void#class> ?a ."
                    + " ?y <http://rdfs.org/ns/void#entities> ?b ."
                    + "}";
            Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
            QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, model);
            ResultSet results = qexec.execSelect();
            QuerySolution sln;
            String name;
            double count;
            while (results.hasNext()) {
                sln = results.nextSolution();
                name = sln.getResource("?a").getLocalName().toLowerCase();
                count = Double.parseDouble(sln.getLiteral("?b").getLexicalForm());
                if (name.length() > 1) {
                    d.features.put(name, count);
                }
            }
        } catch (Exception e) {
            logger.warn("Error reading model for " + label);
        }
        return d;
    }

    public static void main(String args[]) {
        System.out.println(readStats("C:/Users/ngonga/Downloads/LODStats_all_void"));
    }
}
