package org.esupportail.esupsignature.dss.model;

import eu.europa.esig.dss.enumerations.EncryptionAlgorithm;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class DataToSignParams implements Serializable {

	@NotNull
	private String signingCertificate;
	@NotNull
	private List<String> certificateChain;
	@NotNull
	private EncryptionAlgorithm encryptionAlgorithm;

	public DataToSignParams() {
	}

	public String getSigningCertificate() {
		return signingCertificate;
	}

	public void setSigningCertificate(String signingCertificate) {
		this.signingCertificate = signingCertificate;
	}

	public List<String> getCertificateChain() {
		return certificateChain;
	}

	public void setCertificateChain(List<String> certificateChain) {
		this.certificateChain = certificateChain;
	}

	public EncryptionAlgorithm getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
		this.encryptionAlgorithm = encryptionAlgorithm;
	}

}
