#version 300 es



precision highp float;
precision highp sampler2D;

out vec4 out_color;
in vec2 uv;

uniform sampler2D u_texture_0;
uniform sampler2D u_texture_1;
uniform sampler2D u_texture_2;
uniform sampler2D u_texture_fail;

uniform vec2 u_waveStartCell;
uniform float u_waveStart;
uniform vec4 u_waveColor;

uniform float u_failStart;

uniform vec4 u_destroyStartCells;
uniform float u_destroyFadeTime;
uniform float u_destroyStart;

uniform vec2 u_resolution;
uniform float u_time;
uniform sampler2D u_textures[16];


float psrdnoise(vec2 x, vec2 period, float alpha, out vec2 gradient) {

    // Transform to simplex space (axis-aligned hexagonal grid)
    vec2 uv = vec2(x.x + x.y*0.5, x.y);

    // Determine which simplex we're in, with i0 being the "base"
    vec2 i0 = floor(uv);
    vec2 f0 = fract(uv);
    // o1 is the offset in simplex space to the second corner
    float cmp = step(f0.y, f0.x);
    vec2 o1 = vec2(cmp, 1.0-cmp);

    // Enumerate the remaining simplex corners
    vec2 i1 = i0 + o1;
    vec2 i2 = i0 + vec2(1.0, 1.0);

    // Transform corners back to texture space
    vec2 v0 = vec2(i0.x - i0.y * 0.5, i0.y);
    vec2 v1 = vec2(v0.x + o1.x - o1.y * 0.5, v0.y + o1.y);
    vec2 v2 = vec2(v0.x + 0.5, v0.y + 1.0);

    // Compute vectors from v to each of the simplex corners
    vec2 x0 = x - v0;
    vec2 x1 = x - v1;
    vec2 x2 = x - v2;

    vec3 iu, iv;
    vec3 xw, yw;

    // Wrap to periods, if desired
    if(any(greaterThan(period, vec2(0.0)))) {
        xw = vec3(v0.x, v1.x, v2.x);
        yw = vec3(v0.y, v1.y, v2.y);
        if(period.x > 0.0)
        xw = mod(vec3(v0.x, v1.x, v2.x), period.x);
        if(period.y > 0.0)
        yw = mod(vec3(v0.y, v1.y, v2.y), period.y);
        // Transform back to simplex space and fix rounding errors
        iu = floor(xw + 0.5*yw + 0.5);
        iv = floor(yw + 0.5);
    } else { // Shortcut if neither x nor y periods are specified
        iu = vec3(i0.x, i1.x, i2.x);
        iv = vec3(i0.y, i1.y, i2.y);
    }

    // Compute one pseudo-random hash value for each corner
    vec3 hash = mod(iu, 289.0);
    hash = mod((hash*51.0 + 2.0)*hash + iv, 289.0);
    hash = mod((hash*34.0 + 10.0)*hash, 289.0);

    // Pick a pseudo-random angle and add the desired rotation
    vec3 psi = hash * 0.07482 + alpha;
    vec3 gx = cos(psi);
    vec3 gy = sin(psi);

    // Reorganize for dot products below
    vec2 g0 = vec2(gx.x,gy.x);
    vec2 g1 = vec2(gx.y,gy.y);
    vec2 g2 = vec2(gx.z,gy.z);

    // Radial decay with distance from each simplex corner
    vec3 w = 0.8 - vec3(dot(x0, x0), dot(x1, x1), dot(x2, x2));
    w = max(w, 0.0);
    vec3 w2 = w * w;
    vec3 w4 = w2 * w2;

    // The value of the linear ramp from each of the corners
    vec3 gdotx = vec3(dot(g0, x0), dot(g1, x1), dot(g2, x2));

    // Multiply by the radial decay and sum up the noise value
    float n = dot(w4, gdotx);

    // Compute the first order partial derivatives
    vec3 w3 = w2 * w;
    vec3 dw = -8.0 * w3 * gdotx;
    vec2 dn0 = w4.x * g0 + dw.x * x0;
    vec2 dn1 = w4.y * g1 + dw.y * x1;
    vec2 dn2 = w4.z * g2 + dw.z * x2;
    gradient = 10.9 * (dn0 + dn1 + dn2);

    // Scale the return value to fit nicely into the range [-1,1]
    return 10.9 * n;
}

#define PI 3.1415926535897932384626433832795

vec2 rot(vec2 v, float a){
    return mat2x2(
    cos(a), -sin(a),
    sin(a), cos(a)
    ) * v;
}


// Вспомогательная функция для генерации случайного значения на основе координат
float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898,78.233))) * 43758.5453);
}

// Простая реализация шума на основе случайных значений
float noise(vec2 pos) {
    vec2 i = floor(pos);
    vec2 f = fract(pos);

    float a = rand(i);
    float b = rand(i + vec2(1.0, 0.0));
    float c = rand(i + vec2(0.0, 1.0));
    float d = rand(i + vec2(1.0, 1.0));

    // Интерполяция по кубической функции сглаживания
    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x) +
    (c - a) * u.y * (1.0 - u.x) +
    (d - b) * u.x * u.y;
}

