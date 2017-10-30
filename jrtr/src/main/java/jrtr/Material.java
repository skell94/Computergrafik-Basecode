package jrtr;

import java.io.IOException;

import javax.vecmath.*;

import jrtr.swrenderer.SWTexture;

/**
 * Stores the properties of a material.
 */
public class Material {

	// Material properties
	public Texture diffuseMap, normalMap, specularMap, ambientMap, alphaMap;
	public Vector3f diffuse;
	public Vector3f specular;
	public Vector3f ambient;
	public float shininess;
	public Shader shader;
	public Texture texture;
	
	public Material()
	{
		diffuse = new Vector3f(1.f, 1.f, 1.f);
		specular = new Vector3f(1.f, 1.f, 1.f);
		ambient = new Vector3f(1.f, 1.f, 1.f);
		shininess = 1.f;
		diffuseMap = null;
		normalMap = null;
		specularMap = null;
		ambientMap = null;
		alphaMap = null;
		shader = null;
		texture = null;
	}
	
	public void setTexture(String filename) {
		this.texture = new SWTexture();
		try {
			texture.load(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
