<template>
  <div class="cesium-wrapper">
    <!-- 加载动画遮罩 -->
    <Transition name="fade">
      <div v-if="isLoading" class="loading-overlay">
        <div class="loading-content">
          <div class="loading-spinner">
            <div class="spinner-ring"></div>
            <div class="spinner-ring"></div>
            <div class="spinner-ring"></div>
          </div>
          <div class="loading-text">
            <span class="loading-title">地图加载中</span>
            <span class="loading-dots">
              <span>.</span><span>.</span><span>.</span>
            </span>
          </div>
        </div>
      </div>
    </Transition>

    <div
      ref="viewerRef"
      class="cesium-container"
      @mouseleave="hideHoverTooltip"
      @contextmenu.prevent
    ></div>
    <div
      v-if="hoverDroneInfo"
      class="drone-hover-tooltip"
      :class="{ 'is-illegal': hoverDroneInfo.isIllegal }"
      :style="{ left: `${hoverTooltipPosition.x}px`, top: `${hoverTooltipPosition.y}px` }"
    >
      <div class="tooltip-name" :class="{ 'illegal-name': hoverDroneInfo.isIllegal }">
        <span v-if="hoverDroneInfo.isIllegal" class="illegal-badge">未授权无人机</span>
        {{ hoverDroneInfo.name }}
      </div>
      <div class="tooltip-row">
        <span class="tooltip-label">状态</span>
        <span class="tooltip-value" :class="`status-${hoverDroneInfo.isIllegal ? 'illegal' : hoverDroneInfo.status}`">{{ hoverDroneInfo.statusText }}</span>
      </div>
      <div class="tooltip-row" v-if="hoverDroneInfo.categoryText">
        <span class="tooltip-label">类型</span>
        <span class="tooltip-value">{{ hoverDroneInfo.categoryText }}</span>
      </div>
      <div class="tooltip-row">
        <span class="tooltip-label">高度</span>
        <span class="tooltip-value">{{ hoverDroneInfo.altitude }} m</span>
      </div>
      <div class="tooltip-row" v-if="hoverDroneInfo.locationText">
        <span class="tooltip-label">位置</span>
        <span class="tooltip-value">{{ hoverDroneInfo.locationText }}</span>
      </div>
      <div class="tooltip-row" v-if="hoverDroneInfo.coordinateText">
        <span class="tooltip-label">经纬度</span>
        <span class="tooltip-value">{{ hoverDroneInfo.coordinateText }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as Cesium from 'cesium'
import 'cesium/Build/Cesium/Widgets/widgets.css'
import type { DemoDrone } from '../demo/mock-data'

// 银河星空 SkyBox 贴图
import skyBoxBack from '@/assets/imgs/skyBox/cube_back_8k_stars_milky_way.jpg'
import skyBoxBottom from '@/assets/imgs/skyBox/cube_bottom_8k_stars_milky_way.jpg'
import skyBoxFront from '@/assets/imgs/skyBox/cube_front_8k_stars_milky_way.jpg'
import skyBoxLeft from '@/assets/imgs/skyBox/cube_left_8k_stars_milky_way.jpg'
import skyBoxRight from '@/assets/imgs/skyBox/cube_right_8k_stars_milky_way.jpg'
import skyBoxTop from '@/assets/imgs/skyBox/cube_top_8k_stars_milky_way.jpg'

defineOptions({ name: 'CesiumMap' })

type ZoomMode = 'near' | 'global'
type ZoomModeSource = 'auto' | 'manual'
type MapProviderType = 'tianditu' | 'gaode'
type CameraInteractionMode = 'overview' | 'drone-focus'

interface ZoomModeChangePayload {
  mode: ZoomMode
  height: number
  source: ZoomModeSource
}

interface DroneHoverInfo {
  id: number
  name: string
  status: string
  statusText: string
  altitude: number
  lng?: number
  lat?: number
  locationText?: string
  coordinateText?: string
  category?: DroneCategory
  categoryText?: string
  isIllegal?: boolean
}

const props = withDefaults(
  defineProps<{
    forceGlobal?: boolean
    drones?: DemoDrone[]
  }>(),
  {
    forceGlobal: false,
    drones: () => []
  }
)

const emit = defineEmits<{
  (e: 'zoom-mode-change', payload: ZoomModeChangePayload): void
  (e: 'camera-mode-change', mode: CameraInteractionMode): void
}>()

const viewerRef = ref<HTMLDivElement>()
const isLoading = ref(true)
const hoverDroneInfo = ref<DroneHoverInfo | null>(null)
const hoverTooltipPosition = ref({ x: 0, y: 0 })
let viewer: Cesium.Viewer | null = null
let droneEntities: Cesium.Entity[] = []
let clickHandler: Cesium.ScreenSpaceEventHandler | null = null
let pulseTimerByDroneId = new Map<number, number>()
let droneTrailPointMap = new Map<number, Cesium.Cartesian3[]>()
let nearLayer: Cesium.ImageryLayer | null = null
let globalLayer: Cesium.ImageryLayer | null = null
let gaodeVectorLayer: Cesium.ImageryLayer | null = null
let gaodeVectorCoverageRectangle: Cesium.Rectangle | null = null
let gaodeVectorVisibleState = true
let gaodeVectorPendingVisible: boolean | null = null
let gaodeVectorPendingSince = 0
let gaodeVectorLastToggleAt = 0
let labelLayer: Cesium.ImageryLayer | null = null
let useSharedBaseImageryLayer = false
let cameraChangedListener: (() => void) | null = null
let switchByHeightDebounceTimer: number | null = null
let labelLayerResumeTimer: number | null = null
let globalReframeGuardTimer: number | null = null
let orbitRenderRafId: number | null = null
let orbitRenderPending = false
let isDragPerformanceMode = false
let isTerrainEnabledInScene = false
let isCloseRangeDepthTestSuppressed = false
let lastCameraVisualSyncAt = 0
let labelPausedUntil = 0
let lastBaseErrorLogAt = 0
let lastGlobalErrorLogAt = 0
let lastVectorErrorLogAt = 0
let lastLabelErrorLogAt = 0
let currentZoomMode: ZoomMode = 'near'
let oceanLandStage: Cesium.PostProcessStage | null = null
let cinematicEarthStage: Cesium.PostProcessStage | null = null
let isGlobalReframing = false
let isOrbitDragActive = false
let hasOrbitDragged = false
let orbitDragDistance = 0
let orbitDragStartedAt = 0
let orbitLastMousePosition: Cesium.Cartesian2 | null = null
let isOrbitRightDragActive = false
let orbitRightLastMousePosition: Cesium.Cartesian2 | null = null
let hasOrbitStateSyncedFromCamera = false
let lastWheelZoomDirection: 'in' | 'out' | null = null
let cameraInteractionMode: CameraInteractionMode = 'overview'
let activeMapProvider: MapProviderType = 'tianditu'
let activeGaodeKey = ''
let zhejiangBodyEntity: Cesium.Entity | null = null
let zhejiangDistrictDataSource: Cesium.GeoJsonDataSource | null = null
let zhejiangOutlineEntities: Cesium.Entity[] = []
let zhejiangHudEntities: Cesium.Entity[] = []
let zhejiangHudVideoElement: HTMLVideoElement | null = null
let zhejiangHudVideoRenderTimer: number | null = null
let cityLabelEntities: Cesium.Entity[] = []
let zhejiangTopOverlayTexture: string | null = null

const DEFAULT_TDT_TOKEN = '7fe2d7e6d05887f606991b0e22d34424'
const DEFAULT_AMAP_KEY = '5c197336454066d8aa9bcece4b5d7b0b'
const DEFAULT_GLOBE_SWITCH_HEIGHT = 2500000
const DEFAULT_GLOBE_SWITCH_ENTER_HEIGHT = 2600000
const DEFAULT_GLOBE_SWITCH_EXIT_HEIGHT = 2300000
const DEFAULT_GLOBE_FIT_HEIGHT = 22000000
const DEFAULT_GLOBE_CINEMATIC_HEIGHT = 9000000
const DEFAULT_GLOBE_CINEMATIC_HEADING = 0
const DEFAULT_GLOBE_CINEMATIC_PITCH = -82
const DEFAULT_GLOBE_CINEMATIC_ROLL = 0
const AUTO_GLOBAL_REFRAME_PITCH_THRESHOLD_DEG = -68
const GLOBAL_REFRAME_GUARD_EXTRA_MS = 400
const DEFAULT_ENABLE_SPACE_SKY = true
const DEFAULT_TDT_MAX_REQUESTS_PER_SERVER = 2
const DEFAULT_TDT_MAX_REQUESTS = 8
const DEFAULT_TDT_SUBDOMAIN_COUNT = 2
const DEFAULT_TDT_BASE_MAX_LEVEL = 16
const DEFAULT_TDT_GLOBAL_MAX_LEVEL = 10
const DEFAULT_TDT_LABEL_MAX_LEVEL = 15
const DEFAULT_ENABLE_TDT_SHARED_BASE_LAYER = true
const DEFAULT_TDT_ENABLE_LABEL_LAYER = true
const DEFAULT_TDT_LABEL_VISIBLE_MAX_HEIGHT = 500000
const DEFAULT_TDT_LABEL_MIN_LEVEL = 8
const DEFAULT_TDT_LABEL_COOLDOWN_MS = 60000
const DEFAULT_TDT_LABEL_ERROR_LOG_INTERVAL_MS = 15000
const DEFAULT_GLOBE_MAX_SCREEN_SPACE_ERROR = 1.8
const DEFAULT_GLOBE_TILE_CACHE_SIZE = 1400
const DEFAULT_ENABLE_TERRAIN = false
const DEFAULT_TERRAIN_PROVIDER = 'cesium'
const DEFAULT_TERRAIN_REQUEST_VERTEX_NORMALS = true
const DEFAULT_TERRAIN_REQUEST_WATER_MASK = false
const DEFAULT_ENABLE_TERRAIN_LIGHTING = true
const DEFAULT_ENABLE_DEPTH_TEST_AGAINST_TERRAIN = true
const DEFAULT_TERRAIN_VERTICAL_EXAGGERATION = 1.8
const DEFAULT_ENABLE_FUTURISTIC_BLOOM = true
const DEFAULT_BLOOM_CONTRAST = 142
const DEFAULT_BLOOM_BRIGHTNESS = -0.08
const DEFAULT_BLOOM_DELTA = 1
const DEFAULT_BLOOM_SIGMA = 1.7
const DEFAULT_BLOOM_STEP_SIZE = 3.6
const DEFAULT_ENABLE_ZHEJIANG_CITY_LABELS = true
const DEFAULT_GAODE_VECTOR_VISIBLE_MAX_HEIGHT = 1200000
const DEFAULT_GAODE_VECTOR_VISIBLE_EXIT_HEIGHT = 1280000
const DEFAULT_DRAG_MAX_SCREEN_SPACE_ERROR = 4.5
const DEFAULT_ORBIT_DRAG_ACTIVATE_PX = 6
const DEFAULT_ORBIT_DRAG_ACTIVATE_MS = 90
const DEFAULT_ORBIT_RIGHT_ZOOM_PER_PIXEL = 0.012
const DEFAULT_CLOSE_RANGE_DEPTH_TEST_DISABLE_ENTER_RANGE = 1800
const DEFAULT_CLOSE_RANGE_DEPTH_TEST_DISABLE_EXIT_RANGE = 2600
const DEFAULT_CLOSE_RANGE_MAX_PITCH_DEG = -18
const DEFAULT_CLOSE_RANGE_PITCH_RANGE = 2200
const DEFAULT_CENTER_ANCHOR_SWITCH_DURATION = 0.95
const DEFAULT_CENTER_ANCHOR_SWITCH_MAX_DURATION = 1.85
const DEFAULT_CENTER_ANCHOR_SWITCH_DISTANCE_REF = 320000
const CAMERA_VISUAL_SYNC_INTERVAL_MS = 50
const GAODE_VECTOR_VISIBILITY_STABLE_MS = 180
const GAODE_VECTOR_VISIBILITY_MIN_TOGGLE_INTERVAL_MS = 260
const CAMERA_SWITCH_DEBOUNCE_MS = 150
const ENABLE_AUTO_GLOBAL_SWITCH = false

const LEGACY_GLOBE_SWITCH_ENTER_DELTA = DEFAULT_GLOBE_SWITCH_ENTER_HEIGHT - DEFAULT_GLOBE_SWITCH_HEIGHT
const LEGACY_GLOBE_SWITCH_EXIT_DELTA = DEFAULT_GLOBE_SWITCH_HEIGHT - DEFAULT_GLOBE_SWITCH_EXIT_HEIGHT
const SAFE_MAX_CAMERA_HEIGHT = 22000000
const ZHEJIANG_CENTER = { lng: 120.153576, lat: 30.287459 }
const ZHEJIANG_BASE_HEIGHT = 120
const ZHEJIANG_TOP_HEIGHT = 6000
const ZHEJIANG_CITY_LABEL_HEIGHT = ZHEJIANG_TOP_HEIGHT + 1500
const ZHEJIANG_OUTLINE_HEIGHT = 120
const ZHEJIANG_VECTOR_FALLBACK_RECT = Cesium.Rectangle.fromDegrees(118.0, 27.0, 123.5, 31.5)
const ZHEJIANG_DEFAULT_HUD_RADIUS = 420000
const ZHEJIANG_MIN_HUD_RADIUS = 300000
const ZHEJIANG_MAX_HUD_RADIUS = 900000
const ZHEJIANG_HUD_RADIUS_PADDING = 1.18
const DRONE_TRAIL_MAX_POINTS = 160
const DRONE_TRAIL_MIN_SEGMENT_METERS = 80
let zhejiangFocusLngLat: [number, number] = [ZHEJIANG_CENTER.lng, ZHEJIANG_CENTER.lat]
let zhejiangHudRadiusMeters = ZHEJIANG_DEFAULT_HUD_RADIUS
const CITY_LABEL_OVERVIEW_SWITCH_HEIGHT = 1000000
const OVERVIEW_PRIORITY_CITY_NAMES = new Set([
  '杭州市',
  '宁波市',
  '温州市',
  '嘉兴市',
  '绍兴市',
  '金华市',
  '台州市'
])
// 浙江 GeoJSON 数据源
const ZHEJIANG_GEOJSON_URL = '/geojson/浙江省.geojson'
// 中国区域遮罩 GeoJSON（用于非浙江区域暗化）
const CHINA_GEOJSON_URL = 'https://geojson.cn/api/china/1.6.3/100000.topo.json'
const ENABLE_CUSTOM_EARTH_STYLE = false
const ZHEJIANG_HUD_VIDEO_TEXTURE_URL = '/geojson/tietu.dv'

// 地球影像图层基础滤镜（轻微调整，主要效果由后处理着色器实现）
const IMAGERY_FILTER = {
  brightness: 1.0,
  contrast: 1.0,
  saturation: 1.0,
  hue: 0.0,
  gamma: 1.0
}

// 海洋/陆地分离渲染着色器（GLSL）
const OCEAN_LAND_SHADER = `
  uniform sampler2D colorTexture;
  in vec2 v_textureCoordinates;

  // 计算亮度
  float getLuminance(vec3 c) {
    return dot(c, vec3(0.299, 0.587, 0.114));
  }

  // 计算饱和度
  float getSaturation(vec3 c) {
    float maxC = max(max(c.r, c.g), c.b);
    float minC = min(min(c.r, c.g), c.b);
    return (maxC - minC) / (maxC + 0.001);
  }

  void main() {
    vec4 color = texture(colorTexture, v_textureCoordinates);
    vec3 rgb = color.rgb;

    float lum = getLuminance(rgb);
    float sat = getSaturation(rgb);

    // === 检测背景/星空（非常暗的区域）===
    float isBackground = step(lum, 0.025);

    // === 检测地图内标注色（避免大面积陆地被误判为 UI）===
    float isBright = step(0.18, lum);
    float isOrange = step(0.50, rgb.r) * step(rgb.g, rgb.r) * step(rgb.b, rgb.g);
    float isGreen = step(0.45, rgb.g) * step(rgb.r, rgb.g) * step(rgb.b, rgb.g);
    float isRed = step(0.56, rgb.r) * step(rgb.g, 0.24) * step(rgb.b, 0.24);
    float isCyan = step(0.50, rgb.g) * step(0.50, rgb.b) * step(rgb.r, 0.22);
    float isUI = max(max(isOrange, isGreen), max(isRed, isCyan)) * isBright;

    // === 检测海洋（更激进的阈值）===
    // 蓝色比例检测
    float blueRatio = rgb.b / (max(rgb.r, rgb.g) + 0.005);
    float isDeepBlue = step(1.05, blueRatio);
    // 绿蓝联合检测（海洋通常绿蓝都高于红色）
    float gbRatio = (rgb.g + rgb.b) / (rgb.r + 0.005);
    float isBlueGreen = step(1.5, gbRatio);
    // 低红色分量检测
    float isLowRed = step(rgb.r, 0.12);
    float hasBlueTint = step(rgb.r, rgb.b);
    // 综合海洋检测
    float oceanScore = max(isDeepBlue, isBlueGreen * step(lum, 0.4));
    oceanScore = max(oceanScore, isLowRed * hasBlueTint * step(0.015, lum) * step(lum, 0.35));
    float isOcean = oceanScore * (1.0 - isUI) * (1.0 - isBackground);

    // === 陆地处理：墨绿色高亮 ===
    float gray = lum;
    float contrastSignal = max(max(rgb.r, rgb.g), rgb.b) - min(min(rgb.r, rgb.g), rgb.b);
    float detailSignal = clamp(contrastSignal * 2.2 + sat * 0.24, 0.0, 1.0);
    float landEnergy = clamp(pow(gray, 0.62) * 1.55 + detailSignal * 0.22, 0.0, 1.0);

    // 三段式墨绿色调：深墨绿 -> 青墨绿 -> 高亮青绿
    vec3 landShadow = vec3(0.006, 0.046, 0.036);
    vec3 landMid = vec3(0.012, 0.24, 0.18);
    vec3 landHighlight = vec3(0.11, 0.62, 0.46);
    vec3 landBase = mix(landShadow, landMid, smoothstep(0.02, 0.52, landEnergy));
    vec3 landColor = mix(landBase, landHighlight, smoothstep(0.50, 1.0, landEnergy));

    // 强化地形纹理并压掉发灰观感
    float ridgeGlow = pow(detailSignal, 1.45) * 0.22;
    landColor *= 0.78 + gray * 0.78;
    landColor += vec3(0.018, 0.17, 0.13) * ridgeGlow;
    landColor = clamp(pow(landColor, vec3(0.92)), 0.0, 1.0);

    // === 海洋处理：暗黑 ===
    vec3 oceanColor = vec3(0.0, 0.0007, 0.0013);

    // === 最终合成（锐利过渡）===
    // 使用 smoothstep 使边界更锐利
    float oceanMask = smoothstep(0.34, 0.64, isOcean);
    vec3 earthColor = mix(landColor, oceanColor, oceanMask);

    // 背景保持原样（星空）
    vec3 withBackground = mix(earthColor, rgb, isBackground);
    // UI 元素保持原样
    vec3 finalColor = mix(withBackground, rgb, clamp(isUI, 0.0, 1.0));

    out_FragColor = vec4(finalColor, color.a);
  }
`

// 电影感增强后处理（暗角 + 地平线辉光）
const CINEMATIC_EARTH_SHADER = `
  uniform sampler2D colorTexture;
  in vec2 v_textureCoordinates;

  void main() {
    vec4 color = texture(colorTexture, v_textureCoordinates);
    vec2 uv = v_textureCoordinates;

    vec2 centered = uv - vec2(0.5);
    centered.x *= 1.24;
    float distanceToCenter = length(centered);

    // 屏幕边缘暗角，让视线集中到地平线附近
    float vignette = smoothstep(0.25, 0.93, distanceToCenter);
    float edgeDarkening = mix(1.0, 0.52, vignette);

    // 中央地平线附近加一点冷色辉光
    float horizonBand = smoothstep(0.39, 0.52, uv.y) * (1.0 - smoothstep(0.52, 0.66, uv.y));
    float horizonFocus = 1.0 - smoothstep(0.0, 0.46, abs(uv.x - 0.5));
    vec3 horizonGlow = vec3(0.0, 0.18, 0.24) * horizonBand * horizonFocus * 0.32;

    vec3 finalColor = color.rgb * edgeDarkening + horizonGlow;
    out_FragColor = vec4(finalColor, color.a);
  }
`

const parseNumberEnv = (value: unknown, fallback: number) => {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : fallback
}

const parseIntegerEnv = (value: unknown, fallback: number, min = 0) => {
  const parsed = Number.parseInt(String(value), 10)
  if (!Number.isFinite(parsed)) {
    return fallback
  }
  return parsed >= min ? parsed : fallback
}

const parseOptionalNumberEnv = (value: unknown): number | null => {
  if (value === undefined || value === null) {
    return null
  }
  const normalized = String(value).trim()
  if (!normalized) {
    return null
  }
  const parsed = Number(normalized)
  return Number.isFinite(parsed) ? parsed : null
}

const parseBooleanEnv = (value: unknown, fallback: boolean) => {
  if (value === undefined || value === null) {
    return fallback
  }
  const normalized = String(value).trim().toLowerCase()
  if (!normalized) {
    return fallback
  }
  if (['true', '1', 'yes', 'on'].includes(normalized)) {
    return true
  }
  if (['false', '0', 'no', 'off'].includes(normalized)) {
    return false
  }
  return fallback
}

const LEGACY_GLOBE_SWITCH_HEIGHT = parseNumberEnv(
  import.meta.env.VITE_GLOBE_SWITCH_HEIGHT,
  DEFAULT_GLOBE_SWITCH_HEIGHT
)
const rawGlobeSwitchEnterHeight = parseOptionalNumberEnv(import.meta.env.VITE_GLOBE_SWITCH_ENTER_HEIGHT)
const rawGlobeSwitchExitHeight = parseOptionalNumberEnv(import.meta.env.VITE_GLOBE_SWITCH_EXIT_HEIGHT)
const derivedGlobeSwitchEnterHeight = LEGACY_GLOBE_SWITCH_HEIGHT + LEGACY_GLOBE_SWITCH_ENTER_DELTA
const derivedGlobeSwitchExitHeight = Math.max(
  0,
  LEGACY_GLOBE_SWITCH_HEIGHT - LEGACY_GLOBE_SWITCH_EXIT_DELTA
)
const normalizedGlobeSwitchEnterHeight = rawGlobeSwitchEnterHeight ?? derivedGlobeSwitchEnterHeight
const normalizedGlobeSwitchExitHeight = rawGlobeSwitchExitHeight ?? derivedGlobeSwitchExitHeight
const GLOBE_SWITCH_ENTER_HEIGHT =
  normalizedGlobeSwitchEnterHeight > normalizedGlobeSwitchExitHeight
    ? normalizedGlobeSwitchEnterHeight
    : normalizedGlobeSwitchExitHeight + 1
const GLOBE_SWITCH_EXIT_HEIGHT = Math.max(
  0,
  Math.min(normalizedGlobeSwitchExitHeight, GLOBE_SWITCH_ENTER_HEIGHT - 1)
)
const GLOBE_FIT_HEIGHT = parseNumberEnv(import.meta.env.VITE_GLOBE_FIT_HEIGHT, DEFAULT_GLOBE_FIT_HEIGHT)
const GLOBE_CINEMATIC_HEIGHT = Math.min(
  parseNumberEnv(import.meta.env.VITE_GLOBE_CINEMATIC_HEIGHT, DEFAULT_GLOBE_CINEMATIC_HEIGHT),
  GLOBE_FIT_HEIGHT
)
const GLOBE_CINEMATIC_HEADING = parseNumberEnv(
  import.meta.env.VITE_GLOBE_CINEMATIC_HEADING,
  DEFAULT_GLOBE_CINEMATIC_HEADING
)
const GLOBE_CINEMATIC_PITCH = parseNumberEnv(
  import.meta.env.VITE_GLOBE_CINEMATIC_PITCH,
  DEFAULT_GLOBE_CINEMATIC_PITCH
)
const GLOBE_CINEMATIC_ROLL = parseNumberEnv(
  import.meta.env.VITE_GLOBE_CINEMATIC_ROLL,
  DEFAULT_GLOBE_CINEMATIC_ROLL
)
const DEFAULT_ORBIT_MAX_RANGE = 9000000
const DEFAULT_ORBIT_MIN_RANGE_OVERVIEW = 220000
const DEFAULT_ORBIT_MIN_RANGE_DRONE = 20000
const DEFAULT_ORBIT_CENTER_CARTESIAN = Cesium.Cartesian3.fromDegrees(
  ZHEJIANG_CENTER.lng,
  ZHEJIANG_CENTER.lat,
  0
)
const ORBIT_HEADING_PER_PIXEL = Cesium.Math.toRadians(0.22)
const ORBIT_PITCH_PER_PIXEL = Cesium.Math.toRadians(0.18)
const ORBIT_MIN_PITCH = Cesium.Math.toRadians(-88)
const ORBIT_MAX_PITCH = Cesium.Math.toRadians(-8)
const ORBIT_MAX_RANGE = Math.min(
  parseNumberEnv(import.meta.env.VITE_ORBIT_MAX_RANGE, DEFAULT_ORBIT_MAX_RANGE),
  SAFE_MAX_CAMERA_HEIGHT
)
const ORBIT_MIN_RANGE_OVERVIEW = parseNumberEnv(
  import.meta.env.VITE_ORBIT_MIN_RANGE_OVERVIEW,
  DEFAULT_ORBIT_MIN_RANGE_OVERVIEW
)
const ORBIT_MIN_RANGE_DRONE = parseNumberEnv(
  import.meta.env.VITE_ORBIT_MIN_RANGE_DRONE,
  DEFAULT_ORBIT_MIN_RANGE_DRONE
)
const CLOSE_RANGE_MAX_PITCH = Cesium.Math.toRadians(
  parseNumberEnv(import.meta.env.VITE_CLOSE_RANGE_MAX_PITCH_DEG, DEFAULT_CLOSE_RANGE_MAX_PITCH_DEG)
)
const CLOSE_RANGE_PITCH_RANGE = Math.max(
  200,
  parseNumberEnv(import.meta.env.VITE_CLOSE_RANGE_PITCH_RANGE, DEFAULT_CLOSE_RANGE_PITCH_RANGE)
)
const CENTER_ANCHOR_SWITCH_DURATION = Cesium.Math.clamp(
  parseNumberEnv(import.meta.env.VITE_CENTER_ANCHOR_SWITCH_DURATION, DEFAULT_CENTER_ANCHOR_SWITCH_DURATION),
  0.2,
  3.0
)
const CENTER_ANCHOR_SWITCH_MAX_DURATION = Cesium.Math.clamp(
  parseNumberEnv(import.meta.env.VITE_CENTER_ANCHOR_SWITCH_MAX_DURATION, DEFAULT_CENTER_ANCHOR_SWITCH_MAX_DURATION),
  CENTER_ANCHOR_SWITCH_DURATION,
  4.5
)
const CENTER_ANCHOR_SWITCH_DISTANCE_REF = Math.max(
  1,
  parseNumberEnv(
    import.meta.env.VITE_CENTER_ANCHOR_SWITCH_DISTANCE_REF,
    DEFAULT_CENTER_ANCHOR_SWITCH_DISTANCE_REF
  )
)
const getOrbitMinRange = (mode: CameraInteractionMode = cameraInteractionMode) => {
  const rawMinRange = mode === 'drone-focus' ? ORBIT_MIN_RANGE_DRONE : ORBIT_MIN_RANGE_OVERVIEW
  const boundedMinRange = Math.max(100, rawMinRange)
  return Math.min(boundedMinRange, ORBIT_MAX_RANGE)
}
const clampOrbitRange = (range: number, mode: CameraInteractionMode = cameraInteractionMode) =>
  Cesium.Math.clamp(range, getOrbitMinRange(mode), ORBIT_MAX_RANGE)
const getOrbitMaxPitchForRange = (range: number) =>
  range <= CLOSE_RANGE_PITCH_RANGE
    ? Math.min(ORBIT_MAX_PITCH, CLOSE_RANGE_MAX_PITCH)
    : ORBIT_MAX_PITCH
const clampOrbitPitch = (pitch: number, range: number = orbitRange) =>
  Cesium.Math.clamp(pitch, ORBIT_MIN_PITCH, getOrbitMaxPitchForRange(range))
const getCenterAnchorCartesian = () =>
  Cesium.Cartesian3.fromDegrees(zhejiangFocusLngLat[0], zhejiangFocusLngLat[1], 0)
const ORBIT_DRAG_ACTIVATE_PX = parseIntegerEnv(
  import.meta.env.VITE_ORBIT_DRAG_ACTIVATE_PX,
  DEFAULT_ORBIT_DRAG_ACTIVATE_PX,
  1
)
const ORBIT_DRAG_ACTIVATE_MS = parseIntegerEnv(
  import.meta.env.VITE_ORBIT_DRAG_ACTIVATE_MS,
  DEFAULT_ORBIT_DRAG_ACTIVATE_MS,
  0
)
const ORBIT_RIGHT_ZOOM_PER_PIXEL = Cesium.Math.clamp(
  parseNumberEnv(import.meta.env.VITE_ORBIT_RIGHT_ZOOM_PER_PIXEL, DEFAULT_ORBIT_RIGHT_ZOOM_PER_PIXEL),
  0.001,
  0.08
)
let orbitHeadingRad = Cesium.Math.toRadians(GLOBE_CINEMATIC_HEADING)
let orbitPitchRad = clampOrbitPitch(Cesium.Math.toRadians(GLOBE_CINEMATIC_PITCH), GLOBE_CINEMATIC_HEIGHT)
let orbitRange = clampOrbitRange(GLOBE_CINEMATIC_HEIGHT, 'overview')
let orbitAnchorCartesian = Cesium.Cartesian3.clone(DEFAULT_ORBIT_CENTER_CARTESIAN)
const ENABLE_SPACE_SKY =
  String(import.meta.env.VITE_ENABLE_SPACE_SKY ?? String(DEFAULT_ENABLE_SPACE_SKY)).toLowerCase() !==
  'false'
const TDT_MAX_REQUESTS_PER_SERVER = parseIntegerEnv(
  import.meta.env.VITE_TDT_MAX_REQUESTS_PER_SERVER,
  DEFAULT_TDT_MAX_REQUESTS_PER_SERVER,
  1
)
const TDT_MAX_REQUESTS = parseIntegerEnv(
  import.meta.env.VITE_TDT_MAX_REQUESTS,
  DEFAULT_TDT_MAX_REQUESTS,
  1
)
const TDT_SUBDOMAIN_COUNT = parseIntegerEnv(
  import.meta.env.VITE_TDT_SUBDOMAIN_COUNT,
  DEFAULT_TDT_SUBDOMAIN_COUNT,
  1
)
const TDT_BASE_MAX_LEVEL = parseIntegerEnv(
  import.meta.env.VITE_TDT_BASE_MAX_LEVEL,
  DEFAULT_TDT_BASE_MAX_LEVEL,
  1
)
const TDT_GLOBAL_MAX_LEVEL = parseIntegerEnv(
  import.meta.env.VITE_TDT_GLOBAL_MAX_LEVEL,
  DEFAULT_TDT_GLOBAL_MAX_LEVEL,
  1
)
const ENABLE_TDT_LABEL_LAYER =
  String(import.meta.env.VITE_TDT_ENABLE_LABEL_LAYER ?? String(DEFAULT_TDT_ENABLE_LABEL_LAYER)).toLowerCase() !==
  'false'
const LABEL_VISIBLE_MAX_HEIGHT = parseNumberEnv(
  import.meta.env.VITE_TDT_LABEL_VISIBLE_MAX_HEIGHT,
  DEFAULT_TDT_LABEL_VISIBLE_MAX_HEIGHT
)
const LABEL_MIN_LEVEL = parseIntegerEnv(
  import.meta.env.VITE_TDT_LABEL_MIN_LEVEL,
  DEFAULT_TDT_LABEL_MIN_LEVEL,
  0
)
const LABEL_COOLDOWN_MS = parseIntegerEnv(
  import.meta.env.VITE_TDT_LABEL_COOLDOWN_MS,
  DEFAULT_TDT_LABEL_COOLDOWN_MS,
  1000
)
const LABEL_ERROR_LOG_INTERVAL_MS = parseIntegerEnv(
  import.meta.env.VITE_TDT_LABEL_ERROR_LOG_INTERVAL_MS,
  DEFAULT_TDT_LABEL_ERROR_LOG_INTERVAL_MS,
  1000
)
const TDT_LABEL_MAX_LEVEL = Math.max(
  LABEL_MIN_LEVEL,
  parseIntegerEnv(import.meta.env.VITE_TDT_LABEL_MAX_LEVEL, DEFAULT_TDT_LABEL_MAX_LEVEL, 1)
)
const ENABLE_TDT_SHARED_BASE_LAYER =
  String(
    import.meta.env.VITE_TDT_ENABLE_SHARED_BASE_LAYER ?? String(DEFAULT_ENABLE_TDT_SHARED_BASE_LAYER)
  ).toLowerCase() !== 'false'
const GLOBE_MAX_SCREEN_SPACE_ERROR = parseNumberEnv(
  import.meta.env.VITE_GLOBE_MAX_SCREEN_SPACE_ERROR,
  DEFAULT_GLOBE_MAX_SCREEN_SPACE_ERROR
)
const GLOBE_TILE_CACHE_SIZE = parseIntegerEnv(
  import.meta.env.VITE_GLOBE_TILE_CACHE_SIZE,
  DEFAULT_GLOBE_TILE_CACHE_SIZE,
  100
)
const ENABLE_TERRAIN = parseBooleanEnv(import.meta.env.VITE_ENABLE_TERRAIN, DEFAULT_ENABLE_TERRAIN)
const TERRAIN_PROVIDER = String(import.meta.env.VITE_TERRAIN_PROVIDER || DEFAULT_TERRAIN_PROVIDER).toLowerCase()
const TERRAIN_URL = String(import.meta.env.VITE_TERRAIN_URL || '').trim()
const TERRAIN_REQUEST_VERTEX_NORMALS = parseBooleanEnv(
  import.meta.env.VITE_TERRAIN_REQUEST_VERTEX_NORMALS,
  DEFAULT_TERRAIN_REQUEST_VERTEX_NORMALS
)
const TERRAIN_REQUEST_WATER_MASK = parseBooleanEnv(
  import.meta.env.VITE_TERRAIN_REQUEST_WATER_MASK,
  DEFAULT_TERRAIN_REQUEST_WATER_MASK
)
const ENABLE_TERRAIN_LIGHTING = parseBooleanEnv(
  import.meta.env.VITE_ENABLE_TERRAIN_LIGHTING,
  DEFAULT_ENABLE_TERRAIN_LIGHTING
)
const ENABLE_DEPTH_TEST_AGAINST_TERRAIN = parseBooleanEnv(
  import.meta.env.VITE_ENABLE_DEPTH_TEST_AGAINST_TERRAIN,
  DEFAULT_ENABLE_DEPTH_TEST_AGAINST_TERRAIN
)
const TERRAIN_VERTICAL_EXAGGERATION = Math.max(
  0.1,
  parseNumberEnv(import.meta.env.VITE_TERRAIN_VERTICAL_EXAGGERATION, DEFAULT_TERRAIN_VERTICAL_EXAGGERATION)
)
const ENABLE_FUTURISTIC_BLOOM = parseBooleanEnv(
  import.meta.env.VITE_ENABLE_FUTURISTIC_BLOOM,
  DEFAULT_ENABLE_FUTURISTIC_BLOOM
)
const ENABLE_ZHEJIANG_CITY_LABELS = parseBooleanEnv(
  import.meta.env.VITE_ENABLE_ZHEJIANG_CITY_LABELS,
  DEFAULT_ENABLE_ZHEJIANG_CITY_LABELS
)
const GAODE_VECTOR_VISIBLE_MAX_HEIGHT = parseNumberEnv(
  import.meta.env.VITE_GAODE_VECTOR_VISIBLE_MAX_HEIGHT,
  DEFAULT_GAODE_VECTOR_VISIBLE_MAX_HEIGHT
)
const GAODE_VECTOR_VISIBLE_EXIT_HEIGHT = Math.max(
  GAODE_VECTOR_VISIBLE_MAX_HEIGHT + 1,
  parseNumberEnv(
    import.meta.env.VITE_GAODE_VECTOR_VISIBLE_EXIT_HEIGHT,
    DEFAULT_GAODE_VECTOR_VISIBLE_EXIT_HEIGHT
  )
)
const DRAG_MAX_SCREEN_SPACE_ERROR = Math.max(
  GLOBE_MAX_SCREEN_SPACE_ERROR,
  parseNumberEnv(import.meta.env.VITE_DRAG_MAX_SCREEN_SPACE_ERROR, DEFAULT_DRAG_MAX_SCREEN_SPACE_ERROR)
)
const CLOSE_RANGE_DEPTH_TEST_DISABLE_ENTER_RANGE = Math.max(
  200,
  parseNumberEnv(
    import.meta.env.VITE_CLOSE_RANGE_DEPTH_TEST_DISABLE_ENTER_RANGE,
    DEFAULT_CLOSE_RANGE_DEPTH_TEST_DISABLE_ENTER_RANGE
  )
)
const CLOSE_RANGE_DEPTH_TEST_DISABLE_EXIT_RANGE = Math.max(
  CLOSE_RANGE_DEPTH_TEST_DISABLE_ENTER_RANGE + 1,
  parseNumberEnv(
    import.meta.env.VITE_CLOSE_RANGE_DEPTH_TEST_DISABLE_EXIT_RANGE,
    DEFAULT_CLOSE_RANGE_DEPTH_TEST_DISABLE_EXIT_RANGE
  )
)
const BLOOM_CONTRAST = parseNumberEnv(import.meta.env.VITE_BLOOM_CONTRAST, DEFAULT_BLOOM_CONTRAST)
const BLOOM_BRIGHTNESS = parseNumberEnv(import.meta.env.VITE_BLOOM_BRIGHTNESS, DEFAULT_BLOOM_BRIGHTNESS)
const BLOOM_DELTA = parseNumberEnv(import.meta.env.VITE_BLOOM_DELTA, DEFAULT_BLOOM_DELTA)
const BLOOM_SIGMA = parseNumberEnv(import.meta.env.VITE_BLOOM_SIGMA, DEFAULT_BLOOM_SIGMA)
const BLOOM_STEP_SIZE = parseNumberEnv(import.meta.env.VITE_BLOOM_STEP_SIZE, DEFAULT_BLOOM_STEP_SIZE)
const normalizedTdtSubdomainCount = Math.min(8, Math.max(1, TDT_SUBDOMAIN_COUNT))
const TDT_SUBDOMAINS = Array.from({ length: normalizedTdtSubdomainCount }, (_, index) => String(index))

const getMapProviderType = (): MapProviderType => {
  const provider = String(import.meta.env.VITE_MAP_PROVIDER || 'tianditu').toLowerCase()
  return provider === 'gaode' ? 'gaode' : 'tianditu'
}

const createTerrainProvider = async (): Promise<Cesium.TerrainProvider> => {
  if (!ENABLE_TERRAIN) {
    return new Cesium.EllipsoidTerrainProvider()
  }

  if (TERRAIN_PROVIDER === 'custom') {
    if (!TERRAIN_URL) {
      console.warn('[Cesium] 已启用地形但未配置 VITE_TERRAIN_URL，回退到椭球地形')
      return new Cesium.EllipsoidTerrainProvider()
    }
    try {
      return await Cesium.CesiumTerrainProvider.fromUrl(TERRAIN_URL, {
        requestVertexNormals: TERRAIN_REQUEST_VERTEX_NORMALS,
        requestWaterMask: TERRAIN_REQUEST_WATER_MASK
      })
    } catch (error) {
      console.error('[Cesium] 自定义地形服务加载失败，回退到椭球地形:', error)
      return new Cesium.EllipsoidTerrainProvider()
    }
  }

  try {
    return await Cesium.createWorldTerrainAsync({
      requestVertexNormals: TERRAIN_REQUEST_VERTEX_NORMALS,
      requestWaterMask: TERRAIN_REQUEST_WATER_MASK
    })
  } catch (error) {
    console.error('[Cesium] Cesium World Terrain 加载失败，回退到椭球地形:', error)
    return new Cesium.EllipsoidTerrainProvider()
  }
}

const createBaseLayerProvider = (
  mapProvider: MapProviderType,
  tiandituToken: string,
  gaodeKey: string
) => {
  if (mapProvider === 'gaode') {
    return new Cesium.UrlTemplateImageryProvider({
      // 高德影像底图
      url:
        'https://webst0{s}.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}&key=' +
        gaodeKey,
      subdomains: ['1', '2', '3', '4'],
      minimumLevel: 3,
      maximumLevel: 18
    })
  }

  return new Cesium.UrlTemplateImageryProvider({
    // 天地图影像底图
    url: 'https://t{s}.tianditu.gov.cn/DataServer?T=img_w&x={x}&y={y}&l={z}&tk=' + tiandituToken,
    subdomains: TDT_SUBDOMAINS,
    maximumLevel: TDT_BASE_MAX_LEVEL
  })
}

const createGaodeVectorLayerProvider = (gaodeKey: string, rectangle: Cesium.Rectangle) =>
  new Cesium.UrlTemplateImageryProvider({
    // 高德卫星注记叠加（影像优先，只补道路/文字等矢量信息）
    url:
      'https://webst0{s}.is.autonavi.com/appmaptile?style=8&x={x}&y={y}&z={z}&key=' +
      gaodeKey,
    subdomains: ['1', '2', '3', '4'],
    minimumLevel: 5,
    maximumLevel: 18,
    rectangle
  })

const createLabelLayerProvider = (
  mapProvider: MapProviderType,
  tiandituToken: string
): Cesium.UrlTemplateImageryProvider | null => {
  if (!ENABLE_TDT_LABEL_LAYER) {
    return null
  }

  if (mapProvider === 'gaode') {
    // 高德仅使用影像底图，不叠加矢量路网/注记层
    return null
  }

  return new Cesium.UrlTemplateImageryProvider({
    // 天地图注记层
    url: 'https://t{s}.tianditu.gov.cn/DataServer?T=cia_w&x={x}&y={y}&l={z}&tk=' + tiandituToken,
    subdomains: TDT_SUBDOMAINS,
    minimumLevel: LABEL_MIN_LEVEL,
    maximumLevel: TDT_LABEL_MAX_LEVEL
  })
}

const createGlobalLayerProvider = (
  mapProvider: MapProviderType,
  tiandituToken: string,
  gaodeKey: string
) => {
  if (mapProvider === 'gaode') {
    return new Cesium.UrlTemplateImageryProvider({
      url:
        'https://webst0{s}.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}&key=' +
        gaodeKey,
      subdomains: ['1', '2', '3', '4'],
      minimumLevel: 3,
      maximumLevel: 18
    })
  }

  // 全球模式默认使用天地图影像服务，不使用 Cesium 默认全球影像
  return new Cesium.UrlTemplateImageryProvider({
    url: 'https://t{s}.tianditu.gov.cn/DataServer?T=img_w&x={x}&y={y}&l={z}&tk=' + tiandituToken,
    subdomains: TDT_SUBDOMAINS,
    maximumLevel: TDT_GLOBAL_MAX_LEVEL
  })
}

// 无人机类型定义
type DroneCategory = 'logistics' | 'commercial' | 'public' | 'traffic'
type DroneStatus = 'flying' | 'online' | 'offline' | 'illegal'

interface DroneData {
  id: number
  name: string
  lng: number
  lat: number
  alt: number
  status: DroneStatus
  category: DroneCategory
  isIllegal: boolean // 未授权无人机标识
}

// 默认无人机数据（作为 props 缺省兜底）
const DEFAULT_DRONE_DATA: DroneData[] = [
  // 浙江省内无人机
  { id: 1, name: 'UAV-001', lng: 120.153, lat: 30.287, alt: 500, status: 'flying', category: 'logistics', isIllegal: false },
  { id: 2, name: 'UAV-002', lng: 121.550, lat: 29.868, alt: 300, status: 'online', category: 'commercial', isIllegal: false },
  { id: 3, name: 'UAV-003', lng: 120.699, lat: 27.994, alt: 600, status: 'flying', category: 'public', isIllegal: false },
  { id: 4, name: 'UAV-004', lng: 120.755, lat: 30.753, alt: 400, status: 'online', category: 'traffic', isIllegal: false },
  { id: 5, name: 'UAV-005', lng: 120.093, lat: 30.894, alt: 0, status: 'offline', category: 'logistics', isIllegal: false },
  { id: 6, name: 'UAV-006', lng: 120.582, lat: 29.997, alt: 350, status: 'online', category: 'commercial', isIllegal: false },
  { id: 7, name: 'UAV-007', lng: 119.649, lat: 29.089, alt: 550, status: 'flying', category: 'public', isIllegal: false },
  { id: 8, name: 'UAV-008', lng: 121.429, lat: 28.661, alt: 450, status: 'online', category: 'traffic', isIllegal: false },
  // 未授权无人机（分布在浙江各区域）
  { id: 9, name: 'UAV-X01', lng: 120.210, lat: 30.302, alt: 800, status: 'flying', category: 'commercial', isIllegal: true },
  { id: 10, name: 'UAV-X02', lng: 122.207, lat: 29.985, alt: 650, status: 'flying', category: 'logistics', isIllegal: true },
  { id: 11, name: 'UAV-X03', lng: 118.873, lat: 28.942, alt: 720, status: 'flying', category: 'commercial', isIllegal: true }
]

const droneData = ref<DroneData[]>([])

const syncDroneData = (incomingDrones: DemoDrone[] | undefined) => {
  const source = incomingDrones && incomingDrones.length > 0 ? incomingDrones : DEFAULT_DRONE_DATA
  droneData.value = source.map((item) => ({
    id: item.id,
    name: item.name,
    lng: item.lng,
    lat: item.lat,
    alt: item.alt,
    status: item.status,
    category: item.category,
    isIllegal: item.isIllegal
  }))
}

syncDroneData(props.drones)

// 当前筛选的类型
const activeCategories = ref<Set<DroneCategory>>(new Set(['logistics', 'commercial', 'public', 'traffic']))
const showIllegalOnly = ref(false)

// 根据状态获取颜色
const getStatusColor = (status: string, isIllegal: boolean): Cesium.Color => {
  if (isIllegal) {
    return Cesium.Color.fromCssColorString('#ff2d2d') // 红色 - 未授权无人机
  }
  switch (status) {
    case 'flying':
      return Cesium.Color.fromCssColorString('#ffaa00') // 橙色 - 飞行中
    case 'online':
      return Cesium.Color.fromCssColorString('#00ff88') // 绿色 - 在线
    case 'offline':
      return Cesium.Color.fromCssColorString('#666666') // 灰色 - 离线
    default:
      return Cesium.Color.WHITE
  }
}

// 创建无人机图标（SVG）
const createDroneIcon = (status: string, isIllegal: boolean = false): string => {
  // 未授权无人机使用红色
  let color: string
  if (isIllegal) {
    color = '#ff2d2d'
  } else {
    color = status === 'flying' ? '#ffaa00' : status === 'online' ? '#00ff88' : '#666666'
  }

  // 未授权无人机使用不同的图标样式（带警告三角）
  if (isIllegal) {
    const svg = `
<svg width="40" height="40" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <filter id="glowRed">
      <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
      <feMerge>
        <feMergeNode in="coloredBlur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
  </defs>
  <circle cx="20" cy="20" r="18" fill="${color}" opacity="0.4" filter="url(#glowRed)"/>
  <circle cx="20" cy="20" r="14" fill="${color}" opacity="0.7"/>
  <circle cx="20" cy="20" r="8" fill="${color}"/>
  <path d="M20 8 L26 18 L14 18 Z" fill="white" opacity="0.95"/>
  <text x="20" y="17" font-size="8" fill="${color}" text-anchor="middle" font-weight="bold">!</text>
</svg>`
    return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
  }

  // 普通无人机图标
  const svg = `
<svg width="40" height="40" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <filter id="glow">
      <feGaussianBlur stdDeviation="2" result="coloredBlur"/>
      <feMerge>
        <feMergeNode in="coloredBlur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
  </defs>
  <circle cx="20" cy="20" r="18" fill="${color}" opacity="0.3" filter="url(#glow)"/>
  <circle cx="20" cy="20" r="12" fill="${color}" opacity="0.6"/>
  <circle cx="20" cy="20" r="6" fill="${color}"/>
  <path d="M12 20 L20 12 L28 20 L20 28 Z" fill="white" opacity="0.9"/>
</svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

const buildDroneEntityProperties = (drone: DroneData) =>
  new Cesium.PropertyBag({
    droneId: drone.id,
    status: drone.status,
    altitude: drone.alt,
    lng: drone.lng,
    lat: drone.lat,
    category: drone.category,
    isIllegal: drone.isIllegal
  })

const stopDronePulseAnimation = (droneId: number, entity?: Cesium.Entity | null) => {
  const timerId = pulseTimerByDroneId.get(droneId)
  if (timerId !== undefined) {
    clearInterval(timerId)
    pulseTimerByDroneId.delete(droneId)
  }
  if (entity?.billboard) {
    entity.billboard.scale = new Cesium.ConstantProperty(1.0)
  }
}

const clearAllDronePulseAnimations = () => {
  pulseTimerByDroneId.forEach((timerId) => clearInterval(timerId))
  pulseTimerByDroneId.clear()
}

const startDronePulseAnimation = (droneId: number, entity: Cesium.Entity, isIllegal: boolean) => {
  if (!entity.billboard || pulseTimerByDroneId.has(droneId)) return

  let scale = 1.0
  let growing = true
  const interval = isIllegal ? 30 : 50
  const maxScale = isIllegal ? 1.5 : 1.3
  const step = isIllegal ? 0.04 : 0.02

  const timerId = window.setInterval(() => {
    if (!entity.billboard) return
    if (growing) {
      scale += step
      if (scale >= maxScale) growing = false
    } else {
      scale -= step
      if (scale <= 1.0) growing = true
    }
    entity.billboard.scale = new Cesium.ConstantProperty(scale)
  }, interval)

  pulseTimerByDroneId.set(droneId, timerId)
}

const syncDronePulseAnimation = (drone: DroneData, entity: Cesium.Entity) => {
  const shouldPulse = drone.status === 'flying' || drone.isIllegal
  if (shouldPulse) {
    startDronePulseAnimation(drone.id, entity, drone.isIllegal)
  } else {
    stopDronePulseAnimation(drone.id, entity)
  }
}

const appendDroneTrailPoint = (droneId: number, currentPosition: Cesium.Cartesian3) => {
  const trailPoints = droneTrailPointMap.get(droneId) ?? []
  const lastPoint = trailPoints.length > 0 ? trailPoints[trailPoints.length - 1] : null

  if (
    !lastPoint ||
    Cesium.Cartesian3.distance(lastPoint, currentPosition) >= DRONE_TRAIL_MIN_SEGMENT_METERS
  ) {
    trailPoints.push(Cesium.Cartesian3.clone(currentPosition))
    if (trailPoints.length > DRONE_TRAIL_MAX_POINTS) {
      trailPoints.splice(0, trailPoints.length - DRONE_TRAIL_MAX_POINTS)
    }
    droneTrailPointMap.set(droneId, trailPoints)
  }

  return trailPoints
}

const syncDroneTrailEntity = (drone: DroneData, currentPosition: Cesium.Cartesian3) => {
  if (!viewer) return
  const pathEntityId = `path-${drone.id}`
  const pathEntity = viewer.entities.getById(pathEntityId)
  const shouldRenderTrail = drone.status === 'flying'

  if (!shouldRenderTrail) {
    if (pathEntity) {
      pathEntity.show = false
    }
    return
  }

  const trailPoints = appendDroneTrailPoint(drone.id, currentPosition)
  const trailColor = drone.isIllegal
    ? Cesium.Color.fromCssColorString('#ff5858').withAlpha(0.78)
    : Cesium.Color.fromCssColorString('#ffaa00').withAlpha(0.8)

  if (!pathEntity) {
    viewer.entities.add({
      id: pathEntityId,
      polyline: {
        positions: trailPoints.slice(),
        width: drone.isIllegal ? 3.4 : 3,
        material: new Cesium.PolylineGlowMaterialProperty({
          glowPower: 0.2,
          taperPower: 0.5,
          color: trailColor
        }),
        clampToGround: false
      }
    })
    return
  }

  pathEntity.show = true
  if (pathEntity.polyline) {
    pathEntity.polyline.positions = new Cesium.ConstantProperty(trailPoints.slice())
    pathEntity.polyline.width = new Cesium.ConstantProperty(drone.isIllegal ? 3.4 : 3)
    pathEntity.polyline.material = new Cesium.PolylineGlowMaterialProperty({
      glowPower: 0.2,
      taperPower: 0.5,
      color: trailColor
    })
  }
}

const upsertDroneEntity = (drone: DroneData) => {
  if (!viewer) return
  const droneEntityId = `drone-${drone.id}`
  const currentPosition = Cesium.Cartesian3.fromDegrees(drone.lng, drone.lat, drone.alt)
  let entity = viewer.entities.getById(droneEntityId)

  if (!entity) {
    entity = viewer.entities.add({
      id: droneEntityId,
      name: drone.name,
      position: currentPosition,
      billboard: {
        image: createDroneIcon(drone.status, drone.isIllegal),
        width: 40,
        height: 40,
        verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
        heightReference: Cesium.HeightReference.RELATIVE_TO_GROUND,
        disableDepthTestDistance: Number.POSITIVE_INFINITY,
        scale: 1.0
      },
      label: {
        text: drone.isIllegal ? `⚠ ${drone.name}` : drone.name,
        font: '14px Microsoft YaHei',
        fillColor: drone.isIllegal ? Cesium.Color.fromCssColorString('#ff2d2d') : Cesium.Color.WHITE,
        outlineColor: Cesium.Color.BLACK,
        outlineWidth: 2,
        style: Cesium.LabelStyle.FILL_AND_OUTLINE,
        verticalOrigin: Cesium.VerticalOrigin.TOP,
        pixelOffset: new Cesium.Cartesian2(0, 10),
        disableDepthTestDistance: Number.POSITIVE_INFINITY,
        heightReference: Cesium.HeightReference.RELATIVE_TO_GROUND
      },
      properties: buildDroneEntityProperties(drone)
    })
    droneEntities.push(entity)
  } else {
    entity.name = drone.name
    entity.position = new Cesium.ConstantPositionProperty(currentPosition)
    if (entity.billboard) {
      entity.billboard.image = new Cesium.ConstantProperty(createDroneIcon(drone.status, drone.isIllegal))
    }
    if (entity.label) {
      entity.label.text = new Cesium.ConstantProperty(drone.isIllegal ? `⚠ ${drone.name}` : drone.name)
      entity.label.fillColor = new Cesium.ConstantProperty(
        drone.isIllegal ? Cesium.Color.fromCssColorString('#ff2d2d') : Cesium.Color.WHITE
      )
    }
    entity.properties = buildDroneEntityProperties(drone)
  }

  syncDronePulseAnimation(drone, entity)
  syncDroneTrailEntity(drone, currentPosition)
}

const removeDroneEntityById = (droneId: number) => {
  if (!viewer) return

  const entity = viewer.entities.getById(`drone-${droneId}`)
  if (entity) {
    viewer.entities.remove(entity)
  }
  const pathEntity = viewer.entities.getById(`path-${droneId}`)
  if (pathEntity) {
    viewer.entities.remove(pathEntity)
  }

  droneEntities = droneEntities.filter((item) => String(item.id ?? '') !== `drone-${droneId}`)
  droneTrailPointMap.delete(droneId)
  stopDronePulseAnimation(droneId, entity ?? null)
}

const syncDroneEntitiesWithLatestData = () => {
  if (!viewer || viewer.isDestroyed()) return

  const activeDroneIdSet = new Set<number>()
  droneData.value.forEach((drone) => {
    activeDroneIdSet.add(drone.id)
    upsertDroneEntity(drone)
  })

  // 清理已经不存在的无人机实体与轨迹
  droneEntities
    .map((entity) => Number(String(entity.id ?? '').replace('drone-', '')))
    .filter((droneId) => Number.isFinite(droneId) && !activeDroneIdSet.has(droneId))
    .forEach((droneId) => removeDroneEntityById(droneId))

  Array.from(droneTrailPointMap.keys()).forEach((droneId) => {
    if (!activeDroneIdSet.has(droneId)) {
      droneTrailPointMap.delete(droneId)
    }
  })

  Array.from(pulseTimerByDroneId.keys()).forEach((droneId) => {
    if (!activeDroneIdSet.has(droneId)) {
      stopDronePulseAnimation(droneId)
    }
  })

  updateDroneVisibility()
  viewer.scene.requestRender()
}

// 创建无人机标点
const createDroneMarkers = () => {
  syncDroneEntitiesWithLatestData()
}

// 绘制飞行轨迹
const createFlightPaths = () => {
  syncDroneEntitiesWithLatestData()
}

const getStatusText = (status: string, isIllegal: boolean = false) => {
  if (isIllegal) {
    return '未授权无人机'
  }
  switch (status) {
    case 'flying':
      return '飞行中'
    case 'online':
      return '在线'
    case 'offline':
      return '离线'
    default:
      return '未知'
  }
}

const getCategoryText = (category: DroneCategory) => {
  switch (category) {
    case 'logistics':
      return '物流'
    case 'commercial':
      return '商用'
    case 'public':
      return '公共设施'
    case 'traffic':
      return '交通'
    default:
      return '未知'
  }
}

const toRadians = (degrees: number) => (degrees * Math.PI) / 180

const getDistanceMeters = (fromLng: number, fromLat: number, toLng: number, toLat: number) => {
  const earthRadius = 6371000
  const dLat = toRadians(toLat - fromLat)
  const dLng = toRadians(toLng - fromLng)
  const lat1 = toRadians(fromLat)
  const lat2 = toRadians(toLat)
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return earthRadius * c
}

const resolveNearestZhejiangDistrict = (lng: number, lat: number) => {
  let nearestCity: ZhejiangCity | null = null
  let nearestDistance = Number.POSITIVE_INFINITY

  zhejiangCities.forEach((city) => {
    const distance = getDistanceMeters(lng, lat, city.lng, city.lat)
    if (distance < nearestDistance) {
      nearestDistance = distance
      nearestCity = city
    }
  })

  return {
    cityName: nearestCity?.name || '',
    distanceMeters: nearestDistance
  }
}

const formatCoordinate = (value: number, digits = 5) =>
  Number.isFinite(value) ? value.toFixed(digits) : ''

const hideHoverTooltip = () => {
  hoverDroneInfo.value = null
}

const updateHoverTooltipPosition = (x: number, y: number) => {
  hoverTooltipPosition.value = {
    x: x + 14,
    y: y + 16
  }
}

const extractHoverDroneInfo = (entity: Cesium.Entity): DroneHoverInfo | null => {
  const rawEntityId = String(entity.id ?? '')
  if (!rawEntityId.startsWith('drone-')) {
    return null
  }

  const propertyValues = entity.properties?.getValue(Cesium.JulianDate.now()) as
    | {
        droneId?: number
        status?: string
        altitude?: number
        lng?: number
        lat?: number
        category?: DroneCategory
        isIllegal?: boolean
      }
    | undefined

  const droneId = Number(propertyValues?.droneId ?? rawEntityId.replace('drone-', ''))
  if (!Number.isFinite(droneId)) {
    return null
  }

  const status = String(propertyValues?.status ?? '')
  const altitude = Number(propertyValues?.altitude ?? 0)
  let lng = Number(propertyValues?.lng)
  let lat = Number(propertyValues?.lat)
  const category = propertyValues?.category
  const isIllegal = propertyValues?.isIllegal ?? false

  if (!Number.isFinite(lng) || !Number.isFinite(lat)) {
    const droneItem = droneData.value.find((item) => item.id === droneId)
    if (droneItem) {
      lng = droneItem.lng
      lat = droneItem.lat
    }
  }

  if ((!Number.isFinite(lng) || !Number.isFinite(lat)) && entity.position) {
    const position = entity.position.getValue(Cesium.JulianDate.now())
    if (position) {
      const cartographic = Cesium.Cartographic.fromCartesian(position)
      lng = Cesium.Math.toDegrees(cartographic.longitude)
      lat = Cesium.Math.toDegrees(cartographic.latitude)
    }
  }

  const hasCoordinate = Number.isFinite(lng) && Number.isFinite(lat)
  const nearest = hasCoordinate
    ? resolveNearestZhejiangDistrict(lng as number, lat as number)
    : { cityName: '', distanceMeters: Number.POSITIVE_INFINITY }
  const locationText = nearest.cityName
    ? nearest.distanceMeters <= 2000
      ? `浙江省 · ${nearest.cityName}`
      : `浙江省 · ${nearest.cityName}（约 ${(nearest.distanceMeters / 1000).toFixed(1)} km）`
    : '浙江省（位置待定位）'
  const coordinateText = hasCoordinate
    ? `${formatCoordinate(lng as number)}, ${formatCoordinate(lat as number)}`
    : ''

  return {
    id: droneId,
    name: entity.name ?? `UAV-${String(droneId).padStart(3, '0')}`,
    status,
    statusText: getStatusText(status, isIllegal),
    altitude: Number.isFinite(altitude) ? Math.round(altitude) : 0,
    lng: hasCoordinate ? Number(lng) : undefined,
    lat: hasCoordinate ? Number(lat) : undefined,
    locationText,
    coordinateText,
    category,
    categoryText: category ? getCategoryText(category) : undefined,
    isIllegal
  }
}

const clearDroneSceneEntities = () => {
  clearAllDronePulseAnimations()
  droneTrailPointMap.clear()

  if (!viewer) {
    droneEntities = []
    hoverDroneInfo.value = null
    return
  }

  droneEntities.forEach((entity) => {
    viewer!.entities.remove(entity)
  })
  droneEntities = []

  const pathEntities = viewer.entities.values.filter((entity) =>
    String(entity.id ?? '').startsWith('path-')
  )
  pathEntities.forEach((entity) => viewer!.entities.remove(entity))
  hoverDroneInfo.value = null
}

const rebuildDroneSceneEntities = () => {
  if (!viewer || viewer.isDestroyed()) return
  clearDroneSceneEntities()
  syncDroneEntitiesWithLatestData()
}

interface ZhejiangCity {
  name: string
  lng: number
  lat: number
  isCenter?: boolean
}

// 浙江 11 个地级市数据
const zhejiangCities: ZhejiangCity[] = [
  { name: '杭州市', lng: 120.153576, lat: 30.287459, isCenter: true },
  { name: '宁波市', lng: 121.549792, lat: 29.868388 },
  { name: '温州市', lng: 120.699367, lat: 27.994267 },
  { name: '嘉兴市', lng: 120.750865, lat: 30.762653 },
  { name: '湖州市', lng: 120.102398, lat: 30.867198 },
  { name: '绍兴市', lng: 120.582112, lat: 29.997117 },
  { name: '金华市', lng: 119.649506, lat: 29.089524 },
  { name: '衢州市', lng: 118.872630, lat: 28.941708 },
  { name: '舟山市', lng: 122.106863, lat: 30.016028 },
  { name: '台州市', lng: 121.428599, lat: 28.661378 },
  { name: '丽水市', lng: 119.921786, lat: 28.451993 }
]

type CoordinatePair = [number, number]

const toCoordinateKey = (coord: CoordinatePair) => `${coord[0].toFixed(6)},${coord[1].toFixed(6)}`
const toUndirectedEdgeKey = (from: string, to: string) => (from < to ? `${from}|${to}` : `${to}|${from}`)

const collectOuterRingsFromGeoJson = (geoJson: any): CoordinatePair[][] => {
  const rings: CoordinatePair[][] = []
  if (!geoJson?.features) return rings

  const normalizeRing = (ring: any[]): CoordinatePair[] => {
    const coordinates: CoordinatePair[] = []
    ring.forEach((coord) => {
      if (!Array.isArray(coord) || coord.length < 2) return
      const lng = Number(coord[0])
      const lat = Number(coord[1])
      if (!Number.isFinite(lng) || !Number.isFinite(lat)) return
      coordinates.push([lng, lat])
    })
    if (coordinates.length < 3) return []

    const deduped: CoordinatePair[] = [coordinates[0]]
    for (let i = 1; i < coordinates.length; i++) {
      const prev = deduped[deduped.length - 1]
      const current = coordinates[i]
      if (Math.abs(prev[0] - current[0]) < 1e-8 && Math.abs(prev[1] - current[1]) < 1e-8) {
        continue
      }
      deduped.push(current)
    }

    if (deduped.length < 3) return []
    const first = deduped[0]
    const last = deduped[deduped.length - 1]
    if (Math.abs(first[0] - last[0]) > 1e-8 || Math.abs(first[1] - last[1]) > 1e-8) {
      deduped.push([first[0], first[1]])
    }
    return deduped.length >= 4 ? deduped : []
  }

  geoJson.features.forEach((feature: any) => {
    const geometry = feature?.geometry
    if (!geometry) return

    if (geometry.type === 'Polygon' && Array.isArray(geometry.coordinates?.[0])) {
      const ring = normalizeRing(geometry.coordinates[0])
      if (ring.length > 0) rings.push(ring)
      return
    }

    if (geometry.type === 'MultiPolygon' && Array.isArray(geometry.coordinates)) {
      geometry.coordinates.forEach((polygon: any) => {
        if (!Array.isArray(polygon?.[0])) return
        const ring = normalizeRing(polygon[0])
        if (ring.length > 0) rings.push(ring)
      })
    }
  })

  return rings
}

const extractMainOuterBoundaryRing = (geoJson: any): CoordinatePair[] | null => {
  const outerRings = collectOuterRingsFromGeoJson(geoJson)
  if (outerRings.length === 0) return null

  const edgeCounter = new Map<string, { from: CoordinatePair; to: CoordinatePair; count: number }>()
  const vertexPool = new Map<string, CoordinatePair>()

  outerRings.forEach((ring) => {
    for (let i = 0; i < ring.length - 1; i++) {
      const from = ring[i]
      const to = ring[i + 1]
      const fromKey = toCoordinateKey(from)
      const toKey = toCoordinateKey(to)
      if (fromKey === toKey) continue
      vertexPool.set(fromKey, from)
      vertexPool.set(toKey, to)

      const edgeKey = toUndirectedEdgeKey(fromKey, toKey)
      const existing = edgeCounter.get(edgeKey)
      if (existing) {
        existing.count += 1
      } else {
        edgeCounter.set(edgeKey, { from, to, count: 1 })
      }
    }
  })

  const boundaryEdges = Array.from(edgeCounter.values()).filter((edge) => edge.count === 1)
  if (boundaryEdges.length === 0) return null

  const adjacency = new Map<string, string[]>()
  boundaryEdges.forEach((edge) => {
    const fromKey = toCoordinateKey(edge.from)
    const toKey = toCoordinateKey(edge.to)
    if (!adjacency.has(fromKey)) adjacency.set(fromKey, [])
    if (!adjacency.has(toKey)) adjacency.set(toKey, [])
    adjacency.get(fromKey)!.push(toKey)
    adjacency.get(toKey)!.push(fromKey)
  })

  const visitedEdges = new Set<string>()
  const loops: CoordinatePair[][] = []

  adjacency.forEach((neighbors, startKey) => {
    neighbors.forEach((nextKey) => {
      const startEdgeKey = toUndirectedEdgeKey(startKey, nextKey)
      if (visitedEdges.has(startEdgeKey)) return

      const loop: CoordinatePair[] = []
      let previousKey = startKey
      let currentKey = nextKey
      loop.push(vertexPool.get(startKey)!)
      visitedEdges.add(startEdgeKey)

      let guard = 0
      while (guard < 8000) {
        guard += 1
        const currentCoord = vertexPool.get(currentKey)
        if (!currentCoord) break
        loop.push(currentCoord)

        const currentNeighbors = adjacency.get(currentKey) || []
        const candidateKeys = currentNeighbors.filter((candidate) => candidate !== previousKey)
        let chosenKey = candidateKeys.find((candidate) => !visitedEdges.has(toUndirectedEdgeKey(currentKey, candidate)))

        if (!chosenKey) {
          chosenKey = currentNeighbors.find((candidate) => candidate === startKey)
        }
        if (!chosenKey) break

        const edgeKey = toUndirectedEdgeKey(currentKey, chosenKey)
        if (visitedEdges.has(edgeKey) && chosenKey !== startKey) break
        visitedEdges.add(edgeKey)

        previousKey = currentKey
        currentKey = chosenKey
        if (currentKey === startKey) {
          loop.push(vertexPool.get(startKey)!)
          break
        }
      }

      if (loop.length >= 4) {
        loops.push(loop)
      }
    })
  })

  if (loops.length === 0) return null

  const roughLoopPerimeter = (loop: CoordinatePair[]) => {
    let perimeter = 0
    for (let i = 1; i < loop.length; i++) {
      const [lngA, latA] = loop[i - 1]
      const [lngB, latB] = loop[i]
      const meanLatRad = ((latA + latB) * Math.PI) / 360
      const x = (lngB - lngA) * 111320 * Math.cos(meanLatRad)
      const y = (latB - latA) * 110540
      perimeter += Math.hypot(x, y)
    }
    return perimeter
  }

  loops.sort((a, b) => roughLoopPerimeter(b) - roughLoopPerimeter(a))
  return loops[0]
}

const computeZhejiangFocusMetrics = (ring: CoordinatePair[]) => {
  let minLng = Number.POSITIVE_INFINITY
  let maxLng = Number.NEGATIVE_INFINITY
  let minLat = Number.POSITIVE_INFINITY
  let maxLat = Number.NEGATIVE_INFINITY

  ring.forEach(([lng, lat]) => {
    minLng = Math.min(minLng, lng)
    maxLng = Math.max(maxLng, lng)
    minLat = Math.min(minLat, lat)
    maxLat = Math.max(maxLat, lat)
  })

  const fallbackCenterLng = Number.isFinite(minLng) && Number.isFinite(maxLng)
    ? (minLng + maxLng) / 2
    : ZHEJIANG_CENTER.lng
  const fallbackCenterLat = Number.isFinite(minLat) && Number.isFinite(maxLat)
    ? (minLat + maxLat) / 2
    : ZHEJIANG_CENTER.lat

  let signedArea2 = 0
  let centroidAccLng = 0
  let centroidAccLat = 0
  for (let i = 0; i < ring.length - 1; i++) {
    const [x1, y1] = ring[i]
    const [x2, y2] = ring[i + 1]
    const cross = x1 * y2 - x2 * y1
    signedArea2 += cross
    centroidAccLng += (x1 + x2) * cross
    centroidAccLat += (y1 + y2) * cross
  }

  let centerLng = fallbackCenterLng
  let centerLat = fallbackCenterLat
  if (Math.abs(signedArea2) > 1e-10) {
    const polygonCentroidLng = centroidAccLng / (3 * signedArea2)
    const polygonCentroidLat = centroidAccLat / (3 * signedArea2)
    if (Number.isFinite(polygonCentroidLng) && Number.isFinite(polygonCentroidLat)) {
      centerLng = polygonCentroidLng
      centerLat = polygonCentroidLat
    }
  }

  const geodesic = new Cesium.EllipsoidGeodesic()
  const centerCartographic = Cesium.Cartographic.fromDegrees(centerLng, centerLat)
  let maxSurfaceDistance = 0
  ring.forEach(([lng, lat]) => {
    geodesic.setEndPoints(centerCartographic, Cesium.Cartographic.fromDegrees(lng, lat))
    const distance = geodesic.surfaceDistance
    if (Number.isFinite(distance) && distance > maxSurfaceDistance) {
      maxSurfaceDistance = distance
    }
  })

  if (!(maxSurfaceDistance > 0)) {
    geodesic.setEndPoints(centerCartographic, Cesium.Cartographic.fromDegrees(maxLng, maxLat))
    const fallbackDistance = geodesic.surfaceDistance
    if (Number.isFinite(fallbackDistance) && fallbackDistance > 0) {
      maxSurfaceDistance = fallbackDistance
    }
  }

  const hudRadiusMeters = Cesium.Math.clamp(
    maxSurfaceDistance > 0
      ? maxSurfaceDistance * ZHEJIANG_HUD_RADIUS_PADDING
      : ZHEJIANG_DEFAULT_HUD_RADIUS,
    ZHEJIANG_MIN_HUD_RADIUS,
    ZHEJIANG_MAX_HUD_RADIUS
  )

  return {
    centerLng,
    centerLat,
    hudRadiusMeters
  }
}

const computeRingBounds = (ring: CoordinatePair[]) => {
  let minLng = Number.POSITIVE_INFINITY
  let maxLng = Number.NEGATIVE_INFINITY
  let minLat = Number.POSITIVE_INFINITY
  let maxLat = Number.NEGATIVE_INFINITY

  ring.forEach(([lng, lat]) => {
    minLng = Math.min(minLng, lng)
    maxLng = Math.max(maxLng, lng)
    minLat = Math.min(minLat, lat)
    maxLat = Math.max(maxLat, lat)
  })

  if (!Number.isFinite(minLng) || !Number.isFinite(maxLng) || !Number.isFinite(minLat) || !Number.isFinite(maxLat)) {
    return null
  }

  return { minLng, maxLng, minLat, maxLat }
}

const buildFallbackOuterBoundaryRing = (): CoordinatePair[] => {
  const fallbackRect = ZHEJIANG_VECTOR_FALLBACK_RECT
  const west = Cesium.Math.toDegrees(fallbackRect.west)
  const south = Cesium.Math.toDegrees(fallbackRect.south)
  const east = Cesium.Math.toDegrees(fallbackRect.east)
  const north = Cesium.Math.toDegrees(fallbackRect.north)
  return [
    [west, south],
    [east, south],
    [east, north],
    [west, north],
    [west, south]
  ]
}

const buildZhejiangVectorRectangle = (ring?: CoordinatePair[] | null) => {
  if (!ring || ring.length < 4) {
    return ZHEJIANG_VECTOR_FALLBACK_RECT
  }

  const bounds = computeRingBounds(ring)
  if (!bounds) {
    return ZHEJIANG_VECTOR_FALLBACK_RECT
  }

  const lngSpan = Math.max(0.1, bounds.maxLng - bounds.minLng)
  const latSpan = Math.max(0.1, bounds.maxLat - bounds.minLat)
  const lngPadding = Math.min(0.12, lngSpan * 0.02)
  const latPadding = Math.min(0.1, latSpan * 0.02)
  return Cesium.Rectangle.fromDegrees(
    bounds.minLng - lngPadding,
    bounds.minLat - latPadding,
    bounds.maxLng + lngPadding,
    bounds.maxLat + latPadding
  )
}

const registerGaodeVectorLayerError = (provider: Cesium.UrlTemplateImageryProvider) => {
  provider.errorEvent.addEventListener((tileProviderError) => {
    const now = Date.now()
    if (now - lastVectorErrorLogAt < LABEL_ERROR_LOG_INTERVAL_MS) {
      return
    }
    console.error('[Cesium] 高德矢量图层加载失败:', tileProviderError)
    lastVectorErrorLogAt = now
  })
}

const isGaodeVectorVisible = () => {
  if (!viewer) return currentZoomMode === 'near'
  if (currentZoomMode !== 'near') {
    gaodeVectorVisibleState = false
    return false
  }
  const cameraHeight = viewer.camera.positionCartographic.height
  if (!Number.isFinite(cameraHeight)) {
    return gaodeVectorVisibleState
  }

  if (gaodeVectorVisibleState) {
    if (cameraHeight >= GAODE_VECTOR_VISIBLE_EXIT_HEIGHT) {
      gaodeVectorVisibleState = false
    }
  } else if (cameraHeight <= GAODE_VECTOR_VISIBLE_MAX_HEIGHT) {
    gaodeVectorVisibleState = true
  }

  return gaodeVectorVisibleState
}

const syncGaodeVectorVisibility = (force = false) => {
  if (!gaodeVectorLayer) return
  if (isOrbitDragActive && !force) return

  const targetVisible = isGaodeVectorVisible()
  const currentVisible = gaodeVectorLayer.show
  if (currentVisible === targetVisible) {
    gaodeVectorPendingVisible = null
    gaodeVectorPendingSince = 0
    return
  }

  const now = Date.now()
  if (force) {
    gaodeVectorLayer.show = targetVisible
    gaodeVectorLastToggleAt = now
    gaodeVectorPendingVisible = null
    gaodeVectorPendingSince = 0
    viewer?.scene.requestRender()
    return
  }

  if (gaodeVectorPendingVisible !== targetVisible) {
    gaodeVectorPendingVisible = targetVisible
    gaodeVectorPendingSince = now
    return
  }

  if (now - gaodeVectorPendingSince < GAODE_VECTOR_VISIBILITY_STABLE_MS) {
    return
  }
  if (now - gaodeVectorLastToggleAt < GAODE_VECTOR_VISIBILITY_MIN_TOGGLE_INTERVAL_MS) {
    return
  }

  gaodeVectorLayer.show = targetVisible
  gaodeVectorLastToggleAt = now
  gaodeVectorPendingVisible = null
  gaodeVectorPendingSince = 0
  viewer?.scene.requestRender()
}

const applyZhejiangVectorCoverage = (ring?: CoordinatePair[] | null) => {
  if (!viewer || activeMapProvider !== 'gaode' || !activeGaodeKey) return

  const targetRectangle = buildZhejiangVectorRectangle(ring)
  if (
    gaodeVectorCoverageRectangle &&
    Cesium.Rectangle.equalsEpsilon(
      gaodeVectorCoverageRectangle,
      targetRectangle,
      Cesium.Math.EPSILON7
    )
  ) {
    if (gaodeVectorLayer) {
      syncGaodeVectorVisibility(true)
    }
    return
  }

  if (gaodeVectorLayer) {
    viewer.imageryLayers.remove(gaodeVectorLayer, true)
    gaodeVectorLayer = null
  }

  const gaodeVectorProvider = createGaodeVectorLayerProvider(activeGaodeKey, targetRectangle)
  registerGaodeVectorLayerError(gaodeVectorProvider)
  gaodeVectorLayer = viewer.imageryLayers.addImageryProvider(gaodeVectorProvider)
  gaodeVectorLayer.alpha = 0.38
  gaodeVectorLayer.brightness = 0.98
  gaodeVectorLayer.contrast = 1.0
  gaodeVectorLayer.gamma = 1.0
  gaodeVectorLayer.show = true
  syncGaodeVectorVisibility(true)
  gaodeVectorCoverageRectangle = targetRectangle
  viewer.scene.requestRender()
}

const getZhejiangTopOverlayTexture = () => {
  if (zhejiangTopOverlayTexture) {
    return zhejiangTopOverlayTexture
  }
  const canvas = document.createElement('canvas')
  canvas.width = 1024
  canvas.height = 1024
  const ctx = canvas.getContext('2d')
  if (!ctx) {
    zhejiangTopOverlayTexture =
      'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9VEWilQAAAAASUVORK5CYII='
    return zhejiangTopOverlayTexture
  }

  const radial = ctx.createRadialGradient(512, 512, 80, 512, 512, 620)
  radial.addColorStop(0, 'rgba(255, 236, 171, 0.38)')
  radial.addColorStop(0.35, 'rgba(109, 255, 209, 0.24)')
  radial.addColorStop(1, 'rgba(7, 38, 31, 0.08)')
  ctx.fillStyle = radial
  ctx.fillRect(0, 0, canvas.width, canvas.height)

  zhejiangTopOverlayTexture = canvas.toDataURL('image/png')
  return zhejiangTopOverlayTexture
}

const stopZhejiangHudVideoRenderLoop = () => {
  if (zhejiangHudVideoRenderTimer !== null) {
    clearInterval(zhejiangHudVideoRenderTimer)
    zhejiangHudVideoRenderTimer = null
  }
}

const ensureZhejiangHudVideoElement = () => {
  if (zhejiangHudVideoElement) {
    return zhejiangHudVideoElement
  }

  const videoElement = document.createElement('video')
  videoElement.src = ZHEJIANG_HUD_VIDEO_TEXTURE_URL
  videoElement.preload = 'auto'
  videoElement.loop = true
  videoElement.muted = true
  videoElement.autoplay = true
  videoElement.playsInline = true
  videoElement.crossOrigin = 'anonymous'
  videoElement.setAttribute('playsinline', 'true')
  videoElement.setAttribute('muted', 'true')

  videoElement.addEventListener('error', () => {
    console.warn('[Cesium] 浙江圆环动态贴图加载失败，请确认 tietu.dv 是否为浏览器支持的视频编码')
  })

  const playPromise = videoElement.play()
  if (playPromise && typeof playPromise.catch === 'function') {
    playPromise.catch((error) => {
      console.warn('[Cesium] 浙江圆环动态贴图自动播放失败:', error)
    })
  }

  zhejiangHudVideoElement = videoElement
  stopZhejiangHudVideoRenderLoop()
  zhejiangHudVideoRenderTimer = window.setInterval(() => {
    if (!viewer || viewer.isDestroyed()) return
    viewer.scene.requestRender()
  }, 33)
  return zhejiangHudVideoElement
}

const disposeZhejiangHudVideoElement = () => {
  stopZhejiangHudVideoRenderLoop()
  if (!zhejiangHudVideoElement) return

  zhejiangHudVideoElement.pause()
  zhejiangHudVideoElement.removeAttribute('src')
  zhejiangHudVideoElement.load()
  zhejiangHudVideoElement = null
}

const clearZhejiangHighlightEntities = () => {
  disposeZhejiangHudVideoElement()
  if (!viewer) return
  if (zhejiangBodyEntity) {
    viewer.entities.remove(zhejiangBodyEntity)
    zhejiangBodyEntity = null
  }
  zhejiangOutlineEntities.forEach((entity) => viewer!.entities.remove(entity))
  zhejiangHudEntities.forEach((entity) => viewer!.entities.remove(entity))
  zhejiangOutlineEntities = []
  zhejiangHudEntities = []
}

// 加载浙江 GeoJSON 区域
const loadZhejiangRegion = async () => {
  if (!viewer) return

  try {
    const response = await fetch(ZHEJIANG_GEOJSON_URL)
    if (!response.ok) {
      console.warn('[Cesium] 浙江 GeoJSON 加载失败:', response.status)
      return
    }

    const rawBoundaryData = await response.json()
    const geoJson = normalizeBoundaryGeoJson(rawBoundaryData)
    if (!geoJson) {
      console.warn('[Cesium] 地理边界数据解析失败，需为 GeoJSON 或 TopoJSON')
      return
    }

    clearZhejiangHighlightEntities()

    if (zhejiangDistrictDataSource) {
      viewer.dataSources.remove(zhejiangDistrictDataSource, true)
      zhejiangDistrictDataSource = null
    }

    const extractedOuterBoundaryRing = extractMainOuterBoundaryRing(geoJson)
    const mainOuterBoundaryRing =
      extractedOuterBoundaryRing && extractedOuterBoundaryRing.length >= 4
        ? extractedOuterBoundaryRing
        : buildFallbackOuterBoundaryRing()
    if (!extractedOuterBoundaryRing || extractedOuterBoundaryRing.length < 4) {
      console.warn('[Cesium] 未能提取浙江整体外轮廓，已使用兜底包围盒继续渲染省级特效')
    }
    // 高德矢量层仅覆盖浙江范围，避免全图叠加
    applyZhejiangVectorCoverage(mainOuterBoundaryRing)
    const focusMetrics = computeZhejiangFocusMetrics(mainOuterBoundaryRing)
    zhejiangFocusLngLat = [focusMetrics.centerLng, focusMetrics.centerLat]
    zhejiangHudRadiusMeters = focusMetrics.hudRadiusMeters
    if (cameraInteractionMode !== 'drone-focus') {
      orbitAnchorCartesian = getCenterAnchorCartesian()
    }

    const outerRingHierarchy = new Cesium.PolygonHierarchy(
      mainOuterBoundaryRing.map((coord) => Cesium.Cartesian3.fromDegrees(coord[0], coord[1]))
    )
    const topOverlayTexture = getZhejiangTopOverlayTexture()
    zhejiangBodyEntity = viewer.entities.add({
      id: 'zhejiang-main-body',
      polygon: {
        hierarchy: outerRingHierarchy,
        height: ZHEJIANG_BASE_HEIGHT,
        extrudedHeight: ZHEJIANG_TOP_HEIGHT,
        closeTop: true,
        closeBottom: false,
        material: new Cesium.ImageMaterialProperty({
          image: topOverlayTexture,
          repeat: new Cesium.Cartesian2(1, 1),
          transparent: true,
          color: Cesium.Color.fromCssColorString('#89ffd6').withAlpha(0.34)
        }),
        classificationType: Cesium.ClassificationType.TERRAIN,
        perPositionHeight: false,
        outline: false
      }
    })

    // 加载浙江整体外边界轮廓
    loadZhejiangOutline(mainOuterBoundaryRing)
    createZhejiangHudRings()
    updateCityLabelVisibility()

    // 地市层贴地展示，突出整个浙江区域地形层次
    try {
      const dataSource = await Cesium.GeoJsonDataSource.load(geoJson, {
        stroke: Cesium.Color.fromCssColorString('#8fdff2').withAlpha(0.3),
        strokeWidth: 1.3,
        fill: Cesium.Color.fromCssColorString('#1f5d4b').withAlpha(0.2),
        clampToGround: true
      })
      viewer.dataSources.add(dataSource)
      zhejiangDistrictDataSource = dataSource

      const districtEntities = dataSource.entities.values
      districtEntities.forEach((entity) => {
        if (!entity.polygon) return
        entity.polygon.height = undefined
        entity.polygon.perPositionHeight = new Cesium.ConstantProperty(false)
        entity.polygon.extrudedHeight = undefined
        entity.polygon.material = new Cesium.ColorMaterialProperty(
          Cesium.Color.fromCssColorString('#1f5d4b').withAlpha(0.2)
        )
        entity.polygon.outline = new Cesium.ConstantProperty(true)
        entity.polygon.outlineColor = new Cesium.ConstantProperty(
          Cesium.Color.fromCssColorString('#8fdff2').withAlpha(0.34)
        )
      })
    } catch (districtLayerError) {
      console.warn('[Cesium] 浙江地市图层加载失败，已保留省级凸起与圆环特效:', districtLayerError)
    }

    console.info('[Cesium] 浙江 3D 区域加载完成')

    // 请求渲染更新
    viewer.scene.requestRender()

  } catch (error) {
    console.error('[Cesium] 加载浙江区域失败:', error)
  }
}

// TopoJSON 转 GeoJSON 简易实现
const topoJsonToGeoJson = (topo: any) => {
  if (!topo || !topo.objects) return null

  const objectKey = Object.keys(topo.objects)[0]
  if (!objectKey) return null

  const obj = topo.objects[objectKey]
  const arcs = topo.arcs || []
  const transform = topo.transform

  const decodeArc = (arcIndex: number) => {
    const arc = arcs[arcIndex < 0 ? ~arcIndex : arcIndex]
    if (!arc) return []

    let x = 0, y = 0
    const coords: [number, number][] = []

    for (const point of arc) {
      x += point[0]
      y += point[1]
      let lng = x
      let lat = y
      if (transform) {
        lng = x * transform.scale[0] + transform.translate[0]
        lat = y * transform.scale[1] + transform.translate[1]
      }
      coords.push([lng, lat])
    }

    return arcIndex < 0 ? coords.reverse() : coords
  }

  const decodeRing = (ring: number[]) => {
    const coords: [number, number][] = []
    for (const arcIndex of ring) {
      const arcCoords = decodeArc(arcIndex)
      coords.push(...arcCoords)
    }
    return coords
  }

  const features: any[] = []

  const processGeometry = (geom: any, properties: any) => {
    if (geom.type === 'Polygon') {
      const coordinates = geom.arcs.map((ring: number[]) => decodeRing(ring))
      features.push({
        type: 'Feature',
        properties,
        geometry: { type: 'Polygon', coordinates }
      })
    } else if (geom.type === 'MultiPolygon') {
      const coordinates = geom.arcs.map((polygon: number[][]) =>
        polygon.map((ring: number[]) => decodeRing(ring))
      )
      features.push({
        type: 'Feature',
        properties,
        geometry: { type: 'MultiPolygon', coordinates }
      })
    }
  }

  if (obj.type === 'GeometryCollection') {
    for (const geom of obj.geometries) {
      processGeometry(geom, geom.properties || {})
    }
  } else {
    processGeometry(obj, obj.properties || {})
  }

  return {
    type: 'FeatureCollection',
    features
  }
}

const normalizeBoundaryGeoJson = (rawData: any) => {
  if (!rawData || typeof rawData !== 'object') return null
  if (rawData.type === 'FeatureCollection' && Array.isArray(rawData.features)) {
    return rawData
  }
  return topoJsonToGeoJson(rawData)
}

// 绘制浙江整体外边界高亮
const loadZhejiangOutline = (mainOuterBoundaryRing: CoordinatePair[]) => {
  if (!viewer || mainOuterBoundaryRing.length < 4) return

  zhejiangOutlineEntities.forEach((entity) => viewer!.entities.remove(entity))
  zhejiangOutlineEntities = []

  const positions = mainOuterBoundaryRing.map((coord) =>
    Cesium.Cartesian3.fromDegrees(coord[0], coord[1], ZHEJIANG_OUTLINE_HEIGHT)
  )

  const glowOutline = viewer.entities.add({
    id: 'zhejiang-outline-glow',
    polyline: {
      positions,
      width: 7,
      material: new Cesium.PolylineGlowMaterialProperty({
        glowPower: 0.16,
        color: Cesium.Color.fromCssColorString('#00d6ff').withAlpha(0.32)
      }),
      clampToGround: false
    }
  })

  const coreOutline = viewer.entities.add({
    id: 'zhejiang-outline-core',
    polyline: {
      positions,
      width: 2.0,
      material: Cesium.Color.fromCssColorString('#9ff4ff').withAlpha(0.68),
      clampToGround: false
    }
  })

  zhejiangOutlineEntities.push(glowOutline, coreOutline)
}

const createZhejiangHudRings = () => {
  if (!viewer) return

  zhejiangHudEntities.forEach((entity) => viewer!.entities.remove(entity))
  zhejiangHudEntities = []

  const [focusLng, focusLat] = zhejiangFocusLngLat
  const ringRadius = Cesium.Math.clamp(zhejiangHudRadiusMeters, ZHEJIANG_MIN_HUD_RADIUS, ZHEJIANG_MAX_HUD_RADIUS)
  const videoTexture = ensureZhejiangHudVideoElement()
  const ringCenter = Cesium.Cartesian3.fromDegrees(focusLng, focusLat, ZHEJIANG_TOP_HEIGHT + 140)

  const ringGlow = viewer.entities.add({
    id: 'zhejiang-hud-ring-glow',
    position: ringCenter,
    ellipse: {
      semiMajorAxis: ringRadius * 1.12,
      semiMinorAxis: ringRadius * 1.12,
      material: Cesium.Color.fromCssColorString('#74e6ff').withAlpha(0.1),
      outline: false,
      height: ZHEJIANG_TOP_HEIGHT + 126
    }
  })

  const ringBase = viewer.entities.add({
    id: 'zhejiang-hud-ring-base',
    position: ringCenter,
    ellipse: {
      semiMajorAxis: ringRadius,
      semiMinorAxis: ringRadius,
      material: new Cesium.ImageMaterialProperty({
        image: videoTexture,
        transparent: true,
        color: Cesium.Color.fromCssColorString('#86f2ff').withAlpha(0.88)
      }),
      outline: true,
      outlineColor: Cesium.Color.fromCssColorString('#b9f9ff').withAlpha(0.5),
      height: ZHEJIANG_TOP_HEIGHT + 130
    }
  })

  const ringOuter = viewer.entities.add({
    id: 'zhejiang-hud-ring-outer',
    position: ringCenter,
    ellipse: {
      semiMajorAxis: ringRadius * 1.22,
      semiMinorAxis: ringRadius * 1.22,
      fill: false,
      outline: true,
      outlineColor: Cesium.Color.fromCssColorString('#c8fbff').withAlpha(0.55),
      height: ZHEJIANG_TOP_HEIGHT + 220
    }
  })

  const ringOuter2 = viewer.entities.add({
    id: 'zhejiang-hud-ring-outer2',
    position: ringCenter,
    ellipse: {
      semiMajorAxis: ringRadius * 1.38,
      semiMinorAxis: ringRadius * 1.38,
      fill: false,
      outline: true,
      outlineColor: Cesium.Color.fromCssColorString('#a0f0ff').withAlpha(0.28),
      height: ZHEJIANG_TOP_HEIGHT + 320
    }
  })

  zhejiangHudEntities.push(ringGlow, ringBase, ringOuter, ringOuter2)
}

// 加载全球暗化遮罩（非浙江区域显示深色）
const loadDarkMaskLayer = async () => {
  if (!viewer) return

  try {
    const existingMask = viewer.entities.getById('dark-mask-global')
    if (existingMask) {
      viewer.entities.remove(existingMask)
    }

    // 创建一个覆盖全球的深色半透明面（除浙江外）
    // 使用一个大矩形覆盖中国区域，然后浙江区域会覆盖在上面
    viewer.entities.add({
      id: 'dark-mask-global',
      rectangle: {
        coordinates: Cesium.Rectangle.fromDegrees(70, 0, 140, 60), // 覆盖东亚区域
        material: Cesium.Color.fromCssColorString('#050a08').withAlpha(0.56),
        height: 0,
        classificationType: Cesium.ClassificationType.TERRAIN
      }
    })

    console.info('[Cesium] 全球暗化遮罩已加载')
  } catch (error) {
    console.error('[Cesium] 加载暗化遮罩失败:', error)
  }
}

// 创建带箭头的城市标签 SVG
const createCityLabelIcon = (name: string, isCenter: boolean = false): string => {
  const bgColor = isCenter ? 'rgba(255, 100, 100, 0.85)' : 'rgba(0, 40, 80, 0.85)'
  const borderColor = isCenter ? '#ff6b6b' : '#00d4ff'
  const textColor = '#ffffff'
  const width = name.length * 14 + 24
  const height = 32
  const arrowHeight = 10

  const svg = `
<svg width="${width}" height="${height + arrowHeight}" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <filter id="labelGlow" x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur stdDeviation="2" result="blur"/>
      <feMerge>
        <feMergeNode in="blur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
  </defs>
  <rect x="1" y="1" width="${width - 2}" height="${height - 2}" rx="4" ry="4"
        fill="${bgColor}" stroke="${borderColor}" stroke-width="1.5" filter="url(#labelGlow)"/>
  <polygon points="${width / 2 - 6},${height} ${width / 2},${height + arrowHeight} ${width / 2 + 6},${height}"
           fill="${bgColor}" stroke="${borderColor}" stroke-width="1.5"/>
  <line x1="${width / 2 - 5}" y1="${height}" x2="${width / 2 + 5}" y2="${height}"
        stroke="${bgColor}" stroke-width="3"/>
  <text x="${width / 2}" y="${height / 2 + 5}" font-size="13" font-family="Microsoft YaHei"
        fill="${textColor}" text-anchor="middle" font-weight="600">${name}</text>
</svg>`

  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

const updateCityLabelVisibility = () => {
  if (!viewer || cityLabelEntities.length === 0) return
  const cameraHeight = viewer.camera.positionCartographic.height
  const isOverview = cameraHeight >= CITY_LABEL_OVERVIEW_SWITCH_HEIGHT

  cityLabelEntities.forEach((entity) => {
    const properties = entity.properties?.getValue(Cesium.JulianDate.now()) as
      | { priority?: boolean; isCenter?: boolean }
      | undefined
    const isPriority = properties?.priority ?? false
    const isCenter = properties?.isCenter ?? false
    entity.show = !isOverview || isCenter || isPriority
  })
}

// 创建城市标签
const createCityLabels = () => {
  if (!viewer) return

  cityLabelEntities.forEach((entity) => viewer!.entities.remove(entity))
  cityLabelEntities = []

  zhejiangCities.forEach((city) => {
    const isPriority = OVERVIEW_PRIORITY_CITY_NAMES.has(city.name)
    const labelEntity = viewer!.entities.add({
      id: `city-label-${city.name}`,
      position: Cesium.Cartesian3.fromDegrees(city.lng, city.lat, ZHEJIANG_CITY_LABEL_HEIGHT),
      billboard: {
        image: createCityLabelIcon(city.name, city.isCenter),
        verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
        horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
        heightReference: Cesium.HeightReference.NONE,
        disableDepthTestDistance: Number.POSITIVE_INFINITY,
        scale: 1.0,
        distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, SAFE_MAX_CAMERA_HEIGHT)
      },
      properties: {
        priority: isPriority,
        isCenter: !!city.isCenter
      }
    })
    cityLabelEntities.push(labelEntity)
  })

  updateCityLabelVisibility()
  console.info('[Cesium] 城市标签创建完成')
}

const resolveDroneAnchorFromEntity = (entity: Cesium.Entity): Cesium.Cartesian3 | null => {
  const directAnchor = entity.position?.getValue(Cesium.JulianDate.now())
  if (directAnchor) {
    return directAnchor
  }

  const rawEntityId = String(entity.id ?? '')
  if (!rawEntityId.startsWith('drone-')) {
    return null
  }

  const droneId = Number(rawEntityId.replace('drone-', ''))
  if (!Number.isFinite(droneId)) {
    return null
  }

  const droneItem = droneData.value.find((item) => item.id === droneId)
  if (!droneItem) {
    return null
  }

  return Cesium.Cartesian3.fromDegrees(droneItem.lng, droneItem.lat, droneItem.alt)
}

const focusDroneEntity = (entity: Cesium.Entity, duration = 2) => {
  if (!viewer) return
  const droneAnchor = resolveDroneAnchorFromEntity(entity)
  if (droneAnchor) {
    setOrbitAnchor(droneAnchor, 'drone-focus')
  } else {
    console.warn('[Cesium] 无法解析无人机锚点，保持当前锚点', { entityId: String(entity.id ?? '') })
  }
  hasOrbitStateSyncedFromCamera = false

  viewer.flyTo(entity, {
    duration,
    offset: new Cesium.HeadingPitchRange(
      0,
      Cesium.Math.toRadians(-45),
      Math.max(5000, getOrbitMinRange('drone-focus'))
    )
  }).then(() => {
    const latestDroneAnchor = resolveDroneAnchorFromEntity(entity)
    if (latestDroneAnchor) {
      setOrbitAnchor(latestDroneAnchor, 'drone-focus')
    }
    syncOrbitStateFromCurrentCamera()
    lastWheelZoomDirection = null
  })

  highlightDrone(entity)
}

const focusDroneById = (droneId: number) => {
  if (!viewer) {
    console.warn('[Cesium] 地图尚未初始化，无法定位无人机', droneId)
    return
  }
  const droneEntityId = `drone-${droneId}`
  const entity = viewer.entities.getById(droneEntityId)
  if (!entity) {
    console.warn('[Cesium] 未找到目标无人机实体，无法定位', {
      droneId,
      droneEntityId
    })
    return
  }
  focusDroneEntity(entity, 1.8)
}

// 添加点击事件
const setupClickHandler = () => {
  if (!viewer) return

  clickHandler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)

  clickHandler.setInputAction((movement: any) => {
    if (isOrbitRightDragActive) return
    const startPosition = movement?.position
    if (!startPosition) return
    isOrbitDragActive = true
    hasOrbitDragged = false
    orbitDragDistance = 0
    orbitDragStartedAt = Date.now()
    orbitLastMousePosition = new Cesium.Cartesian2(startPosition.x, startPosition.y)
  }, Cesium.ScreenSpaceEventType.LEFT_DOWN)

  clickHandler.setInputAction(() => {
    const wasDragging = hasOrbitDragged || isDragPerformanceMode
    isOrbitDragActive = false
    orbitDragStartedAt = 0
    orbitLastMousePosition = null
    setDragPerformanceMode(false)
    if (wasDragging) {
      syncGaodeVectorVisibility()
    }
  }, Cesium.ScreenSpaceEventType.LEFT_UP)

  clickHandler.setInputAction((movement: any) => {
    const startPosition = movement?.position
    if (!startPosition) return
    isOrbitDragActive = false
    setDragPerformanceMode(false)
    isOrbitRightDragActive = true
    orbitRightLastMousePosition = new Cesium.Cartesian2(startPosition.x, startPosition.y)
    hideHoverTooltip()
  }, Cesium.ScreenSpaceEventType.RIGHT_DOWN)

  clickHandler.setInputAction(() => {
    if (!isOrbitRightDragActive) return
    isOrbitRightDragActive = false
    orbitRightLastMousePosition = null
  }, Cesium.ScreenSpaceEventType.RIGHT_UP)

  clickHandler.setInputAction((movement: any) => {
    const endPosition = movement?.endPosition
    if (!endPosition) {
      if (isOrbitDragActive) {
        const wasDragging = hasOrbitDragged || isDragPerformanceMode
        isOrbitDragActive = false
        orbitDragStartedAt = 0
        setDragPerformanceMode(false)
        if (wasDragging) {
          syncGaodeVectorVisibility()
        }
      }
      if (isOrbitRightDragActive) {
        isOrbitRightDragActive = false
        orbitRightLastMousePosition = null
      }
      hideHoverTooltip()
      return
    }

    if (isOrbitRightDragActive && orbitRightLastMousePosition) {
      const dy = endPosition.y - orbitRightLastMousePosition.y
      orbitRightLastMousePosition = new Cesium.Cartesian2(endPosition.x, endPosition.y)
      if (Math.abs(dy) >= 0.5) {
        if (currentZoomMode === 'near' && !hasOrbitStateSyncedFromCamera) {
          syncOrbitStateFromCurrentCamera()
        }
        const zoomScale = Math.exp(dy * ORBIT_RIGHT_ZOOM_PER_PIXEL)
        if (Number.isFinite(zoomScale) && zoomScale > 0) {
          lastWheelZoomDirection = zoomScale > 1 ? 'out' : 'in'
          orbitRange = clampOrbitRange(orbitRange * zoomScale)
          orbitPitchRad = clampOrbitPitch(orbitPitchRad, orbitRange)
          clearGlobalReframeGuard()
          scheduleOrbitViewRender()
          switchByHeight('auto')
        }
      }
      hideHoverTooltip()
      return
    }

    if (isOrbitDragActive && orbitLastMousePosition) {
      const dx = endPosition.x - orbitLastMousePosition.x
      const dy = endPosition.y - orbitLastMousePosition.y
      orbitDragDistance += Math.hypot(dx, dy)
      orbitLastMousePosition = new Cesium.Cartesian2(endPosition.x, endPosition.y)

      const dragElapsedMs = orbitDragStartedAt > 0 ? Date.now() - orbitDragStartedAt : 0
      if (
        orbitDragDistance >= ORBIT_DRAG_ACTIVATE_PX &&
        dragElapsedMs >= ORBIT_DRAG_ACTIVATE_MS &&
        !hasOrbitDragged
      ) {
        hasOrbitDragged = true
        setDragPerformanceMode(true)
      }

      if (hasOrbitDragged) {
        orbitHeadingRad = normalizeOrbitHeading(orbitHeadingRad - dx * ORBIT_HEADING_PER_PIXEL)
        orbitPitchRad = clampOrbitPitch(orbitPitchRad - dy * ORBIT_PITCH_PER_PIXEL, orbitRange)
        orbitRange = clampOrbitRange(orbitRange)
        clearGlobalReframeGuard()
        scheduleOrbitViewRender()
        hideHoverTooltip()
        return
      }
    }

    const pickedObject = viewer!.scene.pick(endPosition)
    if (!Cesium.defined(pickedObject) || !pickedObject.id) {
      hideHoverTooltip()
      return
    }

    const entity = pickedObject.id as Cesium.Entity
    const info = extractHoverDroneInfo(entity)
    if (!info) {
      hideHoverTooltip()
      return
    }

    hoverDroneInfo.value = info
    updateHoverTooltipPosition(endPosition.x, endPosition.y)
  }, Cesium.ScreenSpaceEventType.MOUSE_MOVE)

  clickHandler.setInputAction((movement: any) => {
    if (hasOrbitDragged) {
      hasOrbitDragged = false
      return
    }

    const pickedObject = viewer!.scene.pick(movement.position)

    if (Cesium.defined(pickedObject) && pickedObject.id) {
      const entity = pickedObject.id as Cesium.Entity

      if (entity.id && String(entity.id).startsWith('drone-')) {
        focusDroneEntity(entity, 2)
      }
    }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

  clickHandler.setInputAction((delta: any) => {
    const wheelDelta = typeof delta === 'number' ? delta : Number((delta as any)?.delta ?? 0)
    if (!Number.isFinite(wheelDelta) || wheelDelta === 0) return

    if (currentZoomMode === 'near' && !hasOrbitStateSyncedFromCamera) {
      syncOrbitStateFromCurrentCamera()
    }

    const zoomScale = wheelDelta > 0 ? 0.88 : 1.12
    lastWheelZoomDirection = zoomScale > 1 ? 'out' : 'in'
    orbitRange = clampOrbitRange(orbitRange * zoomScale)
    orbitPitchRad = clampOrbitPitch(orbitPitchRad, orbitRange)
    clearGlobalReframeGuard()
    scheduleOrbitViewRender()
    switchByHeight('auto')
  }, Cesium.ScreenSpaceEventType.WHEEL)
}

// 高亮无人机
const highlightDrone = (entity: Cesium.Entity) => {
  // 重置所有无人机的缩放
  droneEntities.forEach((e) => {
    if (e.billboard) {
      e.billboard.scale = new Cesium.ConstantProperty(1.0)
    }
  })

  // 放大选中的无人机
  if (entity.billboard) {
    entity.billboard.scale = new Cesium.ConstantProperty(1.5)

    // 2秒后恢复
    setTimeout(() => {
      if (entity.billboard) {
        entity.billboard.scale = new Cesium.ConstantProperty(1.0)
      }
    }, 2000)
  }
}

const getCameraHeight = () => {
  if (!viewer) return 0
  return viewer.camera.positionCartographic.height
}

const emitZoomModeChange = (mode: ZoomMode, source: ZoomModeSource) => {
  emit('zoom-mode-change', {
    mode,
    source,
    height: getCameraHeight()
  })
}

const updateLabelLayerVisibility = () => {
  if (!viewer || !labelLayer) return

  const cameraHeight = viewer.camera.positionCartographic.height
  const visibleByMode = currentZoomMode === 'near'
  const visibleByHeight = cameraHeight <= LABEL_VISIBLE_MAX_HEIGHT
  const coolingDown = Date.now() < labelPausedUntil
  labelLayer.show = visibleByMode && visibleByHeight && !coolingDown
}

const updateCinematicEarthEffect = () => {
  if (!ENABLE_CUSTOM_EARTH_STYLE || !cinematicEarthStage) return
  cinematicEarthStage.enabled = currentZoomMode === 'global'
}

const clearGlobalReframeGuard = () => {
  isGlobalReframing = false
  if (globalReframeGuardTimer) {
    clearTimeout(globalReframeGuardTimer)
    globalReframeGuardTimer = null
  }
}

const updateCloseRangeDepthTestState = () => {
  if (!viewer || !isTerrainEnabledInScene) return
  if (!ENABLE_DEPTH_TEST_AGAINST_TERRAIN) {
    viewer.scene.globe.depthTestAgainstTerrain = false
    isCloseRangeDepthTestSuppressed = false
    return
  }

  if (currentZoomMode !== 'near') {
    viewer.scene.globe.depthTestAgainstTerrain = true
    isCloseRangeDepthTestSuppressed = false
    return
  }

  if (isCloseRangeDepthTestSuppressed) {
    if (orbitRange >= CLOSE_RANGE_DEPTH_TEST_DISABLE_EXIT_RANGE) {
      isCloseRangeDepthTestSuppressed = false
      viewer.scene.globe.depthTestAgainstTerrain = true
    } else {
      viewer.scene.globe.depthTestAgainstTerrain = false
    }
  } else if (orbitRange <= CLOSE_RANGE_DEPTH_TEST_DISABLE_ENTER_RANGE) {
    isCloseRangeDepthTestSuppressed = true
    viewer.scene.globe.depthTestAgainstTerrain = false
  } else {
    viewer.scene.globe.depthTestAgainstTerrain = true
  }
}

const setDragPerformanceMode = (active: boolean) => {
  if (!viewer || isDragPerformanceMode === active) return
  isDragPerformanceMode = active
  viewer.scene.globe.maximumScreenSpaceError = active
    ? DRAG_MAX_SCREEN_SPACE_ERROR
    : GLOBE_MAX_SCREEN_SPACE_ERROR
  updateCloseRangeDepthTestState()
  viewer.scene.requestRender()
}

const normalizeOrbitHeading = (heading: number) => {
  const normalized = heading % Cesium.Math.TWO_PI
  return normalized >= 0 ? normalized : normalized + Cesium.Math.TWO_PI
}

const setOrbitAnchor = (anchor: Cesium.Cartesian3, mode: CameraInteractionMode) => {
  orbitAnchorCartesian = Cesium.Cartesian3.clone(anchor)
  const previousMode = cameraInteractionMode
  cameraInteractionMode = mode
  if (previousMode !== mode) {
    emit('camera-mode-change', mode)
  }
}

const getCenterAnchorSwitchDuration = (anchorDistanceMeters: number, rangeDeltaMeters: number) => {
  const distanceRatio = Cesium.Math.clamp(anchorDistanceMeters / CENTER_ANCHOR_SWITCH_DISTANCE_REF, 0, 1)
  const rangeRef = Math.max(getOrbitMinRange('overview'), 1000)
  const rangeRatio = Cesium.Math.clamp(rangeDeltaMeters / (rangeRef * 1.6), 0, 1)
  const dynamicDuration =
    CENTER_ANCHOR_SWITCH_DURATION + distanceRatio * 0.5 + rangeRatio * 0.35
  return Cesium.Math.clamp(
    dynamicDuration,
    CENTER_ANCHOR_SWITCH_DURATION,
    CENTER_ANCHOR_SWITCH_MAX_DURATION
  )
}

const switchToCenterAnchor = () => {
  if (!viewer || viewer.isDestroyed()) return
  viewer.camera.cancelFlight()
  clearGlobalReframeGuard()
  syncOrbitStateFromCurrentCamera()
  const sourceAnchor = Cesium.Cartesian3.clone(orbitAnchorCartesian)
  const targetAnchor = getCenterAnchorCartesian()
  const sourceRange = orbitRange
  const targetRange = clampOrbitRange(sourceRange, 'overview')
  const targetPitch = clampOrbitPitch(orbitPitchRad, targetRange)
  const targetHeading = normalizeOrbitHeading(orbitHeadingRad)
  const anchorDistance = Cesium.Cartesian3.distance(sourceAnchor, targetAnchor)
  const rangeDelta = Math.abs(targetRange - sourceRange)
  const duration = getCenterAnchorSwitchDuration(anchorDistance, rangeDelta)

  setOrbitAnchor(targetAnchor, 'overview')
  orbitHeadingRad = targetHeading
  orbitPitchRad = targetPitch
  orbitRange = targetRange
  updateCloseRangeDepthTestState()
  hasOrbitStateSyncedFromCamera = false
  lastWheelZoomDirection = null
  isOrbitDragActive = false
  hasOrbitDragged = false
  orbitDragDistance = 0
  orbitDragStartedAt = 0
  orbitLastMousePosition = null
  setDragPerformanceMode(false)

  if (anchorDistance < 1 && rangeDelta < 1) {
    hasOrbitStateSyncedFromCamera = true
    applyOrbitViewImmediately()
    return
  }

  const targetSphere = new Cesium.BoundingSphere(targetAnchor, Math.max(1, targetRange * 0.08))
  viewer.camera.flyToBoundingSphere(targetSphere, {
    offset: new Cesium.HeadingPitchRange(targetHeading, targetPitch, targetRange),
    duration,
    easingFunction: Cesium.EasingFunction.QUADRATIC_IN_OUT,
    complete: () => {
      hasOrbitStateSyncedFromCamera = true
      applyOrbitViewImmediately()
    },
    cancel: () => {
      syncOrbitStateFromCurrentCamera()
      applyOrbitViewImmediately()
    }
  })
}

const resetOrbitStateToGlobalDefaults = () => {
  setOrbitAnchor(getCenterAnchorCartesian(), 'overview')
  orbitHeadingRad = normalizeOrbitHeading(Cesium.Math.toRadians(GLOBE_CINEMATIC_HEADING))
  orbitPitchRad = clampOrbitPitch(Cesium.Math.toRadians(GLOBE_CINEMATIC_PITCH), GLOBE_CINEMATIC_HEIGHT)
  orbitRange = clampOrbitRange(GLOBE_CINEMATIC_HEIGHT, 'overview')
  updateCloseRangeDepthTestState()
  hasOrbitStateSyncedFromCamera = true
}

const syncOrbitStateFromCurrentCamera = () => {
  if (!viewer || viewer.isDestroyed()) return

  const cameraHeading = viewer.camera.heading
  const cameraPitch = viewer.camera.pitch
  const cameraHeight = viewer.camera.positionCartographic.height
  const distanceToCenter = Cesium.Cartesian3.distance(viewer.camera.positionWC, orbitAnchorCartesian)

  const fallbackRange = Number.isFinite(cameraHeight) ? cameraHeight : GLOBE_CINEMATIC_HEIGHT
  const nextRange = Number.isFinite(distanceToCenter) && distanceToCenter > 0 ? distanceToCenter : fallbackRange
  orbitHeadingRad = normalizeOrbitHeading(Number.isFinite(cameraHeading) ? cameraHeading : orbitHeadingRad)
  orbitPitchRad = clampOrbitPitch(Number.isFinite(cameraPitch) ? cameraPitch : orbitPitchRad, nextRange)
  orbitRange = clampOrbitRange(nextRange)
  updateCloseRangeDepthTestState()
  hasOrbitStateSyncedFromCamera = true
}

const applyOrbitViewImmediately = () => {
  if (!viewer || viewer.isDestroyed()) return
  orbitPitchRad = clampOrbitPitch(orbitPitchRad, orbitRange)
  updateCloseRangeDepthTestState()
  viewer.camera.lookAt(
    orbitAnchorCartesian,
    new Cesium.HeadingPitchRange(orbitHeadingRad, orbitPitchRad, orbitRange)
  )
  viewer.camera.lookAtTransform(Cesium.Matrix4.IDENTITY)
  viewer.scene.requestRender()
}

const scheduleOrbitViewRender = () => {
  if (!viewer || viewer.isDestroyed()) return
  orbitRenderPending = true
  if (orbitRenderRafId !== null) return
  orbitRenderRafId = window.requestAnimationFrame(() => {
    orbitRenderRafId = null
    if (!orbitRenderPending) return
    orbitRenderPending = false
    applyOrbitViewImmediately()
  })
}

// 设置相机初始视角到无人机分布区域
const setInitialDroneOverview = () => {
  if (!viewer || droneData.value.length === 0) return
  const centerAnchor = getCenterAnchorCartesian()
  const [centerLng, centerLat] = zhejiangFocusLngLat
  setOrbitAnchor(centerAnchor, 'overview')
  hasOrbitStateSyncedFromCamera = false
  const maxDistanceToCenter = droneData.value.reduce((maxDistance, drone) => {
    const distance = getDistanceMeters(centerLng, centerLat, drone.lng, drone.lat)
    return Math.max(maxDistance, distance)
  }, 0)
  const targetSphereRadius = Math.max(maxDistanceToCenter * 1.25, 1000)
  const targetRange = clampOrbitRange(
    Math.max(targetSphereRadius * 2.4, getOrbitMinRange('overview') * 1.25),
    'overview'
  )
  orbitHeadingRad = normalizeOrbitHeading(Cesium.Math.toRadians(8))
  orbitPitchRad = clampOrbitPitch(
    Cesium.Math.toRadians(ENABLE_TERRAIN ? -58 : -86),
    targetRange
  )
  orbitRange = targetRange
  updateCloseRangeDepthTestState()
  const targetSphere = new Cesium.BoundingSphere(centerAnchor, targetSphereRadius)

  // 默认展示无人机分布总览，并严格保持围绕中心锚点对齐
  viewer.camera.flyToBoundingSphere(targetSphere, {
    offset: new Cesium.HeadingPitchRange(orbitHeadingRad, orbitPitchRad, orbitRange),
    duration: 1.5,
    complete: () => {
      syncOrbitStateFromCurrentCamera()
      lastWheelZoomDirection = null
      applyOrbitViewImmediately()
    },
    cancel: () => {
      syncOrbitStateFromCurrentCamera()
      applyOrbitViewImmediately()
    }
  })
}

const flyToGlobalView = (duration = 1.5) => {
  if (!viewer) return

  resetOrbitStateToGlobalDefaults()
  const targetCenter = orbitAnchorCartesian
  const targetSphere = new Cesium.BoundingSphere(
    targetCenter,
    Cesium.Ellipsoid.WGS84.maximumRadius * 1.02
  )

  isGlobalReframing = true
  if (globalReframeGuardTimer) {
    clearTimeout(globalReframeGuardTimer)
    globalReframeGuardTimer = null
  }

  const guardTimeoutMs = Math.max(500, Math.round(duration * 1000) + GLOBAL_REFRAME_GUARD_EXTRA_MS)
  globalReframeGuardTimer = window.setTimeout(() => {
    clearGlobalReframeGuard()
  }, guardTimeoutMs)

  viewer.camera.flyToBoundingSphere(targetSphere, {
    offset: new Cesium.HeadingPitchRange(
      orbitHeadingRad,
      orbitPitchRad,
      orbitRange
    ),
    duration,
    complete: () => {
      clearGlobalReframeGuard()
    },
    cancel: () => {
      clearGlobalReframeGuard()
    }
  })
}

const fitGlobalView = () => {
  flyToGlobalView(1.5)
}

const setBaseImageryVisibility = (mode: ZoomMode) => {
  if (!nearLayer) return
  if (gaodeVectorLayer) {
    syncGaodeVectorVisibility(true)
  }
  if (useSharedBaseImageryLayer) {
    nearLayer.show = true
    return
  }
  if (!globalLayer) return
  nearLayer.show = mode === 'near'
  globalLayer.show = mode === 'global'
}

const applyZoomMode = (mode: ZoomMode, source: ZoomModeSource) => {
  if (!viewer || !nearLayer || (!useSharedBaseImageryLayer && !globalLayer)) {
    console.warn('[Cesium] 图层尚未就绪，跳过缩放模式切换', {
      hasViewer: !!viewer,
      hasNearLayer: !!nearLayer,
      hasGlobalLayer: !!globalLayer,
      useSharedBaseImageryLayer,
      targetMode: mode
    })
    return
  }
  if (currentZoomMode === mode) return

  currentZoomMode = mode
  lastWheelZoomDirection = null
  updateCloseRangeDepthTestState()
  setBaseImageryVisibility(mode)

  // 切到全球模式后始终回正到全球机位，避免从无人机斜视角继承姿态导致地球偏移/消失。
  if (mode === 'global') {
    fitGlobalView()
  }

  updateLabelLayerVisibility()
  updateCinematicEarthEffect()
  emitZoomModeChange(mode, source)
}

const switchByHeight = (source: ZoomModeSource = 'auto') => {
  if (!viewer) return
  if (!ENABLE_AUTO_GLOBAL_SWITCH && source === 'auto') {
    updateLabelLayerVisibility()
    return
  }
  if (source === 'auto' && isGlobalReframing) {
    // 全球重定位飞行动画进行中时，跳过自动模式抖动切换。
    return
  }

  const height = viewer.camera.positionCartographic.height
  const pitchDeg = Cesium.Math.toDegrees(viewer.camera.pitch)
  let targetMode: ZoomMode = currentZoomMode
  if (props.forceGlobal) {
    targetMode = 'global'
  } else if (currentZoomMode === 'near') {
    const allowAutoEnterByWheel = source !== 'auto' || lastWheelZoomDirection === 'out'
    const shouldEnterByHeight = height >= GLOBE_SWITCH_ENTER_HEIGHT
    const shouldEnterByObliquePose =
      Number.isFinite(pitchDeg) &&
      pitchDeg > AUTO_GLOBAL_REFRAME_PITCH_THRESHOLD_DEG &&
      height >= GLOBE_SWITCH_EXIT_HEIGHT
    const shouldEnterGlobal = (shouldEnterByHeight || shouldEnterByObliquePose) && allowAutoEnterByWheel
    targetMode = shouldEnterGlobal ? 'global' : 'near'
  } else {
    targetMode = height <= GLOBE_SWITCH_EXIT_HEIGHT ? 'near' : 'global'
  }

  if (targetMode === currentZoomMode) {
    updateLabelLayerVisibility()
    return
  }

  applyZoomMode(targetMode, source)
}

const scheduleSwitchByHeight = (source: ZoomModeSource = 'auto') => {
  if (switchByHeightDebounceTimer) {
    clearTimeout(switchByHeightDebounceTimer)
  }
  switchByHeightDebounceTimer = window.setTimeout(() => {
    switchByHeightDebounceTimer = null
    switchByHeight(source)
  }, CAMERA_SWITCH_DEBOUNCE_MS)
}

// 验证相机位置是否安全，防止 NaN 错误
const validateAndClampCameraHeight = (): boolean => {
  if (!viewer) return true

  const cameraHeight = viewer.camera.positionCartographic.height

  // 仅将非数值视为异常；近地形近距离场景下出现负高并不一定是无效状态
  if (!Number.isFinite(cameraHeight)) {
    console.warn('[Cesium] 检测到无效相机高度，正在重置', { cameraHeight })
    resetCameraToSafePosition()
    return false
  }

  return true
}

// 重置相机到安全位置
const resetCameraToSafePosition = () => {
  if (!viewer || viewer.isDestroyed()) return

  try {
    flyToGlobalView(0.5)

    // 确保切换到全球模式
    if (currentZoomMode !== 'global' && nearLayer && (useSharedBaseImageryLayer || globalLayer)) {
      currentZoomMode = 'global'
      setBaseImageryVisibility('global')
      updateLabelLayerVisibility()
      emitZoomModeChange('global', 'auto')
    }
  } catch (error) {
    console.error('[Cesium] 相机重置失败:', error)
  }
}

// 根据筛选条件更新无人机可见性
const updateDroneVisibility = () => {
  if (!viewer) return

  droneEntities.forEach((entity) => {
    const entityProps = entity.properties?.getValue(Cesium.JulianDate.now()) as
      | { droneId?: number; category?: DroneCategory; isIllegal?: boolean }
      | undefined

    const droneId = entityProps?.droneId
    const category = entityProps?.category
    const isIllegal = entityProps?.isIllegal ?? false

    // 判断是否应该显示
    let shouldShow = true

    // 如果只显示未授权无人机
    if (showIllegalOnly.value) {
      shouldShow = isIllegal
    } else {
      // 按类型筛选
      if (category && !activeCategories.value.has(category)) {
        shouldShow = false
      }
    }

    entity.show = shouldShow

    // 同步隐藏/显示对应的飞行轨迹
    if (droneId) {
      const pathEntity = viewer!.entities.getById(`path-${droneId}`)
      if (pathEntity) {
        pathEntity.show = shouldShow && entityProps?.status === 'flying'
      }
    }
  })
}

// 设置筛选类型
const setFilterCategories = (categories: DroneCategory[]) => {
  activeCategories.value = new Set(categories)
  updateDroneVisibility()
}

// 切换只显示未授权无人机
const setShowIllegalOnly = (value: boolean) => {
  showIllegalOnly.value = value
  updateDroneVisibility()
}

// 获取无人机统计数据
const getDroneStats = () => {
  const total = droneData.value.length
  const illegal = droneData.value.filter((d) => d.isIllegal).length
  const byCategory = {
    logistics: droneData.value.filter((d) => d.category === 'logistics').length,
    commercial: droneData.value.filter((d) => d.category === 'commercial').length,
    public: droneData.value.filter((d) => d.category === 'public').length,
    traffic: droneData.value.filter((d) => d.category === 'traffic').length
  }
  return { total, illegal, byCategory }
}

const getCameraInteractionMode = () => cameraInteractionMode

watch(
  () => props.drones,
  (incomingDrones) => {
    syncDroneData(incomingDrones)
    if (!viewer || viewer.isDestroyed()) return
    syncDroneEntitiesWithLatestData()
  },
  { deep: true }
)

defineExpose({
  focusDroneById,
  setFilterCategories,
  setShowIllegalOnly,
  getDroneStats,
  getCameraInteractionMode,
  switchToCenterAnchor
})

watch(
  () => props.forceGlobal,
  (forceGlobal) => {
    if (!viewer) return
    if (forceGlobal) {
      if (currentZoomMode !== 'global') {
        applyZoomMode('global', 'manual')
      } else {
        fitGlobalView()
        emitZoomModeChange('global', 'manual')
      }
      return
    }

    // 退出全球视角时，强制切换回近景并飞回无人机区域
    if (currentZoomMode === 'global') {
      clearGlobalReframeGuard()
      currentZoomMode = 'near'
      setBaseImageryVisibility('near')
      setInitialDroneOverview()
      updateLabelLayerVisibility()
      updateCinematicEarthEffect()
      emitZoomModeChange('near', 'manual')
    } else {
      switchByHeight('manual')
    }
  }
)

onMounted(async () => {
  if (!viewerRef.value) return

  // 配置 Cesium Ion Token
  Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJlYWE1OWUxNy1mMWZiLTQzYjYtYTQ0OS1kMWFjYmFkNjc5YzciLCJpZCI6NTc3MzMsImlhdCI6MTYyNzg5ODI3Mn0.XcKpgANiY19MC4bdFUXMVEBToBmqS8kuYpUlxJHYZxk'

  const mapProvider = getMapProviderType()
  const tiandituToken = import.meta.env.VITE_TDT_TOKEN || DEFAULT_TDT_TOKEN
  const gaodeKey = import.meta.env.VITE_AMAP_KEY || DEFAULT_AMAP_KEY
  activeMapProvider = mapProvider
  activeGaodeKey = mapProvider === 'gaode' ? gaodeKey : ''

  try {
    if (!tiandituToken) {
      console.warn('[Cesium] 未配置天地图 token：VITE_TDT_TOKEN')
    }
    if (mapProvider === 'gaode' && !gaodeKey) {
      console.warn('[Cesium] 未配置高德 key：VITE_AMAP_KEY')
    }

    // 限制并发请求，降低触发天地图 429 的概率
    Cesium.RequestScheduler.maximumRequestsPerServer = TDT_MAX_REQUESTS_PER_SERVER
    Cesium.RequestScheduler.maximumRequests = TDT_MAX_REQUESTS
    if (mapProvider === 'tianditu') {
      TDT_SUBDOMAINS.forEach((subdomain) => {
        Cesium.RequestScheduler.requestsByServer[`t${subdomain}.tianditu.gov.cn:443`] = TDT_MAX_REQUESTS_PER_SERVER
        Cesium.RequestScheduler.requestsByServer[`t${subdomain}.tianditu.gov.cn:80`] = TDT_MAX_REQUESTS_PER_SERVER
      })
    }

    const terrainProvider = await createTerrainProvider()
    const baseProvider = createBaseLayerProvider(mapProvider, tiandituToken, gaodeKey)
    baseProvider.errorEvent.addEventListener((tileProviderError) => {
      const now = Date.now()
      const mutableTileError = tileProviderError as any
      const statusCode = mutableTileError?.error?.statusCode
      if (statusCode === 429) {
        mutableTileError.retry = false
      }
      if (now - lastBaseErrorLogAt < LABEL_ERROR_LOG_INTERVAL_MS) {
        return
      }
      if (statusCode === 429) {
        console.warn(`[Cesium] ${mapProvider} 影像底图触发 429，已停止本次重试`)
      } else {
        console.error(`[Cesium] ${mapProvider} 影像底图加载失败:`, tileProviderError)
      }
      lastBaseErrorLogAt = now
    })

    viewer = new Cesium.Viewer(viewerRef.value, {
      animation: false,
      timeline: false,
      baseLayerPicker: false,
      geocoder: false,
      homeButton: false,
      sceneModePicker: false,
      navigationHelpButton: false,
      fullscreenButton: false,
      vrButton: false,
      infoBox: false,
      selectionIndicator: false,
      baseLayer: false,
      terrainProvider,
      // 使用自定义银河星空 SkyBox
      skyBox: new Cesium.SkyBox({
        sources: {
          positiveX: skyBoxRight,
          negativeX: skyBoxLeft,
          positiveY: skyBoxTop,
          negativeY: skyBoxBottom,
          positiveZ: skyBoxFront,
          negativeZ: skyBoxBack
        }
      }),
      skyAtmosphere: false, // 完全禁用大气层，消除朦胧效果
      // 提升渲染质量
      contextOptions: {
        webgl: {
          alpha: false,
          depth: true,
          stencil: false,
          antialias: true,
          powerPreference: 'high-performance'
        }
      },
      // 启用按需渲染，提升性能
      requestRenderMode: true,
      maximumRenderTimeChange: Infinity
    })

    // 固定渲染分辨率为 1.0，避免高分屏下 Bloom 等后处理成倍消耗 GPU
    viewer.resolutionScale = 1.0

    console.info('[Cesium] 已加载银河星空 SkyBox，启用按需渲染模式')

    nearLayer = viewer.imageryLayers.addImageryProvider(baseProvider)
    // 应用地球影像滤镜
    nearLayer.brightness = IMAGERY_FILTER.brightness
    nearLayer.contrast = IMAGERY_FILTER.contrast
    nearLayer.saturation = IMAGERY_FILTER.saturation
    nearLayer.hue = IMAGERY_FILTER.hue
    nearLayer.gamma = IMAGERY_FILTER.gamma

    useSharedBaseImageryLayer = mapProvider === 'tianditu' && ENABLE_TDT_SHARED_BASE_LAYER
    if (useSharedBaseImageryLayer) {
      // 近景/全球共用同一底图层，避免重复请求同源瓦片
      globalLayer = nearLayer
      nearLayer.show = true
      console.info('[Cesium] 已启用天地图单底图共享层模式，降低瓦片并发峰值')
    } else {
      const globalProvider = createGlobalLayerProvider(mapProvider, tiandituToken, gaodeKey)
      globalProvider.errorEvent.addEventListener((tileProviderError) => {
        const now = Date.now()
        const mutableTileError = tileProviderError as any
        const statusCode = mutableTileError?.error?.statusCode
        if (statusCode === 429) {
          mutableTileError.retry = false
        }
        if (now - lastGlobalErrorLogAt < LABEL_ERROR_LOG_INTERVAL_MS) {
          return
        }
        if (statusCode === 429) {
          console.warn('[Cesium] 全球底图触发 429，已停止本次重试')
        } else {
          console.error('[Cesium] 全球底图加载失败:', tileProviderError)
        }
        lastGlobalErrorLogAt = now
      })
      globalLayer = viewer.imageryLayers.addImageryProvider(globalProvider)
      globalLayer.show = false
      // 应用地球影像滤镜
      globalLayer.brightness = IMAGERY_FILTER.brightness
      globalLayer.contrast = IMAGERY_FILTER.contrast
      globalLayer.saturation = IMAGERY_FILTER.saturation
      globalLayer.hue = IMAGERY_FILTER.hue
      globalLayer.gamma = IMAGERY_FILTER.gamma
    }

    if (mapProvider === 'gaode') {
      // 先使用浙江固定包围盒兜底，GeoJSON 加载后会替换成动态边界
      applyZhejiangVectorCoverage(null)
      console.info('[Cesium] 已启用高德矢量叠加图层（近景）')
    }

    // 添加标注层（天地图注记；可通过环境变量关闭）
    const labelProvider = createLabelLayerProvider(mapProvider, tiandituToken)
    if (labelProvider) {
      labelLayer = viewer.imageryLayers.addImageryProvider(labelProvider)

      // 429 限流时启用熔断，避免重试风暴
      labelProvider.errorEvent.addEventListener((tileProviderError) => {
        const now = Date.now()
        const statusCode = (tileProviderError as any)?.error?.statusCode

        if (statusCode === 429) {
          tileProviderError.retry = false
          labelPausedUntil = now + LABEL_COOLDOWN_MS
          if (labelLayer) {
            labelLayer.show = false
          }

          if (labelLayerResumeTimer) {
            clearTimeout(labelLayerResumeTimer)
          }
          labelLayerResumeTimer = window.setTimeout(() => {
            updateLabelLayerVisibility()
          }, LABEL_COOLDOWN_MS)
        }

        // 日志节流，避免控制台被刷爆
        if (now - lastLabelErrorLogAt >= LABEL_ERROR_LOG_INTERVAL_MS) {
          if (statusCode === 429) {
            console.warn(
              `[Cesium] ${mapProvider} 标注图层触发 429，已临时暂停标注层请求`
            )
          } else {
            console.error(`[Cesium] ${mapProvider} 标注图层加载失败:`, tileProviderError)
          }
          lastLabelErrorLogAt = now
        }
      })
    } else if (!ENABLE_TDT_LABEL_LAYER) {
      console.info('[Cesium] 已根据配置关闭天地图注记层：VITE_TDT_ENABLE_LABEL_LAYER=false')
    }

    // 根据相机高度控制图层显示，缩小时自动切换到全球底图
    viewer.camera.percentageChanged = 0.02
    viewer.scene.screenSpaceCameraController.enableRotate = false
    viewer.scene.screenSpaceCameraController.enableTilt = false
    viewer.scene.screenSpaceCameraController.enableLook = false
    viewer.scene.screenSpaceCameraController.enableTranslate = false
    viewer.scene.screenSpaceCameraController.enableZoom = false
    viewer.scene.screenSpaceCameraController.inertiaSpin = 0
    viewer.scene.screenSpaceCameraController.inertiaTranslate = 0
    viewer.scene.screenSpaceCameraController.inertiaZoom = 0
    // 自定义轨道控制下关闭碰撞检测，避免近景俯仰/旋转时与引擎约束“抢相机”导致跳视角
    viewer.scene.screenSpaceCameraController.enableCollisionDetection = false
    viewer.scene.screenSpaceCameraController.minimumZoomDistance = getOrbitMinRange('overview')
    viewer.scene.screenSpaceCameraController.maximumZoomDistance = SAFE_MAX_CAMERA_HEIGHT
    cameraChangedListener = () => {
      // 先进行安全验证，如果相机位置异常则重置
      if (!validateAndClampCameraHeight()) {
        return
      }
      if (isOrbitDragActive) {
        return
      }
      scheduleSwitchByHeight('auto')
      const now = Date.now()
      if (now - lastCameraVisualSyncAt < CAMERA_VISUAL_SYNC_INTERVAL_MS) {
        return
      }
      lastCameraVisualSyncAt = now
      updateCityLabelVisibility()
      if (gaodeVectorLayer) {
        syncGaodeVectorVisibility()
      }
    }
    viewer.camera.changed.addEventListener(cameraChangedListener)

    switchByHeight('auto')

    if (props.forceGlobal) {
      applyZoomMode('global', 'manual')
    }

    // 隐藏版权信息
    const creditContainer = viewer.cesiumWidget.creditContainer as HTMLElement | null
    if (creditContainer) {
      creditContainer.style.display = 'none'
    }

    const terrainEnabled = ENABLE_TERRAIN && !(viewer.terrainProvider instanceof Cesium.EllipsoidTerrainProvider)
    isTerrainEnabledInScene = terrainEnabled
    viewer.scene.verticalExaggeration = terrainEnabled ? TERRAIN_VERTICAL_EXAGGERATION : 1.0
    viewer.scene.verticalExaggerationRelativeHeight = 0
    viewer.scene.globe.depthTestAgainstTerrain = terrainEnabled && ENABLE_DEPTH_TEST_AGAINST_TERRAIN
    isCloseRangeDepthTestSuppressed = false
    // 开启真实地形时启用光照，平面模式保持关闭避免画面发灰
    viewer.scene.globe.enableLighting = terrainEnabled && ENABLE_TERRAIN_LIGHTING

    // 优化场景渲染设置
    viewer.scene.postProcessStages.fxaa.enabled = true
    const bloomStage = viewer.scene.postProcessStages.bloom
    bloomStage.enabled = ENABLE_FUTURISTIC_BLOOM
    bloomStage.uniforms.glowOnly = false
    bloomStage.uniforms.contrast = BLOOM_CONTRAST
    bloomStage.uniforms.brightness = BLOOM_BRIGHTNESS
    bloomStage.uniforms.delta = BLOOM_DELTA
    bloomStage.uniforms.sigma = BLOOM_SIGMA
    bloomStage.uniforms.stepSize = BLOOM_STEP_SIZE
    viewer.scene.globe.maximumScreenSpaceError = GLOBE_MAX_SCREEN_SPACE_ERROR
    viewer.scene.globe.tileCacheSize = GLOBE_TILE_CACHE_SIZE
    viewer.scene.globe.preloadAncestors = false
    viewer.scene.globe.preloadSiblings = false
    viewer.scene.fog.enabled = false
    viewer.scene.fog.density = 0.0
    viewer.scene.globe.showGroundAtmosphere = false // 禁用地球大气光晕

    // 启用太阳和月亮
    viewer.clock.shouldAnimate = true
    if (viewer.scene.sun) {
      viewer.scene.sun.show = false
    }
    if (viewer.scene.moon) {
      viewer.scene.moon.show = false
    }

    // 仅在地形光照开启时启用动态大气光照
    viewer.scene.globe.dynamicAtmosphereLighting = terrainEnabled && ENABLE_TERRAIN_LIGHTING
    viewer.scene.globe.dynamicAtmosphereLightingFromSun = terrainEnabled && ENABLE_TERRAIN_LIGHTING

    // 设置地球基底颜色（纯黑色，与星空融合）
    viewer.scene.globe.baseColor = Cesium.Color.BLACK
    console.info('[Cesium] 地形状态:', {
      enabled: terrainEnabled,
      provider: terrainEnabled ? TERRAIN_PROVIDER : 'ellipsoid',
      verticalExaggeration: viewer.scene.verticalExaggeration
    })

    if (ENABLE_CUSTOM_EARTH_STYLE) {
      // 添加海洋/陆地分离渲染后处理着色器
      oceanLandStage = new Cesium.PostProcessStage({
        fragmentShader: OCEAN_LAND_SHADER,
        name: 'oceanLandSeparation'
      })
      viewer.scene.postProcessStages.add(oceanLandStage)
      console.info('[Cesium] 海洋/陆地分离渲染着色器已启用')

      cinematicEarthStage = new Cesium.PostProcessStage({
        fragmentShader: CINEMATIC_EARTH_SHADER,
        name: 'cinematicEarth'
      })
      viewer.scene.postProcessStages.add(cinematicEarthStage)
      updateCinematicEarthEffect()
      console.info('[Cesium] 电影感地球后处理已启用')
    } else {
      oceanLandStage = null
      cinematicEarthStage = null
      console.info('[Cesium] 已关闭自定义地球着色，使用原始影像样式')
    }

    // 先压暗背景区域，再加载浙江主体
    loadDarkMaskLayer()

    // 加载浙江 GeoJSON 并创建 3D 挤出效果
    loadZhejiangRegion()

    // 创建城市标签
    if (ENABLE_ZHEJIANG_CITY_LABELS) {
      createCityLabels()
    }

    // 同步无人机实体、位置与轨迹
    syncDroneEntitiesWithLatestData()

    // 设置点击事件
    setupClickHandler()

    // 默认展示无人机分布总览
    setInitialDroneOverview()

    // 添加渲染错误处理，防止相机位置异常导致渲染崩溃
    viewer.scene.renderError.addEventListener((scene, error) => {
      console.error('[Cesium] 渲染错误，正在尝试恢复:', error)
      resetCameraToSafePosition()
    })

    // 地图初始化完成，隐藏加载动画
    isLoading.value = false
    console.info('[Cesium] 地图初始化完成')

  } catch (error) {
    console.error('Cesium 初始化失败:', error)
  }
})

onUnmounted(() => {
  clearGlobalReframeGuard()

  if (cameraChangedListener && viewer) {
    viewer.camera.changed.removeEventListener(cameraChangedListener)
    cameraChangedListener = null
  }

  if (labelLayerResumeTimer) {
    clearTimeout(labelLayerResumeTimer)
    labelLayerResumeTimer = null
  }
  if (switchByHeightDebounceTimer) {
    clearTimeout(switchByHeightDebounceTimer)
    switchByHeightDebounceTimer = null
  }
  if (orbitRenderRafId !== null) {
    cancelAnimationFrame(orbitRenderRafId)
    orbitRenderRafId = null
  }
  orbitRenderPending = false

  if (clickHandler) {
    clickHandler.destroy()
    clickHandler = null
  }

  clearAllDronePulseAnimations()
  droneTrailPointMap.clear()
  disposeZhejiangHudVideoElement()

  if (viewer) {
    clearZhejiangHighlightEntities()
    cityLabelEntities.forEach((entity) => viewer!.entities.remove(entity))
    cityLabelEntities = []
    if (zhejiangDistrictDataSource) {
      viewer.dataSources.remove(zhejiangDistrictDataSource, true)
      zhejiangDistrictDataSource = null
    }
  }

  if (viewer) {
    viewer.destroy()
    viewer = null
  }
  nearLayer = null
  globalLayer = null
  gaodeVectorLayer = null
  gaodeVectorCoverageRectangle = null
  gaodeVectorVisibleState = true
  gaodeVectorPendingVisible = null
  gaodeVectorPendingSince = 0
  gaodeVectorLastToggleAt = 0
  labelLayer = null
  useSharedBaseImageryLayer = false
  isDragPerformanceMode = false
  isTerrainEnabledInScene = false
  isCloseRangeDepthTestSuppressed = false
  lastCameraVisualSyncAt = 0
  oceanLandStage = null
  cinematicEarthStage = null
  labelPausedUntil = 0
  lastBaseErrorLogAt = 0
  lastGlobalErrorLogAt = 0
  lastVectorErrorLogAt = 0
  lastLabelErrorLogAt = 0
  currentZoomMode = 'near'
  isOrbitDragActive = false
  hasOrbitDragged = false
  orbitDragDistance = 0
  orbitDragStartedAt = 0
  isOrbitRightDragActive = false
  orbitRightLastMousePosition = null
  orbitLastMousePosition = null
  activeMapProvider = 'tianditu'
  activeGaodeKey = ''
  zhejiangFocusLngLat = [ZHEJIANG_CENTER.lng, ZHEJIANG_CENTER.lat]
  zhejiangHudRadiusMeters = ZHEJIANG_DEFAULT_HUD_RADIUS
  orbitAnchorCartesian = Cesium.Cartesian3.clone(DEFAULT_ORBIT_CENTER_CARTESIAN)
  droneEntities = []
  zhejiangBodyEntity = null
  zhejiangOutlineEntities = []
  zhejiangHudEntities = []
  zhejiangHudVideoElement = null
  zhejiangHudVideoRenderTimer = null
  pulseTimerByDroneId = new Map<number, number>()
  droneTrailPointMap = new Map<number, Cesium.Cartesian3[]>()
  cityLabelEntities = []
  zhejiangTopOverlayTexture = null
  hoverDroneInfo.value = null
})
</script>

<style scoped>
.cesium-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.cesium-wrapper::before {
  content: '';
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  background:
    radial-gradient(ellipse at center, rgba(0, 0, 0, 0) 52%, rgba(0, 6, 18, 0.56) 100%),
    linear-gradient(
      180deg,
      rgba(1, 10, 28, 0.12) 0%,
      rgba(1, 10, 28, 0.05) 34%,
      rgba(0, 188, 255, 0.07) 50%,
      rgba(0, 0, 0, 0) 63%,
      rgba(0, 0, 0, 0.2) 100%
    );
}

.cesium-container {
  width: 100%;
  height: 100%;
}

.drone-hover-tooltip {
  position: absolute;
  z-index: 40;
  min-width: 10vw;
  max-width: 15vw;
  padding: 0.6vh 0.6vw;
  border-radius: var(--radius-md, 0.5vw);
  border: 0.08vw solid rgba(0, 180, 255, 0.45);
  background: rgba(1, 6, 17, 0.92);
  backdrop-filter: blur(0.6vw);
  box-shadow: 0 0 1.2vw rgba(0, 180, 255, 0.22);
  color: #ffffff;
  pointer-events: none;
  transform: translateY(-100%);
}

.tooltip-name {
  font-size: var(--font-body, 0.75vw);
  font-weight: 600;
  color: #d7f5ff;
  margin-bottom: 0.4vh;
}

.tooltip-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.6vw;
  font-size: var(--font-small, 0.65vw);
  line-height: 1.4;
}

.tooltip-label {
  color: rgba(255, 255, 255, 0.62);
}

.tooltip-value {
  color: #ffffff;
  font-family: 'Consolas', 'Monaco', monospace;
}

.tooltip-value.status-flying {
  color: #ffaa00;
}

.tooltip-value.status-online {
  color: #00ff88;
}

.tooltip-value.status-offline {
  color: #a8b0c2;
}

/* 未授权无人机样式 */
.drone-hover-tooltip.is-illegal {
  border-color: rgba(255, 45, 45, 0.6);
  box-shadow: 0 0 1.5vw rgba(255, 45, 45, 0.35);
}

.tooltip-name.illegal-name {
  color: #ff6b6b;
}

.illegal-badge {
  display: inline-block;
  background: #ff2d2d;
  color: #ffffff;
  font-size: 0.55vw;
  padding: 0.1vh 0.3vw;
  border-radius: 0.2vw;
  margin-right: 0.3vw;
  font-weight: 600;
  animation: illegalPulse 1s ease-in-out infinite;
}

@keyframes illegalPulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

.tooltip-value.status-illegal {
  color: #ff2d2d;
  font-weight: 600;
}

/* 隐藏 Cesium 默认控件 */
.cesium-container :deep(.cesium-viewer-bottom) {
  display: none;
}

.cesium-container :deep(.cesium-viewer-toolbar) {
  display: none;
}

.cesium-container :deep(.cesium-viewer-animationContainer) {
  display: none;
}

.cesium-container :deep(.cesium-viewer-timelineContainer) {
  display: none;
}

.cesium-container :deep(.cesium-viewer-fullscreenContainer) {
  display: none;
}

/* 加载动画遮罩 */
.loading-overlay {
  position: absolute;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(ellipse at center, #0a1a30 0%, #051025 100%);
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
}

.loading-spinner {
  position: relative;
  width: 80px;
  height: 80px;
}

.spinner-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 3px solid transparent;
  border-top-color: #00d4ff;
  animation: spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
}

.spinner-ring:nth-child(1) {
  animation-delay: -0.45s;
}

.spinner-ring:nth-child(2) {
  inset: 8px;
  border-top-color: #00ff88;
  animation-delay: -0.3s;
}

.spinner-ring:nth-child(3) {
  inset: 16px;
  border-top-color: #ffaa00;
  animation-delay: -0.15s;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-text {
  display: flex;
  align-items: baseline;
  gap: 0.25rem;
  color: #d7f5ff;
  font-size: 1rem;
  font-weight: 500;
  letter-spacing: 0.15em;
}

.loading-title {
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
}

.loading-dots span {
  display: inline-block;
  animation: dotPulse 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) {
  animation-delay: 0s;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dotPulse {
  0%, 80%, 100% {
    opacity: 0;
    transform: scale(0.6);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 淡出过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.6s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
