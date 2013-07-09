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
                // System.out.println("Valid endpoint:\t" + endpoint + "\t:\t" + getSizeOfEndpoint(endpoint));

                // ask for all owl:sameAs and save them
                getSameAs("http://dbpedia.org/sparql/", end++);
            } catch (Exception e) {
                log.error("Invalid endpoint:" + endpoint);
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

    /**
     * saves every triple in the form of ?s ?o leaving out owl:sameAs as predicate due to storage reasons
     * 
     * @param sameAs
     * @param endpoint
     * @param end
     * @throws IOException
     */
    private static void saveContinously(ArrayList<ArrayList<String>> sameAs, String endpoint, int end) throws IOException {
        System.out.println("\t" + sameAs.size());
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("resources/endpoint/" + end), true));
        bw.write(endpoint);
        bw.newLine();
        for (ArrayList<String> row : sameAs) {
            for (String entry : row) {
                bw.write(entry);
                bw.write("\t");
            }
            bw.newLine();
        }
        bw.close();
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
        // TODO build in offset
        int oldCount = 0;
        int newCount = 0;
        do {
            oldCount = newCount;
            // TODO cursor
            String query = "PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
                    "SELECT ?s ?o " +
                    "WHERE { " +
                    "       SELECT DISTINCT ?s ?o " +
                    "       WHERE { ?s owl:sameAs ?o. } " +
                    "       ORDER BY ASC (?s) }  " +
                    "OFFSET " + oldCount + " " +
                    "LIMIT " + 40000;
            System.out.println(query);
            saveContinously(ask(query, con), endpoint, end);
            newCount = 40000 + oldCount;
            System.gc();
        } while (oldCount < newCount);
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