/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aksw.org.doodle.lodstats;

import aksw.org.doodle.dataset.Description;
import aksw.org.doodle.engine.DescriptionCollector;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ngonga
 */
public class EndpointReader {

    static Logger logger = Logger.getLogger("DOODLE");

    public static Map<String, Description> getDescriptions(String endpointFile, String dumpFile) {
        Map<String, Description> descriptions = new HashMap<String, Description>();
        if (new File(dumpFile).exists()) {
            logger.info("Reading descriptions from dump file");
            try {
                FileInputStream fileIn =
                        new FileInputStream(dumpFile);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                descriptions = (Map<String, Description>) in.readObject();
                in.close();

                fileIn.close();
            } catch (IOException i) {
                i.printStackTrace();
                logger.warn("Error deserializing " + dumpFile);
            } catch (ClassNotFoundException c) {
                logger.warn("Class error deserializing " + dumpFile);
            }
        } else {
            logger.info("Serializing into dump file");
            descriptions = getDescriptions(endpointFile);
            generateDump(descriptions, dumpFile);
        }
        return descriptions;
    }

    public static Map<String, Description> getDescriptions(String file) {
        Map<String, Description> descriptions = new HashMap<String, Description>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String s = reader.readLine();
            String split[];

            while (s != null) {
                split = s.split("\t");
                System.out.println("Reading from " + split[0]);
                Description d = DescriptionCollector.getDescription(split[0]);
                if (d != null) {
                    descriptions.put(split[0], d);
                }
                s = reader.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return descriptions;
    }

    public static void generateDump(Map<String, Description> descriptions, String dumpFile) {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("resources/sparqlEndpoints.ser");
            ObjectOutputStream out =
                    new ObjectOutputStream(fileOut);
            out.writeObject(descriptions);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
            logger.warn("Error writing dump");
        }
    }
}
