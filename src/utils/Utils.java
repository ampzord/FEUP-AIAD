package utils;
import java.io.*;
import java.util.ArrayList;

public class Utils {

    public final static String PATH_BANDS = "./input/bands.txt";
    public static ArrayList<Object[]> bandsInformation;
    public final static String PATH_VENUES = "./input/venues.txt";
    public static ArrayList<Object[]> venuesInformation;
    public final static String PATH_SPECTATORS = "./input/spectators.txt";
    public static ArrayList<Object[]> spectatorsInformation;
    public final static int ITENERATION_COST_PER_DISTANCE = 10;
    public final static int MAX_SHOWS_PER_BAND = 1;

    public Utils() {
    }

    public static void readFileBands(String filePath) throws IOException {
        String line;
        int aux = 0;

        bandsInformation = new ArrayList<>();

        try {

            File f = new File(filePath);
            if((!f.exists()) || (f.isDirectory()))
                throw new FileNotFoundException();

                FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                //Ignore first line (template)
                if (aux == 0) {
                    aux++;
                    continue;
                }

                Object[] band = new Object[5];

                String[] tokens = line.split(";");

                if( (Integer.parseInt(tokens[1]) < 0) || (Integer.parseInt(tokens[1]) > 100)
                        || (Integer.parseInt(tokens[2]) < 0) || (Integer.parseInt(tokens[2]) > 5)
                        || (Integer.parseInt(tokens[3]) < 0)
                        ||(Integer.parseInt(tokens[4]) < 0))
                    throw new IOException();

                band[0] = tokens[0];
                band[1] = Integer.parseInt(tokens[1]);
                band[2] = Integer.parseInt(tokens[2]);
                band[3] = Integer.parseInt(tokens[3]);
                band[4] = Integer.parseInt(tokens[4]);

                bandsInformation.add(band);
            }

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("Unable to open file '" + filePath + "'");
        }
        catch(IOException ex) {
            ex.printStackTrace();
            System.out.println("Invalid Band information provided.");
        }
    }

    public static void readFileVenues(String filePath) throws IOException {
        String line;
        int aux = 0;

        try {

            File f = new File(filePath);
            if((!f.exists()) || (f.isDirectory()))
                throw new FileNotFoundException();

            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            venuesInformation = new ArrayList<>();

            while((line = bufferedReader.readLine()) != null) {

                //Ignore first line (template)
                if (aux == 0) {
                    aux++; continue;
                }

                Object[] venue = new Object[8];
                String[] tokens = line.split(";");

                if((Integer.parseInt(tokens[1]) < 0)
                        || (Integer.parseInt(tokens[2]) < 0)
                        || (Integer.parseInt(tokens[3]) < 0) || (Integer.parseInt(tokens[3]) > 100)
                        || (Integer.parseInt(tokens[4]) < 0) || (Integer.parseInt(tokens[4]) > 100)
                        || (Integer.parseInt(tokens[5]) < 0) || (Integer.parseInt(tokens[5]) > 5)
                        || (Integer.parseInt(tokens[6]) < 0) || (Integer.parseInt(tokens[6]) > 5)
                        || (Integer.parseInt(tokens[7]) < 0))
                    throw new IOException();

                venue[0] = tokens[0];
                venue[1] = Integer.parseInt(tokens[1]);
                venue[2] = Integer.parseInt(tokens[2]);
                venue[3] = Integer.parseInt(tokens[3]);
                venue[4] = Integer.parseInt(tokens[4]);
                venue[5] = Integer.parseInt(tokens[5]);
                venue[6] = Integer.parseInt(tokens[6]);
                venue[7] = Integer.parseInt(tokens[7]);
                venuesInformation.add(venue);
            }

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("Unable to open file '" + filePath + "'");
        }
        catch(IOException ex) {
            ex.printStackTrace();
            System.out.println("Invalid Venue information provided.");
        }
    }

    public static void readFileSpectators(String filePath) throws IOException {
        String line;
        int aux = 0;

        try {

            File f = new File(filePath);
            if((!f.exists()) || (f.isDirectory()))
                throw new FileNotFoundException();

            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            spectatorsInformation = new ArrayList<>();

            while((line = bufferedReader.readLine()) != null) {
                //Ignore first line (template)
                if (aux == 0) {
                    aux++; continue;
                }

                Object[] spectator = new Object[4];
                String[] tokens = line.split(";");

                if( (Integer.parseInt(tokens[1]) < 0)
                        || (Integer.parseInt(tokens[2]) < 0 ) || (Integer.parseInt(tokens[2]) > 100)
                        || (Integer.parseInt(tokens[3]) < 0) || (Integer.parseInt(tokens[3]) > 100)
                        || (Integer.parseInt(tokens[4]) < 0 ))
                    throw new IOException();

                spectator[0] = Integer.parseInt(tokens[0]);
                spectator[1] = Integer.parseInt(tokens[1]);
                spectator[2] = Integer.parseInt(tokens[2]);
                spectator[3] = Integer.parseInt(tokens[3]);

                spectatorsInformation.add(spectator);
            }

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("Unable to open file '" + filePath + "'");
        }
        catch(IOException ex) {
            ex.printStackTrace();
            System.out.println("Invalid Spectator information provided.");
        }
    }
}



