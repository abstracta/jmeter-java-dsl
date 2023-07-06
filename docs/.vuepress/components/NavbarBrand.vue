<script setup lang="ts">
// changed from original to include autolink to other DSL
import AutoLink from '@theme/AutoLink.vue'
import {
  ClientOnly,
  useRouteLocale,
  useSiteLocaleData,
  withBase,
} from '@vuepress/client'
import { computed, h } from 'vue'
import type { FunctionalComponent } from 'vue'
import { useDarkMode, useThemeLocaleData } from '@vuepress/theme-default/lib/client/composables/index.js'

import { onBeforeMount } from 'vue'

// changed from original to enable google tag manager
onBeforeMount(() => {
  let recaptchaScript = document.createElement('noscript');
  recaptchaScript.innerHTML = '<iframe src="https://www.googletagmanager.com/ns.html?id=GTM-PHSGKLD" height="0" width="0" style="display:none;visibility:hidden"></iframe>'
  document.body.appendChild(recaptchaScript)
});

const routeLocale = useRouteLocale()
const siteLocale = useSiteLocaleData()
const themeLocale = useThemeLocaleData()
const isDarkMode = useDarkMode()

const navbarBrandLink = computed(
  () => themeLocale.value.home || routeLocale.value
)
const navbarBrandTitle = computed(() => siteLocale.value.title)
const navbarBrandLogo = computed(() => {
  if (isDarkMode.value && themeLocale.value.logoDark !== undefined) {
    return themeLocale.value.logoDark
  }
  return themeLocale.value.logo
})
const NavbarBrandLogo: FunctionalComponent = () => {
  if (!navbarBrandLogo.value) return null
  const img = h('img', {
    class: 'logo',
    src: withBase(navbarBrandLogo.value),
    alt: navbarBrandTitle.value,
  })
  if (themeLocale.value.logoDark === undefined) {
    return img
  }
  // wrap brand logo with <ClientOnly> to avoid ssr-mismatch
  // when using a different brand logo in dark mode
  return h(ClientOnly, () => img)
}
</script>

<template>
  <RouterLink :to="navbarBrandLink">
    <NavbarBrandLogo style="margin-right: 0"/><span style="font-size: 1.6rem"> .java</span>
    <!-- Stripped site title from original theme -->
  </RouterLink>
  <hr class="vertical-divider"/>
  <AutoLink :item="{link : 'https://abstracta.github.io/jmeter-dotnet-dsl', text: '.net', style: 'font-size: 1.6rem; color: #a502ce'}"/>
</template>
