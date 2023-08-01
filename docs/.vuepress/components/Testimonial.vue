<script setup lang="ts">
import type { PropType } from 'vue'
import { computed, toRefs, ref } from 'vue'

interface Testimonial {
    source: string;
    name: string;
    position?: string;
}

const root = ref(null)

const props = defineProps({
  item: {
    type: Object as PropType<Testimonial>,
    required: true,
  },
})

const { item } = toRefs(props);

const icon = computed(() => {
    let source = item.value.source;
    if (source.includes('twitter.com')) {
        return 'fa-brands fa-twitter';
    } else if (source.includes('linkedin.com')) {
        return 'fa-brands fa-linkedin';
    } else if (source.includes('github.com')) {
        return 'fa-brands fa-github';
    } else {
        return 'fa-solid fa-globe';
    }
});
</script>

<template>
    <div class="testimonial" ref="root">
        <div class="source">
        <span>
            <font-awesome-icon :icon="icon"/>
            <AutoLink :item="{link: item.source, text: item.name}"/>
        </span>
        </div>
        <div class="position">{{ item.position }}</div>
        <div class="comment"><slot/></div>
    </div>
</template>

<style>
.testimonial {
  display: flex;
  flex-direction: column;
  background-color: var(--c-bg-lighter);
  border-radius: 15px;
  padding: 15px;
  width: 400px;
  white-space: normal;

  scroll-snap-stop: auto;
  flex-shrink: 0;
  justify-content: center;
   /* Fix iOS scrolling */
   transform: translateZ(0);
}

.testimonial .position {
  margin-top: 5px;
  color: var(--c-text-lighter);
}

.testimonial .comment p {
  margin: 10px 0px 0px 0px;
}

.testimonial .fa-github {
  color: var(--c-text);
}

.testimonial .fa-globe {
  color: #aaaaaa;
}

.testimonial .fa-linkedin {
  color: rgb(10, 102, 194);
}

.testimonial .fa-twitter {
  color: rgb(29, 155, 240);
}
</style>