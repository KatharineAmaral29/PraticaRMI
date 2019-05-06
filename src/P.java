import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface P extends Remote{

	public void startElection(String nodeId) throws RemoteException, NotBoundException;
	
	public void setLeader(String nodeId) throws RemoteException;
	
	public void lookForCoordenador() throws RemoteException, NotBoundException;
}
