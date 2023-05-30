import { defineUserConfig } from '@vuepress/cli'
import { defaultTheme } from '@vuepress/theme-default'
import { getDirname, path, fs } from '@vuepress/utils'
import { searchPlugin } from '@vuepress/plugin-search'
import { mediumZoomPlugin } from '@vuepress/plugin-medium-zoom'
import { containerPlugin } from '@vuepress/plugin-container'
import { mdEnhancePlugin } from "vuepress-plugin-md-enhance"
import { repoLinkSolverPlugin } from "./plugins/repoLinkSolverPlugin"
import { includedRelativeLinkSolverPlugin } from "./plugins/includedRelativeLinkSolverPlugin"
import { copyCodePlugin } from "vuepress-plugin-copy-code2"

const __dirname = getDirname(import.meta.url)

const REPO_LINK = "https://github.com/abstracta/jmeter-java-dsl"


export default defineUserConfig({
  lang: 'en-US',
  title: 'jmeter-java-dsl',
  description: 'Simple JMeter performance tests API',
  base: '/jmeter-java-dsl/',
  head: [
    ['link', { rel: 'shortcut icon', href: '/jmeter-java-dsl/favicon.ico'}],
    // when changing this remember also changing components/NavbarBrand.vue
    ['script', {}, `(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
                    new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
                    j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
                    'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
                    })(window,document,'script','dataLayer','GTM-PHSGKLD');
                    `]
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
    editLink: false,
    lastUpdated: false,
    contributors: false,
    navbar: [
      {
        text: 'Guide',
        link: '/guide/',
      },
      {
        text: 'Support',
        link: '/support/',
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
    copyCodePlugin({ pure: true }),
    containerPlugin({
      type: 'grid',
      before: (info: string): string => `<div class="grid">\n`,
      after: (): string => '</div>\n'
    }),
    containerPlugin({
      type: 'grid-logo',
      before: (info: string): string => `<div class="grid-logo"><a href="${info}">\n`,
      after: (): string => '</a></div>\n'
    }),
    mediumZoomPlugin({selector: "*:is(img):not(.card img):not(a img)"}),
  ],
})