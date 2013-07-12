package aksw.org.doodle.silverStandard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * find a knowledge base, number of owl:sameAs links between the different KB
 * 
 * @author ricardousbeck
 * 
 */
public class SilverStandard {
    private static Logger log = LoggerFactory.getLogger(SilverStandard.class);

    public static void main(String args[]) throws RepositoryException, IOException {
        int end = 0;
        for (String endpoint : getEndpoints()) {
            try {
                log.info("Endpoint:\t" + endpoint + "\t:\t" + getSizeOfEndpoint(endpoint));
                // ask for all owl:sameAs and save them
                getSameAs(endpoint, end++);
                log.info("\tFinished");
            } catch (Exception e) {
                log.error("Invalid endpoint:" + endpoint);
                log.error(e.getLocalizedMessage());
            }
        }
    }

    private static int getSizeOfEndpoint(String endpoint) throws RepositoryException, IOException {
        SPARQLRepository rep = new SPARQLRepository(endpoint);
        rep.initialize();
        RepositoryConnection con = rep.getConnection();
        String query = "PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
                "SELECT count(*) " +
                "WHERE { " +
                "       ?s owl:sameAs ?o} ";
        ArrayList<ArrayList<String>> result = ask(query, con);
        System.gc();
        con.close();
        return Integer.valueOf(result.get(0).get(0));
    }

    private static BufferedWriter startSaving(String endpoint, int end) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("resources/endpoint/" + end), true));
        bw.write("@prefix owl:<http://www.w3.org/2002/07/owl#>.");
        bw.newLine();
        bw.write("@prefix void: <http://rdfs.org/ns/void#> .");
        bw.newLine();
        bw.write("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
        bw.newLine();
        bw.write("@prefix foaf: <http://xmlns.com/foaf/0.1/> .");
        bw.newLine();
        bw.write(":this rdf:type void:Dataset ;");
        bw.newLine();
        bw.write("      foaf:homepage <" + endpoint + "> .");
        bw.newLine();
        return bw;
    }

    /**
     * saves every triple
     * 
     */
    private static void saveContinously(BufferedWriter bw, ArrayList<ArrayList<String>> sameAs) throws IOException {
        for (ArrayList<String> row : sameAs) {
            bw.write("<" + row.get(0) + "> owl:sameAs <" + row.get(1) + ">.");
            bw.newLine();
        }
        bw.flush();
        System.gc();
    }

    private static ArrayList<String> getEndpoints() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("resources/numberOfLinksPerEndpoint"));
            while (br.ready()) {
                list.add(br.readLine().split("\t")[0]);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static void getSameAs(String endpoint, int end) throws RepositoryException, IOException {
        SPARQLRepository rep = new SPARQLRepository(endpoint);
        rep.initialize();
        RepositoryConnection con = rep.getConnection();
        int oldCount = 0;
        int newCount = 0;
        ArrayList<ArrayList<String>> ask = null;
        BufferedWriter bw = startSaving(endpoint, end);
        do {
            oldCount = newCount;
            String query = "PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
                    "SELECT ?s ?o " +
                    "WHERE {{        " +
                    "SELECT DISTINCT ?s ?o        " +
                    "WHERE { ?s owl:sameAs ?o. }        " +
                    "ORDER BY ASC (?s) } }  LIMIT 40000 OFFSET " + oldCount;
            ask = ask(query, con);
            saveContinously(bw, ask);
            newCount = 40000 + oldCount;
            System.gc();
        } while (ask.size() > 0);
        bw.close();
        con.close();
    }

    public static ArrayList<ArrayList<String>> ask(String query, RepositoryConnection con) {
        QueryLanguage queryLanguage = QueryLanguage.SPARQL;
        ArrayList<ArrayList<String>> result = null;
        try {
            result = new ArrayList<ArrayList<String>>();
            TupleQuery tupleQuery = con.prepareTupleQuery(queryLanguage, query);
            TupleQueryResultBuilder tQRW = new TupleQueryResultBuilder();
            tupleQuery.evaluate(tQRW);
            TupleQueryResult tQR = tQRW.getQueryResult();
            while (tQR.hasNext()) {
                ArrayList<String> tmp = new ArrayList<String>();
                BindingSet st = tQR.next();
                Iterator<Binding> stIterator = st.iterator();
                while (stIterator.hasNext()) {
                    // watch out! the binding has to ensure the order
                    Binding b = stIterator.next();
                    tmp.add(b.getValue().stringValue());
                }
                result.add(tmp);
            }
        } catch (QueryEvaluationException e) {
            log.error(query);
            log.error(e.getLocalizedMessage());
        } catch (TupleQueryResultHandlerException e) {
            log.error(e.getLocalizedMessage());
        } catch (RepositoryException e) {
            log.error(e.getLocalizedMessage());
        } catch (MalformedQueryException e) {
            log.error(e.getLocalizedMessage());
        }
        return result;
    }
}