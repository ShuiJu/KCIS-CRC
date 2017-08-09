package pers.Brad.CRC.CRC;

import java.net.URL;
import java.util.Map;

import pers.Brad.CRC.CRC.StudentIdentify.StudentID;

/**
 * Every classes implements this interface can be process as a student info holder
 * @author wusatosi/Brad.Wu
 *
 */
public interface StanderStudent extends Cloneable {
	
	public static final String NA="N/A";
	
	/**
	 * Set the ID of the student
	 * @param ID - Student's ID
	 */
	public void setID(StudentID ID);
	/**
	 * Get the ID of the student
	 * @return ID - Student ID
	 */
	public StudentID getID();
	
	
	
	/**
	 * Set the name of the student to a new one
	 * @param name - The Name of the student
	 */
	public void setName(String name);
	/**
	 * Get the name of the student
	 * @return name - The Name of the student
	 */
	public String getName();
	
	
	
	/**
	 * Set the Image of the student
	 * @param ImageURL - The URL of the Image
	 */
	public void setImageURL(URL ImageURL);
	/**
	 * Get the URL of the student's Image
	 * @return - the URL of the student's Image
	 */
	public URL getImageURL();
	
	
	/**
	 * Set the line ID of the student
	 * @param LineID
	 */
	public void setLineID(String LineID);
	/**
	 * Get the line ID of the student
	 * @return line ID
	 */
	public String getLineID();
	
	
	/**
	 * Set speical properties to the student
	 * @param Properties - the new properties
	 */
	public void setAdvanceProperties(Map<String,String> Properties);
	/**
	 * Get speical properties of the student
	 * @return the special properties
	 */
	public Map<String,String> getAdvanceProperties();
	/**
	 * Get the property with the name
	 * @param arg0 - the key of the property
	 * @return the value of the property
	 */
	public String getProperty(String arg0);
	/**
	 * Add a propety to the propetry Map<String,String> for the student
	 * @param arg0 - key
	 * @param arg1 - value
	 */
	public void putProperty(String arg0,String arg1);
	/**
	 * Put propetries into the propetry list
	 * @param arg - more properties
	 */
	public void putAll(Map<String,String> arg);
	
}
