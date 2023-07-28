import { defineClientConfig } from '@vuepress/client'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faDiscord, faGithub, faTwitter, faLinkedin } from '@fortawesome/free-brands-svg-icons'
import { faGlobe, faBullhorn, faChevronLeft, faChevronRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(faDiscord, faGithub, faTwitter, faLinkedin, faGlobe, faBullhorn, faChevronLeft, faChevronRight)

export default defineClientConfig({
  enhance({ app }) {
    app.component('font-awesome-icon', FontAwesomeIcon)
  },
})