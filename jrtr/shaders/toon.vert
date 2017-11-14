#version 150
// GLSL version 1.50
// Vertex shader for diffuse shading in combination with a texture map

// Uniform variables, passed in from host program via suitable
// variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform mat4 camera;
uniform vec4 cameraPoint;
uniform vec4 lightPoint;
uniform int nLights;

// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects
in vec3 normal;
in vec4 position;
in vec4 color;

// Output variables for fragment shader
out float ndotl[8];
out vec4 frag_color;

void main()
{
	for(int i=0; i<nLights && i<8; ++i){
		// compute h vector
		vec4 lightDirection;
		lightDirection = normalize(lightPoint[i] - position);
		ndotl[i] = max(dot(transpose(inverse(modelview)) * vec4(normal,0), camera * -lightDirection),0);
	}

	frag_color = color;

	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	gl_Position = projection * modelview * position;
}
