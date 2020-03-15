package com.studios0110.libgdxbox2d;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;


/**
 * Created by Sam Merante on 2019-02-20.
 */
public class DrawnShape {
    private Polygon polygon;
    private PolygonShape polygonShape;
    World world;
    private Body body;
    private float[] vertices;
    float density;
    boolean debug = false;
    private final float PIXELS_TO_METERS = 64; //Divide by pixels for physics, multiply back for graphics translations

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

    public DrawnShape(World world, float[] vertices){
        this.world = world;
        polygon = new Polygon(vertices);
        this.vertices = new float[vertices.length];
        for(int i=0; i< vertices.length; i++){
            this.vertices[i] =  vertices[i]/PIXELS_TO_METERS;
        }
        body =  createBody(BodyDef.BodyType.DynamicBody, new Vector2(polygon.getOriginX(),polygon.getOriginY()));
        this.density = (polygon.getBoundingRectangle().area()/(PIXELS_TO_METERS*PIXELS_TO_METERS));
        if(debug) System.out.println("DENSITY: " + density);
        try {
            createFixtures();
        } catch (Exception e){
            if(debug) System.out.println("Exception thrown");
        }
    }

    private void createFixtures() throws Exception{
        try{
        int points = vertices.length/2;
        int separateShapes = (points/8); //How many separate shapes to have
        int leftOverPoints = (points % 8);

//        If not one full shape
        if(leftOverPoints > 0 && separateShapes == 0){
            float[] tempShapeVerts  = new float[this.vertices.length];
            for (int i=0; i < this.vertices.length; i++){
                tempShapeVerts[i] = this.vertices[i];
            }
            polygonShape = new PolygonShape();
            polygonShape.set(tempShapeVerts);
            createFixture(body,1f,0,0.1f,false,polygonShape);
            return;
        }

        //Split into even sets of 6 points
        separateShapes = (points/6);
        leftOverPoints = (points % 6);
        if(debug) System.out.println("summary of shape, full shapes: " + separateShapes + " , left over points: " + leftOverPoints + ", total points : " + points);

        int lastAddedVertex = 0;
        for(int i=0; i < separateShapes; i++){
            if (i == 0){
                float[] verts = new float[12];
                for(int j=0; j<12; j++){ //First 6 points
                    if(debug) System.out.println("Adding vertex point: " + j + ", total seperate shapes vertices: " + separateShapes * 12);
                    verts[j] = this.vertices[j];
                    lastAddedVertex++;
                }
                polygonShape = new PolygonShape();
                polygonShape.set(verts);
                createFixture(body,1f,0,this.density,false,polygonShape);
            }
            else { // Add 7 points, first being previous val
                float[] verts = new float[14];
                int prevPoint = lastAddedVertex-2;
                if(debug) System.out.println("Adding prev val vert[0] = vertex point: " + prevPoint + ", total seperate shapes vertices : " + separateShapes * 12);
                verts[0] = this.vertices[prevPoint++];
                if(debug) System.out.println("Adding prev  prev val vert[1] = vertex point: " + prevPoint + ", total seperate shapes vertices : " + separateShapes * 12);
                verts[1] = this.vertices[prevPoint];
                int totalVertsI = lastAddedVertex;
                for(int j=0; j<10; j++){ //Add 5 points
                    if(debug) System.out.println("verts["+(j+2)+"] = vertices[" + totalVertsI + "], total seperate shapes vertices : " + separateShapes * 12);
                    verts[j+2] = this.vertices[totalVertsI];
                    totalVertsI++;
                    lastAddedVertex++;
                }
                if(debug) System.out.println("verts[12] = vertices[" + lastAddedVertex + "], total seperate shapes vertices : " + separateShapes * 12);
                verts[12] = this.vertices[lastAddedVertex++];
                if(debug) System.out.println("verts[13] = vertices[" + lastAddedVertex + "], total seperate shapes vertices : " + separateShapes * 12);
                verts[13] = this.vertices[lastAddedVertex++];
                polygonShape = new PolygonShape();
                polygonShape.set(verts);
                createFixture(body,1f,0,this.density,false,polygonShape);
            }
        }

        int totalLeftOverVertices = leftOverPoints*2;
        float[] leftOverVertices = new float[totalLeftOverVertices + 4]; //2 before + total + 2 (first points)
        if(debug) System.out.println("Left over points: " + leftOverPoints + " Left over verts: "+ leftOverVertices.length);

        for(int i=0; i < totalLeftOverVertices + 1; i++){
            if(i ==0){
                int prevPoint = lastAddedVertex-2;
                if(debug) System.out.println("Adding prev val leftOverVertices[0] = vertex point: " + prevPoint + ", total : " + points * 2);
                leftOverVertices[0] = this.vertices[prevPoint++];
                if(debug) System.out.println("Adding prev val leftOverVertices[1] = vertex point: " + prevPoint + ", total : " + points * 2);
                leftOverVertices[1] = this.vertices[prevPoint++];
            }else{
                int leftOverVertexI = i+1;
                if(debug) System.out.println("Adding leftOverVertices["+leftOverVertexI+"] = vertices["+lastAddedVertex+"]");
                leftOverVertices[leftOverVertexI] = this.vertices[lastAddedVertex++];
            }
            if(i == totalLeftOverVertices){
                if(debug) System.out.println("Adding lastVerts["+(totalLeftOverVertices+2)+"] : vert[0]");
                leftOverVertices[totalLeftOverVertices+2] = this.vertices[0];
                if(debug) System.out.println("Adding lastVerts["+(totalLeftOverVertices+3)+"] : vert[1]");
                leftOverVertices[totalLeftOverVertices+3] = this.vertices[1];

            }
        }
        if(leftOverPoints > 0){
            polygonShape = new PolygonShape();
            polygonShape.set(leftOverVertices);
            createFixture(body,1f,0,this.density,false,polygonShape);
        }

        }
        catch(Exception e){
            throw new Exception(e);
        }
    }
    public void draw(ShapeRenderer shapes) {
        polygon.setOrigin(0,0);
        polygon.setRotation((float)Math.toDegrees(body.getAngle()));
        polygon.setPosition(body.getPosition().x*PIXELS_TO_METERS, body.getPosition().y*PIXELS_TO_METERS);
        shapes.polygon(polygon.getTransformedVertices());
    }
}
