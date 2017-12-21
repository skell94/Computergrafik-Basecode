#version 150
// GLSL version 1.50 
// Vertex shader for diffuse shading in combination with a texture map

// Uniform variables, passed in from host program via suitable 
// variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform mat4 normalview;
uniform mat4 camera;
uniform vec4 materialDiffuse;
uniform vec4 lightPoint[8];
uniform vec4 lightDiffuse[8];
uniform int nLights;

// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects
in vec3 normal;
in vec4 position;

// Output variables for fragment shader
out vec4 frag_position;
out vec4 frag_normal;

void main()
{		
	// Compute dot product of normal and light direction
	// and pass color to fragment shader
	// Note: here we assume "lightDirection" is specified in camera coordinates,
	// so we transform the normal to camera coordinates, and we don't transform
	// the light direction, i.e., it stays in camera coordinates
	frag_position = modelview * position;
	frag_normal = normalize(modelview * vec4(normal, 0));

	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	gl_Position = projection * modelview * position;
}
