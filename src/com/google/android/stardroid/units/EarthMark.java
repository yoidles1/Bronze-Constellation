package com.google.android.stardroid.units;

import java.util.Date;

import com.google.android.stardroid.util.Geometry;

public class EarthMark{
private String name;
private LatLong latlong;
private long starttime = 0;
private double angle = 0;
private double radius = 0;

public EarthMark(String name, LatLong latlong) {
  this.setName(name);
  this.setLatlong(latlong);
}

public Float getLatitude() {
	return latlong.getLatitude();
}

public Float getLongitude() {
	return latlong.getLongitude();
}

public GeocentricCoordinates findPointOnUnitSphereToViewMark(Date utc, EarthMark mark) {
	LatLong fromLatLong = this.latlong;
	LatLong toLatLong = mark.getLatlong();
	RaDec fromRaDec = Geometry.calculateRADecOfZenith(utc, fromLatLong);
	RaDec toRaDec = Geometry.calculateRADecOfZenith(utc, toLatLong);
	Vector3 fromGeo = GeocentricCoordinates.getInstance(fromRaDec);
	Vector3 toGeo = GeocentricCoordinates.getInstance(toRaDec);
	Vector3 minusFromGeo = Geometry.scaleVector(fromGeo, -1);
//	Vector3 diff = Geometry.addVectors(toGeo, minusFromGeo);
	Vector3 diff = Geometry.addVectors(toGeo,  Geometry.scaleVector(minusFromGeo,(float) -1.5)); //Should put the viewport outside the earth 2x diameter opposite of the North West hemisphere
	diff.normalize();
//	Log.d("RLP", "RLP normalized diff" + diff.toString());
	mark.setStarttime(System.currentTimeMillis());
	mark.setAngle(Math.atan2(diff.x, diff.y));
	mark.setRadius(Math.pow(diff.x*diff.x + diff.y*diff.y, .5));
	return GeocentricCoordinates.getInstanceFromVector3(diff);
}

public String toString() {
	return name + " " + Float.toString(latlong.latitude) + " " + Float.toString(latlong.longitude);
}

public LatLong getLatlong() {
	return latlong;
}

public void setLatlong(LatLong latlong) {
	this.latlong = latlong;
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public long getStarttime() {
	return starttime;
}

public void setStarttime(long starttime) {
	this.starttime = starttime;
}

public double getAngle() {
	return angle;
}

public void setAngle(double angle) {
	this.angle = angle;
}

public double getRadius() {
	return radius;
}

public void setRadius(double radius) {
	this.radius = radius;
}

}
