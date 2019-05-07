import java.lang.management.ManagementFactory;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class P2 extends Thread implements P {

	static ArrayList<String> nos = new ArrayList<String>();
	private static String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("[^0-9]", "");
	private static String coordenador;
	static P stub;
	static Registry reg;

	public P2() {
	}

	public void run() {
		Random rand = new Random();

		try {
			sleep(rand.nextInt(30) + 30); //Aguarda entre 30 e 60 segundos para iniciar a eleicao
			stub.startElection(pid);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws RemoteException {
			

		P2 no2 = new P2();
		stub = (P) UnicastRemoteObject.exportObject(no2, 0);
		reg = null;
		try {
			System.out.println("P2 esta no ar!");
			reg = LocateRegistry.createRegistry(1099);
		} catch (Exception e) {
			try {
				reg = LocateRegistry.getRegistry(1099);
			} catch (Exception ee) {
				System.exit(0);
			}
		}
		reg.rebind(pid, stub);

		try {
			sleep(60000); // aguarda um minuto para receber a lista de inscritos no registro RMI
			String[] registrados = new String[7];
			registrados = reg.list();
			for (int i = 0; i< registrados.length; i++) { //busca os registrados
				System.out.println(registrados[i]);
				nos.add(registrados[i]);
			}
			no2.run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startElection(String pid) throws RemoteException, NotBoundException {
		if (pid.equals(this.pid)) {
			System.out.println("N P2 iniciou a eleicao");

			Registry reg = LocateRegistry.getRegistry();
			for (String no : reg.list()) {
				if (!no.equals(this.pid) && Integer.parseInt(no) > Integer.parseInt(this.pid)) {
					stub = (P) reg.lookup(no);
					System.out.println("Enviando eleicao para o n " + no);
					stub.startElection(pid);
				} else
					stub.setLeader(pid);
			}
		}
	}

	@Override
	public void setLeader(String pid) throws RemoteException {
		coordenador = pid;
		if (pid.equals(this.pid)) {
			System.out.println("O n " + pid + ",  o novo coordenadenador!");
			Registry reg = LocateRegistry.getRegistry();
			for (String nodeName : reg.list()) {
				if (!nodeName.equals(this.pid)) {
					try {
						stub = (P) reg.lookup(nodeName);
						stub.setLeader(pid);

					} catch (NotBoundException e) {
						e.printStackTrace();
					}
				}
			}

			System.out.println("O n " + pid + ",  o novo coordenadenador!");
		} else {
			System.out.println("N " + pid + " ganhou a eleicao e  o novo coordenador!");
		}
	}

	public void lookForCoordenador() throws RemoteException, NotBoundException {
		System.out.println("Coordinator has crushed. Iniating new election");
		try {
			stub = (P) reg.lookup(coordenador);
		} catch (RemoteException | NotBoundException e) {
			stub.startElection(pid);
			e.printStackTrace();
		}
	}
}
