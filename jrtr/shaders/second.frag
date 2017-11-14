#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

// Variables passed in from the vertex shader
in vec4 frag_color;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{
	frag_shaded = frag_color;
}

