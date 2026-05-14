import Home from '@/views/oj/Home.vue'
import SetNewPassword from '@/views/oj/user/SetNewPassword.vue'
import Setting from '@/views/oj/user/Setting.vue'
import ProblemList from '@/views/oj/problem/ProblemList.vue'
import Logout from '@/views/oj/user/Logout.vue'
import Problem from '@/views/oj/problem/Problem.vue'
import ComingSoon from '@/views/oj/ComingSoon.vue'
import NotFound from '@/views/404.vue'

const ojRoutes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { title: 'Home' }
  },
  {
    path: '/problem',
    name: 'ProblemList',
    component: ProblemList,
    meta: { title: 'Problem' }
  },
  {
    path: '/problem/:problemID',
    name: 'ProblemDetails',
    component: Problem,
    meta: { title: 'Problem Details' }
  },
  {
    path: '/training',
    name: 'TrainingList',
    component: ComingSoon,
    meta: {
      title: 'Training',
      moduleName: 'Training Coming Soon',
      description: 'Training APIs are planned for Phase 6. This placeholder prevents calls to unfinished endpoints.'
    }
  },
  {
    path: '/contest',
    name: 'ContestList',
    component: ComingSoon,
    meta: {
      title: 'Contest',
      moduleName: 'Contest Coming Soon',
      description: 'Contest APIs are planned for Phase 5. This placeholder prevents calls to unfinished endpoints.'
    }
  },
  {
    path: '/status',
    name: 'SubmissionList',
    component: ComingSoon,
    meta: {
      title: 'Status',
      moduleName: 'Submission Status Coming Soon',
      description: 'Submission and judging records will be enabled with Phase 4.'
    }
  },
  {
    path: '/submission-detail/:submitID',
    name: 'SubmissionDetails',
    component: ComingSoon,
    meta: {
      title: 'Submission Details',
      requireAuth: true,
      moduleName: 'Submission Details Coming Soon',
      description: 'Submission details depend on the Phase 4 judge record APIs.'
    }
  },
  {
    path: '/reset-password',
    name: 'SetNewPassword',
    component: SetNewPassword,
    meta: { title: 'Reset Password' }
  },
  {
    path: '/user-home',
    redirect: '/setting'
  },
  {
    path: '/setting',
    name: 'Setting',
    component: Setting,
    meta: { requireAuth: true, title: 'Setting' }
  },
  {
    path: '/account',
    redirect: '/setting'
  },
  {
    path: '/logout',
    name: 'Logout',
    component: Logout,
    meta: { requireAuth: true, title: 'Logout' }
  },
  {
    path: '*',
    component: NotFound,
    meta: { title: '404' }
  }
]
export default ojRoutes
