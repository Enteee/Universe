//combined projection and view matrixuniform mat4 u_projTrans;
uniform mat4 u_projTrans;

//"in" attributes from our SpriteBatch
attribute vec2 Position;
attribute vec2 TexCoord;
attribute vec4 Color;

//"out" varyings to our fragment shader
varying vec4 vColor;
varying vec2 vTexCoord;

void main() {
    vColor = Color;
    vTexCoord = TexCoord;
    gl_Position = u_projTrans * vec4(Position, 0.0, 1.0);
}