#ifdef GL_ES
#define P_LOW lowp
#define P_MED mediump
#define P_HIGH highp
precision P_MED float;
precision P_MED int;
#else
#define P_MED
#define P_LOW
#define P_HIGH
#endif

//"in" attributes from our vertex shader
varying vec4 v_color;
varying vec2 v_texCoords;

//declare uniforms
uniform sampler2D u_texture;

uniform P_MED vec2 size;
uniform int samples;
uniform float quality; // lower = smaller glow, better quality
uniform float intensity;

void main(){
  vec4 source = texture2D(u_texture, v_texCoords);
  vec4 sum = vec4(0);
  int diff = (samples - 1) / 2;
  vec2 sizeFactor = vec2(1) / size * quality;
  if (source.a == float(0)){
    // do nothing if transparent
    gl_FragColor = source;
  }else{
    for(int x = -diff; x <= diff; x++){
      for (int y = -diff; y <= diff; y++){
        vec2 offset = vec2(x, y) * sizeFactor;
        sum += texture2D(u_texture, v_texCoords + offset);
      }
    }
    gl_FragColor = ((sum / float(samples * samples)) + source) * intensity;
  }
}
