package com.lights0123.Waked;

import com.hoodcomputing.natpmp.ExternalAddressRequestMessage;
import com.hoodcomputing.natpmp.MapRequestMessage;
import com.hoodcomputing.natpmp.NatPmpDevice;
import com.hoodcomputing.natpmp.NatPmpException;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PortMapper {
	private final static Logger logger = Logger.getLogger(PortMapper.class.getName());
	/** Description of Map(int port, boolean isTCP)
	 *
	 * @param port          The port of which to map
	 * @param isTCP         If the mapping is TCP or UDP
	 * @return              If the operation was successful
	 */

	public static boolean Map(int port, boolean isTCP){

		logger.info("Starting weupnp");

		GatewayDiscover discover = new GatewayDiscover();
		logger.info("Looking for Gateway Devices...");
		try {
			discover.discover();
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		GatewayDevice d = discover.getValidGateway();

		if (null != d) {
			logger.log(Level.INFO, "Gateway device found.\n{0} ({1})", new Object[]{d.getModelName(), d.getModelDescription()});
		} else {
			logger.info("Weupnp failed. Trying jNAT-PMP...");
			NatPmpDevice pmpDevice;

			try {
				//starting up jNAT-PMP
				logger.info("Starting jNAT-PMP...");
				pmpDevice = new NatPmpDevice(false);
				//Looking for the external device.
				ExternalAddressRequestMessage extAddr = new ExternalAddressRequestMessage(null);
				pmpDevice.enqueueMessage(extAddr);
				//Waiting...
				logger.info("Waiting... Please wait.");
				pmpDevice.waitUntilQueueEmpty();
				//Is the gateway functional?
				logger.info("Testing...");
				extAddr.getExternalAddress();
				//Okay, now actually port map.
				logger.info("Port mapping...");
				MapRequestMessage map = new MapRequestMessage(isTCP, port, port, 65535, null);
				pmpDevice.enqueueMessage(map);
				pmpDevice.waitUntilQueueEmpty();

				// Let's find out what the external port is.
				logger.info("finishing...");
				int extPort = map.getExternalPort();
				logger.info("done. Port: "+extPort);
				return true;
				// All set!
			} catch (NatPmpException e) {
				e.printStackTrace();
				return false;
			}
		}

		InetAddress localAddress = d.getLocalAddress();
		logger.log(Level.INFO, "Using local address: {0}", localAddress);
		String externalIPAddress = null;
		try {
			externalIPAddress = d.getExternalIPAddress();
		} catch (IOException | SAXException e) {
			e.printStackTrace();
		}
		logger.log(Level.INFO, "External address: {0}", externalIPAddress);
		new PortMappingEntry();

		logger.log(Level.INFO, "Attempting to map port {0}", port);
		logger.log(Level.INFO, "Querying device to see if mapping for port {0} already exists", port);

		try {
			logger.info("Sending port mapping request");
			if (d.addPortMapping(port,port,localAddress.getHostAddress(),"UDP","test")) {
				logger.info("Port mapping SUCCESSFUL");
				return true;
			} else {
				logger.warning("Port mapping FAILED");
				return false;
			}
		} catch (IOException | SAXException e) {
			e.printStackTrace();
			return false;
		}
	}
}
