#version 150
// GLSL version 1.50
// Vertex shader for diffuse shading in combination with a texture map

// Uniform variables, passed in from host program via suitable
// variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform mat4 camera;
uniform vec4 cameraPoint;
uniform vec4 materialDiffuse;
uniform vec4 materialSpecular;
uniform float materialShininess;
uniform vec4 lightPoint[8];
uniform vec4 lightDiffuse[8];
uniform vec4 lightSpecular[8];
uniform int nLights;

// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects
in vec3 normal;
in vec4 position;

// Output variables for fragment shader
out vec4 frag_color;

void main()
{
	frag_color = vec4(0,0,0,0);
	for(int i=0; i<nLights || i<8; ++i){
		// compute h vector
		vec4 e, h, lightDirection;
		lightDirection = lightPoint[i] - position;
		e = cameraPoint - position;
		h = (lightDirection + e)/length(lightDirection + e);
		frag_color += max(dot(transpose(inverse(modelview)) * vec4(normal,0), camera * lightDirection),0) * materialDiffuse * lightDiffuse[i] + pow(max(dot(transpose(inverse(modelview)) * vec4(normal, 0), camera * h),0), materialShininess) * materialSpecular * lightSpecular[i];
	}

	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	gl_Position = projection * modelview * position;
}