// Функция для вычисления шума Перлина с несколькими октавами
float perlinNoise(vec2 pos) {
    float total = 0.0;
    float frequency = 1.0;
    float amplitude = 1.0;
    float maxValue = 0.0;

    for (int i = 0; i < 4; i++) {
        total += noise(pos * frequency) * amplitude;
        maxValue += amplitude;
        amplitude *= 0.5;
        frequency *= 2.0;
    }
    return total / maxValue;
}



// Основная функция для обработки цвета спрайта и сглаживания его границ
vec4 getSpriteColor(vec2 uv, vec2 fieldSize, vec4 color, sampler2D tex) {
    float time = u_time;
    uv = floor(uv * fieldSize * 5.) / (fieldSize * 5.);

    vec2 grad;
    float n = psrdnoise(uv*fieldSize*3.5323, vec2(0.), time, grad);
    float smoothAlpha = grad.x + grad.y;

    vec2 fUv = uv + 0.01 * vec2(rand(uv * time), 0) + vec2(-0.038, -0.02);

    if(fUv.x > 0. && fUv.y > 0.0){
        vec4 nei = texture(tex, fUv);

        if(color.a == 0. && nei.a != 0.){
            color = nei * nei * smoothAlpha;
        }
    }
    return color;
}

vec4 applyWave(vec2 uv, vec2 uvCenter, vec4 color, float time, float startTime, vec4 waveColor) {
    float speed = 1.0;
    float size = 0.050;

    vec2 correction = (u_resolution / max(u_resolution.x, u_resolution.y));

    vec2 pixelSize = vec2(0.01) * (1./correction); // размер в uv-координатах
    uv = round(uv / pixelSize) * pixelSize;

    // Расчёт расстояния до центра волны с учётом отражения:
    float dist = length((uvCenter - uv) * correction);

    float waveRadius = (time / speed) - startTime;

    // Модуляция размера волны в зависимости от угла
    vec2 dir = normalize(uvCenter - uv);
    float angle = atan(dir.y, dir.x);
    size += rand(dir) / 4.0;

    float delta = abs(waveRadius - dist);
    if (delta < size) {
        return color + waveColor *
        clamp((size / delta) / (dist * dist * 400.0), 0.0, 1.0) // затухание и плавные границы
        * min(1.0, 0.5 - dist) // затухание с расстоянием
        * 2.0;
    }
    return color;
}

vec4 applyDestroy(vec2 uv, vec4 startYs, float cellWidth, vec4 color, float time, float startTime, float fadeTime, vec4 waveColor){
    float speed = 1.;
    float size = 0.050;

    vec2 correction = (u_resolution / max(u_resolution.x, u_resolution.y));

    vec2 pixelSize = vec2(0.002) * (1./correction); // размер в uv-координатах
    uv = round(uv / pixelSize) * pixelSize;

    if(time < startTime || time > startTime + fadeTime){
        return color;
    }

    time = time - startTime;

    float NOISE_CONSTANT = 3.;
    float OVERLAP_VALUE = 0.002;

    vec2 noise = vec2(
    perlinNoise(uv * correction * 100.) * (sqrt(time*10.)) * (NOISE_CONSTANT / fadeTime),
    0.00
    );

    for(int i = 0; i < 4; i++){
        float start = startYs[i];
        if(start < uv.y + noise.y + OVERLAP_VALUE && start + cellWidth + OVERLAP_VALUE > uv.y - noise.y){
            if(uv.x < NOISE_CONSTANT - noise.x)
            return color;
            return vec4(0.);
        }

    }

    return color;
}

#define GLSL_APP_


#ifdef GLSL_APP
#define texOfCell u_textures[0]
#define texOfPattern u_textures[1]
#define texOfFalling u_textures[2]
#define texFail u_textures[3]

#define m_waveStartCell vec2(5, 20)
#define m_waveStart -100.0
#define m_waveColor vec4(1, 0., 0., 1.)

#define m_destroyStartCells data
#define m_destroyStart -10.0
#define m_destroyFadeTime 1.0
#define m_failStart 1.0


#else
#define texOfCell u_texture_0
#define texOfPattern u_texture_1
#define texOfFalling u_texture_2
#define texFail u_texture_fail

#define m_waveStartCell u_waveStartCell
#define m_waveStart u_waveStart
#define m_waveColor u_waveColor

#define m_destroyStartCells u_destroyStartCells
#define m_destroyStart u_destroyStart
#define m_destroyFadeTime u_destroyFadeTime
#define m_failStart u_failStart

#endif


