#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map
uniform mat4 projection;
uniform mat4 modelview;
uniform mat4 normalview;
uniform mat4 camera;
uniform vec4 materialDiffuse;
uniform vec4 lightPoint[8];
uniform vec4 lightDiffuse[8];
uniform int nLights;

// Variables passed in from the vertex shader
in vec4 frag_position;
in vec4 frag_normal;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{		
	frag_shaded = vec4(0,0,0,0);
	for(int i=0; i<nLights && i<8; ++i){
		frag_shaded += max(dot(normalize(frag_normal), normalize(camera * lightPoint[i] - frag_position)),0) * materialDiffuse * lightDiffuse[i];
	}
}

