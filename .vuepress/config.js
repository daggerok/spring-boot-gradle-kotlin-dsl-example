const base = !process.env.BASE_HREF ? '/' : process.env.BASE_HREF;

module.exports = {
  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }]
  ],
  base,
  themeConfig: {
    repo: 'daggerok/spring-boot-gradle-kotlin-dsl-example',
    lastUpdated: 'Last Updated', // string | boolean
    '/': {
      sidebar: 'auto'
    },
    sidebarDepth: 2,
    navbar: true,
  }
};
