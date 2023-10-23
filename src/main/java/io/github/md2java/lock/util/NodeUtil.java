package io.github.md2java.lock.util;

import java.net.InetAddress;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NodeUtil {

	private static String nodeId;
	private static String hostName;
	private static String hostId;
	@Value("${server.port:8080}")
	private String port;

	@PostConstruct
	public void init() {
		nodeId = UUID.randomUUID().toString();
		hostName = getHostName();
		hostId = String.format("%s_%s", hostName(),port);
		log.info("initialized - hostId: {} ",hostId());
	}

	private static String getHostName() {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			return localHost.getHostName();
		} catch (Exception e) {
			e.printStackTrace();
			return "Unknown Host";
		}
	}

	public static String nodeId() {
		return nodeId;
	}

	public static String hostName() {
		return hostName;
	}

	public static String hostId() {
		return hostId;
	}
}