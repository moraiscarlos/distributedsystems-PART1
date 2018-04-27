package br.ufu.sistemasdistribuidos.servidor;

import java.io.*;
import java.net.*;



public class Cliente {
	static DatagramSocket clientSocket;
	static DatagramPacket receivePacket;
	static byte[] receiveData = new byte[1024];
	
	public static void main(String args[]) throws Exception {
		// TODO Auto-generated method stub
		clientSocket = new DatagramSocket();
	
		// THREAD DE RESPOSTA
		new Thread(){
			@Override
			public void run(){
				while(true){
					receivePacket = new DatagramPacket(receiveData, receiveData.length);
					try {
						clientSocket.receive(receivePacket);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					String respostaServidor = new String(receivePacket.getData());
					
					System.out.println("Resposta do servidor:" + readResponde(respostaServidor));
					System.out.println("\n\n");
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}.start();
		
		// THREAD DE LEITURA
		new Thread(){
			@Override
			public void run(){
				while(true){
					String comando = null;
					BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		        
					/* CRIANDO MENU */
					System.out.println(" -------------- MENU ------------  ");
					System.out.println("|             C - CREATE	  |");
					System.out.println("|             R - READ		  |");
					System.out.println("|             U - UPDATE	  |");
					System.out.println("|             D - DELETE	  |");
					System.out.println("|             E - EXIT		  |");
					System.out.println(" --------------------------------  ");
					
					// OPÇÃO DO MENU
					System.out.println(" > Digite sua opção: ");
					String opcao = null;
					try {
						opcao = inFromUser.readLine();
					} catch (IOException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
					
					
					if ( opcao.equals("E")){
						System.out.println("Até mais....");
						System.exit(0);
					}
					
					// INSERINDO A CHAVE
					System.out.println(" > Digite a chave: ");
					String chave = null;
					try {
						chave = inFromUser.readLine();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				
					// VERIFICANDO LIMITE BYTES DA CHAVE
					if (chave.getBytes().length > 20){
						System.out.println("Chave ultrapassou limite de bytes!");
						return;
					}
					
					// COMPARAÇOES COM OPÇÃO (CREATE AND UPDATE) 
					if ((opcao.equals("C")) || (opcao.equals("U")) ) {
						
						System.out.println(" > Digite o valor: ");
						String valor = null;
						try {
							valor = inFromUser.readLine();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					    
						if (valor.getBytes().length > 1400){		
							System.out.println("Valor ultrapassou limite de bytes!");
							return;
						}
						
					    comando = opcao+chave.length()+ ":" + chave + valor.length()+ ":" +valor;
						
						
					}else if ( (opcao.equals("R")) || (opcao.equals("D"))){ // COMPARAÇÕES COM OPÇÃO( READ AND DELETE)
						
						comando = opcao+chave.length()+ ":" + chave;
						
						
					}else{
						System.out.println("Opccao inválida!");
					}
					
					

					InetAddress IPAddress = null;
					try {
						IPAddress = InetAddress.getByName("localhost");
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					byte[] sendData = new byte[1024];
					
					

					sendData = comando.getBytes();
					
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
					try {
						clientSocket.send(sendPacket);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						Thread.sleep(150);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		}.start();

		
}
	// RETIRA OS BUG DE BYTES A MAIS
	public static String readResponde(String txt) {
		int estado = 0;
		int v = 0;
		String value = "";
		String r = "";
		for (int i = 0; i < txt.length(); i++) {
			if(estado == 0) {
				if(txt.charAt(i) == ':') {
					v = Integer.parseInt(value);
					estado++;
				}else {
					value = value + txt.charAt(i);
				}
			}else {
				return txt.substring(i,i+v);
			}
		}
		return null;
	}

}
