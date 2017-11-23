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
uniform vec4 materialSpecular;
uniform float materialShininess;
uniform vec4 lightPoint[8];
uniform vec4 lightDiffuse[8];
uniform vec4 lightSpecular[8];
uniform int nLights;

// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects
in vec4 position;
in vec3 normal;
in vec4 color;

out vec4 frag_position;
out vec4 frag_normal;
out vec4 frag_color;

void main()
{
	frag_position = modelview * position;
	frag_normal = normalview * vec4(normal, 0);
	frag_color = color;

	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	gl_Position = projection * modelview * position;
}
