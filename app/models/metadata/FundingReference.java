/**
 * 
 */
package models.metadata;

/**
 * @author aquast
 *
 */
public class FundingReference {

	private String funderName = null;
	private String funderIdentifier = null;
	private String fundingStream = null;
	private String awardNumber = null;
	private String awardURI = null;
	private String awardTitle = null;

	/**
	 * @return the funderName
	 */
	public String getFunderName() {
		return funderName;
	}

	/**
	 * @param funderName the funderName to set
	 */
	public void setFunderName(String funderName) {
		this.funderName = funderName;
	}

	/**
	 * @return the funderIdentifier
	 */
	public String getFunderIdentifier() {
		return funderIdentifier;
	}

	/**
	 * @param funderIdentifier the funderIdentifier to set
	 */
	public void setFunderIdentifier(String funderIdentifier) {
		this.funderIdentifier = funderIdentifier;
	}

	/**
	 * @return the fundingStream
	 */
	public String getFundingStream() {
		return fundingStream;
	}

	/**
	 * @param fundingStream the fundingStream to set
	 */
	public void setFundingStream(String fundingStream) {
		this.fundingStream = fundingStream;
	}

	/**
	 * @return the awardNumber
	 */
	public String getAwardNumber() {
		return awardNumber;
	}

	/**
	 * @param awardNumber the awardNumber to set
	 */
	public void setAwardNumber(String awardNumber) {
		this.awardNumber = awardNumber;
	}

	/**
	 * @return the awardURI
	 */
	public String getAwardURI() {
		return awardURI;
	}

	/**
	 * @param awardURI the awardURI to set
	 */
	public void setAwardURI(String awardURI) {
		this.awardURI = awardURI;
	}

	/**
	 * @return the awardTitle
	 */
	public String getAwardTitle() {
		return awardTitle;
	}

	/**
	 * @param awardTitle the awardTitle to set
	 */
	public void setAwardTitle(String awardTitle) {
		this.awardTitle = awardTitle;
	}

}

/**
 * 
 * <oaire:fundingReferences> <oaire:fundingReference> <oaire:funderName>European
 * Commission</datacite:funderName>
 * <oaire:funderIdentifier funderIdentifierType="Crossref Funder ID">
 * http://doi.org/10.13039/100010661 </oaire:funderIdentifier>
 * <oaire:fundingStream>Horizon 2020 Framework Programme</oaire:fundingStream>
 * <oaire:awardNumber awardURI=
 * "http://cordis.europa.eu/project/rcn/194062_en.html">643410</oaire:awardNumber>
 * <oaire:awardTitle>Open Access Infrastructure for Research in Europe
 * 2020</oaire:awardTitle> </oaire:fundingReference> </oaire:fundingReferences>
 * 
 **/