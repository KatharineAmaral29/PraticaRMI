import java.lang.management.ManagementFactory;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class P5 extends Thread implements P {

	static ArrayList<String> nos = new ArrayList<String>();
	private static String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("[^0-9]", "");
	private static String coordenador;
	static P stub;
	static Registry reg;

	public P5() {
	}

	public void run() {
		Random rand = new Random();

		try {
			sleep(rand.nextInt(3000) + 3000); //Aguarda entre 30 e 60 segundos para iniciar a eleicao
			stub.startElection(pid);
			while (true) {
				lookForCoordenador();
				sleep(rand.nextInt(3000) + 3000);
			}
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws RemoteException {

		P5 no5 = new P5();
		stub = (P) UnicastRemoteObject.exportObject(no5, 0);
		reg = null;
		try {
			System.out.println("P5 esta no ar!");
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
			String[] registrados = reg.list();
			for (int i = 0; i< registrados.length; i++) { //busca os registrados
				System.out.println(registrados[i]);
				nos.add(registrados[i]);
			}
			no5.run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startElection(String pid) throws RemoteException, NotBoundException {
//		if (pid.equals(this.pid)) {
//		System.out.println(this.pid);
//			System.out.println("Processo P1 iniciou a eleicao");
//
//			Registry reg = LocateRegistry.getRegistry();
//			for (String no : reg.list()) {
//				if (!no.equals(this.pid) && Integer.parseInt(no) > Integer.parseInt(this.pid)) {
//					stub = (P) reg.lookup(no);
//					System.out.println("Enviando eleito para o n " + no);
//					stub.startElection(pid);
//				} else
//					stub.setLeader(pid);
//			}
//		}
		
		if (pid.equals(this.pid)) {
			System.out.println("Processo P5 ("+this.pid+") iniciou a eleicao");
			
			boolean bigger = true;
			for (String no : reg.list()) {
				if (!no.equals(this.pid) && Integer.parseInt(no) > Integer.parseInt(this.pid)) {
					P stub = (P) reg.lookup(no);
					System.out.println("Enviando eleito para o n " + no);
					try {
						stub.startElection(pid);
						bigger = false;
					} catch(RemoteException ex) {}
				}
			}
			
			if (bigger) {
				stub.setLeader(pid);
			}
		}
	}

	@Override
	public void setLeader(String pid) throws RemoteException {
		coordenador = pid;
		if (pid.equals(this.pid)) {
			Registry reg = LocateRegistry.getRegistry();
			for (String nodeName : reg.list()) {
				if (!nodeName.equals(this.pid)) {
					try {
						P stub = (P) reg.lookup(nodeName);
						stub.setLeader(pid);
					} catch (Exception e) {	}
				}
			}

			System.out.println("Eu, P5(" + pid + "), sou o novo coordenador!");
		} else {
			System.out.println("N " + pid + " ganhou a eleicao o novo coordenador!");
		}
	}

	public void lookForCoordenador() throws RemoteException, NotBoundException {
		String coordAtual = coordenador;
		if (coordAtual != null && !coordAtual.equals(pid)) {
			try {
				P coord = (P) reg.lookup(coordAtual);
				coord.startElection(pid);
			} catch (Exception e) {
				System.out.println("Coordenador quebrou. Iniciando nova eleição");
				stub.startElection(pid);
			}
		}
	}
}
