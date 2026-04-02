<template>
  <canvas ref="canvasRef" class="particle-canvas"></canvas>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue'

defineOptions({ name: 'ParticleLayer' })

interface Particle {
  x: number
  y: number
  angle: number // 固定发散角度
  speed: number
  initialSize: number
  size: number
  alpha: number
  color: string
  distance: number
  maxDistance: number
}

const canvasRef = ref<HTMLCanvasElement>()
let ctx: CanvasRenderingContext2D | null = null
let particles: Particle[] = []
let animationId: number | null = null
let width = 0
let height = 0
let earthCenterX = 0
let earthCenterY = 0

// 粒子配置
const PARTICLE_COUNT = 500

// 颜色配置：以 #356A7B 为主色调
const COLORS = [
  '53, 106, 123',    // #356A7B 主色
  '53, 106, 123',
  '53, 106, 123',
  '45, 95, 115',
  '65, 120, 140',
  '40, 90, 110',
  '75, 130, 150',
  '50, 100, 120',
  '60, 115, 135'
]

// 创建粒子：从地球中心向外直线发散
const createParticle = (isNew: boolean = false): Particle => {
  const colorBase = COLORS[Math.floor(Math.random() * COLORS.length)]

  // 发散角度：全方位360度，但稍微偏向上方（模拟从地球表面发出）
  const angle = Math.random() * Math.PI * 2

  // 速度：快速发散
  const speed = Math.random() * 1.2 + 0.5 // 0.5-1.7

  // 初始大小
  const initialSize = Math.random() * 3.5 + 1.5 // 1.5-5像素

  // 最大发散距离
  const maxDistance = Math.random() * 400 + 200 // 200-600

  // 起始位置：地球中心附近（有一定随机偏移，模拟从地球表面不同位置发出）
  const startOffset = Math.random() * 30 + 10 // 从地球表面10-40像素处开始
  let distance: number

  if (isNew) {
    // 新粒子从地球表面附近开始
    distance = startOffset
  } else {
    // 初始化时，粒子随机分布在发散路径上
    distance = startOffset + Math.random() * maxDistance * 0.7
  }

  const x = earthCenterX + Math.cos(angle) * distance
  const y = earthCenterY + Math.sin(angle) * distance

  return {
    x,
    y,
    angle, // 固定角度，保持直线运动
    speed,
    initialSize,
    size: initialSize,
    alpha: Math.random() * 0.45 + 0.25,
    color: colorBase,
    distance,
    maxDistance
  }
}

const initParticles = () => {
  particles = []
  for (let i = 0; i < PARTICLE_COUNT; i++) {
    particles.push(createParticle(false))
  }
}

const updateParticle = (p: Particle, index: number) => {
  // 直线向外发散运动（角度固定，不旋转）
  p.distance += p.speed
  p.x = earthCenterX + Math.cos(p.angle) * p.distance
  p.y = earthCenterY + Math.sin(p.angle) * p.distance

  // 根据距离计算缩小比例：距离越远，粒子越小
  const progress = p.distance / p.maxDistance
  p.size = p.initialSize * Math.max(0, 1 - progress * 0.9) // 逐渐缩小
  p.alpha = Math.max(0, 0.5 * (1 - progress * 0.85)) // 透明度衰减

  // 粒子消失或超出边界后重置
  if (p.size <= 0.2 || p.distance >= p.maxDistance ||
      p.x < -100 || p.x > width + 100 || p.y < -100 || p.y > height + 100) {
    const newParticle = createParticle(true)
    particles[index] = newParticle
  }
}

const drawParticle = (p: Particle) => {
  if (!ctx || p.size <= 0) return

  const glowSize = p.size * 2
  const gradient = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, glowSize)
  gradient.addColorStop(0, `rgba(${p.color}, ${p.alpha})`)
  gradient.addColorStop(0.35, `rgba(${p.color}, ${p.alpha * 0.55})`)
  gradient.addColorStop(0.7, `rgba(${p.color}, ${p.alpha * 0.15})`)
  gradient.addColorStop(1, `rgba(${p.color}, 0)`)

  ctx.beginPath()
  ctx.arc(p.x, p.y, glowSize, 0, Math.PI * 2)
  ctx.fillStyle = gradient
  ctx.fill()
}

const animate = () => {
  if (!ctx) return

  ctx.clearRect(0, 0, width, height)

  particles.forEach((p, index) => {
    updateParticle(p, index)
    drawParticle(p)
  })

  animationId = requestAnimationFrame(animate)
}

const resizeCanvas = () => {
  if (!canvasRef.value) return

  width = window.innerWidth
  height = window.innerHeight

  // 地球中心位置：屏幕中央偏下（模拟地图上地球的位置）
  earthCenterX = width * 0.5
  earthCenterY = height * 0.55

  canvasRef.value.width = width
  canvasRef.value.height = height
}

onMounted(() => {
  if (!canvasRef.value) return

  ctx = canvasRef.value.getContext('2d')
  if (!ctx) return

  resizeCanvas()
  initParticles()
  animate()

  window.addEventListener('resize', resizeCanvas)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
  window.removeEventListener('resize', resizeCanvas)
  particles = []
  ctx = null
})
</script>

<style scoped>
.particle-canvas {
  position: absolute;
  inset: 0;
  z-index: 5;
  pointer-events: none;
  opacity: 0.8;
}
</style>
