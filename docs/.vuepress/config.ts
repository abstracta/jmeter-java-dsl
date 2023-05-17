import { defineUserConfig } from '@vuepress/cli'
import { defaultTheme } from '@vuepress/theme-default'
import { getDirname, path, fs } from '@vuepress/utils'
import { searchPlugin } from '@vuepress/plugin-search'
import { googleAnalyticsPlugin } from '@vuepress/plugin-google-analytics'
import { mdEnhancePlugin } from "vuepress-plugin-md-enhance"
import { repoLinkSolverPlugin } from "./plugins/repoLinkSolverPlugin"
import { includedRelativeLinkSolverPlugin } from "./plugins/includedRelativeLinkSolverPlugin"
import { copyCodePlugin } from "vuepress-plugin-copy-code2"
// import type { NavLink } from '@vuepress/theme-default/lib/shared/index.js'


const __dirname = getDirname(import.meta.url)

// interface NavLink { icon: [string] }
// interface ExtendedThemeConfig extends DefaultThemeOptions { navbar: [IconNavLink] }
// interface ExtendedUserConfig extends UserConfig {theme: ExtendedThemeConfig}
const REPO_LINK = "https://github.com/abstracta/jmeter-java-dsl"


export default defineUserConfig({
  lang: 'en-US',
  title: 'jmeter-java-dsl',
  description: 'Simple JMeter performance tests API',
  base: '/jmeter-java-dsl/',
  head: [
    ['link', { rel: 'shortcut icon', href: '/jmeter-java-dsl/favicon.ico'}]
  ],
  // restrict pattern to avoid going into included pages
  pagePatterns: ["*.md", "*/index.md", "!.vuepress", "!node_modules"],
  markdown: {
    headers: {
      level: [2, 3, 4]
    }
  },
  theme: defaultTheme({
    logo: '/logo.svg',
    // repo: 'abstracta/jmeter-java-dsl',
    editLink: false,
    lastUpdated: false,
    contributors: false,
    navbar: [
      {
        text: 'Guide',
        link: '/guide/',
      },
      {
        text: 'Motivation',
        link: '/motivation/',
      },
      {
        link: "https://discord.gg/WNSn5hqmSd",
        icon: ['fab', 'discord']
      },
      {
        link: REPO_LINK,
        icon: ['fab', 'github']
      }
    ],
    sidebarDepth: 3
  }),
  alias: {
    '@theme/NavbarBrand.vue': path.resolve(__dirname, './components/NavbarBrand.vue'),
    '@theme/AutoLink.vue': path.resolve(__dirname, './components/AutoLink.vue'),
    '@theme/HomeHero.vue': path.resolve(__dirname, './components/HomeHero.vue'),
    '@theme/HomeFeatures.vue': path.resolve(__dirname, './components/HomeFeatures.vue'),
  },
  plugins: [
    searchPlugin({ maxSuggestions: 10 }),
    googleAnalyticsPlugin({ id: 'GTM-PHSGKLD' }),
    mdEnhancePlugin({
      include: {
        deep: true,
        resolveImagePath: true,
        resolveLinkPath: true,
        resolvePath: (filePath: string, cwd: string | null) => {
          let ret = path.join(cwd, filePath)
          if (!fs.existsSync(ret)) {
            throw new Error(`File ${ret} not found.`)
          }
          return ret;
        }
      }
    }),
    repoLinkSolverPlugin({ repoUrl: REPO_LINK }),
    includedRelativeLinkSolverPlugin({}),
    copyCodePlugin({ pure: true })
  ],
})