/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aksw.org.doodle.lodstats;

import aksw.org.doodle.dataset.Description;
import aksw.org.doodle.engine.DescriptionCollector;
import aksw.org.doodle.engine.Engine;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 *
 * @author ngonga
 */
public class EndpointReader {

    static Logger logger = Logger.getLogger(EndpointReader.class);
    private static Map<Path,List<String>> endpointUris = new HashMap<Path, List<String>>(4);


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

        if(logger.isDebugEnabled()) {
            StringBuffer msg = new StringBuffer("descriptions read from dump file:\n");
            for(String name : descriptions.keySet())
                msg.append(name).append('\n');
            logger.debug(msg);
        }

        return descriptions;
    }

    public static Map<String, Description> getDescriptions(String file) {
        Map<String, Description> descriptions = new HashMap<String, Description>();
        for(String uri : getEndpointUris(file)) {
            logger.info("Reading from " + uri);
            Description d = DescriptionCollector.getDescription(uri);
            if (d != null) {
                descriptions.put(uri, d);
            }
        }
        /*try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String s = reader.readLine();
            String split[];

            while (s != null) {
                split = s.split("\t");

                s = reader.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return descriptions;
    }

    public static void generateDump(Map<String, Description> descriptions, String dumpFile) {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(dumpFile); //correction by M.Ack - before "resources/sparqlEndpoints.ser"
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

    public static List<String> getEndpointUris(String path) {
        Path filePath = new File(path).toPath().toAbsolutePath().normalize();
        if(!endpointUris.containsKey(filePath)) {
            logger.debug("Reading endpoint uris from" + filePath.toString());
            LinkedList<String> uriList = new LinkedList<String>();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(filePath.toString()));
                String line = reader.readLine();
                while(line != null) {
                    String uri = line.split("\t")[0];
                    uriList.addLast(uri);
                    line = reader.readLine();
                }
            } catch (IOException ioe) {
                logger.error("Error reading endpoint uris");
                ioe.printStackTrace();

                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException innerIoe) {
                        logger.error("Error closing reader in error cleanup");
                        innerIoe.printStackTrace();
                    }
                }
            }
            endpointUris.put(filePath, new ArrayList<String>(uriList));
        }
        return endpointUris.get(filePath);
    }

    public static void main(String[] args) {
        getEndpointUris(Engine.COMPLETE_ENDPOINTS_FILE);
    }
}
