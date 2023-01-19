import { defineClientConfig } from '@vuepress/client'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faDiscord, faGithub } from '@fortawesome/free-brands-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(faDiscord, faGithub)

export default defineClientConfig({
  enhance({ app }) {
    app.component('font-awesome-icon', FontAwesomeIcon)
  },
})