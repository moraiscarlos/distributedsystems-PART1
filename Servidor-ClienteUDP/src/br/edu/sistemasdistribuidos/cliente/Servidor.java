package br.edu.sistemasdistribuidos.cliente;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufu.sistemasdistribuidos.core.Comando;


public class Servidor {

	// CRIANDO MAPA <BIGINTEGER,STRING>
	static Map<BigInteger, String> mapa = new HashMap<BigInteger, String>();
	
	static Comando cabeca = null;
	static Comando calda  = null;
	
	static Comando cabecaLog = null;
	static Comando caldaLog  = null;
	
	static DatagramSocket serverSocket;
	
	static byte[] receiveData = new byte[1024];
    static byte[] sendData = new byte[1024];
	

	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
        serverSocket = new DatagramSocket(9876);
        
        // THREAD DE LEITURA 
        new Thread(){
        	@Override
        	public void run(){
        		System.out.println("Servidor iniciado!");
        		
        		try {
					lerLOG();
				} catch (UnsupportedEncodingException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
        		while(true){

        			// RECEBE COMANDO (OPCAO,KEY,VALUE)
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
						serverSocket.receive(receivePacket);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                    
                    Comando t = null;
					try {
						t = readCommand(receivePacket.getData());
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    
                    
                    //String sentence = new String( receivePacket.getData());
                    //System.out.println("RECEIVED: " + sentence);
                    
                    InetAddress IPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    
                    System.out.println("Cliente conectado: " + IPAddress + ":" + port);
                    
                    push(t.getTipo(), t.getChave(), t.getValor(), IPAddress, port);
                    pushLOG(t.getTipo(), t.getChave(), t.getValor(), IPAddress, port);
                    
                   //String capitalizedSentence = sentence.toUpperCase();
                   //sendData = capitalizedSentence.getBytes();
                    
                    //DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    //serverSocket.send(sendPacket);

                 } 
    
        	}
        }.start();
       
        // THREAD DE PROCESSAMENTO
        new Thread(){
        	@Override
        	public void run(){
        		while(true){  
        			Comando t = pull();
        			if ( t != null){
        				String resposta = CRUD(t);
        				
        				
                        
                        sendData = (resposta.length()+":"+resposta).getBytes();
                         
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, t.getIPAddress(), t.getPort());
                        try {
							serverSocket.send(sendPacket);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
        			}
        			
        			
        			
        			try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		 } 
        	    
        	}
        }.start();
        
        
        // THREAD DE LOG
        new Thread(){
        	@Override
        	public void run(){
        		while(true){
        			
        			Comando t = pullLOG();
        			if ( t != null){
        				String log = t.getTipo()+(t.getChave()+"").length()+":"+t.getChave();
            			if ((t.getTipo().equals("C")) || (t.getTipo().equals("U") )){
            				log = log + t.getValor().length() + ":" +t.getValor();
            			}
            				log = log + "\t<'"+t.getIPAddress()+"':"+t.getPort()+">";
            				
            				List<String> lines = null;
            				Path path = Paths.get("log.txt");
            				try {
    							lines = Files.readAllLines(path, StandardCharsets.UTF_8);
    						} catch (IOException e2) {
    							// TODO Auto-generated catch block
    							e2.printStackTrace();
    						}
            				
            				FileWriter arq = null;
    						try {
    							arq = new FileWriter("log.txt");
    						    PrintWriter gravarArq = new PrintWriter(arq);
                			    
                			    for (int i = 0; lines!= null && i < lines.size(); i++) {
        							gravarArq.println(lines.get(i));
        						}
                			    gravarArq.println(log);
                			    gravarArq.close();
    						} catch (IOException e1) {
    							// TODO Auto-generated catch block
    							e1.printStackTrace();
    						}
    						
            			
        			}
        			
        			try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        		
        	} 	
        }.start();
        		
	}
	
	// DESMONTO STRING SEPARANDO EM PARTES
	public static Comando readCommand(byte[] data) throws UnsupportedEncodingException {
		String command = new String(data, "UTF-8");
		String type = null, key = "", value = "";
		int estado = 0,  v = 0;
		String number = "";
		for (int i = 0; i < command.length(); i++) {
			if(estado == 0) {
				type = command.charAt(i)+"";
				estado = 1;
			}else if(estado == 1 || estado == 4) {
				if(command.charAt(i) == ':') {
					estado++;
					v = Integer.parseInt(number);
				}else {
					number = number + command.charAt(i);
				}
			}else if(estado == 2) {
				key = key + command.charAt(i);
				v--;
				if(v == 0) {
					estado = 3;
				}
			}else if(estado == 3) {
				if(type.equals("D") || type.equals("R")) {
					break;
				}else {
					i--;
					number = "";
					estado = 4;
				}
			}else if(estado == 5) {
				value = value + command.charAt(i);
				v--;
				if(v==0) {
					break;
				}
			}
			
		}
		Comando t = new Comando();
		t.setTipo(type);
		t.setChave(new BigInteger(key));
		t.setValor(value);
		
		
		
		return t;
	}
	
	// ADICIONANDO EM FILA
	public static void push(String tipo, BigInteger chave, String valor,InetAddress IPAddress, int port){
		
		Comando temp = new Comando();
		temp.setTipo(tipo);
		temp.setChave(chave);
		temp.setValor(valor);
		temp.setIPAddress(IPAddress);
		temp.setPort(port);
		
		if (cabeca == null){
			cabeca = temp;
		}
		if (calda != null){
			calda.setProximo(temp);
		}
		
		calda = temp;
		
	}
	
	// RETIRANDO DA FILA
	public static Comando pull(){
		if (cabeca ==  null){
			return null;
		}
		
		Comando temp = cabeca;
		
		if (cabeca.getProximo() == null){
			cabeca = null;
		}else {
			cabeca = cabeca.getProximo();
		}
		
		return temp;
	}
	
	
	// COLOCANDO EM FILA LOG
	public static void pushLOG(String tipo, BigInteger chave, String valor,InetAddress IPAddress, int port){
		
		Comando temp = new Comando();
		temp.setTipo(tipo);
		temp.setChave(chave);
		temp.setValor(valor);
		temp.setIPAddress(IPAddress);
		temp.setPort(port);
		
		if (cabecaLog == null){
			cabecaLog = temp;
		}
		if (caldaLog != null){
			caldaLog.setProximo(temp);
		}
		
		caldaLog = temp;
		
	}
	
	// RETIRANDO DA FILA
	public static Comando pullLOG(){
		if (cabecaLog ==  null){
			return null;
		}
		
		Comando temp = cabecaLog;
		
		if (cabecaLog.getProximo() == null){
			cabecaLog = null;
		}else {
			cabecaLog = cabecaLog.getProximo();
		}
		
		return temp;
	}
	
	// COMANDO CRUD
	public static String CRUD(Comando t){
		String resposta = null;
		switch (t.getTipo()) {
		case "C":
			mapa.put(t.getChave(), t.getValor());
			resposta = "Adicionado com sucesso!";
			
			
			break;
		case "U":
			mapa.put(t.getChave(), t.getValor());
			resposta = "Atualizado com sucesso!";
			
			break;
		case "R":
		
			resposta = "Chave: "+t.getChave()+ " Valor: "+mapa.get(t.getChave());;
										
			break;
		case "D":
			mapa.remove(t.getChave());	
			resposta = "Deletado com sucesso!";
			
			
			break;
		default:
			break;
		}
		
		return resposta;
	}
	
	// PERCORRER LOG
	public static void lerLOG() throws UnsupportedEncodingException{
		
		List<String> lines = null;
		Path path = Paths.get("log.txt");
		try {
			lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			return;
		}
		
		for (int i = 0; i < lines.size(); i++) {
			CRUD(readCommand(lines.get(i).getBytes()));
			
		}
		
	}
	
}
