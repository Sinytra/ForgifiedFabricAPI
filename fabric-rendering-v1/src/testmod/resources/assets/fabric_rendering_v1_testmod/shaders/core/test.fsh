#version 150

#moj_import <fabric_rendering_v1_testmod:test_include.glsl>

uniform vec4 ColorModulator;
out vec4 fragColor;

void main() {
    fragColor = applyColor(vec4(1.0, 1.0, 1.0, 1.0), ColorModulator);
}
