#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map
uniform sampler2D myTexture;
uniform vec4 lightDiffuse[8];
uniform vec4 lightSpecular[8];
uniform vec4 lightAmbient[8];
uniform int nLights;

// Variables passed in from the vertex shader
in float ndotl[8];
in float ndothpows[8];
in vec2 frag_texcoord;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{
	vec4 tex;
	tex = texture(myTexture, frag_texcoord);
	frag_shaded = vec4(0,0,0,0);
	for(int i=0; i<nLights && i < 8; ++i){
		frag_shaded += ndotl[i] * tex * lightDiffuse[i] + ndothpows[i] * (tex.x + tex.y + tex.z) * lightSpecular[i] + tex * lightAmbient[i];
	}
}

