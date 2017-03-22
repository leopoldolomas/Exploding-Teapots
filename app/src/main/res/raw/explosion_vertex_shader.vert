uniform mat4 uModelViewMatrix;
uniform mat4 uCameraMatrix;
uniform mat4 uProjectionMatrix;
uniform float time;

attribute vec4 vPosition;
attribute vec4 vNormal;

void main() {
     vec4 dir = vNormal * time;
     vec4 vertexPosition = uCameraMatrix * uModelViewMatrix * (vPosition + dir);
     gl_Position = uProjectionMatrix * vertexPosition;
}
