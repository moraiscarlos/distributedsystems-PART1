package br.ufu.sistemasdistribuidos.core;

import java.math.BigInteger;
import java.net.InetAddress;

public class Comando {
	
	String 		tipo;
	BigInteger 	chave;
	String 		valor;
	
    InetAddress IPAddress;
    int port;
    
    
	
	public InetAddress getIPAddress() {
		return IPAddress;
	}

	public void setIPAddress(InetAddress iPAddress) {
		IPAddress = iPAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	Comando proximo;

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public BigInteger getChave() {
		return chave;
	}

	public void setChave(BigInteger chave) {
		this.chave = chave;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public Comando getProximo() {
		return proximo;
	}

	public void setProximo(Comando proximo) {
		this.proximo = proximo;
	}
	
	

}
