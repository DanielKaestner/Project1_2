package collisionDetector;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import MultiPlayer.GameScreenMultiPlayer;
import Obstacles.Hole;
import Obstacles.Obstacle;
import gameEngine3D.Golfball;
import physics.VectorComputation;

public class CollisionDetector {
	private static boolean inHole = false;
	private GameScreenMultiPlayer gameScreen;
	public CollisionDetector() {
	}
	public CollisionDetector(GameScreenMultiPlayer gsmp) {
		this.gameScreen = gsmp;
	}
	
	public boolean detectCollision(Golfball ball, Obstacle obstacle) {
		boolean intersects = false;	
		BoundingBox ballBoundingBox = ball.getBoundingBox();
		BoundingBox obstacleBoundingBox = obstacle.getBoundingBox();
		if (obstacle instanceof Hole) {
			if (determineHoleIntersection(ball, (Hole) obstacle)) {
				inHole = true;
				handleHoleCollision(ball, (Hole) obstacle);
			}
			else inHole = false;
		}

		else if (determineIntersection(ballBoundingBox, obstacleBoundingBox) && !inHole) handleCollision(ball, obstacleBoundingBox);
		


		return intersects;
	}
	public boolean detectCollision(Golfball ball, Golfball ball2) {
		boolean intersects = false;
		if(determineIntersection(ball.getBoundingBox(), ball2.getBoundingBox())) handleCollisionBall(ball, ball2);
		
		return intersects;
	}


	private void handleCollisionBall(Golfball ball, Golfball ball2) {
		Vector3 oldVeloBall1 = ball.getVelocity();
		Vector3 oldVeloBall2 = ball2.getVelocity();
		float a = ((ball.getMass()-ball2.getMass())*oldVeloBall1.x + 2* ball2.getMass() * oldVeloBall2.x) / (ball.getMass() + ball2.getMass());
		float b = ((ball.getMass()-ball2.getMass())*oldVeloBall1.z + 2* ball2.getMass() * oldVeloBall2.z) / (ball.getMass() + ball2.getMass());
		
		float c = ((ball2.getMass()-ball.getMass())*oldVeloBall2.x + 2* ball2.getMass() * oldVeloBall1.x) / (ball.getMass() + ball2.getMass());
		float d = ((ball2.getMass()-ball.getMass())*oldVeloBall2.z + 2* ball2.getMass() * oldVeloBall1.z) / (ball.getMass() + ball2.getMass());
		ball.setVelocity(new Vector3(a,0,b));
		ball2.setVelocity(new Vector3(c,0,d));
		handleCollision(ball, ball2.getBoundingBox());
	}

	/**
	 * This code may not work for rotated obstacles
	 * 
	 * @param ball
	 * @param obstacle
	 * @return
	 */
	public boolean determineIntersection(BoundingBox ball, BoundingBox obstacle) {
		Vector3 ballMin = ball.min, ballMax = ball.max, obstacleMin = obstacle.min, obstacleMax = obstacle.max;

		if (ballMin.x < obstacleMax.x && ballMax.x > obstacleMin.x && ballMin.y-0.5 < obstacleMax.y
				&& ballMax.y+0.5 > obstacleMin.y && ballMin.z < obstacleMax.z && ballMax.z > obstacleMin.z) {
			return true;
		}

		return false;
	}

	public boolean determineHoleIntersection(Golfball ball, Hole hole) {
		Vector3 holeCenter = new Vector3();
		hole.getBoundingBox().getCenter(holeCenter);
		float xz = VectorComputation.getInstance().getDistanceXZ(ball.getPosition(), holeCenter);

		if (xz < hole.getRadius()) {
			return true;
		}

		return false;
	}

	private void handleHoleCollision(Golfball ball, Hole hole) {
		Vector3 holeCenter = new Vector3();
		hole.getBoundingBox().getCenter(holeCenter);
		
		Vector3 velocity = holeCenter.sub(ball.getPosition());
		velocity.scl(0f);
		ball.setPosition(ball.getPosition().add(velocity));
		if(hole.getIndex() == ball.getIndex()) {
			//ball.setPosition(hole.getCenter());
			ball.setVelocity(new Vector3(0,0,0));
			if(gameScreen != null)
			gameScreen.removeBall(ball.getIndex());
		}
	}

	/**
	 * 
	 * @param ball
	 * @param obstacle
	 */
	private void handleCollision(Golfball ball, BoundingBox obstacle) {
		
		BoundingBox ballBox = ball.getBoundingBox();
		Vector3 ballMin = ballBox.min, ballMax = ballBox.max, obstacleMin = obstacle.min, obstacleMax = obstacle.max;
		Vector3 ballPosition = ball.getPosition();
		Vector3 reflectionAxis = new Vector3(1, 1, 1);

		boolean collisionX1 = ballMin.x-0.1f < obstacleMax.x && ballMax.x+0.1f > obstacleMax.x;
						
		// Collision with the x sides of an obstacle
		if (collisionX1) {
			reflectionAxis = new Vector3(-1, 1, 1);
			ballPosition.x = obstacleMax.x + ball.getRadius() + 0.1f;
		} else if (ballMax.x+0.1f > obstacleMin.x && ballMin.x-0.1f < obstacleMin.x) {
			reflectionAxis = new Vector3(-1, 1, 1);
			ballPosition.x = obstacleMin.x - ball.getRadius() - 0.1f;
		}
		// Collisions with the y side of an obstacle
		else if (ballMin.y+0.1f < obstacleMax.y && ballMax.y-0.1f > obstacleMax.y) {
			reflectionAxis = new Vector3(1, -1, 1);
			ballPosition.y = obstacleMax.y + ball.getRadius() + 0.1f;
		} else if (ballMax.y-0.1f > obstacleMin.y && ballMin.y+0.1f < obstacleMin.y) {
			reflectionAxis = new Vector3(1, -1, -1);
			ballPosition.y = obstacleMin.y - ball.getRadius() - 0.1f;
		}
		
		// Collisions with the z sides of an obstacle
		else if (ballMin.z-0.1f < obstacleMax.z && ballMax.z+0.1f > obstacleMax.z) {
			reflectionAxis = new Vector3(1, 1, -1);
			ballPosition.z = obstacleMax.z + ball.getRadius() + 0.1f;
		} else if (ballMax.z+0.1f > obstacleMin.z && ballMin.z-0.1f < obstacleMin.z) {
			reflectionAxis = new Vector3(1, 1, -1);
			ballPosition.z = obstacleMin.z - ball.getRadius() - 0.1f;
		}

		ball.bounceOff(reflectionAxis);
		ball.update();
	}
}
