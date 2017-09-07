package pers.wusatosi.CRC.CRCApi;

/**
 * The interface for listening 
 * @author wusatosi/Brad.Wu
 *
 */
public interface StateListener{
	/**
	 * Active the listener
	 * @param state
	 */
	public void call(String state);
}
