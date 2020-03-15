package com.studios0110.libgdxbox2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.PulleyJointDef;

import java.util.ArrayList;

import box2dLight.PointLight;
import box2dLight.RayHandler;

public class Source extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	ShapeRenderer shapes;
	Sprite sprite;
	Texture img;
	World world;
	RayHandler rayHandler;
	Body circle;
	Box2DDebugRenderer debugRenderer;
	Matrix4 debugMatrix;
	OrthographicCamera camera;
	boolean left, right, isDrawing, drawnBody;
	ArrayList<Vector2> drawingPoints;
	final float PIXELS_TO_METERS = 64; //Divide by pixels for physics, multiply back for graphics translations
	DrawnShape drawnShape;
	private Body createBody(BodyDef.BodyType type, Vector2 pos){
		BodyDef bodyDefinition = new BodyDef();
		bodyDefinition.type = type;
		bodyDefinition.position.set( pos.x / PIXELS_TO_METERS, pos.y / PIXELS_TO_METERS);
		return world.createBody(bodyDefinition);
	}

	private void createFixture(Body body, float friction, float restitution, float density, boolean isSensor, Shape shape){
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = friction;
		fixtureDef.restitution = restitution;
		fixtureDef.density = density;
		fixtureDef.isSensor = isSensor;
		fixtureDef.shape = shape;
		body.createFixture(fixtureDef);
		shape.dispose();
	}

	private PolygonShape createPolyShape(float[] verticies){
		PolygonShape polygonShape = new PolygonShape();
		for(int i=0; i < verticies.length; i++){
			verticies[i] = verticies[i]/PIXELS_TO_METERS;
		}
		polygonShape.set(verticies);
		return polygonShape;
	}

	@Override
	public void create() {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		camera.setToOrtho(false, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		batch = new SpriteBatch();
		shapes = new ShapeRenderer();
		img = new Texture("SmileBall.png");
		sprite = new Sprite(img);
		sprite.setPosition(150 - sprite.getWidth()/2,450 - sprite.getHeight()/2);

		world = new World(new Vector2(0, -9.8f),true);
		rayHandler = new RayHandler(world);
		rayHandler.setAmbientLight(0.1f, 0.1f, 0.1f, 0.5f);
		rayHandler.setBlurNum(3);
        new PointLight(rayHandler, 500,Color.WHITE, 1920.0f/PIXELS_TO_METERS,960.0f/PIXELS_TO_METERS,900.0f/PIXELS_TO_METERS);

		circle = createBody(BodyDef.BodyType.DynamicBody, new Vector2((sprite.getX() + sprite.getWidth()/2),(sprite.getY() + sprite.getHeight()/2)));
		Body floor = createBody(BodyDef.BodyType.StaticBody, new Vector2(50f, 0));
		Body triangle = createBody(BodyDef.BodyType.DynamicBody, new Vector2(900, 500));
		Body leftSquare = createBody(BodyDef.BodyType.DynamicBody,new Vector2(50,250));
		Body rightSquare = createBody(BodyDef.BodyType.DynamicBody,new Vector2(250,250));

		CircleShape circleShape = new CircleShape();
		circleShape.setRadius((sprite.getWidth()/2) / PIXELS_TO_METERS);
		createFixture(circle,1f,0,1f,false,circleShape);


		PolygonShape floorShape = createPolyShape(new float[]{0f,0f, 0f,150f, 1800f,150f, 1800f,0f});
		createFixture(floor,1f,0,1f,false,floorShape);

		PolygonShape boxShape = createPolyShape(new float[]{0f,0f, 50f,350f, 100f,0f});
		createFixture(triangle,1f,0,0.5f,false,boxShape);

		createFixture(leftSquare,1f,0,3.1f,false,createPolyShape(new float[]{0f,0f, 0f,50f, 50f,50f, 50f,0f}));
		createFixture(rightSquare,1f,0,1f,false,createPolyShape(new float[]{0f,0f, 0f,50f, 150f,50f, 150f,0f}));


		JointDef jointDef = new PulleyJointDef();
		float ratio = 1.0f;
		((PulleyJointDef) jointDef).initialize(leftSquare,rightSquare,
				new Vector2(75f/PIXELS_TO_METERS,500f/PIXELS_TO_METERS),
				new Vector2(325f/PIXELS_TO_METERS,500f/PIXELS_TO_METERS),
				new Vector2(75f/PIXELS_TO_METERS,300f/PIXELS_TO_METERS),
				new Vector2(325f/PIXELS_TO_METERS,300f/PIXELS_TO_METERS),
				ratio);

		world.createJoint(jointDef);

        world.setContactListener(new ContactListener() {
            public void beginContact(Contact contact) {
                if(contact.getFixtureA().getBody() == circle || contact.getFixtureB().getBody() == circle){
                    System.out.println("Circle collided : ");
                }
            }

            public void endContact(Contact contact) {
                if(contact.getFixtureA().getBody() == circle || contact.getFixtureB().getBody() == circle){
                    System.out.println("Circle collided ended colliding: ");
                }
            }

            public void preSolve(Contact contact, Manifold oldManifold) {
//                System.out.println("preSolve: " + contact + "  :  " + oldManifold);
            }

            public void postSolve(Contact contact, ContactImpulse impulse) {
//                System.out.println("postSolve: " + contact + " : " + impulse);
            }
        });


		debugRenderer = new Box2DDebugRenderer();
		Gdx.input.setInputProcessor(this);

		drawingPoints = new ArrayList<Vector2>();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(1, 0, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		// Scale down the sprite batches projection matrix to box2D size
		debugMatrix = batch.getProjectionMatrix().cpy().scale(PIXELS_TO_METERS, PIXELS_TO_METERS, 0);
		debugRenderer.render(world, debugMatrix);


		// Multiple by pixels for graphics
		sprite.setPosition((circle.getPosition().x * PIXELS_TO_METERS) - sprite.getWidth()/2 ,
						   (circle.getPosition().y * PIXELS_TO_METERS) - sprite.getHeight()/2 );

		sprite.setRotation(circle.getAngle() * PIXELS_TO_METERS);

		batch.setProjectionMatrix(camera.combined);
		shapes.setProjectionMatrix(camera.combined);

		shapes.setColor(Color.BLACK);
		shapes.setAutoShapeType(true);
		shapes.begin();
			shapes.set(ShapeRenderer.ShapeType.Filled);
			if(drawingPoints.size() > 3){
				float[] vertecies = new float[drawingPoints.size()*2];
				int ithPoint=0;
				for(int i=0; i < drawingPoints.size(); i++){
					vertecies[ithPoint] = drawingPoints.get(i).x;
					ithPoint++;
					vertecies[ithPoint] = drawingPoints.get(i).y;
					ithPoint++;
				}
				if(isDrawing){
					shapes.polyline(vertecies);
				}else{
					if(!this.drawnBody){
						drawnShape = new DrawnShape(world,vertecies);
					}
					drawnShape.draw(shapes);
					this.drawnBody = true;
				}
			}
		shapes.end();
		batch.begin();
			sprite.draw(batch);
		batch.end();

		world.step(Gdx.graphics.getDeltaTime(), 6, 2);
		camera.position.x = circle.getPosition().x * PIXELS_TO_METERS;
		camera.update();
		updateMovement();
        rayHandler.setCombinedMatrix(debugMatrix,0,0,0,0);
        rayHandler.updateAndRender();
	}

	@Override
	public void dispose() {
		img.dispose();
		world.dispose();
	}

	private void updateMovement(){
		if(right){
			if(circle.getLinearVelocity().x < 5f){
				circle.applyForceToCenter(new Vector2(10f,0),true);
			}
		}
		if(left){
			if(circle.getLinearVelocity().x > -5f){
				circle.applyForceToCenter(new Vector2(-10f,0),true);
			}
		}
	}
	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Input.Keys.D){
			right = true;
		}
		if(keycode == Input.Keys.A){
			left = true;
		}
		if(keycode == Input.Keys.W){
			circle.applyForceToCenter(new Vector2(0f,50f),true);
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if(keycode == Input.Keys.D){
			right = false;
		}
		if(keycode == Input.Keys.A){
			left = false;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		isDrawing = true;
		drawingPoints.clear();
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		isDrawing = false;
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		Vector3 touchPos = camera.unproject(new Vector3(screenX, screenY, 0));
		drawnBody = false;
		if(isDrawing){
			if(drawingPoints.isEmpty()){
				drawingPoints.add(new Vector2(touchPos.x,touchPos.y));
			}else{
				float a = drawingPoints.get(drawingPoints.size()-1).x - touchPos.x;
				float b = drawingPoints.get(drawingPoints.size()-1).y - touchPos.y;
				float distance = (float)(Math.sqrt(a*a + b*b));
				if(distance >= 10){
					drawingPoints.add(new Vector2(touchPos.x,touchPos.y));
				}
			}
		}
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
