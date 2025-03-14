package org.esupportail.esupsignature.dss.model;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.AssertTrue;

public class CertificateForm {

	private MultipartFile certificateFile;

	private boolean addToKeystore;

	public MultipartFile getCertificateFile() {
		return certificateFile;
	}

	public void setCertificateFile(MultipartFile certificateFile) {
		this.certificateFile = certificateFile;
	}

	public boolean isAddToKeystore() {
		return addToKeystore;
	}

	public void setAddToKeystore(boolean addToKeystore) {
		this.addToKeystore = addToKeystore;
	}

	@AssertTrue(message = "{error.certificate.mandatory}")
	public boolean isCertificateFile() {
		return (certificateFile != null) && (!certificateFile.isEmpty());
	}

}
