import mditPlugin from 'markdown-it-replace-link'

export const repoLinkSolverPlugin = ({repoUrl} : {repoUrl: String}) => ({
    name: 'repoLinkSolverPlugin',
    extendsMarkdown: (md) => {
        md.use(mditPlugin, {
            replaceLink: (link, env) => link.startsWith('/') ? repoUrl + '/tree/master' + link : link
        })
    }
});
