package com.example.rubikrobot.Camera;

public class KnnNode {
    private float x; //X坐标
    private float y; //Y坐标
    private Float distance; //目标节点到此节点的距离
    private String type; //所属类别

    public KnnNode(float x, float y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Node{" +
                "x=" + x +
                ", y=" + y +
                ", distance=" + distance +
                ", type='" + type + '\'' +
                '}';
    }

}