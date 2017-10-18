#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

// Variables passed in from the vertex shader
in float ndotl;
in vec4 frag_color;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{		
	// The built-in GLSL function "texture" performs the texture lookup
	frag_shaded = (0.4+ndotl) * frag_color;
}

