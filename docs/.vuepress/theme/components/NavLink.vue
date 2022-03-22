<template>
  <RouterLink
    v-if="isInternal"
    class="nav-link"
    :to="link"
    :exact="exact"
    @focusout.native="focusoutAction"
  >
    <!-- changed from original file to include icons support in links -->
    <template v-if="item.icon">
      <font-awesome-icon :icon="item.icon" size="xl" @mouseover="hover = true" @mouseleave="hover = false" :class="{ 'fa-fade': hover }"/>
    </template>
    <template v-else>
      {{ item.text }}
    </template>
  </RouterLink>
  <a
    v-else
    :href="link"
    class="nav-link external"
    :target="target"
    :rel="rel"
    @focusout="focusoutAction"
  >
    <!-- changed from original file to include icons support in links -->
    <template v-if="item.icon">
      <font-awesome-icon :icon="item.icon" size="xl" @mouseover="hover = true" @mouseleave="hover = false" :class="{ 'fa-fade': hover }"/>
    </template>
    <template v-else>
      {{ item.text }}
    </template>
    <OutboundLink v-if="isBlankTarget" />
  </a>
</template>

<script>
import { isExternal, isMailto, isTel, ensureExt } from '@parent-theme/util'

export default {
  name: 'NavLink',

  props: {
    item: {
      required: true
    }
  },

  // changed from original file to include icons hover animation
  data() {
    return { hover : false };
  },

  computed: {

    link () {
      return ensureExt(this.item.link)
    },

    exact () {
      if (this.$site.locales) {
        return Object.keys(this.$site.locales).some(rootLink => rootLink === this.link)
      }
      return this.link === '/'
    },

    isNonHttpURI () {
      return isMailto(this.link) || isTel(this.link)
    },

    isBlankTarget () {
      return this.target === '_blank'
    },

    isInternal () {
      return !isExternal(this.link) && !this.isBlankTarget
    },

    target () {
      if (this.isNonHttpURI) {
        return null
      }
      if (this.item.target) {
        return this.item.target
      }
      return isExternal(this.link) ? '_blank' : ''
    },

    rel () {
      if (this.isNonHttpURI) {
        return null
      }
      if (this.item.rel === false) {
        return null
      }
      if (this.item.rel) {
        return this.item.rel
      }
      return this.isBlankTarget ? 'noopener noreferrer' : null
    }
  },

  methods: {
    focusoutAction () {
      this.$emit('focusout')
    }
  }
}
</script>
