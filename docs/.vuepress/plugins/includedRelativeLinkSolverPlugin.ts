import mditPlugin from 'markdown-it-replace-link'

export const includedRelativeLinkSolverPlugin = ({}) => ({
    name: 'includedRelativeLinkSolverPlugin',
    extendsMarkdown: (md) => {
        md.use(mditPlugin, {
            replaceLink: (link, env) => {
              if (link.startsWith('.')) {
                let hashPos = link.indexOf('#')
                return hashPos > 0 ? link.substring(hashPos) : link
              }
              return link
            }
        })
    }
});
