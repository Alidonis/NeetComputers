#version 150

// NeetComputers screen shader.
//
// Replaces the old per-rectangle rendering (which suffered from cracks,
// missing pixels and half-drawn pixels caused by floating point seams,
// rasterization rules and block-atlas mipmaps).
//
// Instead the screen content is uploaded once as a 1:1 texture and the GPU
// performs the scaling. The filter below emulates the requested pipeline:
//   1. nearest-neighbor upscale of the source image by 3x (virtual),
//   2. bilinear downscale of that virtual image to the drawn quad size.
//
// This keeps pixels crisp when magnified (no blurry half-pixels) while still
// blending smoothly when minified, and a single quad can never have seams.
// Implemented fully in-shader via texelFetch so no 3x CPU-side allocation
// is needed and filtering is identical in-world and in GUIs.

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

vec4 fetchVirtual(ivec2 v, ivec2 texSize, vec2 virtualSize) {
    // Clamp the virtual texel into the virtual image, then map to the
    // source texel with integer division by 3 (each source pixel covers
    // a 3x3 block of virtual texels = nearest-neighbor upscale).
    v = clamp(v, ivec2(0), ivec2(virtualSize) - ivec2(1));
    ivec2 src = v / 3;
    src = clamp(src, ivec2(0), texSize - ivec2(1));
    return texelFetch(Sampler0, src, 0);
}

void main() {
    ivec2 texSize = textureSize(Sampler0, 0);
    if (texSize.x <= 0 || texSize.y <= 0) {
        discard;
    }

    vec2 virtualSize = vec2(texSize) * 3.0;

    // Position in the virtual (3x) image, texel-centered for bilinear.
    vec2 p = texCoord0 * virtualSize - 0.5;
    vec2 i = floor(p);
    vec2 f = fract(p);

    ivec2 v00 = ivec2(i);
    ivec2 v10 = v00 + ivec2(1, 0);
    ivec2 v01 = v00 + ivec2(0, 1);
    ivec2 v11 = v00 + ivec2(1, 1);

    vec4 c00 = fetchVirtual(v00, texSize, virtualSize);
    vec4 c10 = fetchVirtual(v10, texSize, virtualSize);
    vec4 c01 = fetchVirtual(v01, texSize, virtualSize);
    vec4 c11 = fetchVirtual(v11, texSize, virtualSize);

    // Bilinear blend of the virtual image = smooth downscale.
    vec4 c0 = mix(c00, c10, f.x);
    vec4 c1 = mix(c01, c11, f.x);
    vec4 color = mix(c0, c1, f.y);

    fragColor = color * vertexColor * ColorModulator;
}
