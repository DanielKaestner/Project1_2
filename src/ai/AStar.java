package ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.lwjgl.Sys;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import Obstacles.Hole;
import Obstacles.Obstacle;
import collisionDetector.CollisionDetector;
import gameEngine3D.GameScreen3D;
import gameEngine3D.Golfball;
import physics.DifferentialEquationSolver;
import physics.VectorComputation;


// Find vectors for direction
// Figure out velocity required
// Fire ball accordingly. <-- WHERE THE FUCK IS THIS!?

public class AStar 
{
	/*
	 TODO: We dont need to find the path to the hole. Apparently the A* algorithm is allowed to know, where the hole is. 
	 In that case replacing the sets with a map and assigning costs to a vector/position would be more useful. 
	 The structure of the algorithm stays very similar though.
	 
	 TODO: The step size might be adjusted to the smallest obstacle size .Only add the element with the cheapest cost to the expandable list.
	 Take the cheapest element of the queue and iterate over it. Create new individual objects for the nodes.
	 
	 TODO: Add a break clause for when the hole cannot be reached (Period 6).
	 */
	
	//Objekte m�ssen Nodes haben! (Objects must have nodes!) ///* WHAT DOES THIS EVEN MEAN!? *///

	private HashSet<AStarTile> openList; // Viable area, needs to be evaluated.
	private HashSet<AStarTile> closedList; // Cleared area.
	private ArrayList<AStarTile> pathList; // Derp.
	private AStarTile lastTile; // Final tile in the path.
	
	
	private BoundingBox courseDimensions;
	private Vector3 holePosition;
	private Vector3 ballPosition;
	private CollisionDetector collisionDetector;
	private Set<Obstacle> obstacleList;
	private boolean hasFoundPath = false;
	private float stepSize; 
	private Golfball golfBall;
	private DifferentialEquationSolver ode;
	/*
	 * Currently this should be able to find the hole in the entire course.
	 * Identifying the shortest path to it will be a different thing though.
	 */

	
	// Constructor.
	public AStar(GameScreen3D gamescreen, float stepSize) 
	{
		this.golfBall = gamescreen.getGolfball();
		this.stepSize = stepSize;
		
		courseDimensions = gamescreen.getCouserDimensions();
		holePosition = gamescreen.getHole().getBoundingBox().getCenter(new Vector3());
		ballPosition = gamescreen.getGolfball().getPosition();
		obstacleList = gamescreen.getAllObstacles();
		ode = gamescreen.getDifferentialEquationSolver();
		collisionDetector = new CollisionDetector(ode);
		openList = new HashSet<>();
		closedList = new HashSet<>();
		pathList = new ArrayList<>();
	}
 
	
	// Path-finder method.
	public void findPathToHole() 
	{
		AStarTile start = new AStarTile(ballPosition, holePosition); // Initial tile constructor.
		openList.add(start);
		AStarTile cheapestElement = null; ///* THIS LINE GOT ADDED SO A NEW AStarTile OBJECT WASN'T CREATED EACH LOOP *///
		
		System.out.println("Starting WHILE loop.");
		// Search.
		while (!hasFoundPath && !openList.isEmpty()) 
		{
			cheapestElement = findCheapestElement(); // Checks openList and finds the best valued tile.
			expandArea(cheapestElement); // Uses the "best valued tile" to the expand the openlist, rinse and repeat.
		}
		if (hasFoundPath)
			printPath();
		else System.out.println("ERROR: Path impossible.");
		/*
		TODO: iterate through all expandable areas' centre positions, check if there is a an overlap otherwise expand the area and shift it in the explored area set.
		*/ 		///* WHAT DOES THIS EVEN MEAN!? *///
	}
	
	
	private void printPath()
	{
		pathList.add(lastTile);
		System.out.println("Found Path!");
		for (AStarTile o : pathList)
			System.out.println("I'm so happy I've finally contributed!  " + o.getPosition());
	}
	
	
	private AStarTile findCheapestElement() 
	{
		AStarTile cheapestTile = null;
		float minCost = Integer.MAX_VALUE;
		for(AStarTile a : openList) // Scan the openList for the lowest value tile.
			if(a.getTotalCost() < minCost) 
			{ 
				cheapestTile = a;
				minCost = a.getTotalCost();
			}
		
		pathList.add(cheapestTile);
		return cheapestTile;
	}

	
	/**
	 * Computes all the areas around the current position with a distance of the
	 * hole radius.
	 * 
	 * This method is mainly composed of two helper methods: helperA (1) and helperB (2),(3). 
	 * This method performs (4) on its own.
	 * 
	 * 1) Create 3x3x3 box around the input position and label the edge bounding boxes "neighbours".
	 * 2) Scan through the neighbours list and check their validity.
	 * 3) Also check if each neighbour intersects with an object, and if that object is the Hole (goal).
	 * 4) Modify openList and closedList.
	 * 
	 * @param expandablePosition
	 */
	private void expandArea(AStarTile expandTile) 
	{
		HashSet<AStarTile> neighbours = helperA(expandTile);
		helperB(neighbours);
		closedList.add(expandTile);
		openList.remove(expandTile);
	}
	
	
	// Generates 3x3x3 grid and thus creates the given tile's neighbours.
	private HashSet<AStarTile> helperA(AStarTile expandTile)
	{
		Vector3 expandPosition = expandTile.getPosition(); // Current coordinates.
		// TODO: see, if we still are in the feasible region ///* LIKE CHECK THIS TILE ISNT IN closedList? *///

		System.out.println("Remaining Distance: " + VectorComputation.getInstance().getDistance(holePosition, expandTile.getPosition())); // From this tile to the goal.
		
		
		// Build a "Box" around the current position by computing the centre positions of the touching boxes.
		// The for loop is for the y positions. We build a square below the position of 9 tiles, one above and one at the same y level with 8 tiles. ///* ...WHAT? *///
		
		// Create a 3x3x3 "box" around the current position with the edge tiles the neighbouring tiles of this tile.
		// 
		HashSet<AStarTile> neighbours = new HashSet<>(); 
		for (int x = -1; x <= 1; x++) // The limits on these loops are to mimic a 3x3x3 grid.
		{
			for (int y = -1; y <= 1; y++) 
			{
				for (int z = -1; z <= 1; z++) 
				{
					if (x == 0 && y == 0 && z == 0)
						continue; // Skip the centre box.
					
					Vector3 newPosition = new Vector3(); // Create new centre position for one of the surrounding tiles.
					newPosition.x = expandPosition.x + (x *stepSize);
					newPosition.y = expandPosition.y + (y *stepSize);
					newPosition.z = expandPosition.z + (z *stepSize);
					AStarTile temp = new AStarTile(expandTile, newPosition, holePosition); //
					neighbours.add(temp);
				}
			}
		}
		return neighbours;
	}
	
	
	// Inspects neighbours' validity and object intersection status.
	private void helperB(HashSet<AStarTile> neighbours)
	{
		for (AStarTile v : neighbours)
		{
			// If the current position is already in one of the two sets, just jump to the next position.
			if (isInOpenList(v) || isInClosedList(v))
				continue;
			
			// Create an obstacle representation of the current neighposition +- some leeway.
			BoundingBox boundingBox = buildBoundingBoxAroundPosition(v.getPosition());
			
			// Check if this bounding box is within the course's dimensions.
			if (collisionDetector.determineIntersection(boundingBox, courseDimensions)) 
			{
				// Then check if this bounding box intersects with any other obstacles.
				boolean intersectsWithObstacle = false;
				for (Obstacle o : obstacleList) 
				{
					///* MY VERSION OF CHECKING FOR INTERSECTION, YOURS BUT REARRANGED *///
					if (collisionDetector.determineIntersection(boundingBox, o.getBoundingBox()))
					{
						if (o instanceof Hole) // Then check if said obstacle is the hole (goal).
						{
							hasFoundPath = true;
							lastTile = v;
							System.out.println("PATHFOUND WITH TILE: " + v.getPosition());
							//System.out.println( "A* Intersection position is at: " + boundingBox + "\n The hole position at: " + o.getBoundingBox().toString() );
						}
						else
						{
							intersectsWithObstacle = true;
							break;
						}
					}
				/*	THIS IS WHAT YOU WROTE AND I REARRANGED IT. THAT COOL?:
				 * 
				    if (o instanceof Hole && collisionDetector.determineIntersection(boundingBox, o.getBoundingBox())) 
					{
						hasFoundPath = true;
						lastTile = v;
						//System.out.println( "A* Intersection position is at: " + boundingBox + "\n The hole position at: " + o.getBoundingBox().toString() );
					}
					
					if (collisionDetector.determineIntersection(boundingBox, o.getBoundingBox())) 
					{
						intersectsWithObstacle = true;
						break;
					} */
				}
				
				if (!intersectsWithObstacle)
				{
					//System.out.println("Add: " + boundingBox);
					openList.add(v);
				}
			}
		}
	}

	
	/**
	 * Builds a boundingbox around a given vector. The boundingbox has the size of
	 * the hole bounding box TODO: Prehaps scale down the bounding box, if it does
	 * not find the hole, since it might just barely overjump it.
	 * 
	 * @param position
	 *            The center position of the point, where the boundingbox should be
	 *            build around
	 * @return A boundingbox, with the dimensions of the hole and the center in the
	 *         position from the parameter
	 */
	private BoundingBox buildBoundingBoxAroundPosition(Vector3 position) 
	{
		// Vectro3.sub/add did not work....
		Vector3 min = new Vector3(position.x - stepSize/2, position.y - stepSize/2, position.z - stepSize/2);
		Vector3 max = new Vector3(position.x + stepSize/2, position.y + stepSize/2, position.z + stepSize/2);
		return new BoundingBox(min, max);
	}
	
	
	public void setToNextPosition() 
	{
		if(lastTile == null)
			return;
		
		golfBall.setPosiition(lastTile.getPosition());
		lastTile = lastTile.getParent();
	}

	
	public boolean isInOpenList(AStarTile comparableTile) // Derp.
	{
		for(AStarTile a : openList) 
		{
			if(a.getPosition().equals(comparableTile)) 
			{
				return true;
			}
		}
		return false;
	}
	
	
	public boolean isInClosedList(AStarTile comparableTile)  // Derp.
	{
		for(AStarTile a : closedList) 
		{
			if(a.getPosition().equals(comparableTile)) 
			{
				return true;
			}
		}
		return false;
	}


	public Vector3 getDirection() // Direction from the ball to the lastTile
	{
		Vector3 movement = holePosition.sub(ballPosition);
		return movement;
	}
}
