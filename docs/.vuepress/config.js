const { description } = require('../package')
const REPO_URL='https://github.com/abstracta/jmeter-java-dsl'

module.exports = {
  /**
   * Ref：https://v1.vuepress.vuejs.org/config/#title
   */
  title: "jmeter-java-dsl",
  /**
   * Ref：https://v1.vuepress.vuejs.org/config/#description
   */
  description: "Simple JMeter performance tests API",
  base: "/jmeter-java-dsl/",

  markdown: {
    lineNumbers: true,
    extractHeaders: [ 'h2', 'h3', 'h4' ],
    extendMarkdown: md => {
      // rewrite relative urls to point to the github repository
      md.use(require('markdown-it-replace-link'), {
        replaceLink: (link, env) => link.startsWith('../')
          ? REPO_URL + '/tree/master/' + link.replace(/\.\.\//g,'')
          : link
      });
    }
  },

  /**
   * Extra tags to be injected to the page HTML `<head>`
   *
   * ref：https://v1.vuepress.vuejs.org/config/#head
   */
  head: [
    ['meta', { name: 'theme-color', content: '#00ace6' }],
    ['meta', { name: 'apple-mobile-web-app-capable', content: 'yes' }],
    ['meta', { name: 'apple-mobile-web-app-status-bar-style', content: 'black' }],
    ['link', { rel: 'shortcut icon', href: '/favicon.ico'}],
    ['script', { async: true, src: 'https://www.googletagmanager.com/gtag/js?id=G-R8N6GMWK1H' }],
    ['script', {}, ["window.dataLayer = window.dataLayer || [];\nfunction gtag(){dataLayer.push(arguments);}\ngtag('js', new Date());\ngtag('config', 'G-R8N6GMWK1H');"]]
  ],

  /**
   * Theme configuration, here is the default theme configuration for VuePress.
   *
   * ref：https://v1.vuepress.vuejs.org/theme/default-theme-config.html
   */
  themeConfig: {
    repo: REPO_URL,
    logo: '/logo.svg',
    editLinks: false,
    docsDir: '',
    editLinkText: '',
    lastUpdated: false,
    nav: [
      {
        text: 'Guide',
        link: '/guide/',
      },
      {
          text: 'Motivation',
          link: '/motivation/',
        }
    ]
  },

  /**
   * Apply plugins，ref：https://v1.vuepress.vuejs.org/zh/plugin/
   */
  plugins: [
    '@vuepress/plugin-back-to-top',
    '@vuepress/plugin-medium-zoom',
    'vuepress-plugin-element-tabs',
    '@xiaopanda/vuepress-plugin-code-copy'
  ]
}
