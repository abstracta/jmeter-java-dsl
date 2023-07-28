<script setup lang="ts">
// Most of this logic is based on https://github.com/ismail9k/vue3-carousel/blob/master/src/components/Carousel.ts
import { ref, reactive, Ref, computed, onUnmounted, onMounted, nextTick } from 'vue';
import { ClientOnly } from '@vuepress/client'

const track: Ref<Element | null> = ref(null)
const root: Ref<Element | null> = ref(null)
let isTouch = false
const isSliding = ref(false)
let startPosition = 0
const isDragging = ref(false)
const dragged = reactive({ val: 0 })
// this value must match the width assigned to testimonial in style and padding in track
const columnWidth = 400 + 10 * 2
const columnsPerWindow = ref(0)
const transitionMillis = 300
const throttleTime = 16

const currentColumnIndex = ref(0)

let transitionTimer: ReturnType<typeof setTimeout> | null

onMounted(() => {
    nextTick(() => {
        columnsPerWindow.value = Math.floor((root.value?.clientWidth ?? 0) / columnWidth)
    })
})

onUnmounted(() => {
    if (transitionTimer) {
        clearTimeout(transitionTimer)
    }
})

const prev = (): void => {
    slideToColumn(currentColumnIndex.value - columnsPerWindow.value)
}

const next = (): void => {
    slideToColumn(currentColumnIndex.value + columnsPerWindow.value)
}

const slideToColumn = (columnIndex: number): void => {
    const currentVal = getNumberInRange(columnIndex, 0, columnsCount() - columnsPerWindow.value)
    if (currentColumnIndex.value === currentVal || isSliding.value) {
        return
    }
    isSliding.value = true
    currentColumnIndex.value = currentVal
    transitionTimer = setTimeout((): void => {
        isSliding.value = false
    }, transitionMillis)
}

function getNumberInRange(val: number, min: number, max: number): number {
    return Math.min(Math.max(val, min), max)
}

const handleDragStart = (event: MouseEvent): void => {
    event.preventDefault()
    if (event.button !== 0) {
        return
    }
    isTouch = false
    handleDragStartEvent(event.clientX, 'mousemove', 'mouseup')
}

function handleDragStartEvent(position: number, moveEvent: string, stopEvent: string): void {
    if (isSliding.value) {
        return;
    }
    startPosition = position
    document.addEventListener(moveEvent, handleDragging, true)
    document.addEventListener(stopEvent, handleDragEnd, true)
}

const handleTouchStart = (event: TouchEvent): void => {
    isTouch = true
    handleDragStartEvent(event.touches[0].clientX, 'touchmove', 'touchend')
}

const handleDragging = throttle((event: MouseEvent & TouchEvent): void => {
    isDragging.value = true
    let endPosition = isTouch ? event.touches[0].clientX : event.clientX
    dragged.val = endPosition - startPosition
}, throttleTime)

function throttle(fn: (...args: any[]) => unknown, limit: number): typeof fn {
    let inThrottle: boolean
    if (!limit) {
        return fn;
    }
    return function (...args: any[]) {
        const self = this
        if (!inThrottle) {
            fn.apply(self, args)
            inThrottle = true
            setTimeout(() => (inThrottle = false), limit)
        }
    }
}

const handleDragEnd = (): void => {
    let tolerance = Math.sign(dragged.val) * 0.4
    let draggedColumns = Math.round(dragged.val / columnWidth + tolerance)

    // Prevent clicking if there are clicked columns
    if (draggedColumns && !isTouch) {
        let captureClick = (e: MouseEvent) => {
            e.stopPropagation()
            window.removeEventListener('click', captureClick, true)
        }
        window.addEventListener('click', captureClick, true)
    }

    slideToColumn(currentColumnIndex.value - draggedColumns)
    dragged.val = 0
    isDragging.value = false
    document.removeEventListener(isTouch ? 'touchmove' : 'mousemove', handleDragging, true)
    document.removeEventListener(isTouch ? 'touchend' : 'mouseup', handleDragEnd, true)
}

const columnsCount = (): number => {
    if (track.value == null) {
        return 1
    }
    // we add 20 since we are always considering each column with 20 of gap, but on margins carousel is 0
    let trackWidth = track.value?.scrollWidth + 20;
    return Math.max(trackWidth / columnWidth, 1)
}

const trackStyle = computed(() => {
    let columnsToScroll = getNumberInRange(currentColumnIndex.value, 0, columnsCount() - columnsPerWindow.value)
    const scroll = columnsToScroll * columnWidth
    return {
        transform: `translateX(${dragged.val - scroll}px)`,
        transition: `${isSliding.value ? transitionMillis : 0}ms`,
        margin: '',
        width: `100%`,
    }
})
</script>

<template>
    <ClientOnly>
        <section :class="['carousel', isDragging && 'is-dragging']" ref="root">
            <div class="viewport" @mousedown="handleDragStart" @touchstart="handleTouchStart">
                <div class="track" :style="trackStyle" ref="track">
                    <slot />
                </div>
            </div>
            <button type="button" title="previous testimonials"
                :class="['carousel-control', 'prev-slide', currentColumnIndex <= 0 && 'carousel-control-disabled']"
                @click="prev">
                <font-awesome-icon icon="fa-solid fa-chevron-left" />
            </button>
            <button type="button" title="next testimonials"
                :class="['carousel-control', 'next-slide', currentColumnIndex >= (columnsCount() - columnsPerWindow) && 'carousel-control-disabled']"
                @click="next">
                <font-awesome-icon icon="fa-solid fa-chevron-right" />
            </button>
        </section>
    </ClientOnly>
</template>