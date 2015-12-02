package core.type;

public class NetworkHeader {

	public String destination;
	public String source;
	public String user;

	public NetworkHeader(String user, String destination, String source) {
		this.destination = destination;
		this.source = source;
		this.user = user;
	}
}
