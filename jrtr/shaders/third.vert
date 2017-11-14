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
in vec2 texcoord;

// Output variables for fragment shader
out float ndotl[8];
out float ndothpows[8];
out vec2 frag_texcoord;

void main()
{
	for(int i=0; i<nLights && i<8; ++i){
		// compute h vector
		vec4 e, h, lightDirection;
		lightDirection = normalize(lightPoint[i] - position);
		e = cameraPoint - position;
		h = (lightDirection + e)/length(lightDirection + e);
		ndotl[i] = max(dot(transpose(inverse(modelview)) * vec4(normal,0), camera * lightDirection),0);
		ndothpows[i] = pow(max(dot(transpose(inverse(modelview)) * vec4(normal, 0), camera * h),0), materialShininess);
	}

	// Pass texture coordiantes to fragment shader, OpenGL automatically
	// interpolates them to each pixel  (in a perspectively correct manner)
	frag_texcoord = texcoord;

	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	gl_Position = projection * modelview * position;
}
