#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform float Separation;
uniform float Strength;
uniform float VerticalSkew;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 pixel = vec2(1.0 / InSize.x, 1.0 / InSize.y);

    // Deliberately exaggerated, uneven displacement so the effect reads as
    // two supernatural echoes instead of ordinary chromatic aberration.
    vec2 coldOffset = vec2(
        pixel.x * Separation * 1.35,
        pixel.y * Separation * VerticalSkew
    );
    vec2 warmOffset = vec2(
        pixel.x * Separation * 1.05,
        -pixel.y * Separation * VerticalSkew * 0.78
    );

    vec4 center = texture(DiffuseSampler, texCoord);
    vec3 coldSample = texture(DiffuseSampler,
        clamp(texCoord - coldOffset, vec2(0.0), vec2(1.0))).rgb;
    vec3 warmSample = texture(DiffuseSampler,
        clamp(texCoord + warmOffset, vec2(0.0), vec2(1.0))).rgb;

    // Restore the deeper blue treatment from the previous version.
    vec3 coldGhost = coldSample * vec3(0.46, 0.82, 1.08);
    vec3 warmGhost = warmSample * vec3(1.10, 0.62, 0.43);

    float ghostOpacity = Strength * 0.43;
    vec3 color = center.rgb * (1.0 - ghostOpacity * 0.80);
    color += coldGhost * ghostOpacity;
    color += warmGhost * ghostOpacity * 0.88;

    // Reinforce the separated silhouettes while favoring the blue echo.
    vec3 coldDifference = abs(coldSample - center.rgb);
    vec3 warmDifference = abs(warmSample - center.rgb);
    color += coldDifference * vec3(0.08, 0.22, 0.40) * Strength;
    color += warmDifference * vec3(0.22, 0.08, 0.035) * Strength;

    float luminance = dot(color, vec3(0.299, 0.587, 0.114));
    color = mix(color, vec3(luminance), Strength * 0.06);
    color = (color - 0.5) * (1.0 + Strength * 0.13) + 0.5;
    color += vec3(-0.008, -0.003, 0.016) * Strength;

    fragColor = vec4(clamp(color, 0.0, 1.0), center.a);
}
