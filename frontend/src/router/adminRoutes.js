
// 引入 view 组件
const Login= ()=>import('@/views/admin/Login')
const Home= ()=>import('@/views/admin/Home')
const Dashboard= ()=>import('@/views/admin/Dashboard')
const User= ()=>import('@/views/admin/general/User')
const ProblemList= ()=>import('@/views/admin/problem/ProblemList')
const Problem= ()=>import('@/views/admin/problem/Problem')
const Tag= ()=>import('@/views/admin/problem/Tag')
// Announcement/Notice/Conf/Switch/Group/Discussion/Contest/Training removed for Phase 1
const adminRoutes= [
    {
      path: '/admin/login',
      name: 'admin-login',
      component: Login,
      meta: { title: 'Login' }
    },
    {
      path: '/admin/',
      component: Home,
      meta: { requireAuth:true, requireAdmin: true },
      children: [
        {
          path: '',
          redirect: 'dashboard'
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: Dashboard,
          meta: { title: 'Dashboard' }
        },
        {
          path: 'user',
          name: 'admin-user',
          component: User,
          meta: { requireSuperAdmin: true,title:'User Admin'},
      },
        {
          path: 'problems',
          name: 'admin-problem-list',
          component: ProblemList,
          meta: { title:'Problem List'},
        },
        {
          path: 'problem/create',
          name: 'admin-create-problem',
          component: Problem,
          meta: { title:'Create Problem'},
        },
        {
          path: 'problem/edit/:problemId',
          name: 'admin-edit-problem',
          component: Problem,
          meta: { title:'Edit Problem'},
        },
        {
          path: 'problem/tag',
          name: 'admin-problem-tag',
          component: Tag,
          meta: { title:'Admin Tag'},
        },
      ]
    },
    {
      path: '/admin/*', redirect: '/admin/login'
    }
  ]

  export default adminRoutes
