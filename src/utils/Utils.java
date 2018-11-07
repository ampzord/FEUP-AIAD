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

                if (!validInputOfBands(tokens))
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

                if (!validInputOfVenues(tokens))
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

                if (!validInputOfSpectators(tokens))
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

    private static boolean validInputOfBands(String[] tokens) {
        if (isValidGenre(Integer.parseInt(tokens[1]))
                && isValidPrestige(Integer.parseInt(tokens[2]))
                && isValidMinPrice(Integer.parseInt(tokens[3]))
                && isValidAttendance(Integer.parseInt(tokens[4])))
            return true;
        else {
            System.out.println("Error parsing input - bands.txt");
            return false;
        }
    }

    private static boolean validInputOfVenues(String[] tokens) {
        if (isValidAttendance(Integer.parseInt(tokens[1]))
                && isValidBudget(Integer.parseInt(tokens[2]))
                && isValidGenre(Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]))
                && isValidPrestige(Integer.parseInt(tokens[5]), Integer.parseInt(tokens[6]))
                && isValidLocation(Integer.parseInt(tokens[7])))
            return true;
        else {
            System.out.println("Error parsing input - venues.txt");
            return false;
        }
    }

    private static boolean validInputOfSpectators(String[] tokens) {
        if (isValidBudget(Integer.parseInt(tokens[0]))
                && isValidGenre(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]))
                && isValidLocation(Integer.parseInt(tokens[3])))
            return true;
        else {
            System.out.println("Error parsing input - spectators.txt");
            return false;
        }
    }


    private static boolean isValidLocation(int location) {
        if (location >=1 && location <= 5)
            return true;
        else {
            System.out.println("Error parsing input - Location of value: " + location);
            return false;
        }
    }

    private static boolean isValidGenre(int genre) {
        if (genre >= 1 && genre <= 100)
            return true;
        else {
            System.out.println("Error parsing input - Genre of value: " + genre);
            return false;
        }
    }

    private static boolean isValidGenre(int min_genre, int max_genre) {
        if(min_genre >= 1 && min_genre <= 100 && max_genre >= 1
                && max_genre <= 100 && min_genre < max_genre)
            return true;
        else {
            System.out.println("Error parsing input - Genre Spectrum of values: " +
                    " min_genre: " + min_genre + " max_genre: " + max_genre);
            return false;
        }
    }

    private static boolean isValidPrestige(int prestige) {
        if (prestige >= 1 && prestige <= 5) {
            return true;
        }
        else {
            System.out.println("Error parsing input - Prestige of value: " + prestige);
            return false;
        }
    }

    private static boolean isValidPrestige(int min_prestige, int max_prestige) {
        if(min_prestige >= 1 && min_prestige <= 100 && max_prestige >= 1
                && max_prestige <= 100 && min_prestige <= max_prestige)
            return true;
        else {
            System.out.println("Error parsing input - Prestige of values: " +
                    " min_prestige: " + min_prestige + " max_prestige: " + max_prestige);
            return false;
        }
    }

    private static boolean isValidBudget(int budget) {
        if (budget > 0)
            return true;
        else {
            System.out.println("Error parsing input - Budget of value: " + budget);
            return false;
        }

    }

    private static boolean isValidAttendance(int attendance) {
        if (attendance > 0)
            return true;
        else {
            System.out.println("Error parsing input - Attendance of value: " + attendance);
            return false;
        }
    }

    private static boolean isValidMinPrice(int min_price) {
        if (min_price > 0)
            return true;
        else {
            System.out.println("Error parsing input - Min Price of value: " + min_price);
            return false;
        }
    }
}



