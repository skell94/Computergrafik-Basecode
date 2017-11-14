#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map
uniform sampler2D myTexture;
uniform vec4 lightDiffuse[8];
uniform vec4 materialDiffuse;
uniform int nLights;

// Variables passed in from the vertex shader
in float ndotl[8];
in vec4 frag_color;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{
	frag_shaded = vec4(0,0,0,0);
	for(int i=0; i<nLights && i < 8; ++i){
		if(ndotl[i] > 0.65){
			frag_shaded += 1 * materialDiffuse * lightDiffuse[i];
		} else if(ndotl[i] > 0.3){
			frag_shaded += 0.6 * materialDiffuse * lightDiffuse[i];
		} else {
			frag_shaded += 0.25 * materialDiffuse * lightDiffuse[i];
		}
	}
}

