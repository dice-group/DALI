package aksw.org.doodle.silverStandard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.turtle.TurtleParser;

/**
 * "SourceDataSet \t TargetDataSet \t NumberOfLinks"
 * 
 * @author ricardousbeck
 * 
 */
public class FormatTransformer {
    private static HashMap<String, HashMap<String, Integer>> map = new HashMap<String, HashMap<String, Integer>>();

    public static void main(String args[]) throws Exception {
        // for all files
        for (File f : (new File("resources/endpoint/")).listFiles()) {
            try {
                // read each triple
                RDFParser parser = new TurtleParser();
                OnlineStatementHandler osh = new OnlineStatementHandler();
                parser.setRDFHandler(osh);
                parser.setStopAtFirstError(false);
                parser.parse(new FileReader(f), "");
                System.gc();
                System.out.println(f);
            } catch (Exception e) {
                System.out.println("Didn't work: " + f);
            }
        }
        // write hashmap to file
        BufferedWriter bw = new BufferedWriter(new FileWriter("resources/endpoint2endpointSurvey.tsv"));
        for (String source : map.keySet()) {
            for (String target : map.get(source).keySet()) {
                bw.write(source + "\t" + target + "\t" + map.get(source).get(target));
                bw.newLine();
            }
        }
        bw.close();

    }

    private static class OnlineStatementHandler extends RDFHandlerBase {
        @Override
        public void handleStatement(Statement st) {
            try {
                String subject = st.getSubject().stringValue();
                String object = st.getObject().stringValue();
                // System.out.println("\t" + subject + "\t" + object);
                // ask for domain
                try {
                    String subjectdomain = getDomainName(subject);
                    String objectDomain = getDomainName(object);

                    if (!subjectdomain.equals("aksw.org")) {
                        // System.out.println("\t" + subjectdomain + "\t" + objectDomain);
                        // put count into hashmap
                        if (map.containsKey(subjectdomain)) {
                            if (map.get(subjectdomain).containsKey(objectDomain)) {
                                int val = map.get(subjectdomain).get(objectDomain) + 1;
                                map.get(subjectdomain).put(objectDomain, val);
                            } else {
                                map.get(subjectdomain).put(objectDomain, 1);
                            }
                        } else {
                            map.put(subjectdomain, new HashMap<String, Integer>());
                            map.get(subjectdomain).put(objectDomain, 1);
                        }
                    }
                } catch (URISyntaxException e) {

                }
            } catch (Exception e) {

            }
        }
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}