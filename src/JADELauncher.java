import agents.Band;
import agents.Spectator;
import agents.Venue;
import utils.Utils;

import java.io.IOException;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class JADELauncher {

	public static void main(String[] args) throws IOException {

		/* Read Files */
		Utils.readFileBands(Utils.PATH_BANDS);
		for (Band band : Utils.bandsList)
			System.out.println(band.toString());

		Utils.readFileVenues(Utils.PATH_VENUES);
		for (Venue venue : Utils.venuesList)
			System.out.println(venue.toString());

		Utils.readFileSpectators(Utils.PATH_SPECTATORS);
		for (Spectator spectator : Utils.spectatorsList)
			System.out.println(spectator.toString());


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
			ac2 = bands.createNewAgent("band1", "agents.Band", new Object[0]);
			ac2.start();
			ac2 = bands.createNewAgent("band2", "agents.Band", new Object[0]);
			ac2.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		/* INIT VENUES */
		AgentController ac3;
		try {
			ac3 = venues.createNewAgent("venue1", "agents.Venue", new Object[0]);
			ac3.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		/* INIT SPECTATORS */
		AgentController ac4;
		try {
			ac4 = venues.createNewAgent("spectator1", "agents.Spectator", new Object[0]);
			ac4.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		/*FIPAS
		try {
			ac2 = container.createNewAgent("NET_Initiator", "agents.FIPAContractNetInitiatorAgent", agentArgs);
			ac2.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

		try {
			ac2 = container.createNewAgent("NET_Responder", "agents.FIPAContractNetResponderAgent", agentArgs);
			ac2.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}*/
	}

}
