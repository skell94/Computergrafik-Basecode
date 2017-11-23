#version 150
// GLSL version 1.50
// Vertex shader for diffuse shading in combination with a texture map

// Uniform variables, passed in from host program via suitable
// variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform mat4 normalview;
uniform mat4 camera;
uniform vec4 lightPoint[8];
uniform vec4 lightDiffuse[8];
uniform vec4 materialDiffuse;
uniform int nLights;

// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects
in vec4 frag_normal;
in vec4 frag_position;
in vec4 frag_color;

// Output variables for fragment shader
out vec4 frag_shaded;

void main()
{
	float ndotl;
	frag_shaded = vec4(0,0,0,0);
	for(int i=0; i<nLights && i<8; ++i){
		// compute h vector
		vec4 lightDirection;
		lightDirection = normalize(camera * lightPoint[i] - frag_position);
		ndotl = max(dot(frag_normal, lightDirection),0);
		if(ndotl > 0.85){
			frag_shaded += 1 * materialDiffuse * lightDiffuse[i];
		} else if(ndotl > 0.5){
			frag_shaded += 0.6 * materialDiffuse * lightDiffuse[i];
		} else {
			frag_shaded += 0.25 * materialDiffuse * lightDiffuse[i];
		}
	}
}
