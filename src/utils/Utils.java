package utils;

import agents.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public final static String PATH_BANDS = "./input/bands.txt";
    public static List<Band> bandsList;
    public final static String PATH_VENUES = "./input/venues.txt";
    public static List<Venue> venuesList;
    public final static String PATH_SPECTATORS = "./input/spectators.txt";
    public static List<Spectator> spectatorsList;

    public Utils() {
    }

    public static void readFileBands(String filePath) throws IOException {
        String line = null;
        int aux = 0;

        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            bandsList = new ArrayList<Band>();

            while((line = bufferedReader.readLine()) != null) {
                //Ignore first line (template)
                if (aux == 0) {
                    aux++; continue;
                }

                String[] tokens = line.split(";");
                bandsList.add(new Band(Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]),Integer.parseInt(tokens[3])));
            }

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + filePath + "'");
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void readFileVenues(String filePath) throws IOException {
        String line = null;
        int aux = 0;

        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            venuesList = new ArrayList<Venue>();

            while((line = bufferedReader.readLine()) != null) {
                //Ignore first line (template)
                if (aux == 0) {
                    aux++; continue;
                }

                String[] tokens = line.split(";");
                venuesList.add(new Venue(Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]),Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]),Integer.parseInt(tokens[5]),Integer.parseInt(tokens[6])));
            }

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + filePath + "'");
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void readFileSpectators(String filePath) throws IOException {
        String line = null;
        int aux = 0;

        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            spectatorsList = new ArrayList<Spectator>();

            while((line = bufferedReader.readLine()) != null) {
                //Ignore first line (template)
                if (aux == 0) {
                    aux++; continue;
                }

                String[] tokens = line.split(";");
                spectatorsList.add(new Spectator(Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1]),Integer.parseInt(tokens[2]),Integer.parseInt(tokens[3])));
            }

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + filePath + "'");
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}



