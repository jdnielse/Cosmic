package cosmic.object;

import java.util.ArrayList;
import com.jme3.math.Vector3f;

/**
 * The {@link Formations} class handles moving units in different formations. A formation may be defined as
 * what position shape the given units will make when sent to any given area (or point).
 * @author mpstefan
 *
 */
public class Formations {

	public static final int BOX_FORMATION = 0;
	
	/**
	 * The given units are used to generate a set of points to move to specified by the formationType
	 * @return Returns a list of points for the units to move to corresponding to the order they were given
	 * @param units An ArrayList of all units to be moved into formation
	 * @param point The point clicked to send the units to. The Z coordinate is ignored.
	 * @param formationType An integer corresponding to what type of formation the given units are to take at the target location<br>
	 * Check this classes' final ints for valid integers.
	 */
	public static ArrayList<Vector3f> moveUnits(ArrayList<Unit> units, Vector3f point, int formationType){
		// Ensure the given ArrayList is not empty
		if(units.isEmpty()){
			System.out.println("Formations ERROR: Tried to move an empty array of units");
			return null;
		}
		// Generate list of units' positions
		ArrayList<Vector3f> unitPos = new ArrayList<Vector3f>();
		for(int i = 0; i < units.size(); i++){
			unitPos.add(units.get(i).getLocalTranslation());
		}
		// Generate directional vectors
		ArrayList<Vector3f> dirVectors = generateVectors(unitPos, point);
		
		// Hand of to relevant methods based on formationType, break if invalid type is specified
		switch(formationType){
		case BOX_FORMATION:
			return moveUnitsBox(units, dirVectors, point);
		default:
			System.out.println("Formations ERROR: Invalid formation type specified: "+formationType);
			return null;
		}
		
			
	}
	
	/**
	 * Generates a series of {@link Vector3f}s corresponding to up, down, left and right to help with the positioning
	 * of a formation at a point with direction respective to the {@link Unit}'s old location.
	 * @param units An ArrayList of units that is to be moved in formation
	 * @param point Point at which the formation should be centered around
	 * @return An {@link ArrayList} of Unit Vectors with elements corresponding to (UP, DOWN, LEFT, RIGHT) <br>
	 * This return {@link ArrayList} should be used for all formation methods.
	 */
	private static ArrayList<Vector3f> generateVectors(ArrayList<Vector3f> units, Vector3f point){
		// Get the "average location" of all units, essentially finding the centerpoint of all their positions
		float averageX = 0f;
		float averageY = 0f;
		for(Vector3f u: units){
			averageX+=u.x;
			averageY+=u.y;
		}
		
		averageX = averageX/((float)units.size());
		averageY = averageY/((float)units.size());
		
		// This points represents the center of all selected units
		Vector3f unitCenter = new Vector3f(averageX, averageY, 1.0f);
				
		// Calculate direction from unitCenter to the commanded point
		float directionX = unitCenter.x - point.x;
		float directionY = unitCenter.y - point.y;
		
		// Use convenience vectors to help position a formation on a point with a direction (unit vector)
		Vector3f UP_VECTOR = new Vector3f(directionX, directionY, 0f);
		UP_VECTOR = UP_VECTOR.normalize();
		UP_VECTOR = UP_VECTOR.negate();
		
		Vector3f DOWN_VECTOR = UP_VECTOR.negate();
		Vector3f LEFT_VECTOR = new Vector3f(-UP_VECTOR.y, UP_VECTOR.x, 0f);
		Vector3f RIGHT_VECTOR = LEFT_VECTOR.negate();
		
		// Return the answer
		ArrayList<Vector3f> ret = new ArrayList<Vector3f>();
		ret.add(UP_VECTOR);
		ret.add(DOWN_VECTOR);
		ret.add(LEFT_VECTOR);
		ret.add(RIGHT_VECTOR);
		
		//System.out.println("Calculated facing is UP:"+UP_VECTOR.toString()+" DOWN:"+DOWN_VECTOR.toString()+" LEFT:"+LEFT_VECTOR.toString()+" RIGHT:"+RIGHT_VECTOR.toString());
		
		return ret;
		
	}
	
	/**
	 * Moves the given {@link Unit}s to the given point in a standard box Formation with a max length for each row being 5 {@link Unit}s
	 * @param units {@link Unit}s to move
	 * @param directionalVectors Vectors corresponding to UP, DOWN, LEFT, RIGHT so align units. Get this from generateVectors
	 * @param point {@link Vector3f} of point to move units to
	 */
	private static ArrayList<Vector3f> moveUnitsBox(ArrayList<Unit> units, ArrayList<Vector3f> directionalVectors, Vector3f point){
		int size = units.size();
		int rows = ((int) Math.ceil(((double)size)/5.0));
		int lastRowLength = size % 5;
		
		ArrayList<Vector3f> ret = new ArrayList<Vector3f>();
		
		// Zero means that the last row is full as well
		if(lastRowLength == 0){
			lastRowLength = 5;
		}
		
		Vector3f position = point.clone();
		for(int i = 0; i < rows; i++){
			if( (i+1) == rows){ //If this is the last row, we might not have a row of five units
				
				// If lastRowLength is 1, no changes to position are needed
				
				if(lastRowLength == 2){
					position = position.add(directionalVectors.get(2).mult(0.5f));
				}else if(lastRowLength == 3){
					position = position.add(directionalVectors.get(2));
				}else if(lastRowLength == 4){
					position = position.add(directionalVectors.get(2).mult(0.5f));
					position = position.add(directionalVectors.get(2));
				}else if(lastRowLength == 5){
					position = position.add(directionalVectors.get(2).mult(2.0f));
				}
				
				for(int j = 0; j < lastRowLength; j++){
					ret.add(position);
					position = position.add(directionalVectors.get(3));
				}
				
			}else{ //We will always have five units in a row when it is not the last one
				
				// Go Left twice
				position = position.add(directionalVectors.get(2));
				position = position.add(directionalVectors.get(2));
				
				// Send 5 units to this row
				for(int j = 0; j < 5; j++){
					ret.add(position);
					position = position.add(directionalVectors.get(3));
				}
				
				// Set position to next row
				
				// Go Left twrice
				position = position.add(directionalVectors.get(2));
				position = position.add(directionalVectors.get(2));
				position = position.add(directionalVectors.get(2));
				
				// Go Down
				position = position.add(directionalVectors.get(1));
				
			}
		}
		
		return ret;
		
	}
	
//	public static void main(String[] args) {
//		//Hijack method for testing
////		ArrayList<Vector3f> neo = new ArrayList<Vector3f>();
////		neo.add(new Vector3f(7f, 14f, 1.0f));
////		neo.add(new Vector3f(17f, 8f, 1.0f));
////		ArrayList<Vector3f> directions = generateVectors(neo, new Vector3f(20.0f, 20.0f, 1.0f));
////		moveUnitsBox(directions, new Vector3f(20.0f, 20.0f, 1.0f));
//		
//	}
}
