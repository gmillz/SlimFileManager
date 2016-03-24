package com.slim.fileshare;

import java.io.Serializable;

public class WiFiTransferModal implements Serializable {

	private String fileName;
	private Long fileLength;
	private String iNetAddress;


	public WiFiTransferModal() {
	}
	
	public WiFiTransferModal(String inetaddress) {
		iNetAddress = inetaddress;
	}

	public WiFiTransferModal(String name, Long filelength) {
		fileName = name;
		this.fileLength = filelength;
	}
	
	public String getInetAddress() {
		return iNetAddress;
	}

	public void setInetAddress(String inetAddress) {
		iNetAddress = inetAddress;
	}
	
	
	public Long getFileLength() {
		return fileLength;
	}

	public void setFileLength(Long fileLength) {
		this.fileLength = fileLength;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
	
}
