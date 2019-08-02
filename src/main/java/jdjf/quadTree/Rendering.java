package jdjf.quadTree;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.HashSet;

public class Rendering extends Application {
    private static OcTree octTree;

    final static int AXIS_LENGTH = 10000;
    final static float AXIS_THICKNESS = 1;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setResizable(false);

        // Axis
        Sphere origin = new Sphere(AXIS_THICKNESS * 2);
        origin.setMaterial(new PhongMaterial(Color.WHITE));

        Box xAxis = new Box(AXIS_LENGTH, AXIS_THICKNESS, AXIS_THICKNESS);
        xAxis.getTransforms().add(new Translate(AXIS_LENGTH / 2, 0, 0));
        xAxis.setMaterial(new PhongMaterial(Color.RED));

        Box yAxis = new Box(AXIS_THICKNESS, AXIS_LENGTH, AXIS_THICKNESS);
        yAxis.getTransforms().add(new Translate(0, AXIS_LENGTH / 2, 0));
        yAxis.setMaterial(new PhongMaterial(Color.GREEN));

        Box zAxis = new Box(AXIS_THICKNESS, AXIS_THICKNESS, AXIS_LENGTH);
        zAxis.getTransforms().add(new Translate(0, 0, AXIS_LENGTH / 2));
        zAxis.setMaterial(new PhongMaterial(Color.BLUE));



        // Build the Scene Graph
        Group root = new Group();
        root.getChildren().add(origin);
        root.getChildren().add(xAxis);
        root.getChildren().add(yAxis);
        root.getChildren().add(zAxis);
        root.getChildren().add(octTreeToNode());


        Scene scene = new Scene(root, 600, 600, true);
        scene.setFill(Color.ALICEBLUE);
        scene.setCamera(makeCamera(scene));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Camera makeCamera(Scene scene)
    {
        Camera camera = new PerspectiveCamera(true);
        camera.setFarClip(500);

        Rotate tilt;
        Rotate pan;
        Translate position = new Translate();



        camera.getTransforms().addAll(
                position,
                tilt = new Rotate(0, Rotate.X_AXIS),
                pan = new Rotate(0, Rotate.Y_AXIS)
        );



        HashSet<KeyCode> pressedKeys = new HashSet<>();

        scene.addEventHandler(KeyEvent.KEY_PRESSED, key -> pressedKeys.add(key.getCode()));
        scene.addEventHandler(KeyEvent.KEY_RELEASED, key -> pressedKeys.remove(key.getCode()));

        scene.addEventHandler(KeyEvent.ANY, key ->
        {
            Translate deltaTranslate = new Translate();

            final double moveStep = 1;

            if (pressedKeys.contains(KeyCode.W))
            {
                 deltaTranslate.setZ(moveStep);
            }
            else if (pressedKeys.contains(KeyCode.S))
            {
                deltaTranslate.setZ(-moveStep);
            }

            if (pressedKeys.contains(KeyCode.D))
            {
                deltaTranslate.setX(moveStep);
            }
            else if (pressedKeys.contains(KeyCode.A))
            {
                deltaTranslate.setX(-moveStep);
            }

            if (pressedKeys.contains(KeyCode.E))
            {
                deltaTranslate.setY(moveStep);
            }
            else if (pressedKeys.contains(KeyCode.Q))
            {
                deltaTranslate.setY(-moveStep);
            }


//            position.()

//            camera.getTransforms().add(deltaTranslate);

//            position.deltaTransform(deltaTranslate);


//            if (pressedKeys.contains(KeyCode.ESCAPE))
//            {
//                camera.getTransforms().clear();
//            }
        });



        tilt.angleProperty().bind(angleX);
        pan.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngleY + anchorX - event.getSceneX());
        });

        return camera;
    }

    //Tracks drag starting point for x and y
    private double anchorX, anchorY;
    //Keep track of current angle for x and y
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    //We will update these after drag. Using JavaFX property to bind with object
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    private Node octTreeToNode()
    {
        Group group = new Group();

        nodenode(octTree.getRoot(), group, octTree.getSize(), 0, 0, 0);

        return group;
    }

    private void nodenode(OTNode node, Group group, int size, int minX, int minY, int minZ)
    {
        final int halfSize = size / 2;
        final int midX = minX + halfSize;
        final int midY = minY + halfSize;
        final int midZ = minZ + halfSize;

        if (node.isDivided())
        {


            nodenode(node.lnw, group, halfSize, minX, minY, minZ);
            nodenode(node.lne, group, halfSize, midX, minY, minZ);
            nodenode(node.lsw, group, halfSize, minX, midY, minZ);
            nodenode(node.lse, group, halfSize, midX, midY, minZ);
            nodenode(node.unw, group, halfSize, minX, minY, midZ);
            nodenode(node.une, group, halfSize, midX, minY, midZ);
            nodenode(node.usw, group, halfSize, minX, midY, midZ);
            nodenode(node.use, group, halfSize, midX, midY, midZ);
        }
        else if(node.coloured)
        {
            Sphere leaf = new Sphere(size / 4f);
            leaf.setMaterial(new PhongMaterial(new Color(1,0,0,1)));
            leaf.getTransforms().add(new Translate(midX, midY, midZ));
            group.getChildren().add(leaf);
        }

//        Box chef = new Box(size, size, size);
//        chef.setDrawMode(DrawMode.LINE);
//        chef.setMaterial(new PhongMaterial(new Color(1, 0, 0, 0.1f)));
//        chef.getTransforms().add(new Translate(midX, midY, midZ));
//        group.getChildren().add(chef);
    }

    /**
     * Java main for when running without JavaFX launcher
     */
    public static void main(String[] args) {
        final int size = 30;
        final int halfSize = size / 2;
        final int r = 10;
        octTree = new OcTree(size, size, size);

        for (int z = -r; z < r; z++)
        {
            for (int y = -r; y < r; y++)
            {
                for (int x = -r; x < r; x++)
                {
                    if(x * x + y * y + z * z < r * r) octTree.add(halfSize + x, halfSize + y, halfSize + z);
                }
            }
        }

        octTree.optimise();

        launch(args);
    }
}