package gameEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Circle;

public class Golfball {

	private Circle ballShape;
	private Texture ballImage;
	private float mass;
	public Golfball() {
		
		Pixmap pixmap = new Pixmap( 64, 64, Format.RGBA8888 );
		pixmap.setColor(Color.RED);
		pixmap.fillCircle( 32, 32, 32 );
		
		ballImage = new Texture(pixmap);
		
		ballShape = new Circle();
		ballShape.x = 100;
		ballShape.y = 100;
		ballShape.radius = 32;
		mass = 1;
	}
	public float getMass() {
		return mass;
	}
	
	public Circle getCircle() {
		return ballShape;
	}
	
	public Texture getSprite() {
		return ballImage;
	}
}