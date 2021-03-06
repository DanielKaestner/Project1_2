package gameEngine3D;

import java.util.Collection;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;

import Obstacles.Obstacle;
import physics.VectorComputation;

public class InputListener implements InputProcessor {

	GameScreen3D gameScreen3D;
	LineIndicator lineIndicator;
	boolean initialize = true;

	public InputListener(GameScreen3D gameScreen3D, LineIndicator lineIndicator) {
		this.gameScreen3D = gameScreen3D;
		this.lineIndicator = lineIndicator;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Vector3 mousePosition = gameScreen3D.getWorldCoords();
		Golfball ball = gameScreen3D.getGolfball();
		Vector3 directionVector = ball.getPosition().sub(mousePosition);

		float stength = VectorComputation.getInstance().getDistance(mousePosition, ball.getPosition())/100;
		System.out.println("STRENGTH " + stength);
		directionVector.nor();

		if (!initialize && ball.getVelocity().isZero()) {
			directionVector.y = 0;

				Collection<Obstacle> obstaclePath = gameScreen3D.getAi().makeMove();
				gameScreen3D.addObstacles(obstaclePath);
				
//				gameScreen3D.getAi().makeMove();
		}
		initialize = false;
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
