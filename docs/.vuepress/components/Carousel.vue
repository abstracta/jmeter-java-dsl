<script setup lang="ts">
import { ref, Ref } from 'vue';
import { ClientOnly } from '@vuepress/client'

const viewport: Ref<HTMLElement | null> = ref(null)
const isDragging = ref(false)
let scrollX = ref(0)
let startX = 0
// this value must match the width assigned to testimonial in style and padding in track
const scrollSize = (400 + 10 * 2) * 2

const startDrag = (event: MouseEvent | TouchEvent): void => {
    event.preventDefault()
    startX = getX(event)
    isDragging.value = true
    scrollX.value = viewport.value!.scrollLeft
    document.addEventListener('mousemove', drag)
    document.addEventListener('touchmove', drag)
    document.addEventListener('mouseup', stopDrag)
    document.addEventListener('touchend', stopDrag)
}

const getX = (event: MouseEvent | TouchEvent): number => isMouseEvent(event) ? (event as MouseEvent).pageX : (event as TouchEvent).touches[0].pageX

const isMouseEvent = (event: MouseEvent | TouchEvent): boolean => event.type.startsWith('mouse')

const drag = (event: MouseEvent | TouchEvent): void => {
    const dragX = getX(event) - startX
    viewport.value!.scrollLeft = scrollX.value - dragX
}

const stopDrag = () => {
    document.removeEventListener('mousemove', drag)
    document.removeEventListener('touchmove', drag)
    document.removeEventListener('mouseup', stopDrag)
    document.removeEventListener('touchend', stopDrag)
    scrollX.value = viewport.value!.scrollLeft
    isDragging.value = false
}

const scrollLeft = () => scroll(-scrollSize)

const scroll = (size: number) => viewport.value!.scroll({ top: 0, left: viewport.value!.scrollLeft + size, behavior: 'smooth' })

const scrollRight = () => scroll(scrollSize)

// scrollWidth is not yet properly solved in onMount, and using a method that depends con scrollX is the way we found to get the actual value
const canScrollRight = (scrollX: number): boolean => {
    if (!viewport.value) {
        return true
    }
    let maxScroll = viewport.value!.scrollWidth - viewport.value!.offsetWidth
    return maxScroll === 0 ? true : scrollX < maxScroll
}

const onScroll = () => {
    if (isDragging.value) {
        return;
    }
    scrollX.value = viewport.value!.scrollLeft
}
</script>

<template>
    <ClientOnly>
        <section :class="['carousel', isDragging && 'is-dragging']">
            <div class="carousel-viewport" ref="viewport" @mousedown="startDrag" @touchstart="startDrag" @scroll="onScroll">
                <div class="carousel-track">
                    <slot />
                </div>
            </div>
            <button @click="scrollLeft" class="carousel-control carousel-prev" :disabled="scrollX <= 0">
                <font-awesome-icon icon="fa-solid fa-chevron-left" />
            </button>
            <button @click="scrollRight" class="carousel-control carousel-next" :disabled="!canScrollRight(scrollX)">
                <font-awesome-icon icon="fa-solid fa-chevron-right" />
            </button>
        </section>
    </ClientOnly>
</template>
  
<style>
.carousel {
    position: relative;
    box-sizing: border-box;
    touch-action: pan-y;
    overscroll-behavior: none;
    margin: 10px 0px;
}

.carousel.is-dragging {
    touch-action: none;
}

.carousel * {
    box-sizing: border-box;
}

.carousel-viewport {
    cursor: grab;
    overflow: auto;
}

.carousel-track {
    display: flex;
    position: relative;
    flex-direction: column;
    flex-wrap: wrap;
    align-items: flex-start;
    gap: 20px;
    height: 550px;
}

.carousel-control {
    --control-size: 20px;
    box-sizing: content-box;
    background: transparent;
    border-radius: 0;
    width: var(--control-size);
    height: var(--control-size);
    text-align: center;
    font-size: var(--control-size);
    padding: 0;
    color: var(--c-text);
    display: flex;
    justify-content: center;
    align-items: center;
    position: absolute;
    border: 0;
    cursor: pointer;
    margin: 0 10px;
    top: 50%;
    transform: translateY(-50%);
}

.carousel-control:hover {
    color: var(--c-text-lighter);
}

.carousel-control:disabled {
    cursor: not-allowed;
    opacity: 0.5;
}

.carousel-prev {
    left: 0;
}

.carousel-next {
    right: 0;
}
</style>