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
in vec4 frag_normal;
in vec4 frag_position;

// Output variables for fragment shader
out vec4 frag_shaded;

void main()
{
	frag_shaded = vec4(0,0,0,0);
	for(int i=0; i<nLights && i<8; ++i){
		// compute h vector
		vec4 e, h, lightDirection;
		lightDirection = normalize(camera * lightPoint[i] - frag_position);
		e = -frag_position;
		h = (lightDirection + e)/length(lightDirection + e);
		frag_shaded += max(dot(frag_normal, lightDirection),0) * materialDiffuse * lightDiffuse[i] + pow(max(dot(frag_normal, h),0), materialShininess) * materialSpecular * lightSpecular[i];
	}
}
