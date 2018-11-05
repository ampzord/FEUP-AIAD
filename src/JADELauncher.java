import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import utils.Utils;
import java.io.IOException;

public class JADELauncher {

	public static void main(String[] args) throws IOException {

		Runtime rt = Runtime.instance();

		Profile p1 = new ProfileImpl();
		ContainerController mainContainer = rt.createMainContainer(p1);

		Profile p2 = new ProfileImpl();
		p2.setParameter(Profile.CONTAINER_NAME, "Venues");
		ContainerController venues = rt.createAgentContainer(p2);

		Profile p3 = new ProfileImpl();
		p3.setParameter(Profile.CONTAINER_NAME, "Bands");
		ContainerController bands = rt.createAgentContainer(p3);

		Profile p4 = new ProfileImpl();
		p4.setParameter(Profile.CONTAINER_NAME, "Spectators");
		ContainerController spectators = rt.createAgentContainer(p4);

		AgentController ac1;
		try {
			ac1 = mainContainer.acceptNewAgent("myRMA", new jade.tools.rma.rma());
			ac1.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		/* INIT BANDS */
		AgentController ac2;
		try {
			Utils.readFileBands(Utils.PATH_BANDS);
			for (Object[] band : Utils.bandsInformation) {
				ac2 = bands.createNewAgent((String) band[0], "agents.Band", band);
				ac2.start();
			}

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		/* INIT VENUES */
		AgentController ac3;
		try {
			Utils.readFileVenues(Utils.PATH_VENUES);
			for (Object[] venue : Utils.venuesInformation) {
				ac3 = venues.createNewAgent((String) venue[0], "agents.Venue", venue);
				ac3.start();
			}

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		/* INIT SPECTATORS */
		AgentController ac4;
		try {
			Utils.readFileSpectators(Utils.PATH_SPECTATORS);
			for (int i = 0; i < Utils.spectatorsInformation.size(); i++) {
				String spectatorName = "Spectator" + ++i;
				ac4 = spectators.createNewAgent(spectatorName, "agents.Spectator", Utils.spectatorsInformation.get(i));
				ac4.start();
			}

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

	}





}