vec2 transformUV(vec2 _uv, float time) {
    // Параметры эффекта взрыва
    float explosionSpeed = .3;
    float blastRadius = 2.;
    float waveFrequency = 32.0;
    float chaosFactor = 0.25;

    // Центр взрыва (можно сделать параметром)
    vec2 explosionCenter = vec2(0.5, 0.5);

    // Вектор направления от центра к текущей точке
    vec2 dir = _uv - explosionCenter;
    float distance = length(dir);

    // Нормализованное направление и время взрыва
    vec2 normDir = dir / (distance + 0.001);
    float blastPower = smoothstep(0.0, 1.0, time * explosionSpeed);

    // Основное радиальное смещение
    vec2 displacement = normDir * blastPower * blastRadius;

    // Волновые искажения
    float wave = sin(distance * waveFrequency - time * 8.0)
    * cos(distance * waveFrequency * 0.5 + time * 4.0);
    displacement += normDir * wave * 0.02 * blastPower;

    // Шумовые искажения
    vec2 noiseOffset = vec2(
    perlinNoise(_uv * 8.0 + time * 2.0),
    perlinNoise(_uv * 8.0 - time * 2.0)
    ) * chaosFactor * blastPower;

    // Хаотические вспышки в эпицентре
    if(distance < 0.2) {
        vec2 randomBurst = vec2(
        rand(_uv * time) - 0.5,
        rand(_uv * time + vec2(12.34, 56.78)) - 0.5
        ) * 0.1 * (1.0 - distance/0.2);
        displacement += randomBurst * blastPower;
    }

    // Комбинированный эффект
    return _uv + displacement + noiseOffset;
}

void main(){

    //sampler2D texOfCell = u_textures[0];
    //sampler2D texOfPattern = ;



    vec2 st = uv * vec2(u_resolution.x / u_resolution.y, 1.);

    vec2 gradient;
    float n = psrdnoise(vec2(-0.441, 0.353) * st, vec2(0.), 1.2 * u_time, gradient);

    vec2 fieldSize = vec2(10., 24.);

    //vec2 cellScreenSize = vec2(20., 20.);

    vec2 cellScreenSize = min(vec2(u_resolution.y / fieldSize.y), vec2(u_resolution.x / fieldSize.x));

    vec2 fieldSizeUv = cellScreenSize * fieldSize / u_resolution;


    vec2 _uv = uv;
    if(u_failStart != 0.0 && u_time >= m_failStart){
        _uv = transformUV(uv, u_time - m_failStart);
        _uv = mix(_uv, uv, clamp((u_time - m_failStart)/10., 0., 1.));
    }


    _uv = _uv + vec2(-0.5 + fieldSizeUv.x/2., -0.5 + fieldSizeUv.y/2.);



    //vec2 patternSize = vec2(287., 600.);

    vec2 cellUvSize =  cellScreenSize / u_resolution;

    vec2 cellIndex = (_uv / cellUvSize);




    vec2 uv2 = ((cellIndex) + 0.5) / fieldSize;
    vec2 patternUvSampler = (floor(cellIndex) + 0.5) / fieldSize;

    if(
    cellIndex.x < fieldSize.x &&
    cellIndex.y < fieldSize.y &&
    cellIndex.x > 0.0 &&
    cellIndex.y > 0.0
    ){
        //RENDERING STATIC BLOCKS
        vec4 pattern = texture(texOfPattern, patternUvSampler);

        vec4 newColor = texture(texOfCell, mod(cellIndex, 1.0)) * mix(pattern, tan(pattern), (abs(cos(n * PI))));
        newColor = getSpriteColor(uv2, fieldSize, newColor, texOfPattern);
        if(newColor.a != 0.0){
            out_color += newColor;
        }

        //RENDERING FALLING BLOCK (OVER STARTIC)
        vec4 fallingColor = texture(texOfCell, mod(cellIndex, 1.0)) * texture(texOfFalling, patternUvSampler);
        if(fallingColor != vec4(0.)){
            out_color = fallingColor;
        }

        vec2 waveStartCell = m_waveStartCell;
        float waveStart = m_waveStart;
        vec4 waveColor = m_waveColor;

        //For glsl.app

        #ifdef GLSL_APP
        vec4 data = vec4(
        cellUvSize.y * 10.,
        cellUvSize.y * 2.,
        cellUvSize.y * 1.,
        cellUvSize.y * 0.
        );
        #else
        vec4 data = vec4(
        cellUvSize.y * u_destroyStartCells[0],
        cellUvSize.y * u_destroyStartCells[1],
        cellUvSize.y * u_destroyStartCells[2],
        cellUvSize.y * u_destroyStartCells[3]
        );
        #endif

        out_color = applyWave(_uv, cellUvSize * waveStartCell, out_color, u_time, waveStart, waveColor);
        out_color = applyDestroy(_uv, data, cellUvSize.y, out_color, u_time, m_destroyStart, m_destroyFadeTime, waveColor);
    }
    else{
        float tr_time = u_time / 100. + 2.;

        out_color =
        vec4(perlinNoise(_uv + tr_time), 0.2, perlinNoise(_uv - tr_time) , 1.);

        out_color.a = 0.;

        //out_color = vec4(0.15, 0.20, 0.22, 1.);

    }

    if(u_failStart != 0.0 && u_time >= m_failStart){
        out_color = mix(out_color, texture(texFail, uv), clamp((u_time - m_failStart -2.) / 5., 0., 1.));
    }

}